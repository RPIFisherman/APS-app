// Helper class for Orders
package ygong.APS;

public class Order {

  protected final String name;
  protected final int order_ID; // unique ID REQUIRED, used as hashcode
  protected final int earliest_start_time;
  protected final int delivery_time;
  protected final int latest_due_time;
  protected final int quantity;
  protected final int production_type_ID;

  public Order(String name, int order_ID, int quantity, int production_type_ID,
      int earliest_start_time, int delivery_time, int latest_due_time)
      throws AssertionError {
    assert earliest_start_time <= delivery_time &&
        delivery_time <= latest_due_time;
    assert quantity > 0;
    this.name = name;
    this.order_ID = order_ID;
    this.quantity = quantity;
    this.production_type_ID = production_type_ID;
    this.earliest_start_time = earliest_start_time;
    this.delivery_time = delivery_time;
    this.latest_due_time = latest_due_time;
  }

  public Order(Order o) {
    this.name = o.name;
    this.order_ID = o.order_ID;
    this.quantity = o.quantity;
    this.production_type_ID = o.production_type_ID;
    this.earliest_start_time = o.earliest_start_time;
    this.delivery_time = o.delivery_time;
    this.latest_due_time = o.latest_due_time;
  }

  public String getName() {
    return name;
  }

  public int getOrderID() {
    return order_ID;
  }

  public int getEarliestStartTime() {
    return earliest_start_time;
  }

  public int getDeliveryTime() {
    return delivery_time;
  }

  public int getLatestDueTime() {
    return latest_due_time;
  }

  public int getQuantity() {
    return quantity;
  }

  public int getProductionTypeID() {
    return production_type_ID;
  }

  @Override
  public String toString() {
    return "Order: " + name +
        " Order ID: " + order_ID + " Quantity: " + quantity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Order order = (Order) o;
    return order_ID == order.order_ID;
  }

  @Override
  public int hashCode() {
    return order_ID;
  }
}