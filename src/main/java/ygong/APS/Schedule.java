package ygong.APS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Spliterator;

// a helper class that works the same as List<Machine> with grades
public class Schedule implements Comparable<Schedule>,
    Iterable<Schedule.MachineWithOrders> {

  public static Comparator<Schedule> scheduleComparator = Comparator.comparingDouble(
      Schedule::getGrade);
  private final ArrayList<MachineWithOrders> _machines;
  private Grade _grade;

  public Schedule(ArrayList<Machine> machines) {
    _machines = new ArrayList<>(machines.size());
    machines.forEach(m -> _machines.add(new MachineWithOrders(m)));
  }

  public Schedule(Schedule s) {
    _machines = new ArrayList<>(s._machines.size());
    // s._machines.forEach(m -> _machines.add(new MachineWithOrders(m)));
    for (MachineWithOrders m : s._machines) {
      _machines.add(new MachineWithOrders(m));
    }
    if (s._grade == null) {
      _grade = null;
    } else {
      _grade = new Grade(s._grade.grade, s._grade.on_time_percentage,
          s._grade.makespan_percentage, s._grade.est_percentage,
          s._grade.ldt_percentage);
    }
  }

  public double getMaxMakespan() {
    assert !_machines.isEmpty() : "Machine list is empty";
    boolean allEmpty = _machines.stream().allMatch(m -> m.orders.isEmpty());
    assert !allEmpty : "All machines are empty";
    /*
     NOTE: not sure the parallel is really needed or not, here is a
           commented out version for reference
      if (_machines.size() > 100) {
        AtomicReference<Double> maxMakespan = new AtomicReference<>((double)
        0); _machines.parallelStream().map(m -> m.orders).forEach(o -> {
          assert !o.isEmpty();
          maxMakespan.set(
              Math.max(maxMakespan.get(), o.stream().skip(o.size() -
              1).findFirst().get()._end_time));
        });
        return maxMakespan.get();
      }
    */
    double max = 0;
    for (MachineWithOrders m : _machines) {
      ArrayList<OrderWithSchedule> o = m.orders;
      if (o.isEmpty()) {
        continue;
      }
      /*
       NOTE: getLast() is not supported until java21 for java8 compatibility,
             use stream().skip(<size>-1).findFirst() instead
      */
      max = Math.max(max, o.getLast()._end_time);
    }
    return max;
  }

  void calcStat(double min_makespan, int num_orders) {
    double on_time = 0;
    double makespan = 0;
    double est = 0;
    double ldt = 0;
    for (MachineWithOrders m : _machines) {
      for (OrderWithSchedule o : m.orders) {
        assert o._start_time >= 0 && o._end_time >= 0;
        int status = o.statusCheck();
        // NOTE: enhanced switch is not supported in Java 8, use switch-case instead
        switch (status & 0b11) {
          case 0b00 -> on_time++;
          case 0b01 -> ldt++;
          case 0b10 -> est++;
          case 0b11 -> {
            ldt++;
            est++;
          }
        }
      }
      /*
       NOTE: getLast() is not supported in Java 8
             use stream().skip(<size>-1).findFirst() instead
      */
      makespan = Math.max(makespan, m.orders.getLast()._end_time);
    }
    // IMPORTANT: We change to percentage of good orders
    ldt = num_orders-ldt;
    est = num_orders-est;
    on_time /= num_orders;
    makespan = 2 - makespan / min_makespan;
    est /= num_orders;
    ldt /= num_orders;
    _grade = new Grade(-1, on_time, makespan, est, ldt);
  }

  public Grade calcGradeByWeights(int on_time_weight, int makespan_weight,
      int est_weight, int ldt_weight) {
    _grade.calcGradeByWeights(on_time_weight, makespan_weight, est_weight,
        ldt_weight);
    return _grade;
  }

  /**
   * We only consider the grade for comparison, if the grade is the same, we
   * consider the schedule is the same no matter the order of the
   * machines/Orders.
   *
   * @param o the object to be compared. If o is null, return 1
   * @return 0 if the grade is the same, 1 if the grade is smaller, -1 if the
   */
  @Override
  public int compareTo(Schedule o) {
    return Double.compare(this.getGrade(), o.getGrade());
  }

  public double getGrade() {
    if (_grade == null) {
      return 0.0;
    }
    return _grade.getGrade();
  }

  @Override
  public int hashCode() {
    return _machines.hashCode(); // TODO think hash code for schedules
  }

  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("0.000");
    return "Schedule{" + "grade=" + df.format(getGrade()) + ", machines="
        + _machines.size() + (_grade == null ? "" : ", " + _grade.toString())
        + '}';
  }

  @Override
  public Iterator<MachineWithOrders> iterator() {
    return _machines.iterator();
  }

  @Override
  public Spliterator<MachineWithOrders> spliterator() {
    return _machines.spliterator();
  }

  public MachineWithOrders getMachine(int i) {
    if (i < 0 || i >= _machines.size()) {
      throw new IndexOutOfBoundsException(
          "Index " + i + " out of bounds for length " + _machines.size());
    }
    return _machines.get(i);
  }

  public void scheduleAllOrders(Scheduler scheduler) {
    _machines.forEach(m -> m.scheduleAllOrders(scheduler));
  }


  /**
   * Helper class for Machines
   * NOTE: if memory size is not big issue, we can extend Machine class
   *       for memory efficiency, we use composition by reference
   */
  public static class MachineWithOrders implements Iterable<OrderWithSchedule> {

    protected final Machine machine;
    // private final LinkedHashSet<OrderWithSchedule> orders;
    private final ArrayList<OrderWithSchedule> orders;
    protected double _approx_run_time = 0;

    public MachineWithOrders(Machine machine) {
      this.machine = machine;
      this.orders = new ArrayList<>();
    }

    public MachineWithOrders(MachineWithOrders machine) {
      this.machine = machine.machine;
      this.orders = new ArrayList<>(machine.orders.size());
      // machine.orders.forEach(o -> this.orders.add(new OrderWithSchedule(o)));
      for (OrderWithSchedule o : machine.orders) {
        this.orders.add(new OrderWithSchedule(o));
      }
      this._approx_run_time = machine._approx_run_time;
    }

    /**
     * Add order to the machine
     * NOTE: This add order doesn't check if the order o is already in the
     * machine!
     *
     * @param o the order to be added, not checking if it is already in the
     *          machine
     * @return true if the order is added successfully, false otherwise
     */
    protected boolean addOrder(Order o) {
      // check if producible
      if (machine.checkViableOrder(o)) {
        boolean add = orders.add(new OrderWithSchedule(o));
        if (add) {
          _approx_run_time += (double) o.quantity / machine.getProductionPace(
              o.production_type_ID);
        }
        return add;
      }
      return false;
    }

    protected boolean removeOrder(Order o) {
      boolean remove = orders.remove(new OrderWithSchedule(o));
      if (remove) {
        _approx_run_time -= (double) o.quantity / machine.getProductionPace(
            o.production_type_ID);
      }
      return remove;
    }

    /**
     * precisely calculate the start end time for each order on this.machine
     *
     * @param scheduler the scheduler to get switch time
     */
    public void scheduleAllOrders(final Scheduler scheduler) {
      double current_time = 0;
      int prev_id = -1;
      for (OrderWithSchedule o : orders) {
        int production_type_ID = o.order.production_type_ID;
        // double check for producible
        assert machine.checkViableOrder(o.order) : "Order is not producible";
        double start_time = current_time;
        double switch_time =
            prev_id >= 0 ? scheduler.getSwitchTime(prev_id, production_type_ID)
                : 0;
        // NOTE: end time is rounded up to the next integer
        double end_time = Math.ceil(start_time + switch_time
            + (double) o.order.quantity / machine.getProductionPace(
            production_type_ID));
        o.setStartEndTime(start_time, end_time);

        current_time = end_time;
        prev_id = production_type_ID;
      }
    }

    /**
     * schedule all orders without switch time
     */
    public void scheduleAllOrders() {
      double current_time = 0;
      int prev_id = -1;
      for (OrderWithSchedule o : orders) {
        int order_id = o.order.order_ID;
        double start_time = current_time;
        double switch_time = 0;
        double end_time = start_time + switch_time + (int) Math.ceil(
            (double) o.order.quantity / machine.getProductionPace(
                o.order.production_type_ID));
        o.setStartEndTime(start_time, end_time);

        current_time = end_time;
        prev_id = order_id;
      }
    }

    /**
     * Iterator for OrdersWithSchedule from MachineWithOrders
     *
     * @return a iterator for OrdersWithSchedule
     */
    @Override
    public Iterator<OrderWithSchedule> iterator() {
      return orders.iterator();
    }

    public String getName() {
      return machine.name;
    }

    public int getMachineID() {
      return machine.machine_ID;
    }

    public ArrayList<OrderWithSchedule> getOrders() {
      return new ArrayList<>(orders);
    }

  }

  /**
   * Helper class for Orders
   *   NOTE: if memory size is not big issue, we can extend Order class
   *         for memory efficiency, we use composition by reference
   *   XXX: All the time variables in Order/OrderWithSchedule is in integer
   */

  public static class OrderWithSchedule {

    protected final Order order;
    private int _start_time = -1;
    private int _end_time = -1;

    // bitwise status (1 for true, 0 for false)
    // 0...00      | 00             | 00
    // error code  | running status | schedule status
    // NOTE: error code and running status are not used (for now)
    // schedule status: 0b00 for on time(GREEN),
    //                  0b01 for violate LDT(RED),
    //                  0b10 for violate EST(PURPLE),
    //                  0b11 for violate delivery(MAGENTA)
    // ....
    private int status = 0b10000;

    public OrderWithSchedule(Order order) {
      this.order = order;
    }

    public OrderWithSchedule(OrderWithSchedule o) {
      this.order = o.order;
      this._start_time = o._start_time;
      this._end_time = o._end_time;
      this.status = o.status;
    }

    public int getStartTime() {
      return _start_time;
    }

    public int getEndTime() {
      return _end_time;
    }

    public int getProductionTypeID() {
      return order.production_type_ID;
    }

    public int getOrderID() {
      return order.order_ID;
    }

    public String getColorCode() throws AssertionError {
      // TODO assert status >> 4 == 0 : "Error code must be 0";
      // NOTE: enhanced switch is not supported in Java 8, use switch-case instead
      return switch (status & 0b11) {
        case 0b00 -> "status-green";
        case 0b01 -> "status-red";
        case 0b10 -> "status-est-violate";
        default -> // 0b11 for violate delivery, no way out of scope
            "status-deli-violate";
      };
    }

    protected void setStartEndTime(final double start_time,
        final double end_time) {
      setStartEndTime((int) start_time, (int) end_time);
    }

    protected void setStartEndTime(final int start_time, final int end_time)
        throws AssertionError {
      assert start_time < end_time : "Start time must be less than end time";
      assert start_time >= 0 : "Start time must be non-negative";
      this._start_time = start_time;
      this._end_time = end_time;
      statusCheck();
    }

    public int statusCheck() throws IllegalStateException {
      // UNDONE assert status >> 4 == 0 : "Error code must be 0";
      int earliest_start_time = order.earliest_start_time;
      int delivery_time = order.delivery_time;
      int latest_due_time = order.latest_due_time;
      // clean last two bit of status
      status = status & ~(0b11);
      if (_start_time < 0 || _end_time < 0) {
        throw new IllegalStateException("Start time and end time must be set");
      } else if (_start_time >= earliest_start_time
          && _end_time <= delivery_time) {
        status |= 0b00;
      } else if (_end_time > latest_due_time) {
        status |= 0b01;
      } else if (_start_time < earliest_start_time) {
        status |= 0b10;
      } else {
        status |= 0b11;
      }
      return status;
    }

    @Override
    public int hashCode() {
      return order.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      return this.order.getOrderID()
          == ((OrderWithSchedule) o).order.getOrderID();
    }
  }

  // Helper class for Grades
  public static class Grade implements Comparable<Grade> {

    protected final double on_time_percentage;
    protected final double makespan_percentage;
    protected final double est_percentage;
    protected final double ldt_percentage;
    private double grade;

    private Grade(double grade, double on_time, double makespan,
        double est_percentage, double ldt_percentage) {
      this.grade = grade;
      on_time_percentage = on_time;
      makespan_percentage = makespan;
      this.est_percentage = est_percentage;
      this.ldt_percentage = ldt_percentage;
    }

    private void calcGradeByWeights(int on_time_weight, int makespan_weight,
        int est_weight, int ldt_weight) {
      grade = on_time_percentage * on_time_weight
          + makespan_percentage * makespan_weight + est_percentage * est_weight
          + ldt_percentage * ldt_weight;
    }

    @Override
    public String toString() {
      DecimalFormat df = new DecimalFormat("0.000");
      return "Grade{" + "grade: " + df.format(grade) + ", on_time=" + df.format(
          on_time_percentage * 100) + "%, makespan(2-best%)=" + df.format(
          makespan_percentage * 100) + "%, earliest=" + df.format(
          est_percentage * 100) + "%, latest=" + df.format(ldt_percentage * 100)
          + "%}";
    }

    public double getGrade() {
      return grade;
    }

    @Override
    public int compareTo(Grade o) {
      return Double.compare(grade, o.grade);
    }

  }
}