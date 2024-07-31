// Helper class for Orders
package ygong.APS;

/**
 * This class represents an order in the APS system. An order is a task that
 * needs to be scheduled in the production environment.
 * <p>An order has a name, an order ID,
 * a quantity, a production type ID, an earliest start time, a delivery time,
 * and a latest due time. The order ID is unique and is used as the hashcode for
 * the order. The order class is used by the scheduler to schedule orders in the
 * production environment.
 * </p>
 *
 * <dl>
 *   <dt>Order has following constraints:</dt>
 *   <dd>1. earliest_start_time &le; delivery_time &le; latest_due_time</dd>
 *   <dd>2. quantity &gt; 0</dd>
 *   <dt>{@link Schedule.Grade} will check(as soft-bounds, not hard-requirements) to give a score:</dt>
 *   <dd>1. earliest_start_time: the time when the order can start</dd>
 *   <dd>2. delivery_time: the time when the order is delivered</dd>
 *   <dd>3. latest_due_time: the latest time when the order can be delivered</dd>
 * </dl>
 *
 * @author <a href="mailto:yuyanggong.rpi@gmail.com">Yuyang Gong</a>
 * @version 1.0
 * @see Schedule.OrderWithTime
 * @see Schedule.Grade
 */
public class Order {

  /**
   * Order name, can be any string, just use for toString method
   *
   * @see Order#toString()
   */
  protected final String name;

  /**
   * Order ID, unique ID <strong>STONGLY RECOMMENDED</strong>
   * <br> Used as the hashCode
   *
   * @see Order#hashCode()
   */
  protected final int order_ID;

  /**
   * earliest starting time, the time when the order can start
   */
  protected final int earliest_start_time;

  /**
   * delivery time, the time when the order is delivered
   */
  protected final int delivery_time;

  /**
   * latest due time, the latest time when the order can be delivered
   */
  protected final int latest_due_time;

  /**
   * quantity of the order
   */
  protected final int quantity;

  /**
   * production type ID, used to identify the production type
   */
  protected final int production_type_ID;

  /**
   * Constructor for Order
   *
   * @param name                order name
   * @param order_ID            order ID
   * @param quantity            quantity of the order
   * @param production_type_ID  production type ID
   * @param earliest_start_time earliest starting time
   * @param delivery_time       delivery time
   * @param latest_due_time     latest due time
   * @throws AssertionError if order is invalid
   */
  public Order(String name, int order_ID, int quantity, int production_type_ID,
      int earliest_start_time, int delivery_time, int latest_due_time)
      throws AssertionError {
    assert earliest_start_time <= delivery_time
        && delivery_time <= latest_due_time;
    assert quantity > 0;
    this.name = name;
    this.order_ID = order_ID;
    this.quantity = quantity;
    this.production_type_ID = production_type_ID;
    this.earliest_start_time = earliest_start_time;
    this.delivery_time = delivery_time;
    this.latest_due_time = latest_due_time;
  }

  /**
   * Copy constructor for Order
   *
   * @param o Order to copy
   */
  public Order(Order o) {
    this.name = o.name;
    this.order_ID = o.order_ID;
    this.quantity = o.quantity;
    this.production_type_ID = o.production_type_ID;
    this.earliest_start_time = o.earliest_start_time;
    this.delivery_time = o.delivery_time;
    this.latest_due_time = o.latest_due_time;
  }

  /**
   * @return order name
   */
  public String getName() {
    return name;
  }

  /**
   * @return order ID
   */
  public int getOrderID() {
    return order_ID;
  }

  /**
   * @return earliest starting time
   */
  public int getEarliestStartTime() {
    return earliest_start_time;
  }

  /**
   * @return delivery time
   */
  public int getDeliveryTime() {
    return delivery_time;
  }

  /**
   * @return latest due time
   */
  public int getLatestDueTime() {
    return latest_due_time;
  }

  /**
   * @return quantity of the order
   */
  public int getQuantity() {
    return quantity;
  }

  /**
   * @return production type ID
   */
  public int getProductionTypeID() {
    return production_type_ID;
  }

  /**
   * @return hashCode of the order, which is the order ID
   */
  @Override
  public int hashCode() {
    return order_ID;
  }

  /**
   * @param o object to compare
   * @return true if the order ID is the same
   */
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

  /**
   * @return string representation of the order
   */
  @Override
  public String toString() {
    return "Order: " + name + " Order ID: " + order_ID + " Quantity: "
        + quantity;
  }
}