package ygong.APS;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MachineTest {
  Machine machine;

  @BeforeAll
  static void setUpAll() {
    System.out.println("\nRunning MachineTest...");
  }

  @AfterAll
  static void tearDownAll() {
    System.out.println("Finished MachineTest...\n");
  }

  @BeforeEach
  void setUp() {
    HashMap<Integer, Integer> products_pace_per_hour = new HashMap<>();
    products_pace_per_hour.put(1, 1);
    products_pace_per_hour.put(2, 2);
    products_pace_per_hour.put(3, -1);
    products_pace_per_hour.put(4, 4);
    this.machine = new Machine("machine1", 1, products_pace_per_hour);
  }

  @Test
  void checkReference() {
    Machine machine2 = new Machine(machine);
    assertEquals(machine.name, machine2.name);
    assertEquals(machine.machine_ID, machine2.machine_ID);
    assertEquals(machine.getProductionPace(1), machine2.getProductionPace(1));
    assertEquals(machine.getProductionPace(3), machine2.getProductionPace(3));
  }

  @Test
  void checkViableOrder() {
    Order order = new Order("order1", 1, 10, 1, 0, 10, 20);
    assertTrue(machine.checkViableOrder(order));
    order = new Order("order2", 3, 10, 5, 0, 10, 20);
    assertFalse(machine.checkViableOrder(order));
  }

  @Test
  void getProductionPace() {
    assertEquals(1, machine.getProductionPace(1));
    assertEquals(2, machine.getProductionPace(2));
    assertEquals(-1, machine.getProductionPace(3));
    assertEquals(4, machine.getProductionPace(4));

    assertEquals(-1, machine.getProductionPace(5));
  }

  @Test
  void testToString() {
    assertEquals("Machine{name='machine1', machine_ID=1\n\t products_per_hour={1=1, 2=2, 3=-1, 4=4}}", machine.toString());
  }

  @Test
  void testHashCode() {
    assertEquals(1, machine.hashCode());
  }
}