package ygong.APS;

import static ygong.APS.Rules.aboveCapacity;
import static ygong.APS.Rules.belowCapacity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import ygong.APS.GanttChart.ExtraData;
import ygong.APS.Schedule.MachineWithOrders;
import ygong.APS.Schedule.OrderWithTime;

/**
 * The Scheduler class is used to generate all possible schedules and calculate
 * the grade for each schedule based on the weights given.
 * <p>
 * The Scheduler class can be used to generate all possible schedules and
 * calculate the grade for each schedule based on the weights given. The
 * Scheduler class can also be used to generate random schedules based on the
 * given parameters.
 *
 * @author <a href="mailto:yuyanggong.rpi@gmail.com">Yuyang Gong</a>
 * @version 1.0
 */
public class Scheduler {

  /**
   * The list of all schedules generated by the Scheduler
   */
  private final List<Schedule> _schedules = new ArrayList<>();

  /**
   * The list of all futures generated by the Scheduler
   * <p>
   * Used for {@link #depthFirstSearch(int, Schedule)} to store the futures of
   * {@link scheduleOrder } for each schedule to precisely calculate the
   * start/end time with the switch time.
   * </p>
   */
  private final ArrayList<Future<Double>> _futures = new ArrayList<>();

  /**
   * Number of threads created in the ExecutorService
   * <p>
   * Defined at {@link #Scheduler(int)} by the int or <br>default to <strong>
   * {@link Runtime#availableProcessors()} - 1 </strong> at
   * {@link #Scheduler()}
   * </p>
   */
  private final int _num_threads;

  /**
   * The ExecutorService to manage the threads
   */
  private ExecutorService _executor;


  /**
   * number of production types <br>e.g. 3 types of products each with different
   * production pace on different machines and also have different switch time
   * between each other
   */
  private int _num_production_types = -1;

  /**
   * number of machines/production lines/production hours... {@link Order}
   */
  private int _num_machines = -1;

  /**
   * number of orders/products/goods need to be scheduled
   */
  private int _num_orders = -1;

  /**
   * the 'maximum'(actually recommend) hours allowed for all orders to be
   * schedules
   */
  private int _max_hours_allowed = -1;

  /**
   * the maximum capacity that can be <strong>Overload</strong> on machine
   * <br>
   * e.g. We have 40 hours of {@link Scheduler#_max_hours_allowed} on a machine
   * with 1.5
   * <strong>{@code Scheduler._max_capacity_per_machine}</strong>. Then we
   * allowed total <strong>40*1.5= 60</strong> hours of rough working time on
   * each machine
   *
   * @see Rules
   */
  private double _max_capacity_per_machine = -1;

  /**
   * the minimum capacity that a machine can work.
   * <br>
   * Similar to {@link Scheduler#_max_capacity_per_machine}
   * <br>
   * e.g. We have 40 hours of {@link Scheduler#_max_hours_allowed} on each
   * machine with 0.5
   * <strong>{@code Scheduler._min_capacity_per_machine}</strong>. Then we have
   * <strong>40*0.5= 20</strong> hours of rough working time on each machine.
   * If the machine works less than 20 hours, the schedule is invalid.
   *
   * @see Rules
   */
  private double _min_capacity_per_machine = -1;

  /**
   * the minimum makespan of all schedules
   *
   * <br> <strong>Note:</strong> this is the precisely calculated makespan
   * including the switch
   * time between different orders
   */
  private double _min_makespan = Double.MAX_VALUE;

  /**
   * the switch time between different order types
   *
   * @see Schedule#scheduleAllOrders(Scheduler)
   * @see MachineWithOrders#scheduleAllOrders(Scheduler)
   */
  private ArrayList<ArrayList<Double>> _order_type_switch_times;

  /**
   * the list of all orders need to be scheduled
   *
   * @see Order
   */
  private ArrayList<Order> _orders;

  /**
   * the list of all machines need to be scheduled
   *
   * @see Machine
   */
  private ArrayList<Machine> _machines;

  /**
   * Default constructor
   * <p>
   * Default to {@link Runtime#availableProcessors()} - 1 number of threads in
   * the ExecutorService
   * </p>
   */
  public Scheduler() {
    _num_threads = Runtime.getRuntime().availableProcessors() - 1;
  }

  /**
   * Constructor with the number of threads in the ExecutorService
   *
   * @param num_threads the number of threads in the ExecutorService
   *                    <p>
   *                    The number of threads should be greater than zero
   *                    </p>
   * @throws AssertionError if the number of threads is less than or equal to
   *                        zero
   */
  public Scheduler(int num_threads) {
    assert num_threads > 0 : "Threads should be greater than zero!";
    _num_threads = num_threads;
  }

  /**
   * Recursion method to generate all possible schedules
   *
   * @param order_index the index of the order to be scheduled
   * @param schedule    the current schedule
   * @throws AssertionError if the schedule is null or the order index is
   *                        greater than the number of orders
   * @see Scheduler#generateAllPossible()
   * @see MachineWithOrders#addOrder(Order)
   */
  private void depthFirstSearch(final int order_index, Schedule schedule)
      throws AssertionError {
    assert schedule != null;
    assert order_index <= _num_orders;
    if (order_index == _num_orders) {
      for (MachineWithOrders m : schedule) {
        if (belowCapacity(m)) {
          return;
        }
      }
      Schedule new_schedule = new Schedule(schedule);
      _schedules.add(new_schedule);
      _futures.add(_executor.submit(new scheduleOrder(new_schedule, this)));
      return;
    }
    for (int i = 0; i < _num_machines; i++) {
      MachineWithOrders machine = schedule.getMachine(i);

      /*
        NOTE removed the checker since the add will check viability
             if order does not fit machine, skip:
          if (!orderFitsMachine(machine, _orders.get(order_index))) {
            continue;
          }

        @see Rules#orderFitsMachine(MachineWithOrders, Order)
        @see Schedule.MachineWithOrders#addOrder(Order)
       */
      boolean added = machine.addOrder(_orders.get(order_index));

      // check viability
      if (added && !aboveCapacity(machine)) {
        /*
         IMPORTANT: ROUNDING issue of Double, if task need to be really precise,
           and don't care about runtime use new copy:
          depthFirstSearch(order_index + 1, new Schedule(schedule));
        */
        depthFirstSearch(order_index + 1, schedule);
      }
      machine.removeOrder(_orders.get(order_index));
    }
  }

  /**
   * Generate all possible schedules
   * <p>
   * Generate all possible schedules by DFS and store them in the _schedules
   * list
   * </p>
   *
   * @see Scheduler#depthFirstSearch(int, Schedule)
   */
  public void generateAllPossible() {
    // BAB with DFS to generate all possible schedules
    _schedules.clear();
    _executor = Executors.newFixedThreadPool(_num_threads);
    _futures.clear();

    depthFirstSearch(0, new Schedule(_machines));
    // ??? Maybe wait until needed(calc grades) to get the result
    for (Future<Double> future : _futures) {
      try {
        _min_makespan = Math.min(_min_makespan, future.get().intValue());
      } catch (InterruptedException | ExecutionException e) {
        System.err.println(e.getMessage());
      }
    }
    _executor.shutdown();
    _futures.clear();

  }

  /**
   * The init method to initialize the Scheduler with the given parameters
   * <br>
   * Read in all the fields that needed to schedule the orders
   *
   * @param num_order_types          the number of order types
   * @param num_machines             the number of machines
   * @param num_orders               the number of orders
   * @param max_hours_allowed        the maximum hours allowed for all orders to
   *                                 be scheduled
   * @param max_capacity_per_machine the maximum capacity that can be Overload
   *                                 on a machine
   * @param min_capacity_per_machine the minimum capacity that a machine can
   *                                 work
   * @param machines                 the list of all machines
   * @param orders                   the list of all orders
   * @param order_type_switch_times  the switch time between different order
   *                                 types
   * @throws AssertionError if the number of machines/orders/order types is not
   *                        equal to the size of the machines/orders.order types
   *                        list
   */
  public void init(final int num_order_types, final int num_machines,
      final int num_orders, final int max_hours_allowed,
      final double max_capacity_per_machine,
      final double min_capacity_per_machine, final ArrayList<Machine> machines,
      final ArrayList<Order> orders,
      final ArrayList<ArrayList<Double>> order_type_switch_times) {
    // assert validity
    assert machines.size() == num_machines;
    assert orders.size() == num_orders;
    assert order_type_switch_times.size() == num_order_types;
    for (ArrayList<Double> o : order_type_switch_times) {
      assert o.size() == num_order_types;
    }
    Rules.capacityLowerBound = _max_hours_allowed * _min_capacity_per_machine;
    Rules.capacityUpperBound = _max_hours_allowed * _max_capacity_per_machine;

    this._num_machines = num_machines;
    this._num_production_types = num_order_types;
    this._num_orders = num_orders;
    this._max_hours_allowed = max_hours_allowed;
    this._max_capacity_per_machine = max_capacity_per_machine;
    this._min_capacity_per_machine = min_capacity_per_machine;
    this._machines = machines;
    this._orders = orders;
    this._order_type_switch_times = order_type_switch_times;
  }

  /**
   * The init method to initialize the Scheduler with random orders and machines
   * just use for demo/test
   *
   * @param num_order_types          the number of order types
   * @param num_machines             the number of machines
   * @param num_orders               the number of orders
   * @param max_hours_allowed        the maximum hours allowed for all orders to
   *                                 be scheduled
   * @param max_capacity_per_machine the maximum capacity that can be Overload
   *                                 on a machine
   * @param min_capacity_per_machine the minimum capacity that a machine can
   *                                 work
   * @param seed                     the random seed
   * @throws AssertionError if the number of order types, machines, orders, max
   *                        hours allowed, max capacity per machine, min
   *                        capacity per machine is less than or equal to zero
   *                        or the max capacity per machine is less than the min
   *                        capacity per machine
   * @see Scheduler#init(int, int, int, int, double, double, ArrayList,
   * ArrayList, ArrayList)
   */
  public void initRandom(final int num_order_types, final int num_machines,
      final int num_orders, final int max_hours_allowed,
      final double max_capacity_per_machine,
      final double min_capacity_per_machine, Integer... seed)
      throws AssertionError {
    System.out.println(
        "Init Random Scheduler: order_types: " + num_order_types + " machines: "
            + num_machines + " orders: " + num_orders + " max_hours_allowed: "
            + max_hours_allowed + " max_capacity_per_machine: "
            + max_capacity_per_machine + " min_capacity_per_machine: "
            + min_capacity_per_machine);
    assert seed.length <= 1;
    assert num_order_types > 0;
    assert num_machines > 0;
    assert num_orders > 0;
    assert max_hours_allowed > 0;
    assert max_capacity_per_machine > 0;
    assert min_capacity_per_machine >= 0;
    assert max_capacity_per_machine > min_capacity_per_machine;
    final int ORDER_QUANTITY_GRANULARITY = 5;
    final int PRODUCT_PACE_GRANULARITY = 2;
    final int PRODUCT_PACE_MIN = 10;
    final int PRODUCT_PACE_MAX = 21;
    final int MIN_ORDER_QUANTITY = 50;
    final int MAX_ORDER_QUANTITY = 100;
    final double MIN_ORDER_TYPE_SWITCH_TIME = 0.0;
    final double MAX_ORDER_TYPE_SWITCH_TIME = 1.0;
    final int MIN_EARLIEST_START_TIME = 0;
    final int MAX_EARLIEST_START_TIME = 10;
    final int MIN_LATEST_DUE_TIME = 20;
    final int MAX_LATEST_DUE_TIME = 40;
    final int RANDOM_SEED = seed.length == 1 ? seed[0] : 1337;

    Random random = new Random(RANDOM_SEED);

    // assign check const
    _max_hours_allowed = max_hours_allowed;
    _max_capacity_per_machine = max_capacity_per_machine;
    _min_capacity_per_machine = min_capacity_per_machine;
    Rules.capacityLowerBound = _max_hours_allowed * _min_capacity_per_machine;
    Rules.capacityUpperBound = _max_hours_allowed * _max_capacity_per_machine;

    // generate order types
    _num_production_types = num_order_types;

    // generate orders
    _num_orders = num_orders;
    _orders = new ArrayList<>(_num_orders);
    for (int i = 0; i < _num_orders; i++) {
      int quantity = (random.nextInt(MAX_ORDER_QUANTITY - MIN_ORDER_QUANTITY)
          + MIN_ORDER_QUANTITY) / ORDER_QUANTITY_GRANULARITY
          * ORDER_QUANTITY_GRANULARITY;
      int production_type_ID = random.nextInt(_num_production_types);
      int earliest_start_time =
          random.nextInt(MAX_EARLIEST_START_TIME - MIN_EARLIEST_START_TIME + 1)
              + MIN_EARLIEST_START_TIME;
      int delivery_time =
          random.nextInt(MAX_LATEST_DUE_TIME - MIN_LATEST_DUE_TIME + 1)
              + MIN_LATEST_DUE_TIME;
      int latest_due_time = delivery_time + 2;
      boolean init = _orders.add(
          new Order("Order " + i, i, quantity, production_type_ID,
              earliest_start_time, delivery_time, latest_due_time));
      assert init;
    }

    // generate machines
    _num_machines = num_machines;
    _machines = new ArrayList<>(_num_machines);
    for (int i = 0; i < _num_machines; i++) {
      HashMap<Integer, Integer> products_pace_per_hour = new HashMap<>(
          _num_production_types);
      for (int j = 0; j < _num_production_types; j++) {
        products_pace_per_hour.put(j,
            (random.nextInt(PRODUCT_PACE_MAX - PRODUCT_PACE_MIN + 1)
                + PRODUCT_PACE_MIN) / PRODUCT_PACE_GRANULARITY
                * PRODUCT_PACE_GRANULARITY);
      }
      boolean add = _machines.add(
          new Machine("Machine " + i, i, products_pace_per_hour));
      assert add;
    }

    // generate switch_time matrix
    _order_type_switch_times = new ArrayList<>(_num_production_types);
    for (int i = 0; i < _num_production_types; i++) {
      _order_type_switch_times.add(new ArrayList<>(_num_production_types));
      for (int j = 0; j < _num_production_types; j++) {
        _order_type_switch_times.get(i).add(j,
            (random.nextDouble() % MAX_ORDER_TYPE_SWITCH_TIME
                + MIN_ORDER_TYPE_SWITCH_TIME));
      }
    }
  }

  /**
   * Calculate the grade for all schedules based on the weights given
   *
   * @param weights the weights for on_time, makespan, est_violate, ldt_violate.
   *                if not given, the default weights are used: on_time: 40,
   *                makespan: 30, est_violate: 15, ldt_violate: 15
   * @apiNote The Schedules will be sorted by grade in descending order after
   * the call
   * @see Schedule#calcGradeByWeights(int, int, int, int)
   */
  public void calcAllSchedulesGrade(Integer... weights) {
    assert weights.length <= 4;
    int on_time_weight = (weights.length > 0) ? weights[0] : 40;
    int makespan_weight = (weights.length > 1) ? weights[1]
        : Math.max(0, (100 - on_time_weight) / 2);
    int est_weight = (weights.length > 2) ? weights[2]
        : Math.max(0, (100 - on_time_weight - makespan_weight) / 2);
    int ldt_weight = (weights.length > 3) ? weights[3]
        : Math.max(0, (100 - on_time_weight - makespan_weight - est_weight));

    // calc the grades for each schedule
    _schedules.parallelStream().forEach(schedule -> {
      schedule.calcStat(_min_makespan, _num_orders);
      schedule.calcGradeByWeights(on_time_weight, makespan_weight, est_weight,
          ldt_weight);
    });

    _schedules.sort(Schedule.scheduleComparator.reversed());
  }

  /**
   * @return the list of all schedules
   */
  public ArrayList<Schedule> getSchedules() {
    return new ArrayList<>(_schedules);
  }

  /**
   * @param index the index of the schedule in {@link Scheduler#_schedules}
   * @return the {@link GanttChart} of the schedule
   * @see Scheduler#createChart(Schedule)
   */
  public GanttChart<Number, String> createChart(final int index) {
    return createChart(_schedules.get(index));
  }

  /**
   * Create a GanttChart of the schedule
   *
   * @param schedule the schedule to be displayed
   * @return the {@link GanttChart} of the schedule
   * @apiNote we need the Scheduler to calculate the switch time between
   * different order types, so it is not static
   */
  public GanttChart<Number, String> createChart(final Schedule schedule) {
    assert schedule != null;

    ArrayList<String> machine_names = new ArrayList<>();
    final NumberAxis xAxis = new NumberAxis();
    final CategoryAxis yAxis = new CategoryAxis();
    final GanttChart<Number, String> chart = new GanttChart<>(xAxis, yAxis);
    xAxis.setLabel("Time");
    xAxis.setTickLabelFill(Color.CHOCOLATE);
    xAxis.setMinorTickCount(4);
    xAxis.setForceZeroInRange(true);
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(0);
    xAxis.setUpperBound(_max_hours_allowed * _max_capacity_per_machine);
    yAxis.setLabel("Machine name");
    yAxis.setTickLabelFill(Color.CHOCOLATE);
    yAxis.setTickLabelGap(10);

    chart.setTitle("Schedule output");
    chart.setLegendVisible(false);
    chart.setBlockHeight(50);

    for (MachineWithOrders machine : schedule) {
      String name = machine.getName();
      machine_names.add(name);
      Series<Number, String> series = new Series<>();
      int prev_order_ID = -1;

      for (OrderWithTime order : machine) {
        int start_time = order.getStartTime();
        int end_time = order.getEndTime();
        series.getData().add(new Data<>(start_time, machine.getName(),
            new ExtraData(end_time - start_time, order.getColorCode())));
        if (prev_order_ID != -1) {
          series.getData().add(new Data<>(start_time, name, new ExtraData(
              getSwitchTime(_orders.get(prev_order_ID).getProductionTypeID(),
                  order.getProductionTypeID()), "status-init")));
        }
        prev_order_ID = order.getOrderID();
      }
      chart.getData().add(series);
    }
    yAxis.setCategories(FXCollections.observableArrayList(machine_names));

    chart.getStylesheets().add(
        Objects.requireNonNull(Scheduler.class.getResource("/ganttchart.css"))
            .toExternalForm());

    // put the point on the chart
    return chart;
  }

  /**
   * @param i the order type of the first order
   * @param j the order type of the second order
   * @return the switch time from order type i to order type j
   * @throws IndexOutOfBoundsException if i or j is out of bound
   */
  protected double getSwitchTime(int i, int j)
      throws IndexOutOfBoundsException {
    if (i < 0 || i >= _num_production_types || j < 0
        || j >= _num_production_types) {
      throw new IndexOutOfBoundsException(
          "Order type out of bound: " + i + " " + j + " "
              + _num_production_types);
    }
    return _order_type_switch_times.get(i).get(j);
  }

  /**
   * @return the number of machines
   */
  public int getMachineNum() {
    return _num_machines;
  }

  /**
   * @return the number of Orders
   */
  public int getOrderNum() {
    return _num_orders;
  }

  /**
   * @return the maximum capacity that can be Overload on a machine
   */
  public double getMaxCapacityPerMachine() {
    return _max_capacity_per_machine;
  }

  /**
   * @return the minimum capacity that a machine can work
   */
  public double getMinCapacityPerMachine() {
    return _min_capacity_per_machine;
  }

  /**
   * @return the minimum makespan of all schedules
   */
  public double getMinMakespan() {
    return _min_makespan;
  }

  /**
   * @return the list of all orders
   */
  public List<Schedule> getBestSchedule() {
    return getBestSchedule(0);
  }

  /**
   * @param num if num is positive, return the first num schedules; if num is
   *            negative, return the last num schedules; if num is zero, return
   *            all schedules
   * @return the list of the best schedules
   * @apiNote Need to first call
   * {@link Scheduler#calcAllSchedulesGrade(Integer...)} to get a sorted list of
   * schedules otherwise is just a list of schedules!
   * @see Scheduler#calcAllSchedulesGrade(Integer...)
   * @see Schedule#scheduleAllOrders(Scheduler)
   */
  public List<Schedule> getBestSchedule(int num) {
    if (num == 0) {
      return new ArrayList<>(_schedules);
    } else if (num > 0) {
      return new ArrayList<>(
          _schedules.subList(0, Math.min(num, _schedules.size())));
    } else {
      return new ArrayList<>(
          _schedules.subList(_schedules.size() + num, _schedules.size()));
    }
  }

  /**
   * Override the toString function
   *
   * @return a string representation of the Scheduler
   */
  @Override
  public String toString() {
    return "Scheduler{" + "_schedules=" + _schedules + " _num_threads="
        + _num_threads + ", _num_production_types=" + _num_production_types
        + ", _num_machines=" + _num_machines + ", _num_orders=" + _num_orders
        + ", _max_hours_allowed=" + _max_hours_allowed
        + ", _max_capacity_per_machine=" + _max_capacity_per_machine
        + ", _min_capacity_per_machine=" + _min_capacity_per_machine
        + ", _min_makespan=" + _min_makespan + ", _order_type_switch_times="
        + _order_type_switch_times + ", _orders=" + _orders + ", _machines="
        + _machines + '}';
  }


  /**
   * The scheduleOrder class is used to schedule all orders in a schedule
   * <p>
   * For multithreading the precise scheduling process after BAB out the
   * suitable schedules
   * </p>
   * <p>
   * The class implements {@link Callable} to return the makespan of the
   * schedule.
   * </p>
   *
   * @see Schedule#scheduleAllOrders(Scheduler)
   * @see Callable
   */
  private static class scheduleOrder implements Callable<Double> {

    /**
     * The schedule to be scheduled
     */
    private final Schedule _schedule;

    /**
     * The scheduler to schedule the orders
     */
    private final Scheduler _scheduler;

    /**
     * @param schedule  the schedule to be scheduled, copied by reference
     * @param scheduler the scheduler to schedule the orders, copied by
     *                  reference
     */
    public scheduleOrder(Schedule schedule, Scheduler scheduler) {
      this._schedule = schedule;
      this._scheduler = scheduler;
    }

    /**
     * @return the makespan of the schedule
     */
    @Override
    public Double call() {
      _schedule.scheduleAllOrders(_scheduler);
      return _schedule.getMaxMakespan();
    }
  }
}