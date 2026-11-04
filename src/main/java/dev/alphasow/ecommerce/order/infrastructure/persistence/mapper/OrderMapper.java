package dev.alphasow.ecommerce.order.infrastructure.persistence.mapper;

import dev.alphasow.ecommerce.order.domain.model.*;
import dev.alphasow.ecommerce.order.infrastructure.persistence.entity.*;

import java.util.List;

public class OrderMapper {
  public Order toDomain(OrderJpaEntity entity) {

    List<OrderItem> items =
        entity.getItems().stream()
            .map(item ->
                new OrderItem(
                    item.getId(),
                    item.getProductId(),
                    item.getQuantity(),
                    new Money(item.getUnitPrice())))
            .toList();

    Order order =
        Order.reconstitute(
            OrderId.from(entity.getId()),
            entity.getCustomerId(),
            items,
            OrderStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt());

    if (order.total().amount().compareTo(entity.getTotalAmount()) != 0) {
      throw new IllegalStateException("Persisted order total is inconsistent");
    }

    return order;
  }

  public OrderJpaEntity toJpaEntity(Order order) {

    OrderJpaEntity entity =
        new OrderJpaEntity(
            order.getId().value(),
            order.getCustomerId(),
            order.getStatus().name(),
            order.total().amount(),
            order.getCreatedAt());

    for (OrderItem item : order.getItems()) {
      entity.addItem(toJpaItem(item));
    }

    return entity;
  }

  public void updateEntity(Order order, OrderJpaEntity entity) {
    entity.setStatus(order.getStatus().name());
    entity.setTotalAmount(order.total().amount());

    // Conserver les lignes déjà présentes, et ajouter
    // uniquement les nouvelles lignes du domaine.
    var existingIds =
        entity.getItems().stream()
            .map(OrderItemJpaEntity::getId)
            .collect(java.util.stream.Collectors.toSet());

    var domainIds =
        order.getItems().stream().map(OrderItem::id).collect(java.util.stream.Collectors.toSet());

    // Le domaine actuel ne permet pas de supprimer
    // ou modifier une ligne existante.
    if (!domainIds.containsAll(existingIds)) {
      throw new IllegalStateException("Existing order items cannot be removed");
    }

    for (OrderItem item : order.getItems()) {
      if (!existingIds.contains(item.id())) {
        entity.addItem(toJpaItem(item));
      }
    }
  }

  private OrderItemJpaEntity toJpaItem(OrderItem item) {
    return new OrderItemJpaEntity(
        item.id(), item.productId(), item.quantity(), item.unitPrice().amount());
  }
}
