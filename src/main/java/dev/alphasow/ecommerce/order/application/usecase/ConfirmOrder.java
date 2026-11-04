package dev.alphasow.ecommerce.order.application.usecase;

import dev.alphasow.ecommerce.order.application.command.ConfirmOrderCommand;
import dev.alphasow.ecommerce.order.application.exception.OrderNotFoundException;
import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.model.Order;
import dev.alphasow.ecommerce.order.domain.model.OrderId;

public class ConfirmOrder {
  private final OrderRepository repository;

  public ConfirmOrder(OrderRepository repository) {
    this.repository = repository;
  }

  public void execute(ConfirmOrderCommand command) {

    Order order =
        repository
            .findById(OrderId.from(command.orderId()))
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    order.confirm();

    repository.save(order);
  }
}
