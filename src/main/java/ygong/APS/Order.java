// Helper class for Orders
package ygong.APS;

public class Order {

  private final String _name;
  private final int _order_id;
  private final int _quantity;
  private final int _production_type_id;
  private final int _earliest_start_time;
  private final int _latest_due_time;
  protected int start_time;
  protected int end_time;
  protected Machine machine;
  protected String status;

  public Order(String name, int order_id, int quantity, int production_type_id,
               int earliest_start_time, int latest_due_time, int start_time,
               int end_time, Machine machine, String status) {
    this._name = name;
    this._order_id = order_id;
    this._quantity = quantity;
    this._production_type_id = production_type_id;
    this._earliest_start_time = earliest_start_time;
    this._latest_due_time = latest_due_time;
    this.start_time = start_time;
    this.end_time = end_time;
    this.machine = machine;
    this.status = status;
  }

  public Order(Order o) {
    this(o._name, o._order_id, o._quantity, o._production_type_id,
         o._earliest_start_time, o._latest_due_time, o.start_time, o.end_time,
         o.machine, o.status);
  }

  public int getOrderId() {return _order_id;}

  public int getQuantity() {return _quantity;}

  public int getProductionTypeId() {return _production_type_id;}

  private int statusCheck() {
    if (start_time < 0 || end_time < 0) {
      throw new IllegalStateException("Start time and end time must be set");
    } else if (start_time >= _earliest_start_time &&
               end_time <= _latest_due_time) {
      status = OrderStatus.GREEN;
      return OrderStatus.GREEN_CODE;
    } else if (start_time < _earliest_start_time &&
               end_time > _latest_due_time) {
      status = OrderStatus.RED;
      return OrderStatus.RED_CODE;
    } else if (start_time < _earliest_start_time) {
      status = OrderStatus.EST_VIOLATE;
      return OrderStatus.EST_VIOLATE_CODE;
    } else {
      status = OrderStatus.LDT_VIOLATE;
      return OrderStatus.LDT_VIOLATE_CODE;
    }
  }

  @Override
  public String toString() {
    return "Order ID: " + _order_id + " Quantity: " + _quantity;
  }

  @Override
  public Order clone() {
    return new Order(this);
  }

  //  // Verbose one
  //  @Override
  //  public String toString() {
  //    return "Order ID: " + _order_id + " Quantity: " + _quantity + " produce
  //    "
  //            + "on Machine:"
  //            + " " + machine.machine_id + " from " + start_time + " to " +
  //            end_time + " "
  //            + "Status: " + status;
  //  }

  public void updateStatus() {statusCheck();}

  public int setStartEndTime(final int start_time, final int end_time)
          throws AssertionError {
    assert start_time < end_time : "Start time must be less than end time";
    assert start_time >= 0 : "Start time must be non-negative";
    this.start_time = start_time;
    this.end_time = end_time;
    updateStatus();
    return end_time;
  }

  public static final class OrderStatus {
    public static final String GREEN = "green";
    public static final int GREEN_CODE = 0;
    public static final String RED = "red";
    public static final int RED_CODE = -1;
    public static final String EST_VIOLATE = "est violate";
    public static final int EST_VIOLATE_CODE = -2;
    public static final String LDT_VIOLATE = "ldt violate";
    public static final int LDT_VIOLATE_CODE = -3;

    protected static String code2Status(int code)
            throws IllegalArgumentException {
      switch (code) {
        case GREEN_CODE:
          return GREEN;
        case RED_CODE:
          return RED;
        case EST_VIOLATE_CODE:
          return EST_VIOLATE;
        case LDT_VIOLATE_CODE:
          return LDT_VIOLATE;
        default:
          throw new IllegalArgumentException("Invalid code");
      }
    }

    protected static int status2Code(String status)
            throws IllegalArgumentException {
      switch (status) {
        case GREEN:
          return GREEN_CODE;
        case RED:
          return RED_CODE;
        case EST_VIOLATE:
          return EST_VIOLATE_CODE;
        case LDT_VIOLATE:
          return LDT_VIOLATE_CODE;
        default:
          throw new IllegalArgumentException("Invalid status");
      }
    }

    protected static String chooseColor(String status)
    throws IllegalArgumentException {
      switch (status) {
        case GREEN:
          return "status-green";
        case RED:
          return "status-red";
        case EST_VIOLATE:
          return "status-est-violate";
        case LDT_VIOLATE:
          return "status-ldt-violate";
        default:
          throw new IllegalArgumentException("Invalid status");
      }
    }

    protected static String chooseColor(int code)
            throws IllegalArgumentException {
      return chooseColor(code2Status(code));
    }
  }
}