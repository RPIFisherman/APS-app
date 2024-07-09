package ygong.APS;

import java.util.HashMap;
import java.util.LinkedHashSet;

public class Machine {
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
    for(Order o : machine._orders) {
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
  public Machine clone() {
    return new Machine(this);
  }
}
