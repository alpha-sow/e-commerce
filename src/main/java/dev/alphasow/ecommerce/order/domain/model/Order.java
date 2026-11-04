package dev.alphasow.ecommerce.order.domain.model;

import dev.alphasow.ecommerce.order.domain.exception.OrderDomainException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {
  private final OrderId id;
  private final UUID customerId;
  private final List<OrderItem> items;
  private final OffsetDateTime createdAt;

  private OrderStatus status;

  private Order(
      OrderId id,
      UUID customerId,
      List<OrderItem> items,
      OrderStatus status,
      OffsetDateTime createdAt) {
    this.id = Objects.requireNonNull(id);
    this.customerId = Objects.requireNonNull(customerId);
    this.items = new ArrayList<>(Objects.requireNonNull(items));
    this.status = Objects.requireNonNull(status);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static Order create(UUID customerId) {
    return new Order(
        OrderId.generate(), customerId, List.of(), OrderStatus.DRAFT, OffsetDateTime.now());
  }

  public static Order reconstitute(
      OrderId id,
      UUID customerId,
      List<OrderItem> items,
      OrderStatus status,
      OffsetDateTime createdAt) {
    Objects.requireNonNull(items);

    if (status == OrderStatus.CONFIRMED && items.isEmpty()) {
      throw new OrderDomainException("Confirmed order cannot be empty");
    }

    if (items.stream().anyMatch(Objects::isNull)) {
      throw new OrderDomainException("Order items cannot be null");
    }

    return new Order(id, customerId, items, status, createdAt);
  }

  public void addItem(UUID productId, int quantity, Money unitPrice) {
    ensureDraft();

    OrderItem item = OrderItem.create(productId, quantity, unitPrice);

    items.add(item);
  }

  public void confirm() {
    ensureDraft();

    if (items.isEmpty()) {
      throw new OrderDomainException("Cannot confirm an empty order");
    }

    status = OrderStatus.CONFIRMED;
  }

  public void cancel() {
    if (status == OrderStatus.CANCELLED) {
      throw new OrderDomainException("Order already cancelled");
    }

    status = OrderStatus.CANCELLED;
  }

  public Money total() {
    return items.stream().map(OrderItem::total).reduce(Money.zero(), Money::add);
  }

  private void ensureDraft() {
    if (status != OrderStatus.DRAFT) {
      throw new OrderDomainException("Order must be in DRAFT status");
    }
  }

  public OrderId getId() {
    return id;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public List<OrderItem> getItems() {
    return List.copyOf(items);
  }

  public OrderStatus getStatus() {
    return status;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
}
