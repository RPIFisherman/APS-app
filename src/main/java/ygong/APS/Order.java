// Helper class for Orders
package ygong.APS;

public class Order {
  private final String name;
  private final int order_id;
  private final ygong.APS.Machine machine;
  private final int start_time;
  private final int end_time;
  private final int quantity;
  private final int production_type_id;

  // for stats calc
  private final int earliest_start_time;
  private final int latest_due_time;

  public Order(String name, int order_id, ygong.APS.Machine machine,
               int start_time, int end_time, int quantity,
               int production_type_id, int earliest_start_time,
               int latest_due_time) {
    this.name = name;
    this.order_id = order_id;
    this.machine = machine;
    this.start_time = start_time;
    this.end_time = end_time;
    this.quantity = quantity;
    this.production_type_id = production_type_id;
    this.earliest_start_time = earliest_start_time;
    this.latest_due_time = latest_due_time;
  }
}