package dev.alphasow.ecommerce.order.application.usecase;

import dev.alphasow.ecommerce.order.application.command.AddOrderItemCommand;
import dev.alphasow.ecommerce.order.application.exception.OrderNotFoundException;
import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.model.*;

public class AddOrderItem {
  private final OrderRepository repository;

  public AddOrderItem(OrderRepository repository) {
    this.repository = repository;
  }

  public void execute(AddOrderItemCommand command) {

    Order order =
        repository
            .findById(OrderId.from(command.orderId()))
            .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

    order.addItem(command.productId(), command.quantity(), new Money(command.unitPrice()));

    repository.save(order);
  }
}
