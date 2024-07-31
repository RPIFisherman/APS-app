package ygong.APS;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;

/**
 * This class represents a schedule in the APS system. A schedule is a list of
 * {@link MachineWithOrders} that contains bunch of {@link OrderWithTime}
 *
 * <p> A schedule has a grade, which is calculated by the scheduler based on
 * the on-time percentage, makespan percentage, earliest start time percentage,
 * and latest due time percentage. The grade is used to compare schedules and
 * determine the best schedule.
 * </p>
 *
 * <p> A schedule is used by the scheduler to schedule orders in the production
 * environment. The scheduler generates multiple schedules and selects the best
 * schedule based on the grade.
 * </p>
 *
 * <p> A schedule is iterable, which means it can be iterated over the list of
 * {@link MachineWithOrders} in the schedule.
 * </p>
 *
 * <p> A schedule is comparable, which means it can be compared to other
 * schedules based on the grade. The schedule with the highest grade is
 * considered the best schedule.
 * </p>
 *
 * @author <a href="mailto:yuyanggong.rpi@gmail.com">Yuyang Gong</a>
 * @version 1.0
 * @note The schedule is implemented as an ArrayList of {@link MachineWithOrders} and the grade is calculated based on the on-time percentage, makespan percentage, earliest start time percentage, and latest due time percentage. The grade is used to compare schedules and determine the best schedule.
 * @see MachineWithOrders
 * @see OrderWithTime
 * @see Grade
 * @see Scheduler
 */
public class Schedule implements Comparable<Schedule>,
    Iterable<Schedule.MachineWithOrders> {

  /**
   * The comparator used for sorting schedules by grade(Double)
   */
  public static final Comparator<Schedule> scheduleComparator = Comparator.comparingDouble(
      Schedule::getGrade);

  /**
   * The list of machines that are used in the schedule
   *
   * @see MachineWithOrders
   */
  private final ArrayList<MachineWithOrders> _machines;

  /**
   * The grade of the schedule
   */
  private Grade _grade;

  /**
   * Constructor for Schedule
   *
   * @param machines arraylist of machines
   */
  public Schedule(ArrayList<Machine> machines) {
    _machines = new ArrayList<>(machines.size());
    for (Machine m : machines) {
      _machines.add(new MachineWithOrders(m));
    }
  }

  /**
   * Copy constructor for Schedule
   *
   * @param s schedule that copied
   */
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

  /**
   * Calculate the makespan of the schedule
   *
   * @return the max makespan of working machines, which is consider the total
   * work time of the schedule
   * @throws AssertionError if the schedule is empty, there is no need to
   *                        calculate the max makespan
   * @see MachineWithOrders
   */
  public double getMaxMakespan() throws AssertionError {
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

  /**
   * Calculate the statistics(percentage of violation) of the orders via the
   * schedule
   *
   * @param min_makespan the minimum makespan get from the other schedules, used
   *                     as a baseline (100%) for the stats
   * @param num_orders   the number of orders in the schedule, for now we don't
   *                     check whether there are depricated orders in the
   *                     schedule/machines, and we don't hold a number of
   *                     schedules in <strong>Schedule</strong> level, we get it
   *                     from the Scheduler
   * @see Scheduler#calcAllSchedulesGrade(Integer...)
   * @see OrderWithTime
   * @see Grade
   */
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
      if (!m.orders.isEmpty()) {
        makespan = Math.max(makespan,
            m.orders.get(m.orders.size() - 1)._end_time);
      }
    }
    // NOTE: We change to percentage of good orders
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

  /**
   * Calculate the Grade based on the weights and stats calculated before.
   * <p><strong>call {@link Schedule#calcStat(double, int)}</strong> first to
   * make sure the stats are correctly calculated!
   * </p>
   *
   * @param on_time_weight  weight for the on time percentage
   * @param makespan_weight weight for the makespan percentage
   * @param est_weight      weight for the earliest start time percentage
   * @param ldt_weight      weight for the latest due time percentage
   * @return the Grade with percentage
   * @note we can have weights in any kind of size
   * @see Grade#calcGradeByWeights(int, int, int, int)
   * @see Grade
   */
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
    try {
      return this._grade.compareTo(o._grade);
    } catch (NullPointerException e) {
      return Double.compare(this.getGrade(), o.getGrade());
    }
  }

  /**
   * get the Grade in double
   *
   * @return if grade is not calculated, return 0.0, else return the grade value
   */
  public double getGrade() {
    if (_grade == null) {
      return 0.0;
    }
    return _grade.getGrade();
  }

  /**
   * Override the hashCode function
   *
   * @return hashCode of {@code ArrayList<MachineWithSchedule>}
   */
  @Override
  public int hashCode() {
    return _machines.hashCode(); // TODO think hash code for schedules
  }

  /**
   * Override the toString function
   *
   * @return a string representation of the Scheduler
   */
  @Override
  public String toString() {
    DecimalFormat df = new DecimalFormat("0.000");
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
        .append(_grade == null ? "" : ", " + _grade).append('}');

    return s.toString();
  }

  /**
   * Override iterator() to make Schedule Iterable
   *
   * @return the iterator of {@code ArrayList<MachineWithOrder>}
   * @see Iterable
   */
  @Override
  public Iterator<MachineWithOrders> iterator() {
    return _machines.iterator();
  }

  /**
   * Override spliterator() to make Schedule Spliterable
   *
   * @return the spliterator of {@code ArrayList<MachineWithOrder>}
   * @see Iterable
   */
  @Override
  public Spliterator<MachineWithOrders> spliterator() {
    return _machines.spliterator();
  }

  /**
   * @param i the index of the machine
   * @return the machine at index i
   * @throws IndexOutOfBoundsException if the index is out of bounds
   */
  public MachineWithOrders getMachine(int i) throws IndexOutOfBoundsException {
    if (i < 0 || i >= _machines.size()) {
      throw new IndexOutOfBoundsException(
          "Index " + i + " out of bounds for length " + _machines.size());
    }
    return _machines.get(i);
  }

  /**
   * @param scheduler the scheduler to get switch time
   */
  public void scheduleAllOrders(Scheduler scheduler) {
    _machines.forEach(m -> m.scheduleAllOrders(scheduler));
  }


  /**
   * Helper class for Machines
   *
   * <p> The class that takes the original Machine as a reference and have
   * additional orders and approximate run time for the machine.
   * </p>
   *
   * @note if memory size is not big issue, we can extend Machine class
   *       for memory efficiency, we use composition by reference
   */
  public static class MachineWithOrders implements Iterable<OrderWithTime> {

    /**
     * The machine that is used in the schedule
     */
    protected final Machine machine;
    private final ArrayList<OrderWithTime> orders;
    protected double _approx_run_time = 0;

    /**
     * Constructor for MachineWithOrders
     *
     * @param machine the machine to be added, copied by reference
     */
    public MachineWithOrders(Machine machine) {
      this.machine = machine;
      this.orders = new ArrayList<>();
    }

    /**
     * Copy constructor for MachineWithOrders
     *
     * @param machine the machine to be copied
     */
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
     *
     * @param o the order to be added, not checking if it is already in the
     *          machine
     * @return true if the order is added successfully, false otherwise
     * @note This add order doesn't check if the order o is already in the
     *       machine. Implement with caution.
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
     * @return an iterator for OrdersWithSchedule
     */
    @Override
    public Iterator<OrderWithTime> iterator() {
      return orders.iterator();
    }

    /**
     * @return the name of the machine
     */
    public String getName() {
      return machine.name;
    }

    /**
     * @return the ID of the machine
     */
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
   *
   * <dl>
   *   <dt>We have four {@link OrderWithTime#status} for an order:</dt>
   *    <dd>0b00 for on time(GREEN), the order is normally executed, it doesn't violate any time constraints(EST, LDT, delivery)</dd>
   *    <dd>0b01 for violate LDT(RED), the order is executed after the latest due time(LDT)</dd>
   *    <dd>0b10 for violate EST(PURPLE), the order is executed before the earliest start time(EST)</dd>
   *    <dd>0b11 for violate delivery(MAGENTA), the order is executed after the delivery time</dd>
   * </dl>
   *
   * @note if memory size is not big issue, we can extend Order class. But
   *       for memory efficiency, we use composition by reference
   * @note All the time variables in Order/OrderWithTime is in integer
   *       format, which may not enough for precision. We can change to double
   *       format if needed.
   * @see Order
   */
  public static class OrderWithTime {

    /**
     * The order that is used in this time
     */
    // reference to the original order
    protected final Order order;

    /**
     * start time of the Order
     */
    private int _start_time = -1;

    /**
     * end time of the Order
     */
    private int _end_time = -1;

    /**
     * bitwise status (1 for true, 0 for false) last two digits are used for
     * schedule status
     * <br>
     * <table>
     *   <caption>schedule status</caption>
     *   <thead>
     *   <tr>
     *     <th>status</th>
     *     <th>description</th>
     *     <th>color code</th>
     *   </tr>
     *   </thead>
     *   <tbody>
     *   <tr>
     *     <td>0b00</td>
     *     <td>on time</td>
     *     <td>status-green</td>
     *   </tr>
     *   <tr>
     *     <td>0b01</td>
     *     <td>Latest Due Time violate</td>
     *     <td>status-red</td>
     *   </tr>
     *   <tr>
     *     <td>0b10</td>
     *     <td>Earliest Start Time violate</td>
     *     <td>status-est-violate</td>
     *   </tr>
     *   <tr>
     *     <td>0b11</td>
     *     <td>Delivery Time violate</td>
     *     <td>status-deli-violate</td>
     *   </tr>
     *   </tbody>
     * </table>
     *
     * @note other digits are not used (for now), change to enum if needed
     */
    private int status = 0b10000;

    /**
     * Constructor for OrderWithTime
     *
     * @param order the order to be scheduled, copied by reference
     */
    public OrderWithTime(Order order) {
      this.order = order;
    }

    /**
     * Copy constructor for OrderWithTime
     *
     * @param o the order to be copied
     */
    public OrderWithTime(OrderWithTime o) {
      this.order = o.order;
      this._start_time = o._start_time;
      this._end_time = o._end_time;
      this.status = o.status;
    }

    /**
     * @return start time for this order
     */
    public int getStartTime() {
      return _start_time;
    }

    /**
     * @return end time for this order
     */
    public int getEndTime() {
      return _end_time;
    }

    /**
     * @return the production type ID of this order
     */
    public int getProductionTypeID() {
      return order.production_type_ID;
    }

    /**
     * @return the order ID
     */
    public int getOrderID() {
      return order.order_ID;
    }

    /**
     * color code for construct Gantt Plot
     *
     * @return CSS color code
     */
    public String getColorCode() {
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

    /**
     * TODO: decide time in double or int
     * <p>
     * convert double time to int time
     * </p>
     *
     * @param start_time the start time
     * @param end_time   the end time
     * @see OrderWithTime#setStartEndTime(int, int)
     */
    protected void setStartEndTime(final double start_time,
        final double end_time) {
      setStartEndTime((int) start_time, (int) Math.ceil(end_time));
    }

    /**
     * set the start end time of the order
     *
     * @param start_time the start time
     * @param end_time   the end time
     * @throws AssertionError if the start time is greater than the end time
     * @throws AssertionError if the start time is less than 0
     */
    protected void setStartEndTime(final int start_time, final int end_time)
        throws AssertionError {
      assert start_time < end_time : "Start time must be less than end time";
      assert start_time >= 0 : "Start time must be non-negative";
      this._start_time = start_time;
      this._end_time = end_time;
      statusCheck();
    }

    /**
     * check the status of the order
     *
     * @return the status of the order
     * @throws IllegalStateException if the start time and end time are not set
     *                               correctly
     */
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

    /**
     * @return the status of the order
     */
    @Override
    public int hashCode() {
      return order.hashCode();
    }

    /**
     * @param o object to compare
     * @return true if order ID is the same
     */
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

  /**
   * Helper class for calculating the grade of the schedule
   *
   * <p> The class that contains the grade of the schedule, which is calculated
   * based on the on-time percentage, makespan percentage, earliest start time
   * percentage, and latest due time percentage.
   * </p>
   *
   * <p> The grade is used to compare schedules and determine the best
   * schedule.
   * </p>
   *
   * @see Schedule
   */
  public static class Grade implements Comparable<Grade> {

    /**
     * percentage of on-time orders
     */
    protected final double on_time_percentage;

    /**
     * 2-% to the best makespan schedule
     * <p>
     * e.g. if the best makespan is 100, and the current schedule makespan is
     * 110 the <strong>makespan_percentage</strong> is equal to
     * {@code 2 - 110/100 = 0.9}.
     * <br> This makes sure the best makespan is getting 100% on this
     * percentage and other longer ones get lower
     * </p>
     *
     * @note this percentage can be negative if the makespan is over twice the best makespan
     * @see Schedule#calcStat(double, int)
     */
    protected final double makespan_percentage;

    /**
     * The percentage of schedules that <strong>does not</strong> violate the
     * earliest start time.
     * <p>
     * e.g. if we have 3 orders vialate the earliest starting time, and we have
     * 20 orders total. We will have {@code 17/20 = 0.85} on est_percentage.
     * <br> This make sure the best schedule get 100% on this percentage and
     * other schedules get lower.
     * </p>
     *
     * @see Schedule#calcStat(double, int)
     */
    protected final double est_percentage;
    /**
     * The percentage of schedules that <strong>does not</strong> violate the
     * latest due time.
     * <p>
     * e.g. if we have 3 orders vialate the latest due time, and we have 20
     * orders total. We will have {@code 17/20 = 0.85} on ldt_percentage.
     * <br> This make sure the best schedule get 100% on this percentage and
     * other schedules get lower.
     * </p>
     *
     * @see Schedule#calcStat(double, int)
     */
    protected final double ldt_percentage;

    /**
     * The final grade for the schedule
     *
     * @see Schedule#calcGradeByWeights(int, int, int, int)
     */
    private double grade;

    /**
     * Constructor for Grade
     *
     * @param grade          the grade of the schedule
     * @param on_time        the percentage of on-time orders
     * @param makespan       the makespan percentage
     * @param est_percentage the percentage of schedules that do not violate
     *                       the
     * @param ldt_percentage the percentage of schedules that do not violate
     *                       the
     */
    private Grade(double grade, double on_time, double makespan,
        double est_percentage, double ldt_percentage) {
      this.grade = grade;
      on_time_percentage = on_time;
      makespan_percentage = makespan;
      this.est_percentage = est_percentage;
      this.ldt_percentage = ldt_percentage;
    }

    /**
     * Calculate the grade based on the weights
     *
     * @param on_time_weight  the weight for the on-time percentage
     * @param makespan_weight the weight for the makespan percentage
     * @param est_weight      the weight for the earliest start time percentage
     * @param ldt_weight      the weight for the latest due time percentage
     */
    private void calcGradeByWeights(int on_time_weight, int makespan_weight,
        int est_weight, int ldt_weight) {
      grade = on_time_percentage * on_time_weight
          + makespan_percentage * makespan_weight + est_percentage * est_weight
          + ldt_percentage * ldt_weight;
    }

    /**
     * @return a string representation of the grade
     */
    @Override
    public String toString() {
      DecimalFormat df = new DecimalFormat("0.000");
      return "Grade{" + "grade: " + df.format(grade) + ", on_time=" + df.format(
          on_time_percentage * 100) + "%, makespan(2-best%)=" + df.format(
          makespan_percentage * 100) + "%, earliest=" + df.format(
          est_percentage * 100) + "%, latest=" + df.format(ldt_percentage * 100)
          + "%}";
    }

    /**
     * @return The grade score
     * @note The grade need to be calculate first by
     *      {@link Schedule#calcGradeByWeights(int, int, int, int)}
     *      before get the grade
     * @see Schedule#calcStat(double, int)
     * @see Schedule#calcGradeByWeights(int, int, int, int)
     */
    public double getGrade() {
      return grade;
    }

    /**
     * @param o the object to be compared.
     * @return 0 if the grade is the same, 1 if the grade is smaller, -1 if the
     */
    @Override
    public int compareTo(Grade o) {
      return Double.compare(grade, o.grade);
    }

  }
}
