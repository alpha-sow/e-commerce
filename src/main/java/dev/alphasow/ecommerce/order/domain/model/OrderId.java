package dev.alphasow.ecommerce.order.domain.model;

import java.util.Objects;
import java.util.UUID;

public record OrderId(UUID value) {
  public OrderId {
    Objects.requireNonNull(value);
  }

  public static OrderId generate() {
    return new OrderId(UUID.randomUUID());
  }

  public static OrderId from(UUID value) {
    return new OrderId(value);
  }
}
