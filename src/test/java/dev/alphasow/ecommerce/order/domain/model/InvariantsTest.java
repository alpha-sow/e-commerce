package dev.alphasow.ecommerce.order.domain.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.alphasow.ecommerce.order.domain.exception.OrderDomainException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InvariantsTest {

  @Test
  void negativeMoneyIsDomainViolation() {
    assertThrows(OrderDomainException.class, () -> new Money(new BigDecimal("-1")));
  }

  @Test
  void nonPositiveQuantityIsDomainViolation() {
    assertThrows(
        OrderDomainException.class, () -> OrderItem.create(UUID.randomUUID(), 0, Money.zero()));
  }
}
