package dev.alphasow.ecommerce.order.application.usecase;

import dev.alphasow.ecommerce.order.application.command.ConfirmOrderCommand;
import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.model.*;
import dev.alphasow.ecommerce.order.application.exception.OrderNotFoundException;
import dev.alphasow.ecommerce.order.domain.exception.OrderDomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfirmOrderTest {
  @Test
  void shouldConfirmOrder() {

    UUID orderId = UUID.randomUUID();

    Order order =
        Order.reconstitute(
            OrderId.from(orderId),
            UUID.randomUUID(),
            java.util.List.of(
                OrderItem.create(UUID.randomUUID(), 2, new Money(new BigDecimal("50.00")))),
            OrderStatus.DRAFT,
            java.time.OffsetDateTime.now());

    OrderRepository repository = mock(OrderRepository.class);

    when(repository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

    ConfirmOrder useCase = new ConfirmOrder(repository);

    useCase.execute(new ConfirmOrderCommand(orderId));

    assertEquals(OrderStatus.CONFIRMED, order.getStatus());

    verify(repository).save(order);
  }

  @Test
  void shouldFailWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    OrderRepository repository = mock(OrderRepository.class);
    when(repository.findById(OrderId.from(orderId))).thenReturn(Optional.empty());

    assertThrows(
        OrderNotFoundException.class,
        () -> new ConfirmOrder(repository).execute(new ConfirmOrderCommand(orderId)));

    verify(repository, never()).save(any());
  }

  @Test
  void shouldFailWhenOrderIsEmpty() {
    UUID orderId = UUID.randomUUID();
    Order order = orderWith(orderId, java.util.List.of(), OrderStatus.DRAFT);
    OrderRepository repository = mock(OrderRepository.class);
    when(repository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

    assertThrows(
        OrderDomainException.class,
        () -> new ConfirmOrder(repository).execute(new ConfirmOrderCommand(orderId)));

    assertEquals(OrderStatus.DRAFT, order.getStatus());
    verify(repository, never()).save(any());
  }

  @ParameterizedTest
  @EnumSource(
      value = OrderStatus.class,
      names = {"CONFIRMED", "CANCELLED"})
  void shouldFailWhenOrderIsNotDraft(OrderStatus status) {
    UUID orderId = UUID.randomUUID();
    Order order =
        orderWith(
            orderId,
            java.util.List.of(
                OrderItem.create(UUID.randomUUID(), 1, new Money(new BigDecimal("10.00")))),
            status);
    OrderRepository repository = mock(OrderRepository.class);
    when(repository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

    assertThrows(
        OrderDomainException.class,
        () -> new ConfirmOrder(repository).execute(new ConfirmOrderCommand(orderId)));

    assertEquals(status, order.getStatus());
    verify(repository, never()).save(any());
  }

  private static Order orderWith(
      UUID orderId, java.util.List<OrderItem> items, OrderStatus status) {
    return Order.reconstitute(
        OrderId.from(orderId), UUID.randomUUID(), items, status, java.time.OffsetDateTime.now());
  }
}
