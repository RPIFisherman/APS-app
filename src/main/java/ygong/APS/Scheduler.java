package ygong.APS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Scheduler {
  // for random range {
  private static final int RANDOM_SEED = 1337;
  private final int ORDER_QUANTITY_GRANULARITY = 10;
  private final int PRODUCT_PACE_GRANULARITY = ORDER_QUANTITY_GRANULARITY;
  private final int PRODUCT_PACE_MIN = 10;
  private final int PRODUCT_PACE_MAX = 21;
  private final int MIN_ORDER_QUANTITY = 10;
  private final int MAX_ORDER_QUANTITY = 100;
  private final int MIN_ORDER_TYPE_SWITCH_TIME = 1;
  private final int MAX_ORDER_TYPE_SWITCH_TIME = 5;
  private final int MIN_EARLIEST_START_TIME = 0;
  private final int MAX_EARLIEST_START_TIME = 3;
  private final int MIN_LATEST_DUE_TIME = 0;
  private final int MAX_LATEST_DUE_TIME = 10;
  protected HashMap<Integer, Integer> _order_types;
  // } for random range

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
                                ArrayList<Machine> machines) throws AssertionError {
    if (order_index == _num_orders && machines != null) {
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
                         final double min_capacity_per_machine,
                         Integer... seed) throws AssertionError {
    assert seed.length <= 1;
    assert num_order_types > 0;
    assert num_machines > 0;
    assert num_orders > 0;
    assert max_hours_allowed > 0;
    assert max_capacity_per_machine > 0;
    assert min_capacity_per_machine >= 0;
    assert max_capacity_per_machine > min_capacity_per_machine;
    assert ORDER_QUANTITY_GRANULARITY > 0;
    assert ORDER_QUANTITY_GRANULARITY < MIN_ORDER_QUANTITY;
    assert PRODUCT_PACE_GRANULARITY > 0;
    assert PRODUCT_PACE_GRANULARITY < PRODUCT_PACE_MIN;
    assert PRODUCT_PACE_MIN < PRODUCT_PACE_MAX;
    assert ORDER_QUANTITY_GRANULARITY < MIN_ORDER_QUANTITY;
    assert MIN_ORDER_QUANTITY < MAX_ORDER_QUANTITY;
    assert MIN_ORDER_TYPE_SWITCH_TIME > 0;
    assert MIN_ORDER_TYPE_SWITCH_TIME < MAX_ORDER_TYPE_SWITCH_TIME;

    int random_seed = seed.length == 1 ? seed[0] : RANDOM_SEED;
    Random random = new Random(random_seed);

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
      int quantity =
              (random.nextInt((MAX_ORDER_QUANTITY - MIN_ORDER_QUANTITY) / ORDER_QUANTITY_GRANULARITY) + 1) * ORDER_QUANTITY_GRANULARITY;
      int production_type_id = random.nextInt(_num_production_types);
      int earliest_start_time =
              random.nextInt(MAX_EARLIEST_START_TIME - MIN_EARLIEST_START_TIME + 1) + MIN_EARLIEST_START_TIME;
      int latest_due_time =
              earliest_start_time + random.nextInt(MAX_LATEST_DUE_TIME - MIN_LATEST_DUE_TIME + 1) + MIN_LATEST_DUE_TIME;
      int start_time = -1;
      int end_time = -1;
      boolean init = _orders.add(new Order("Order " + i, i, quantity,
              production_type_id,
              earliest_start_time, latest_due_time, start_time, end_time,
              null, "init"));
      assert init;
    }

    // generate machines
    _num_machines = num_machines;
    _machines = new ArrayList<>(_num_machines);
    for (int i = 0; i < _num_machines; i++) {
      HashMap<Integer, Integer> products_pace_per_hour =
              new HashMap<>(_num_production_types);
      for (int j = 0; j < _num_production_types; j++) {
        products_pace_per_hour.put(j,
                (random.nextInt(PRODUCT_PACE_MAX - PRODUCT_PACE_MIN + 1) + PRODUCT_PACE_MIN) / PRODUCT_PACE_GRANULARITY * PRODUCT_PACE_GRANULARITY);
      }
      boolean add = _machines.add(new Machine("Machine " + i, i,
              products_pace_per_hour));
      assert add;
    }

    // generate switch_time matrix
    _order_type_switch_times = new ArrayList<>(_num_production_types);
    for (int i = 0; i < _num_production_types; i++) {
      _order_type_switch_times.add(new ArrayList<>(_num_production_types));
      for (int j = 0; j < _num_production_types; j++) {
        _order_type_switch_times.get(i).add(random.nextInt(MAX_ORDER_TYPE_SWITCH_TIME - MIN_ORDER_TYPE_SWITCH_TIME + 1) + MIN_ORDER_TYPE_SWITCH_TIME);
      }
    }

    _schedules = new ArrayList<>();
  }

  public void generateAllPossible() {
    // BAB with DFS to generate all possible schedules
    _schedules.clear();
    depthFirstSearch(0, new ArrayList<>(_machines));
  }

  public ArrayList<ArrayList<Machine>> getSchedules() {
    return _schedules;
  }

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

  public static final class Stat {
    public final Machine belong_to;
    protected HashMap<Integer, Integer> each_production_type_time;
    protected int total_time;

    protected int num_violation_due_time;
    protected int num_violation_start_time;

    private Stat(Machine machine) {
      this.belong_to = machine;
    }
  }
}