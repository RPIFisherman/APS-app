package ygong.APS;

import java.util.ArrayList;
import ygong.APS.Schedule.MachineWithOrders;
import ygong.APS.Schedule.OrderWithTime;

/**
 * Rules for the APS project
 * <p>
 * Feel free to add more rules to the class
 * </p>
 *
 * @author <a href="mailto:yuyanggong.rpi@gmail.com">Yuyang Gong</a>
 * @version 1.0
 */
public enum Rules {
  ;
  /**
   * the lower bound of the machine capacity
   * <p>
   * Set in the Initialize of {@link Scheduler} to avoid multiple calculations
   * </p>
   *
   * @see Scheduler#init(int, int, int, int, double, double, ArrayList,
   * ArrayList, ArrayList)
   * @see Scheduler#initRandom(int, int, int, int, double, double, Integer...)
   */
  static double capacityLowerBound = -1;

  /**
   * the upper bound of the machine capacity
   * <p>
   * Set in the Initialize of {@link Scheduler} to avoid multiple calculations
   * </p>
   *
   * @see Scheduler#init(int, int, int, int, double, double, ArrayList,
   * ArrayList, ArrayList)
   * @see Scheduler#initRandom(int, int, int, int, double, double, Integer...)
   */
  static double capacityUpperBound = -1;

  // * after all order are settled
  // Other rules can be added e.g. belowCapacityInSize

  /**
   * @param machine   the machine will be checked
   * @param threshold lower bound of the machine capacity(unit time)
   * @return true if the machine is below the capacity
   */
  public static boolean belowCapacity(MachineWithOrders machine,
      double threshold) {
    capacityLowerBound = threshold;
    return machine._approx_run_time < threshold;
  }

  /**
   * Rule using the default lower bound of the machine capacity
   *
   * @param machine the machine will be checked
   * @return true if the machine is below the capacity
   */
  public static boolean belowCapacity(MachineWithOrders machine) {
    return machine._approx_run_time < capacityLowerBound;
  }

  // * can be checked during and after the setting

  /**
   * @param machine   the machine will be checked
   * @param threshold upper bound of the machine capacity(unit time)
   * @return true if the machine is above the capacity
   */
  public static boolean aboveCapacity(MachineWithOrders machine,
      double threshold) {
    capacityUpperBound = threshold;
    return machine._approx_run_time > threshold;
  }

  /**
   * Rule using the default upper bound of the machine capacity
   *
   * @param machine the machine will be checked
   * @return true if the machine is above the capacity
   */
  public static boolean aboveCapacity(MachineWithOrders machine) {
    return machine._approx_run_time > capacityUpperBound;
  }

  // * before adding the order

  /**
   * @param machine the machine will be checked
   * @param order   the order will be added
   * @return true if the order fits the machine
   */
  public static boolean orderFitsMachine(MachineWithOrders machine,
      OrderWithTime order) {
    return machine.machine.checkViableOrder(order.order.production_type_ID);
  }


  /**
   * @param machine the machine will be checked
   * @param order   the order will be added
   * @return true if the order fits the machine
   */
  public static boolean orderFitsMachine(MachineWithOrders machine,
      Order order) {
    return machine.machine.checkViableOrder(order.production_type_ID);
  }

}