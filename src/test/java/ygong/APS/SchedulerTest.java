package ygong.APS;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

/**
 * Test class for Scheduler
 * <p>
 * NOTE: This test has random generator, change with caution
 */
class SchedulerTest {

  static int num_order_types = 3;
  static int num_machines = 2;
  static int num_orders = 10;
  static int max_hours = 20;
  static double max_capacity = 1.3;
  static double min_capacity = 0.50;
  static int seed = 1337;
  Scheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler = new Scheduler();
    scheduler.initRandom(num_order_types, num_machines, num_orders, max_hours,
        max_capacity, min_capacity, seed);
  }

  @Test
  void initTest() {
    assertThrows(AssertionError.class, () -> scheduler = new Scheduler(-1));

    assertThrows(AssertionError.class, () -> scheduler = new Scheduler(0));

    scheduler = new Scheduler(1);
  }

  @Test
  void checkRandomInit() {
    assertEquals(num_machines, scheduler.getMachineNum());
    assertEquals(num_orders, scheduler.getOrderNum());
    assertEquals(max_capacity, scheduler.getMaxCapacityPerMachine());
    assertEquals(min_capacity, scheduler.getMinCapacityPerMachine());
  }

  @Test
  void testGenerateAll() {
    // test original
    scheduler.generateAllPossible();
    List<Schedule> schedules = scheduler.getSchedules();
    assertEquals(524, schedules.size());
    assertFalse(schedules.isEmpty());
    assertTrue(schedules.size() < Math.pow(num_machines, num_orders));
    assertNotEquals(0.0, scheduler.getMinMakespan());
    assertNotEquals(Double.MAX_VALUE, scheduler.getMinMakespan());
    assertEquals(16.0, scheduler.getMinMakespan());

    // test lose bound
    scheduler = new Scheduler();
    scheduler.initRandom(num_order_types, num_machines, num_orders, max_hours,
        max_capacity, 0.0, seed);
    scheduler.generateAllPossible();
    schedules = scheduler.getSchedules();
    assertEquals((int) Math.pow(num_machines, num_orders), schedules.size());

    // NOTE: getFirst() is not supported in Java 8, use get(0) instead if necessary
    assertEquals(num_orders,
        schedules.getFirst().getMachine(0).getOrders().size());
    assertEquals(0, schedules.getFirst().getMachine(1).getOrders().size());
    assertEquals(0,
        schedules.getFirst().getMachine(0).getOrders().getFirst().getOrderID());

    // test Max Capacity upper bound
    scheduler = new Scheduler();
    scheduler.initRandom(num_order_types, num_machines, num_orders, max_hours,
        min_capacity + 0.0001, min_capacity, seed);
    scheduler.generateAllPossible();
    schedules = scheduler.getSchedules();
    assertEquals(0, schedules.size());

    // test Min Capacity lower bound
    scheduler = new Scheduler();
    scheduler.initRandom(num_order_types, num_machines, num_orders, max_hours,
        max_capacity, 1.0, seed);
    scheduler.generateAllPossible();
    schedules = scheduler.getSchedules();
    assertEquals(0, schedules.size());
  }

  /**
   * Test for simple APSDemo.java, which is a typical usage of the package
   */
  @Test
  void testTypicalUsage() {
    // generate all possible schedules
    scheduler.generateAllPossible();
    ArrayList<Schedule> schedules = scheduler.getSchedules();
    assertEquals(524, schedules.size());

    // calculate all schedules grade
    scheduler.calcAllSchedulesGrade(0, 30, 0, 0);
    schedules.sort(Schedule.scheduleComparator.reversed());
    assertEquals(524, schedules.size());
    // NOTE: getFirst() is not supported in Java 8, use get(0) instead if necessary
    assertEquals(30, schedules.getFirst().getGrade());
    assertEquals(16.0, schedules.getFirst().getMaxMakespan());

    // get the best schedule
    List<Schedule> sortedSchedules = scheduler.getBestSchedule(0);
    for (int i = 0; i < 524; i++) {
      assertEquals(sortedSchedules.get(i), schedules.get(i));
    }
  }

  @Nested
  @ExtendWith(ApplicationExtension.class)
  class GanttPlotTest {

    @Start
    void start(Stage stage) {
      scheduler = new Scheduler();
      scheduler.initRandom(num_order_types, num_machines, num_orders, max_hours,
          max_capacity, min_capacity, seed);
      scheduler.generateAllPossible();

      scheduler.calcAllSchedulesGrade();
      ArrayList<Schedule> schedules = scheduler.getSchedules();
      assertEquals(524, schedules.size());
      GanttChart<Number, String> chart = scheduler.createChart(0);
      assertNotNull(chart);

      Scene scene = new Scene(chart, 800, 600);
      stage.setScene(scene);
      stage.show();

    }

    @Test
    void testContains() {
      FxAssert.verifyThat(".chart-title", Node::isVisible);
      FxAssert.verifyThat(".chart-plot-background", Node::isVisible);
      FxAssert.verifyThat(".chart-horizontal-grid-lines", Node::isVisible);
      FxAssert.verifyThat(".chart-vertical-grid-lines", Node::isVisible);
    }

  }


}