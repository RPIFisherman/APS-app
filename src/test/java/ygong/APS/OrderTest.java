package ygong.APS;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderTest {
  Order order;

  @BeforeAll
  static void setUpAll() {
    System.out.println("\nRunning OrderTest...");
  }

  @AfterAll
  static void tearDownAll() {
    System.out.println("Finished OrderTest...\n");
  }

  @BeforeEach
  void setUp() {
     this.order = new Order("order1", 1, 10, 1, 0, 10, 20);
  }

  @Test
  void testOrder() {
    assertThrows(AssertionError.class, () -> new Order("order1", 1, 10, 1, 10, 0, 20));
    assertThrows(AssertionError.class, () -> new Order("order1", 1, 10, 1, 0, 10, 5));

    Order order = new Order("order1", 1, 10, 1, 0, 10, 20);
    assertEquals("order1", order.getName());
    assertEquals(1, order.getOrderID());
    assertEquals(0, order.getEarliestStartTime());
    assertEquals(10, order.getDeliveryTime());
    assertEquals(20, order.getLatestDueTime());
    assertEquals(10, order.getQuantity());
    assertEquals(1, order.getProductionTypeID());

    Order order2 = new Order(order);
    assertEquals("order1", order2.getName());
    assertEquals(1, order2.getOrderID());
    assertEquals(0, order2.getEarliestStartTime());
    assertEquals(10, order2.getDeliveryTime());
    assertEquals(20, order2.getLatestDueTime());
    assertEquals(10, order2.getQuantity());
    assertEquals(1, order2.getProductionTypeID());
  }

  @Test
  void getName() {
    assertEquals("order1", order.getName());
  }

  @Test
  void getOrderID() {
    assertEquals(1, order.getOrderID());
  }

  @Test
  void getEarliestStartTime() {
    assertEquals(0, order.getEarliestStartTime());
  }

  @Test
  void getDeliveryTime() {
    assertEquals(10, order.getDeliveryTime());
  }

  @Test
  void getLatestDueTime() {
    assertEquals(20, order.getLatestDueTime());
  }

  @Test
  void getQuantity() {
    assertEquals(10, order.getQuantity());
  }

  @Test
  void getProductionTypeID() {
    assertEquals(1, order.getProductionTypeID());
  }

  @Test
  void testToString() {
    assertEquals("Order: order1 Order ID: 1 Quantity: 10", order.toString());
  }

  @Test
  void testHashCode() {
    assertEquals(1, order.hashCode());
  }
}