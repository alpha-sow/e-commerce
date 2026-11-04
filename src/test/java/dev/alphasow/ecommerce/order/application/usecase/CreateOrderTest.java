package dev.alphasow.ecommerce.order.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.alphasow.ecommerce.order.application.command.CreateOrderCommand;
import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.model.Order;
import dev.alphasow.ecommerce.order.domain.model.OrderStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CreateOrderTest {

  @Test
  void shouldCreateOrder() {
    UUID customerId = UUID.randomUUID();
    OrderRepository repository = mock(OrderRepository.class);

    UUID result = new CreateOrder(repository).execute(new CreateOrderCommand(customerId));

    ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
    verify(repository).save(captor.capture());
    Order saved = captor.getValue();

    assertNotNull(result);
    assertEquals(result, saved.getId().value());
    assertEquals(customerId, saved.getCustomerId());
    assertEquals(OrderStatus.DRAFT, saved.getStatus());
    assertTrue(saved.getItems().isEmpty());
  }
}
