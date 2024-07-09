package ygong.APS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Scheduler {
  protected HashMap<Integer, Integer> _order_types;
  private int _num_production_types = -1;
  private int _num_machines = -1;
  private int _num_orders = -1;
  private int _max_hours_allowed = -1;
  private double _max_capacity_per_machine = -1;
  private double _min_capacity_per_machine = -1;
  private ArrayList<ArrayList<Integer>> _order_type_switch_times;

  private ArrayList<Order> _orders;
  private ArrayList<Machine> _machines;
  private ArrayList<ArrayList<Machine>> _schedules;

  private ArrayList<Machine> deepCopy(ArrayList<Machine> machines) {
    ArrayList<Machine> copy = new ArrayList<>(machines.size());
    for (Machine machine : machines) {
      copy.add(new Machine(machine));
    }
    return copy;
  }

  private void depthFirstSearch(final int order_index,
                                ArrayList<Machine> machines)
      throws AssertionError {
    assert machines != null;
    assert order_index <= _num_orders;
    if (order_index == _num_orders) {
      for (Machine m : machines) {
        if (m.belowCapacity(_max_hours_allowed, _min_capacity_per_machine)) {
          return;
        }
      }
      boolean add = _schedules.add(deepCopy(machines));
      assert add;
      return;
    }
    for (int i = 0; i < _num_machines; i++) {
      Machine machine = machines.get(i);
      if (machine.aboveCapacity(_max_hours_allowed,
                                _max_capacity_per_machine)) {
        return;
      }
      machine.addOrder(_orders.get(order_index));
      depthFirstSearch(order_index + 1, new ArrayList<>(machines));
      machine.removeOrder(_orders.get(order_index));
    }
  }

  public void init() {
    // TODO
    System.err.println("Not implemented");
  }

  public void initRandom(final int num_order_types, final int num_machines,
                         final int num_orders, final int max_hours_allowed,
                         final double max_capacity_per_machine,
                         final double min_capacity_per_machine, Integer... seed)
      throws AssertionError {
    assert seed.length <= 1;
    assert num_order_types > 0;
    assert num_machines > 0;
    assert num_orders > 0;
    assert max_hours_allowed > 0;
    assert max_capacity_per_machine > 0;
    assert min_capacity_per_machine >= 0;
    assert max_capacity_per_machine > min_capacity_per_machine;
    final int ORDER_QUANTITY_GRANULARITY = 10;
    final int PRODUCT_PACE_GRANULARITY = 10;
    final int PRODUCT_PACE_MIN = 10;
    final int PRODUCT_PACE_MAX = 21;
    final int MIN_ORDER_QUANTITY = 10;
    final int MAX_ORDER_QUANTITY = 100;
    final int MIN_ORDER_TYPE_SWITCH_TIME = 1;
    final int MAX_ORDER_TYPE_SWITCH_TIME = 5;
    final int MIN_EARLIEST_START_TIME = 0;
    final int MAX_EARLIEST_START_TIME = 3;
    final int MIN_LATEST_DUE_TIME = 0;
    final int MAX_LATEST_DUE_TIME = 10;
    final int RANDOM_SEED = seed.length == 1 ? seed[0] : 1337;
    // make sure the constant are correct
    assert ORDER_QUANTITY_GRANULARITY <= MIN_ORDER_QUANTITY;
    assert PRODUCT_PACE_GRANULARITY > 0;
    assert PRODUCT_PACE_GRANULARITY <= PRODUCT_PACE_MIN;
    assert PRODUCT_PACE_MIN < PRODUCT_PACE_MAX;
    assert MIN_ORDER_QUANTITY < MAX_ORDER_QUANTITY;
    assert MIN_ORDER_TYPE_SWITCH_TIME > 0;
    assert MIN_ORDER_TYPE_SWITCH_TIME < MAX_ORDER_TYPE_SWITCH_TIME;

    Random random = new Random(RANDOM_SEED);

    // assign check const
    _max_hours_allowed = max_hours_allowed;
    _max_capacity_per_machine = max_capacity_per_machine;
    _min_capacity_per_machine = min_capacity_per_machine;

    // generate order types
    _num_production_types = num_order_types;
    _order_types = new HashMap<>(_num_production_types);
    for (int i = 0; i < _num_production_types; i++) {
      _order_types.put(i + 10000, i);
    }

    // generate orders
    _num_orders = num_orders;
    _orders = new ArrayList<>(_num_orders);
    for (int i = 0; i < _num_orders; i++) {
      int order_type_id = random.nextInt(_num_production_types);
      int quantity = (random.nextInt((MAX_ORDER_QUANTITY - MIN_ORDER_QUANTITY) /
                                     ORDER_QUANTITY_GRANULARITY) +
                      1) *
                     ORDER_QUANTITY_GRANULARITY;
      int production_type_id = random.nextInt(_num_production_types);
      int earliest_start_time = random.nextInt(MAX_EARLIEST_START_TIME -
                                               MIN_EARLIEST_START_TIME + 1) +
                                MIN_EARLIEST_START_TIME;
      int latest_due_time =
          earliest_start_time +
          random.nextInt(MAX_LATEST_DUE_TIME - MIN_LATEST_DUE_TIME + 1) +
          MIN_LATEST_DUE_TIME;
      int start_time = -1;
      int end_time = -1;
      boolean init = _orders.add(new Order(
          "Order " + i, i, quantity, production_type_id, earliest_start_time,
          latest_due_time, start_time, end_time, null, "init"));
      assert init;
    }

    // generate machines
    _num_machines = num_machines;
    _machines = new ArrayList<>(_num_machines);
    for (int i = 0; i < _num_machines; i++) {
      HashMap<Integer, Integer> products_pace_per_hour =
          new HashMap<>(_num_production_types);
      for (int j = 0; j < _num_production_types; j++) {
        products_pace_per_hour.put(
            j, (random.nextInt(PRODUCT_PACE_MAX - PRODUCT_PACE_MIN + 1) +
                PRODUCT_PACE_MIN) /
                   PRODUCT_PACE_GRANULARITY * PRODUCT_PACE_GRANULARITY);
      }
      boolean add =
          _machines.add(new Machine("Machine " + i, i, products_pace_per_hour));
      assert add;
    }

    // generate switch_time matrix
    _order_type_switch_times = new ArrayList<>(_num_production_types);
    for (int i = 0; i < _num_production_types; i++) {
      _order_type_switch_times.add(new ArrayList<>(_num_production_types));
      for (int j = 0; j < _num_production_types; j++) {
        _order_type_switch_times.get(i).add(
            random.nextInt(MAX_ORDER_TYPE_SWITCH_TIME -
                           MIN_ORDER_TYPE_SWITCH_TIME + 1) +
            MIN_ORDER_TYPE_SWITCH_TIME);
      }
    }

    _schedules = new ArrayList<>();
  }

  public void generateAllPossible() {
    // BAB with DFS to generate all possible schedules
    _schedules.clear();
    depthFirstSearch(0, new ArrayList<>(_machines));
  }

  public ArrayList<ArrayList<Stat>> updateAllPossibleSchedule() {
    ArrayList<ArrayList<Stat>> allStats = new ArrayList<>();
    for (ArrayList<Machine> schedule : _schedules) {
      ArrayList<Stat> stats = new ArrayList<>();
      for (Machine machine : schedule) {
        stats.add(calcMachineWorkTime(machine));
      }
      allStats.add(stats);
    }
    return allStats;
  }

  private Stat calcMachineWorkTime(Machine machine) {
    HashMap<Integer, Integer> eachProductionTypeTime = new HashMap<>();
    int totalTime = 0;
    int numViolationDueTime = 0;
    int numViolationStartTime = 0;
    int previous_index = -1;
    for (Order order : machine.getOrders()) {
      int productionTypeId = order.getProductionTypeId();
      int duration = (int)Math.ceil(
          (double)order.getQuantity() /
              machine.products_pace_per_hour.get(productionTypeId) +
          ((previous_index < 0) ? 0
                                : _order_type_switch_times.get(previous_index)
                                      .get(productionTypeId)));
      previous_index = productionTypeId;
      //      order.start_time =  totalTime;
      //      totalTime += duration;
      //      order.end_time = totalTime;
      totalTime = order.setStartEndTime(totalTime, totalTime + duration);
      eachProductionTypeTime.put(
          productionTypeId,
          eachProductionTypeTime.getOrDefault(productionTypeId, 0) + duration);
      if (Objects.equals(order.status, Order.OrderStatus.LDT_VIOLATE)) {
        numViolationDueTime++;
      } else if (Objects.equals(order.status, Order.OrderStatus.EST_VIOLATE)) {
        numViolationStartTime++;
      } else if (Objects.equals(order.status, Order.OrderStatus.RED)) {
        numViolationDueTime++;
        numViolationStartTime++;
      }
    }
    return new Stat(machine, eachProductionTypeTime, totalTime,
                    numViolationDueTime, numViolationStartTime);
  }

  public ArrayList<ArrayList<Machine>> getSchedules() { return _schedules; }

  public void printSchedules() {
    for (ArrayList<Machine> schedule : _schedules) {
      for (Machine machine : schedule) {
        System.out.println(machine);
        for (Order order : machine.getOrders()) {
          System.out.println("\t" + order);
        }
      }
      System.out.println();
    }
  }

  public void plotSchedule(final int index) {
    ArrayList<Machine> Schedule = _schedules.get(index);
    Stage stage = new Stage();
    stage.setTitle("Plot Schedule " + index);
    ArrayList<String> machine_names = new ArrayList<>();

    final NumberAxis xAxis = new NumberAxis();
    final CategoryAxis yAxis = new CategoryAxis();
    final GanttChart<Number, String> chart = new GanttChart<>(xAxis, yAxis);
    xAxis.setLabel("");
    xAxis.setTickLabelFill(Color.CHOCOLATE);
    xAxis.setMinorTickCount(4);

    yAxis.setLabel("");
    yAxis.setTickLabelFill(Color.CHOCOLATE);
    yAxis.setTickLabelGap(10);

    chart.setTitle("Schedule " + index);
    chart.setLegendVisible(false);
    chart.setBlockHeight(50);

    for (Machine machine : Schedule) {
      machine_names.add(machine.name);
      XYChart.Series series = new XYChart.Series();
      for (Order order : machine.getOrders()) {
        series.getData().add(
            new XYChart.Data(order.start_time, machine.name,
                             new GanttChart.ExtraData(
                                 order.end_time - order.start_time,
                                 Order.OrderStatus.chooseColor(order.status))));
      }
      chart.getData().add(series);
    }
    yAxis.setCategories(FXCollections.observableArrayList(machine_names));

    chart.getStylesheets().add(
        getClass().getResource("/ganttchart.css").toExternalForm());

    Scene scene = new Scene(chart, 620, 350);
    stage.setScene(scene);
    stage.show();
  }

  public final class Stat {
    public final Machine belong_to;
    public final HashMap<Integer, Integer> each_production_type_time;
    public final int total_time;

    public final int num_violation_due_time;
    public final int num_violation_start_time;

    private Stat(Machine machine,
                 HashMap<Integer, Integer> eachProductionTypeTime,
                 int totalTime, int numViolationDueTime,
                 int numViolationStartTime) {
      this.belong_to = machine;
      each_production_type_time = eachProductionTypeTime;
      total_time = totalTime;
      num_violation_due_time = numViolationDueTime;
      num_violation_start_time = numViolationStartTime;
    }
  }
}