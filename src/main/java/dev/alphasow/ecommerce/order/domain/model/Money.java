package dev.alphasow.ecommerce.order.domain.model;

import dev.alphasow.ecommerce.order.domain.exception.OrderDomainException;
import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount) {
  public Money {
    if (amount.signum() < 0) {
      throw new OrderDomainException("Amount cannot be negative");
    }
  }

  public Money multiply(int quantity) {
    if (quantity <= 0) {
      throw new OrderDomainException("Quantity must be positive");
    }
    return new Money(amount.multiply(BigDecimal.valueOf(quantity)));
  }

  public Money add(Money other) {
    Objects.requireNonNull(other);
    return new Money(amount.add(other.amount));
  }

  public static Money zero() {
    return new Money(BigDecimal.ZERO);
  }
}
