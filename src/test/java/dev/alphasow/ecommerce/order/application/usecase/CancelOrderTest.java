package dev.alphasow.ecommerce.order.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.alphasow.ecommerce.order.application.command.CancelOrderCommand;
import dev.alphasow.ecommerce.order.application.exception.OrderNotFoundException;
import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.exception.OrderDomainException;
import dev.alphasow.ecommerce.order.domain.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class CancelOrderTest {

  @ParameterizedTest
  @EnumSource(
      value = OrderStatus.class,
      names = {"DRAFT", "CONFIRMED"})
  void shouldCancelOrder(OrderStatus status) {
    UUID orderId = UUID.randomUUID();
    Order order = orderWith(orderId, status);
    OrderRepository repository = mock(OrderRepository.class);
    when(repository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

    new CancelOrder(repository).execute(new CancelOrderCommand(orderId));

    assertEquals(OrderStatus.CANCELLED, order.getStatus());
    verify(repository).save(order);
  }

  @Test
  void shouldFailWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    OrderRepository repository = mock(OrderRepository.class);
    when(repository.findById(OrderId.from(orderId))).thenReturn(Optional.empty());

    assertThrows(
        OrderNotFoundException.class,
        () -> new CancelOrder(repository).execute(new CancelOrderCommand(orderId)));

    verify(repository, never()).save(any());
  }

  @Test
  void shouldFailWhenOrderAlreadyCancelled() {
    UUID orderId = UUID.randomUUID();
    Order order = orderWith(orderId, OrderStatus.CANCELLED);
    OrderRepository repository = mock(OrderRepository.class);
    when(repository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

    assertThrows(
        OrderDomainException.class,
        () -> new CancelOrder(repository).execute(new CancelOrderCommand(orderId)));

    assertEquals(OrderStatus.CANCELLED, order.getStatus());
    verify(repository, never()).save(any());
  }

  private static Order orderWith(UUID orderId, OrderStatus status) {
    return Order.reconstitute(
        OrderId.from(orderId),
        UUID.randomUUID(),
        List.of(OrderItem.create(UUID.randomUUID(), 1, new Money(new BigDecimal("10.00")))),
        status,
        java.time.OffsetDateTime.now());
  }
}
