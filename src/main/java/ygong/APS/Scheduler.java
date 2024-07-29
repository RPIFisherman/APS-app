package ygong.APS;

import static ygong.APS.Rules.aboveCapacity;
import static ygong.APS.Rules.belowCapacity;
import static ygong.APS.Rules.orderFitsMachine;

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
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
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
 * <p>
 */
public class Scheduler {

  private final List<Schedule> _schedules = new ArrayList<>();
  private final ArrayList<Future<Double>> _futures = new ArrayList<>();
  private final int _num_threads;
  private ExecutorService _executor;
  private int _num_production_types = -1;
  private int _num_machines = -1;
  private int _num_orders = -1;
  private int _max_hours_allowed = -1;
  private double _max_capacity_per_machine = -1;
  private double _min_capacity_per_machine = -1;
  private double _min_makespan = Double.MAX_VALUE;
  private ArrayList<ArrayList<Double>> _order_type_switch_times;
  private ArrayList<Order> _orders;
  private ArrayList<Machine> _machines;

  public Scheduler() {
    _num_threads = Runtime.getRuntime().availableProcessors() - 1;
  }

  public Scheduler(int num_threads) {
    assert num_threads > 0 : "Threads should be greater than zero!";
    _num_threads = num_threads;
  }

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

      // UNDONE remove the checker since the add will check viability
      // if order does not fit machine, skip
      if (!orderFitsMachine(machine, _orders.get(order_index))) {
        continue;
      }

      // add order
      machine.addOrder(_orders.get(order_index));
      // check viability UNDONE
      if (aboveCapacity(machine)) {
        return;
      }

      // depthFirstSearch(order_index + 1, new Schedule(schedule));
      depthFirstSearch(order_index + 1, schedule);
      machine.removeOrder(_orders.get(order_index));
    }
  }

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
          + MIN_ORDER_QUANTITY)
          / ORDER_QUANTITY_GRANULARITY * ORDER_QUANTITY_GRANULARITY;
      int production_type_ID = random.nextInt(_num_production_types);
      int earliest_start_time =
          random.nextInt(MAX_EARLIEST_START_TIME - MIN_EARLIEST_START_TIME + 1)
              + MIN_EARLIEST_START_TIME;
      int delivery_time =
          random.nextInt(MAX_LATEST_DUE_TIME - MIN_LATEST_DUE_TIME + 1)
              + MIN_LATEST_DUE_TIME;
      int latest_due_time = delivery_time + 2;
      int start_time = -1;
      int end_time = -1;
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
   * Calculate the grade for all schedules based on the weights gives
   *
   * @param weights the weights for on_time, makespan, est_violate, ldt_violate.
   *                if not given, the default weights are used: on_time: 40,
   *                makespan: 30, est_violate: 15, ldt_violate: 15
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

  public ArrayList<Schedule> getSchedules() {
    return new ArrayList<>(_schedules);
  }

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

  public GanttChart<Number, String> createChart(final int index) {
    return createChart(_schedules.get(index));
  }

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
      XYChart.Series<Number, String> series = new XYChart.Series<>();
      int prev_order_ID = -1;

      for (OrderWithTime order : machine) {
        int start_time = order.getStartTime();
        int end_time = order.getEndTime();
        series.getData().add(new XYChart.Data<>(start_time, machine.getName(),
            new GanttChart.ExtraData(end_time - start_time,
                order.getColorCode())));
        if (prev_order_ID != -1) {
          series.getData().add(new XYChart.Data<>(start_time, name,
              new GanttChart.ExtraData(getSwitchTime(
                  _orders.get(prev_order_ID).getProductionTypeID(),
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
  protected double getSwitchTime(int i, int j) {
    return _order_type_switch_times.get(i).get(j);
  }

  public int getMachineNum() {
    return _num_machines;
  }

  public int getOrderNum() {
    return _num_orders;
  }

  public double getMaxCapacityPerMachine() {
    return _max_capacity_per_machine;
  }

  public double getMinCapacityPerMachine() {
    return _min_capacity_per_machine;
  }

  public double getMinMakespan() {
    return _min_makespan;
  }

  // num = o return all schedules
  // if num >0 , return the best num schedules TODO
  // if num < 0 , return the worst abs(num) schedules
  public List<Schedule> getBestSchedule(int num) {
    if (num == 0) {
      return _schedules;
    } else if (num > 0) {
      return _schedules.subList(0, Math.min(num, _schedules.size()));
    } else {
      return _schedules.subList(_schedules.size() + num, _schedules.size());
    }
  }


  // TODO: add a new thread to schedule all orders in current schedule
  private static class scheduleOrder implements Callable<Double> {

    private final Schedule _schedule;
    private final Scheduler _scheduler;

    public scheduleOrder(Schedule schedule, Scheduler scheduler) {
      this._schedule = schedule;
      this._scheduler = scheduler;
    }

    @Override
    public Double call() throws ExecutionException, InterruptedException {
      _schedule.scheduleAllOrders(_scheduler);
      return _schedule.getMaxMakespan();
    }
  }
}