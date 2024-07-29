package ygong.APS;

import ygong.APS.Schedule.MachineWithOrders;
import ygong.APS.Schedule.OrderWithTime;

public enum Rules {
  ;
  protected static double capacityLowerBound = -1;
  protected static double capacityUpperBound = -1;

  // * after all order are settled
  // Other rules can be added belowCapacityInSize
  public static boolean belowCapacity(MachineWithOrders machine, double threshold) {
    return machine._approx_run_time < threshold;
  }

  public static boolean belowCapacity(MachineWithOrders machine) {
    return machine._approx_run_time < capacityLowerBound;
  }

  // * can be checked during and after the setting
  public static boolean aboveCapacity(MachineWithOrders machine, double threshold) {
    return machine._approx_run_time > threshold;
  }

  public static boolean aboveCapacity(MachineWithOrders machine) {
    return machine._approx_run_time > capacityUpperBound;
  }

  // * before adding the order
  public static boolean orderFitsMachine(MachineWithOrders machine, OrderWithTime order) {
    return machine.machine.checkViableOrder(order.order.production_type_ID);
  }

  public static boolean orderFitsMachine(MachineWithOrders machine, Order order) {
    return machine.machine.checkViableOrder(order.production_type_ID);
  }

}