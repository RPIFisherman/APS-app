package ygong.APS;

import java.util.HashMap;

/**
 * A class that represents a machine in the APS system
 * <p>
 * A machine is defined as a device that can produce products. It can be a
 * single machine/workstation/production line.
 *
 * <br>Each machine has a name, an ID, and a production pace for each product
 * type that it can produce. The production pace is defined as the number of
 * products of a given type that the machine can produce in one hour.
 * </p>
 *
 * @see Schedule.MachineWithOrders
 */
public class Machine {

  /**
   * The name of the machine
   */
  public final String name;

  /**
   * The machine's ID
   * <p>
   * The ID is unique for each machine and is used as the hashcode for the
   * machine object.
   * </p>
   *
   * @see Machine#hashCode()
   */
  public final int machine_ID;

  /**
   * A map that stores the production pace for each product type that the
   * machine
   */
  private final HashMap<Integer, Integer> products_pace_per_hour;

  /**
   * Constructor of the Machine class
   *
   * @param name                   name of the machine
   * @param machine_ID             ID of the machine
   * @param products_pace_per_hour a map that stores the production pace for
   *                               each
   */
  public Machine(String name, int machine_ID,
      HashMap<Integer, Integer> products_pace_per_hour) {
    this.name = name;
    this.machine_ID = machine_ID;
    this.products_pace_per_hour = new HashMap<>(products_pace_per_hour);
  }

  /**
   * Make a deep copy of the Machine
   *
   * @param machine the machine want to copy
   */
  public Machine(Machine machine) {
    this.name = machine.name;
    this.machine_ID = machine.machine_ID;
    this.products_pace_per_hour = new HashMap<>(machine.products_pace_per_hour);
  }

  /**
   * Check if the product type can be produced on the machine
   *
   * @param production_type_ID ID of the <strong>production type</strong>,
   * @return return true if the product type can be produced on the machine
   * @apiNote production ID is <strong>NOT</strong> the order ID
   */
  public boolean checkViableOrder(int production_type_ID) {
    // return products_pace_per_hour.containsKey(production_type_ID) &&
    Integer o = products_pace_per_hour.get(production_type_ID);
    return o != null && o > 0;
  }

  /**
   * get the production pace f the specific production type
   *
   * @param production_type_id ID of the <strong>production type</strong>,
   * @return return the pace per hour of given production type. return -1 if not
   * found the production type
   * @apiNote production ID is <strong>NOT</strong> the order ID
   */
  public int getProductionPace(int production_type_id) {
    Integer o = products_pace_per_hour.get(production_type_id);
    return o == null ? -1 : o;
  }

  /**
   * Overwrite hashCode using machine ID
   *
   * @return the machine ID as the hashCode
   */
  @Override
  public int hashCode() {
    return machine_ID;
  }

  /**
   * Overwrite toString method
   *
   * @return a string representation of the machine
   */
  @Override
  public String toString() {
    return "Machine{" + "name='" + name + '\'' + ", machine_ID=" + machine_ID
        + "\n\t products_per_hour=" + products_pace_per_hour + '}';
  }

}
