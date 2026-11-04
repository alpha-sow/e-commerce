package dev.alphasow.ecommerce.order.domain.model;

import dev.alphasow.ecommerce.order.domain.exception.OrderDomainException;
import java.util.Objects;
import java.util.UUID;

public record OrderItem(UUID id, UUID productId, int quantity, Money unitPrice) {
  public OrderItem {
    Objects.requireNonNull(id);
    Objects.requireNonNull(productId);
    Objects.requireNonNull(unitPrice);

    if (quantity <= 0) {
      throw new OrderDomainException("Quantity must be positive");
    }
  }

  public static OrderItem create(UUID productId, int quantity, Money unitPrice) {
    return new OrderItem(UUID.randomUUID(), productId, quantity, unitPrice);
  }

  public Money total() {
    return unitPrice.multiply(quantity);
  }
}
