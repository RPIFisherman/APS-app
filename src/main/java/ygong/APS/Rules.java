package ygong.APS;

import ygong.APS.Order;

import ygong.APS.Schedule.MachineWithOrders;
import ygong.APS.Schedule.OrderWithSchedule;

public enum Rules {
  ;

  // * after all order are settled
  // Other rules can be added belowCapacityInSize
  public static boolean belowCapacity(MachineWithOrders machine, double threshold) {
    return machine._approx_run_time < threshold;
  }

  // * during the setting
  public static boolean orderFitsMachine(MachineWithOrders machine, OrderWithSchedule order) {
    return machine.machine.checkViableOrder(order.order);
  }

  public static boolean orderFitsMachine(MachineWithOrders machine, Order order) {
    return machine.machine.checkViableOrder(order);
  }

  // * can be checked during and after the setting
  public static boolean aboveCapacity(MachineWithOrders machine, double threshold) {
    return machine._approx_run_time > threshold;
  }
}