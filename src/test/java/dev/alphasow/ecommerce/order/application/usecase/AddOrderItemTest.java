package dev.alphasow.ecommerce.order.application.usecase;

import dev.alphasow.ecommerce.order.application.command.AddOrderItemCommand;
import dev.alphasow.ecommerce.order.application.exception.OrderNotFoundException;
import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AddOrderItemTest {
  @Test
  void shouldAddItemAndSaveOrder() {
    UUID orderId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();
    Order order =
        Order.reconstitute(
            OrderId.from(orderId),
            UUID.randomUUID(),
            java.util.List.of(),
            OrderStatus.DRAFT,
            java.time.OffsetDateTime.now());
    OrderRepository repository = mock(OrderRepository.class);
    when(repository.findById(OrderId.from(orderId))).thenReturn(Optional.of(order));

    new AddOrderItem(repository)
        .execute(new AddOrderItemCommand(orderId, productId, 3, new BigDecimal("10.00")));

    assertEquals(1, order.getItems().size());
    OrderItem item = order.getItems().getFirst();
    assertEquals(productId, item.productId());
    assertEquals(3, item.quantity());
    assertEquals(new Money(new BigDecimal("10.00")), item.unitPrice());
    verify(repository).save(order);
  }

  @Test
  void shouldThrowWhenOrderNotFound() {
    UUID orderId = UUID.randomUUID();
    OrderRepository repository = mock(OrderRepository.class);
    when(repository.findById(OrderId.from(orderId))).thenReturn(Optional.empty());

    AddOrderItem useCase = new AddOrderItem(repository);
    AddOrderItemCommand command =
        new AddOrderItemCommand(orderId, UUID.randomUUID(), 1, BigDecimal.ONE);

    assertThrows(OrderNotFoundException.class, () -> useCase.execute(command));
    verify(repository, never()).save(any());
  }
}
