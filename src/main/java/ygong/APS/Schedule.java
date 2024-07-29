package ygong.APS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public class Schedule implements Comparable<Schedule>,
    Iterable<Schedule.MachineWithOrders> {

  public static Comparator<Schedule> scheduleComparator = Comparator.comparingDouble(
      Schedule::getGrade);
  private final ArrayList<MachineWithOrders> _machines;
  private Grade _grade;

  public Schedule(ArrayList<Machine> machines) {
    _machines = new ArrayList<>(machines.size());
    for (Machine m : machines) {
      _machines.add(new MachineWithOrders(m));
    }
  }

  public Schedule(Schedule s) {
    _machines = new ArrayList<>(s._machines.size());
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
      ArrayList<OrderWithTime> o = m.orders;
      if (o.isEmpty()) {
        continue;
      }
      max = Math.max(max, o.get(o.size() - 1)._end_time);
    }
    return max;
  }

  void calcStat(double min_makespan, int num_orders) {
    double num_on_time = num_orders;
    double makespan = 0;
    double num_violation_earliest_start_time = 0;
    double num_violation_latest_due_time = 0;
    for (MachineWithOrders m : _machines) {
      for (OrderWithTime o : m.orders) {
        assert o._start_time >= 0 && o._end_time >= 0;
        int status = o.statusCheck();
        switch (status & 0b11) {
          // 0b01 for violate LDT(RED),
          case 0b01:
            num_violation_latest_due_time++;
            num_on_time--;
            break;
          // 0b10 for violate EST(PURPLE),
          case 0b10:
            num_violation_earliest_start_time++;
            break;
          // 0b11 for violate delivery(MAGENTA)
          case 0b11:
            num_on_time--;
            break;
          default: // 0b00: normal do nothing
            break;
        }
      }
      if (m.orders.isEmpty()) {
        continue;
      } else {
        makespan = Math.max(makespan,
            m.orders.get(m.orders.size() - 1)._end_time);
      }
    }
    // IMPORTANT: We change to percentage of good orders
    num_violation_latest_due_time = num_orders - num_violation_latest_due_time;
    num_violation_earliest_start_time =
        num_orders - num_violation_earliest_start_time;
    num_on_time /= num_orders;
    makespan = 2 - makespan / min_makespan;
    num_violation_earliest_start_time /= num_orders;
    num_violation_latest_due_time /= num_orders;
    _grade = new Grade(-1, num_on_time, makespan,
        num_violation_earliest_start_time, num_violation_latest_due_time);
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

  /**
   * <div class="en">Documentation in English</div>
   * <div class="nl">Documentatie in Nederlands</div>
   */
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
    // return "Schedule{" + "grade=" + df.format(getGrade()) + ", machines="
    //     + _machines.size() + (_grade == null ? "" : ", " + _grade.toString())
    //     + '}';
    StringBuilder s = new StringBuilder();
    for (MachineWithOrders m : _machines) {
      s.append(m.getName()).append(": ");
      for (OrderWithTime o : m) {
        s.append(o.getOrderID()).append(" ");
      }
      s.append("| ");
    }
    s.append("\nSchedule{" + "grade=").append(df.format(getGrade()))
        .append(", machines=").append(_machines.size())
        .append(_grade == null ? "" : ", " + _grade.toString()).append('}');

    return s.toString();
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
   * <p>
   * NOTE: if memory size is not big issue, we can extend Machine class
   *       for memory efficiency, we use composition by reference
   * </p>
   */
  public static class MachineWithOrders implements Iterable<OrderWithTime> {

    protected final Machine machine;
    private final ArrayList<OrderWithTime> orders;
    protected double _approx_run_time = 0;

    public MachineWithOrders(Machine machine) {
      this.machine = machine;
      this.orders = new ArrayList<>();
    }

    public MachineWithOrders(MachineWithOrders machine) {
      this.machine = machine.machine;
      this.orders = new ArrayList<>(machine.orders.size());
      for (OrderWithTime o : machine.orders) {
        this.orders.add(new OrderWithTime(o));
      }
      this._approx_run_time = machine._approx_run_time;
    }

    /**
     * Add order to the machine
     * <p>
     * NOTE: This add order doesn't check if the order o is already in the
     *       machine. Implement with caution.
     *
     * @param o the order to be added, not checking if it is already in the
     *          machine
     * @return true if the order is added successfully, false otherwise
     */
    protected boolean addOrder(Order o) {
      // check if producible
      if (machine.checkViableOrder(o.production_type_ID)) {
        boolean add = orders.add(new OrderWithTime(o));
        if (add) {
          _approx_run_time += (double) o.quantity / machine.getProductionPace(
              o.production_type_ID);
        }
        return add;
      }
      return false;
    }

    protected boolean removeOrder(Order o) {
      boolean remove = orders.remove(new OrderWithTime(o));
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
      for (OrderWithTime o : orders) {
        int production_type_ID = o.order.production_type_ID;
        double start_time = current_time;
        double switch_time =
            prev_id >= 0 ? scheduler.getSwitchTime(prev_id, production_type_ID)
                : 0;
        // NOTE: end time is rounded up to the next integer
        double end_time = start_time + switch_time
            + (double) o.order.quantity / machine.getProductionPace(
            production_type_ID);
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
      for (OrderWithTime o : orders) {
        double start_time = current_time;
        double end_time =
            start_time + (double) o.order.quantity / machine.getProductionPace(
                o.order.production_type_ID);
        o.setStartEndTime(start_time, end_time);
        current_time = end_time;
      }
    }

    /**
     * Iterator for OrdersWithSchedule from MachineWithOrders
     *
     * @return a iterator for OrdersWithSchedule
     */
    @Override
    public Iterator<OrderWithTime> iterator() {
      return orders.iterator();
    }

    public String getName() {
      return machine.name;
    }

    public int getMachineID() {
      return machine.machine_ID;
    }

    /**
     * @return a copy of the orders
     */
    public ArrayList<OrderWithTime> getOrders() {
      return new ArrayList<>(orders);
    }

  }

  /**
   * Helper class for Orders。
   * <p> The class that takes the original Order as a reference and have
   * additional start time, end time, and status for schedule checking.
   * </p>
   * <p><strong>
   * NOTE: if memory size is not big issue, we can extend Order class. But
   *       for memory efficiency, we use composition by reference
   * </strong></p>
   * <p><strong>
   * NOTE: All the time variables in Order/OrderWithTime is in integer
   *       format, which may not enough for precision. We can change to double
   *       format if needed.
   * </strong></p>
   */

  public static class OrderWithTime {

    // reference to the original order
    protected final Order order;

    // start time and end time for the order
    private int _start_time = -1;
    private int _end_time = -1;

    /**
     * bitwise status (1 for true, 0 for false) 0...00      | 00             |
     * 00 error code  | running status | schedule status
     * NOTE: error code and running status are not used (for now)
     *       change to enum if needed
     * schedule status: 0b00 for on time(GREEN),
     * 0b01 for violate LDT(RED),
     * 0b10 for violate EST(PURPLE),
     * 0b11 for violate delivery(MAGENTA)
     * ....
     */
    private int status = 0b10000;

    public OrderWithTime(Order order) {
      this.order = order;
    }

    public OrderWithTime(OrderWithTime o) {
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
      switch (status & 0b11) {
        case 0b00:
          return "status-green";
        case 0b01:
          return "status-red";
        case 0b10:
          return "status-est-violate";
        default: // 0b11 for violate delivery, no way out of scope
          return "status-deli-violate";
      }
    }

    protected void setStartEndTime(final double start_time,
        final double end_time) {
      setStartEndTime((int) start_time, (int) Math.ceil(end_time));
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
      return this.order.getOrderID() == ((OrderWithTime) o).order.getOrderID();
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
