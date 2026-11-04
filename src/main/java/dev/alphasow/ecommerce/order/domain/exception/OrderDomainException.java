package dev.alphasow.ecommerce.order.domain.exception;

public class OrderDomainException extends RuntimeException {
  public OrderDomainException(String message) {
    super(message);
  }
}
