package dev.alphasow.ecommerce.order.application.usecase;

import dev.alphasow.ecommerce.order.application.command.CreateOrderCommand;
import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.model.Order;

import java.util.UUID;

public class CreateOrder {
  private final OrderRepository repository;

  public CreateOrder(OrderRepository repository) {
    this.repository = repository;
  }

  public UUID execute(CreateOrderCommand command) {

    Order order = Order.create(command.customerId());

    repository.save(order);

    return order.getId().value();
  }
}
