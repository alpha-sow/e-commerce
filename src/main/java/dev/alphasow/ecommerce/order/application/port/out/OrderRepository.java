package dev.alphasow.ecommerce.order.application.port.out;

import dev.alphasow.ecommerce.order.domain.model.Order;
import dev.alphasow.ecommerce.order.domain.model.OrderId;

import java.util.Optional;

public interface OrderRepository {
  Optional<Order> findById(OrderId id);

  void save(Order order);
}
