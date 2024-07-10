package ygong.APS;

import java.text.DecimalFormat;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import ygong.APS.Machine.Stat;

public class Scheduler {
  private final ArrayList<ArrayList<Machine>> _schedules = new ArrayList<>();
  private final SortedMap<Grade, ArrayList<Machine>> _sorted_machines =
      new TreeMap<>(Grade.gradeComparator);
  protected HashMap<Integer, Integer> _order_types;
  private int _num_production_types = -1;
  private int _num_machines = -1;
  private int _num_orders = -1;
  private int _max_hours_allowed = -1;
  private double _max_capacity_per_machine = -1;
  private double _min_capacity_per_machine = -1;
  private int _min_makespan = Integer.MAX_VALUE;
  private ArrayList<ArrayList<Double>> _order_type_switch_times;
  private ArrayList<Order> _orders;
  private ArrayList<Machine> _machines;

  public static GanttChart<Number, String>
  createChart(final ArrayList<Machine> schedule) {

    ArrayList<String> machine_names = new ArrayList<>();
    final NumberAxis xAxis = new NumberAxis();
    final CategoryAxis yAxis = new CategoryAxis();
    final GanttChart<Number, String> chart = new GanttChart<>(xAxis, yAxis);
    xAxis.setLabel("");
    xAxis.setTickLabelFill(Color.CHOCOLATE);
    xAxis.setMinorTickCount(4);
    xAxis.setForceZeroInRange(true);
    xAxis.setAutoRanging(false);
    xAxis.setLowerBound(0);
    xAxis.setUpperBound(40);

    yAxis.setLabel("");
    yAxis.setTickLabelFill(Color.CHOCOLATE);
    yAxis.setTickLabelGap(10);

    chart.setTitle("Schedule output");
    chart.setLegendVisible(false);
    chart.setBlockHeight(50);

    for (Machine machine : schedule) {
      machine_names.add(machine.name);
      XYChart.Series<Number, String> series = new XYChart.Series<>();
      for (Order order : machine.getOrders()) {
        series.getData().add(new XYChart.Data<>(
            order.start_time, machine.name,
            new GanttChart.ExtraData(
                order.end_time - order.start_time,
                Order.OrderStatus.chooseColor(order.status))));
        series.getData().add(
            new XYChart.Data<>(order.start_time, machine.name,
                               new GanttChart.ExtraData(0.2, "init")));
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

  public void init(final int num_order_types, final int num_machines,
                   final int num_orders, final int max_hours_allowed,
                   final double max_capacity_per_machine,
                   final double min_capacity_per_machine) {
    // TODO
    this._num_production_types = num_order_types;
    this._num_machines = num_machines;
    this._num_orders = num_orders;
    this._max_hours_allowed = max_hours_allowed;
    this._max_capacity_per_machine = max_capacity_per_machine;
    this._min_capacity_per_machine = min_capacity_per_machine;
    System.err.println("Not implemented");
  }

  public void initRandom(final int num_order_types, final int num_machines,
                         final int num_orders, final int max_hours_allowed,
                         final double max_capacity_per_machine,
                         final double min_capacity_per_machine, Integer... seed)
      throws AssertionError {
    System.out.println(
        "Init Random Scheduler: order_types: " + num_order_types +
        " machines: " + num_machines + " orders: " + num_orders +
        " max_hours_allowed: " + max_hours_allowed +
        " max_capacity_per_machine: " + max_capacity_per_machine +
        " min_capacity_per_machine: " + min_capacity_per_machine);
    assert seed.length <= 1;
    assert num_order_types > 0;
    assert num_machines > 0;
    assert num_orders > 0;
    assert max_hours_allowed > 0;
    assert max_capacity_per_machine > 0;
    assert min_capacity_per_machine >= 0;
    assert max_capacity_per_machine > min_capacity_per_machine;
    final int ORDER_QUANTITY_GRANULARITY = 5;
    final int PRODUCT_PACE_GRANULARITY = 10;
    final int PRODUCT_PACE_MIN = 10;
    final int PRODUCT_PACE_MAX = 11;
    final int MIN_ORDER_QUANTITY = 50;
    final int MAX_ORDER_QUANTITY = 100;
    final double MIN_ORDER_TYPE_SWITCH_TIME = 0.0;
    final double MAX_ORDER_TYPE_SWITCH_TIME = 1.0;
    final int MIN_EARLIEST_START_TIME = 0;
    final int MAX_EARLIEST_START_TIME = 10;
    final int MIN_LATEST_DUE_TIME = 20;
    final int MAX_LATEST_DUE_TIME = 40;
    final int RANDOM_SEED = seed.length == 1 ? seed[0] : 1337;
    // make sure the constant are correct
    assert ORDER_QUANTITY_GRANULARITY <= MIN_ORDER_QUANTITY;
    assert PRODUCT_PACE_GRANULARITY > 0;
    assert PRODUCT_PACE_GRANULARITY <= PRODUCT_PACE_MIN;
    assert PRODUCT_PACE_MIN < PRODUCT_PACE_MAX;
    assert MIN_ORDER_QUANTITY < MAX_ORDER_QUANTITY;
    assert MIN_ORDER_TYPE_SWITCH_TIME >= 0;
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
            j, (random.nextDouble() + MIN_ORDER_TYPE_SWITCH_TIME));
      }
    }
  }

  public void generateAllPossible() {
    // BAB with DFS to generate all possible schedules
    _schedules.clear();
    depthFirstSearch(0, new ArrayList<>(_machines));
  }

  //  public ArrayList<ArrayList<Stat>> updateAllPossibleSchedule() {
  public ArrayList<ArrayList<Stat>>
  calcAllPossibleSchedule(Integer... weights) {
    assert weights.length <= 4;
    int on_time_weight = (weights.length > 0) ? weights[0] : 40;
    int makespan_weight =
        (weights.length > 1) ? weights[1] : (100 - on_time_weight) / 2;
    int est_weight = (weights.length > 2)
                         ? weights[2]
                         : (100 - on_time_weight - makespan_weight) / 2;
    int ldt_weight =
        (weights.length > 3)
            ? weights[3]
            : (100 - on_time_weight - makespan_weight - est_weight);

    // calculate the work time for each machine
    ArrayList<ArrayList<Stat>> allStats = new ArrayList<>();
    for (ArrayList<Machine> schedule : _schedules) {
      ArrayList<Stat> stats = new ArrayList<>();
      for (Machine machine : schedule) {
        Stat stat = calcMachineWorkTime(machine);
        stats.add(stat);
      }
      _min_makespan = Math.min(
          _min_makespan,
          stats.stream().mapToInt(s -> s.total_time).max().isPresent()
              ? stats.stream().mapToInt(s -> s.total_time).max().getAsInt()
              : 0);
      allStats.add(stats);
    }

    // calculate the grade for each schedule
    for (ArrayList<Stat> stats : allStats) {
      Grade grade = getGrade(stats);
      grade.calcGradeByWeights(on_time_weight, makespan_weight, est_weight,
                               ldt_weight);
      _sorted_machines.put(grade, stats.stream()
                                      .map(s -> s.belong_to)
                                      .collect(ArrayList::new, ArrayList::add,
                                               ArrayList::addAll));
    }
    return allStats;
  }

  private Grade getGrade(ArrayList<Stat> stats) {
    double on_time = 0;
    double makespan = 0;
    double est_violation_time = 0;
    double ldt_violation_time = 0;
    double total_time = 0;
    for (Stat stat : stats) {
      on_time += stat.num_on_time;
      makespan = Math.max(makespan, stat.total_time);
      est_violation_time += stat.violation_due_time;
      ldt_violation_time += stat.violation_start_time;
      total_time += stat.total_time;
    }
    return new Grade(0, (double)on_time / _num_orders,
                     2.0 - (((double)makespan / _min_makespan)),
                     1 - ((double)est_violation_time / total_time),
                     1 - ((double)ldt_violation_time / total_time));
  }

  private Stat calcMachineWorkTime(Machine machine) {
    HashMap<Integer, Integer> each_production_type_time = new HashMap<>();
    int total_time = 0;
    int num_on_time = 0;
    int makespan = 0;
    int violation_due_time = 0;
    int violation_start_time = 0;
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
      total_time = order.setStartEndTime(total_time, total_time + duration);
      makespan = Math.max(makespan, total_time);
      each_production_type_time.put(
          productionTypeId,
          each_production_type_time.getOrDefault(productionTypeId, 0) +
              duration);
      if (Objects.equals(order.status, Order.OrderStatus.LDT_VIOLATE)) {
        violation_due_time += order.end_time - order.latest_due_time;
      } else if (Objects.equals(order.status, Order.OrderStatus.EST_VIOLATE)) {
        violation_start_time += order.earliest_start_time - order.start_time;
      } else if (Objects.equals(order.status, Order.OrderStatus.RED)) {
        violation_due_time += order.end_time - order.latest_due_time;
        violation_start_time += order.earliest_start_time - order.start_time;
      } else {
        num_on_time++;
      }
    }
    return new Stat(machine, each_production_type_time, total_time, num_on_time,
                    makespan, violation_due_time, violation_due_time);
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

  public GanttChart<Number, String> createChart(final int index) {
    return createChart(_schedules.get(index));
  }

  // num = o return all schedules
  // if num >0 , return the best num schedules
  // if num < 0 , return the worst abs(num) schedules
  public Map<Grade, ArrayList<Machine>> getBestSchedule(int num) {
    if (num == 0) {
      return _sorted_machines;
    } else {
      Map<Grade, ArrayList<Machine>> result =
          new TreeMap<>(Grade.gradeComparator);
      if (num > 0) {
        // reverse the sorted order
        Map<Grade, ArrayList<Machine>> reverse_sorted_machines =
            new TreeMap<>(Collections.reverseOrder(Grade.gradeComparator));
        reverse_sorted_machines.putAll(_sorted_machines);
        for (int i = 0; i < num && i < _sorted_machines.size(); i++) {
          Map.Entry<Grade, ArrayList<Machine>> entry =
              reverse_sorted_machines.entrySet().iterator().next();
          result.put(entry.getKey(), entry.getValue());
          reverse_sorted_machines.remove(entry.getKey());
        }
      } else {
        for (int i = 0; i < -num && i < _sorted_machines.size(); i++) {
          Map.Entry<Grade, ArrayList<Machine>> entry =
              _sorted_machines.entrySet().iterator().next();
          result.put(entry.getKey(), entry.getValue());
          _sorted_machines.remove(entry.getKey());
        }
        // add back the removed entries
        _sorted_machines.putAll(result);
      }
      return result;
    }
  }

  public final static class Grade {
    public final double on_time_percentage;
    public final double makespan_percentage;
    public final double est_percentage;
    public final double ldt_percentage;
    // compare parameter for sorted map
    public static Comparator<Grade> gradeComparator =
        Comparator.comparingDouble(o -> o.grade_);
    double grade_;

    Grade(double grade, double on_time, double makespan, double est_percentage,
          double ldt_percentage) {
      grade_ = grade;
      on_time_percentage = on_time;
      makespan_percentage = makespan;
      this.est_percentage = est_percentage;
      this.ldt_percentage = ldt_percentage;
    }

    void calcGradeByWeights(int on_time_weight, int makespan_weight,
                            int est_weight, int ldt_weight) {
      grade_ = on_time_percentage * on_time_weight +
               makespan_percentage * makespan_weight +
               est_percentage * est_weight + ldt_percentage * ldt_weight;
    }

    @Override
    public String toString() {
      DecimalFormat df = new DecimalFormat("0.000");
      return "Grade{"
          + "grade_=" + df.format(grade_) +
          ", on_time_percentage=" + df.format(on_time_percentage) +
          ", makespan_percentage=" + df.format(makespan_percentage) +
          ", est_percentage=" + df.format(est_percentage) +
          ", ldt_percentage=" + df.format(ldt_percentage) + '}';
    }

    public double getGrade() { return grade_; }
  }
}