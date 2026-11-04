package dev.alphasow.ecommerce.order.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_order")
public class OrderJpaEntity {
  @Id private UUID id;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "status", nullable = false, length = 30)
  private String status;

  @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderItemJpaEntity> items = new ArrayList<>();

  protected OrderJpaEntity() {
    // JPA
  }

  public OrderJpaEntity(
      UUID id, UUID customerId, String status, BigDecimal totalAmount, OffsetDateTime createdAt) {
    this.id = id;
    this.customerId = customerId;
    this.status = status;
    this.totalAmount = totalAmount;
    this.createdAt = createdAt;
  }

  public void addItem(OrderItemJpaEntity item) {
    items.add(item);
    item.setOrder(this);
  }

  public void replaceItems(List<OrderItemJpaEntity> newItems) {
    for (OrderItemJpaEntity item : items) {
      item.setOrder(null);
    }
    items.clear();

    for (OrderItemJpaEntity item : newItems) {
      addItem(item);
    }
  }

  public UUID getId() {
    return id;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public String getStatus() {
    return status;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public List<OrderItemJpaEntity> getItems() {
    return items;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }
}
