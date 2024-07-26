package ygong.APS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ygong.APS.Rules.aboveCapacity;
import static ygong.APS.Rules.belowCapacity;
import static ygong.APS.Rules.orderFitsMachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ygong.APS.Schedule.MachineWithOrders;
import ygong.APS.Schedule.OrderWithSchedule;

class ScheduleTest {

  Scheduler scheduler;
  Schedule schedule;
  ArrayList<Machine> machines;
  ArrayList<Order> orders;

  @BeforeAll
  static void setUpAll() {
    System.out.println("\nRunning ScheduleTest...");
  }

  @AfterAll
  static void tearDown() {
    System.out.println("Finished ScheduleTest...\n");
  }

  @BeforeEach
  void setUp() {
    // Create Machines
    this.machines = new ArrayList<>();
    HashMap<Integer, Integer> products_pace_per_hour = new HashMap<>();
    products_pace_per_hour.put(1, -1);
    products_pace_per_hour.put(2, 2);
    products_pace_per_hour.put(3, 3);
    products_pace_per_hour.put(4, 4);
    this.machines.add(new Machine("machine1", 1, products_pace_per_hour));
    products_pace_per_hour = new HashMap<>();
    products_pace_per_hour.put(1, 1);
    products_pace_per_hour.put(2, -2);
    products_pace_per_hour.put(3, 3);
    products_pace_per_hour.put(4, 4);
    this.machines.add(new Machine("machine2", 2, products_pace_per_hour));

    // Create Orders
    this.orders = new ArrayList<>();
    this.orders.add(new Order("1", 1, 1, 1, 1, 1, 1));
    this.orders.add(new Order("2", 2, 2, 2, 2, 2, 2));
    this.orders.add(new Order("3", 3, 3, 3, 3, 3, 3));
    this.orders.add(new Order("4", 4, 4, 4, 4, 4, 4));
    this.orders.add(new Order("5", 5, 5, 3, 0, 5, 5));
    this.orders.add(new Order("6", 6, 6, 3, 0, 1, 1));
    this.orders.add(new Order("7", 7, 7, 3, 0, 1, 8));

    schedule = new Schedule(machines);

  }

  @Test
  void testCopy() {
    Schedule schedule1 = new Schedule(schedule);
    assertEquals(schedule.getGrade(), schedule1.getGrade());
    assertEquals(schedule.getMachine(0).getMachineID(),
        schedule1.getMachine(0).getMachineID());
    assertEquals(schedule.getMachine(1).getMachineID(),
        schedule1.getMachine(1).getMachineID());

    assertTrue(schedule1.getMachine(0).addOrder(orders.get(1)));
    // IMPORTANT: change to arraylist, so can add same order to any machine
    // assertFalse(schedule1.getMachine(0).addOrder(orders.get(1)));
    assertNotEquals(schedule.getMachine(0).iterator(),
        schedule1.getMachine(0).iterator());
    assertTrue(schedule1.getMachine(0).removeOrder(orders.get(1)));
    assertEquals(schedule.getMachine(0).iterator().hasNext(),
        schedule1.getMachine(0).iterator().hasNext());
  }

  @Test
  void testSchedule() {
    // grade is null
    Schedule schedule2 = new Schedule(schedule);
    assertEquals(0.0, schedule2.getGrade());

    // set grade
    this.testStatsAndStatus();
    schedule2 = new Schedule(schedule);
    assertEquals(1.0, schedule2.getGrade());
    assertNotNull(schedule2.toString());
  }

  @Test
  void testStatsAndStatus() {
    assertTrue(schedule.getMachine(0).addOrder(orders.get(1)));
    assertTrue(schedule.getMachine(0).addOrder(orders.get(4)));
    assertTrue(schedule.getMachine(0).addOrder(orders.get(5)));
    assertTrue(schedule.getMachine(0).addOrder(orders.get(6)));
    assertTrue(schedule.getMachine(1).addOrder(orders.get(0)));

    schedule.getMachine(0).scheduleAllOrders();
    schedule.getMachine(1).scheduleAllOrders();
    // schedule.g
    schedule.calcStat(8.0, 5);
    assertEquals(8.0, schedule.getMaxMakespan());
    assertEquals(-1, schedule.getGrade());
    assertEquals(1.0, schedule.calcGradeByWeights(0, 1, 0, 0).getGrade());

    // use jump matrix
    this.scheduler = new Scheduler();
    ArrayList<ArrayList<Double>> jump_matrix = new ArrayList<>();
    for (int i = 0; i < 4; i++) {
      ArrayList<Double> row = new ArrayList<>();
      for (int j = 0; j < 4; j++) {
        row.add(0.0);
      }
      jump_matrix.add(row);
    }
    this.scheduler.init(3, 2, 20, 40, 1.1, 0.60, new ArrayList<>(2),
        new ArrayList<>(20), jump_matrix);

    Schedule schedule1 = new Schedule(schedule);
    schedule1.getMachine(0).scheduleAllOrders(this.scheduler);
    schedule1.getMachine(1).scheduleAllOrders(this.scheduler);
    schedule1.calcStat(8.0, 5);
    assertEquals(8.0, schedule1.getMaxMakespan());
    assertEquals(-1, schedule1.getGrade());
    assertEquals(1.0, schedule1.calcGradeByWeights(0, 1, 0, 0).getGrade());

    // compare schedule and schedule1
    assertEquals(schedule.getGrade(), schedule1.getGrade());
    assertEquals(schedule.getMaxMakespan(), schedule1.getMaxMakespan());

    // assert gets the same value
    assertEquals(schedule.getMaxMakespan(), schedule1.getMaxMakespan());
    ArrayList<OrderWithSchedule> orders = schedule.getMachine(0).getOrders();
    ArrayList<OrderWithSchedule> orders1 = schedule1.getMachine(0).getOrders();
    for (int i = 0; i < 4; ++i) {
      assertEquals(orders.get(i).getOrderID(), orders1.get(i).getOrderID());
      assertEquals(orders.get(i).getStartTime(), orders1.get(i).getStartTime());
      assertEquals(orders.get(i).getEndTime(), orders1.get(i).getEndTime());
      assertEquals(orders.get(i).getProductionTypeID(),
          orders1.get(i).getProductionTypeID());
      assertEquals(orders.get(i).getColorCode(), orders1.get(i).getColorCode());
    }
  }

  @Test
  void forEach() {
    for (MachineWithOrders mwo : schedule) {
      assertNotNull(mwo);
      assertTrue(mwo.addOrder(orders.get(2)));
      assertTrue(mwo.addOrder(orders.get(3)));
    }
    assertFalse(schedule.getMachine(0).addOrder(orders.get(0)));
    assertFalse(schedule.getMachine(1).addOrder(orders.get(1)));
  }

  @Test
  void spliterator() {
    assertNotNull(schedule.spliterator());
  }

  @Test
  void testGetMaxMakespan() {
    // empty schedule
    Schedule schedule1 = new Schedule(new ArrayList<>());
    AssertionError e = assertThrows(AssertionError.class,
        schedule1::getMaxMakespan);
    assertEquals("Machine list is empty", e.getMessage());
    ArrayList<Machine> machines = new ArrayList<>();
    machines.add(this.machines.get(0));
    machines.add(this.machines.get(1));
    schedule1 = new Schedule(machines);
    AssertionError e2 = assertThrows(AssertionError.class,
        schedule1::getMaxMakespan);
    assertEquals("All machines are empty", e2.getMessage());
    assertTrue(schedule1.getMachine(1).addOrder(orders.getFirst()));
    schedule1.getMachine(1).scheduleAllOrders();
    assertEquals(1.0, schedule1.getMaxMakespan());

    schedule.getMachine(1).addOrder(orders.get(0));
    schedule.getMachine(0).addOrder(orders.get(1));

    schedule.getMachine(0).scheduleAllOrders();
    schedule.getMachine(1).scheduleAllOrders();

    schedule.calcStat(1.0, 2);
    assertEquals(1.0, schedule.getMaxMakespan());
  }

  @Test
  void compareTo() {
    Schedule schedule1 = new Schedule(schedule);
    assertEquals(0, schedule.compareTo(schedule1));
  }

  @Test
  void testToString() {
    assertNotNull(schedule.toString());
  }

  @Test
  void testHashCode() {
    Schedule schedule1 = new Schedule(schedule);
    assertNotEquals(schedule1.hashCode(), schedule.hashCode());
  }

  @Test
  void iterator() {
    assertNotNull(schedule.iterator());
  }

  @Test
  void testForEach() {
    for (MachineWithOrders mwo : schedule) {
      assertNotNull(mwo);
    }
  }

  @Test
  void testSpliterator() {
    assertNotNull(schedule.spliterator());
  }

  @Test
  void testGetMachine() {
    assertNotNull(schedule.getMachine(0));
    assertNotNull(schedule.getMachine(1));
    IndexOutOfBoundsException e = assertThrows(IndexOutOfBoundsException.class,
        () -> schedule.getMachine(2));
    assertEquals("Index 2 out of bounds for length 2", e.getMessage());
  }

  @Test
  void testOrder() {
    OrderWithSchedule o1 = new OrderWithSchedule(orders.get(0));
    assertEquals(1, o1.getOrderID());
    OrderWithSchedule o2 = new OrderWithSchedule(orders.get(0));
    assertEquals(1, o2.getOrderID());

    assertEquals(o1.hashCode(), o2.hashCode());
    LinkedHashSet<OrderWithSchedule> orders = new LinkedHashSet<>();
    assertTrue(orders.add(o1));
    assertFalse(orders.add(o2));
    assertTrue(orders.remove(o2));
    assertTrue(orders.isEmpty());
  }

  @Test
  void testMachineWithOrderCopy() {
    MachineWithOrders mwo = new MachineWithOrders(machines.get(0));
    MachineWithOrders mwo1 = new MachineWithOrders(mwo);
    assertEquals(mwo.getMachineID(), mwo1.getMachineID());
    assertEquals(mwo.getName(), mwo1.getName());
    assertFalse(mwo.addOrder(orders.get(0)));
    assertTrue(mwo.addOrder(orders.get(1)));
    assertFalse(mwo1.removeOrder(orders.get(1)));
  }

  @Test
  void testOrderWithScheduleEquals() {
    OrderWithSchedule o1 = new OrderWithSchedule(orders.get(0));
    OrderWithSchedule o2 = new OrderWithSchedule(orders.get(0));
    assertEquals(o1, o2);
    assertEquals(o1.hashCode(), o2.hashCode());
    assertNotEquals(o1, orders.get(0));
    assertNotEquals(o1, null);
    assertEquals(o1, o1);
  }

  @Test
  void testStatusCheck() {
    OrderWithSchedule o1 = new OrderWithSchedule(orders.get(0));
    IllegalStateException e = assertThrows(IllegalStateException.class,
        o1::statusCheck);
    assertEquals("Start time and end time must be set", e.getMessage());
  }

  @Test
  void testRulesOnSchedule() {
    assertTrue(schedule.getMachine(0).addOrder(orders.get(1)));
    assertTrue(schedule.getMachine(0).addOrder(orders.get(4)));
    assertTrue(belowCapacity(schedule.getMachine(0), 10000.0));
    assertFalse(orderFitsMachine(schedule.getMachine(0), orders.get(0)));
    assertFalse(orderFitsMachine(schedule.getMachine(0),
        new OrderWithSchedule(orders.get(0))));
    assertTrue(aboveCapacity(schedule.getMachine(0), 0.0));
  }
}