package ygong.APS;

import java.util.HashMap;
import java.util.LinkedHashSet;

public class Machine implements Cloneable {
  public final String name;
  public final int machine_id;
  final HashMap<Integer, Integer> products_pace_per_hour;
  private final LinkedHashSet<Order> _orders;
  private int _approx_run_time;

  public Machine(String name, int machine_id,
                 HashMap<Integer, Integer> products_pace_per_hour) {
    this.name = name;
    this.machine_id = machine_id;
    this.products_pace_per_hour = new HashMap<>(products_pace_per_hour);
    this._approx_run_time = 0;
    _orders = new LinkedHashSet<>();
  }

  public Machine(Machine machine) {
    this.name = machine.name;
    this.machine_id = machine.machine_id;
    this.products_pace_per_hour = new HashMap<>(machine.products_pace_per_hour);
    this._approx_run_time = machine._approx_run_time;
    this._orders = new LinkedHashSet<>(machine._orders.size());
    for (Order o : machine._orders) {
      this._orders.add(o.clone());
    }
  }

  public void addOrder(Order order) {
    _approx_run_time +=
        (int)Math.ceil((double)order.getQuantity() /
                       products_pace_per_hour.get(order.getProductionTypeId()));
    _orders.add(order);
  }

  public void removeOrder(Order order) {
    _approx_run_time -=
        (int)Math.ceil((double)order.getQuantity() /
                       products_pace_per_hour.get(order.getProductionTypeId()));
    _orders.remove(order);
  }

  public LinkedHashSet<Order> getOrders() { return _orders; }

  public boolean aboveCapacity(final int hours_allowed,
                               final double upper_percentage) {
    return _approx_run_time > hours_allowed * upper_percentage;
  }

  public boolean belowCapacity(final int hours_allowed,
                               final double lower_percentage) {
    return _approx_run_time < hours_allowed * lower_percentage;
  }

  @Override
  public String toString() {
    return "Machine{"
        + "name='" + name + '\'' + ", machine_id=" + machine_id +
        "\n\t products_per_hour=" + products_pace_per_hour +
        "\n\t _approx_run_time=" + _approx_run_time +
        //            "\n\t _orders=" + Arrays.toString(_orders.toArray()) +
        '}';
  }

  @Override
  public Machine clone() throws AssertionError {
    try {
      Machine machine = (Machine)super.clone();
      machine._orders.clear();
      for (Order o : _orders) {
        machine._orders.add(o.clone());
      }
      return machine;
    } catch (CloneNotSupportedException e) {
      throw new AssertionError("Machine clone failed");
    }
  }

  public static final class Stat {
    public final Machine belong_to;
    public final HashMap<Integer, Integer> each_production_type_time;
    public final int total_time;
    public final int num_on_time;
    public final int makespan;

    public final int violation_due_time;
    public final int violation_start_time;

    Stat(Machine belong_to, HashMap<Integer, Integer> each_production_type_time,
         int total_time, int num_on_time, int makespan, int violation_due_time,
         int violation_start_time) {
      this.belong_to = belong_to;
      this.each_production_type_time = new HashMap<>(each_production_type_time);
      this.total_time = total_time;
      this.num_on_time = num_on_time;
      this.makespan = makespan;
      this.violation_due_time = violation_due_time;
      this.violation_start_time = violation_start_time;
    }
  }
}
