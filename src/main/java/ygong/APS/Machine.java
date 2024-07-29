package ygong.APS;

import java.util.HashMap;

public class Machine {

  public final String name;
  public final int machine_ID;
  private final HashMap<Integer, Integer> products_pace_per_hour;

  public Machine(String name, int machine_ID,
                 HashMap<Integer, Integer> products_pace_per_hour) {
    this.name = name;
    this.machine_ID = machine_ID;
    this.products_pace_per_hour = new HashMap<>(products_pace_per_hour);
  }

  public Machine(Machine machine) {
    this.name = machine.name;
    this.machine_ID = machine.machine_ID;
    this.products_pace_per_hour = new HashMap<>(machine.products_pace_per_hour);
  }

  public boolean checkViableOrder(int production_type_ID) {
    // return products_pace_per_hour.containsKey(production_type_ID) &&
    Integer o = products_pace_per_hour.get(production_type_ID);
    return o != null && o > 0;
  }

  public int getProductionPace(int production_type_id) {
    Integer o = products_pace_per_hour.get(production_type_id);
    return o == null ? -1 : o;
  }

  @Override
  public String toString() {
    return "Machine{"
        + "name='" + name + '\'' + ", machine_ID=" + machine_ID +
        "\n\t products_per_hour=" + products_pace_per_hour + '}';
  }

  @Override
  public int hashCode() {
    return machine_ID;
  }

  // public static final class Stat {
  //   public final Machine belong_to;
  //   public final HashMap<Integer, Integer> each_production_type_time;
  //   public final int total_time;
  //   public final int num_on_time;
  //   public final int makespan;
  //
  //   public final int num_violation_due_time;
  //   public final int num_violation_start_time;
  //
  //   Stat(Machine belong_to, HashMap<Integer, Integer>
  //   each_production_type_time,
  //        int total_time, int num_on_time, int makespan, int
  //        violation_due_time, int violation_start_time) {
  //     this.belong_to = belong_to;
  //     this.each_production_type_time = new
  //     HashMap<>(each_production_type_time); this.total_time = total_time;
  //     this.num_on_time = num_on_time;
  //     this.makespan = makespan;
  //     this.num_violation_due_time = violation_due_time;
  //     this.num_violation_start_time = violation_start_time;
  //   }
  // }
}
