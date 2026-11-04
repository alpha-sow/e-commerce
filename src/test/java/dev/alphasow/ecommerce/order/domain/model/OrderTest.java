package dev.alphasow.ecommerce.order.domain.model;

import dev.alphasow.ecommerce.order.domain.exception.OrderDomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
  @Test
  void shouldCreateDraftOrder() {
    Order order = Order.create(UUID.randomUUID());

    assertEquals(OrderStatus.DRAFT, order.getStatus());
    assertTrue(order.getItems().isEmpty());
  }

  @Test
  void shouldCalculateOrderTotal() {
    Order order = Order.create(UUID.randomUUID());

    order.addItem(UUID.randomUUID(), 2, new Money(new BigDecimal("50.00")));

    order.addItem(UUID.randomUUID(), 1, new Money(new BigDecimal("25.00")));

    assertEquals(0, new BigDecimal("125.00").compareTo(order.total().amount()));
  }

  @Test
  void shouldRejectEmptyOrderConfirmation() {
    Order order = Order.create(UUID.randomUUID());

    assertThrows(OrderDomainException.class, order::confirm);
  }

  @Test
  void shouldPreventModificationAfterConfirmation() {
    Order order = Order.create(UUID.randomUUID());

    order.addItem(UUID.randomUUID(), 1, new Money(new BigDecimal("100.00")));

    order.confirm();

    assertThrows(
        OrderDomainException.class,
        () -> order.addItem(UUID.randomUUID(), 1, new Money(BigDecimal.TEN)));
  }

  private static Order orderWithItem() {
    Order order = Order.create(UUID.randomUUID());
    order.addItem(UUID.randomUUID(), 1, new Money(BigDecimal.TEN));
    return order;
  }

  @Test
  void shouldConfirmNonEmptyDraftOrder() {
    Order order = orderWithItem();

    order.confirm();

    assertEquals(OrderStatus.CONFIRMED, order.getStatus());
  }

  @Test
  void shouldCancelDraftOrder() {
    Order order = Order.create(UUID.randomUUID());

    order.cancel();

    assertEquals(OrderStatus.CANCELLED, order.getStatus());
  }

  @Test
  void shouldCancelConfirmedOrder() {
    Order order = orderWithItem();
    order.confirm();

    order.cancel();

    assertEquals(OrderStatus.CANCELLED, order.getStatus());
  }

  @Test
  void shouldRejectRepeatedCancellation() {
    Order order = Order.create(UUID.randomUUID());
    order.cancel();

    assertThrows(OrderDomainException.class, order::cancel);
  }

  @Test
  void shouldRejectAddItemAfterCancellation() {
    Order order = Order.create(UUID.randomUUID());
    order.cancel();

    assertThrows(
        OrderDomainException.class,
        () -> order.addItem(UUID.randomUUID(), 1, new Money(BigDecimal.TEN)));
  }

  @Test
  void shouldHaveZeroTotalWhenEmpty() {
    assertEquals(
        0, Money.zero().amount().compareTo(Order.create(UUID.randomUUID()).total().amount()));
  }

  @Test
  void shouldRejectConfirmedOrderWithoutItemsOnReconstitution() {
    assertThrows(
        OrderDomainException.class,
        () ->
            Order.reconstitute(
                OrderId.generate(),
                UUID.randomUUID(),
                List.of(),
                OrderStatus.CONFIRMED,
                OffsetDateTime.now()));
  }

  @Test
  void shouldRejectNullItemOnReconstitution() {
    List<OrderItem> items = new ArrayList<>();
    items.add(null);

    assertThrows(
        OrderDomainException.class,
        () ->
            Order.reconstitute(
                OrderId.generate(),
                UUID.randomUUID(),
                items,
                OrderStatus.DRAFT,
                OffsetDateTime.now()));
  }

  @Test
  void shouldExposeImmutableItems() {
    Order order = orderWithItem();
    List<OrderItem> items = order.getItems();

    assertThrows(UnsupportedOperationException.class, items::clear);
  }
}
