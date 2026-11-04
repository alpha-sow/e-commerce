package dev.alphasow.ecommerce.order.application.usecase;

import dev.alphasow.ecommerce.order.application.command.CancelOrderCommand;
import dev.alphasow.ecommerce.order.application.exception.OrderNotFoundException;
import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.model.Order;
import dev.alphasow.ecommerce.order.domain.model.OrderId;

public class CancelOrder {
  private final OrderRepository repository;

  public CancelOrder(OrderRepository repository) {
    this.repository = repository;
  }

  public void execute(CancelOrderCommand command) {

    Order order =
        repository
            .findById(OrderId.from(command.orderId()))
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    order.cancel();

    repository.save(order);
  }
}
