package dev.alphasow.ecommerce.order.infrastructure.persistence.repository;

import dev.alphasow.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataOrderRepository extends JpaRepository<OrderJpaEntity, UUID> {

  @Override
  @EntityGraph(attributePaths = "items")
  Optional<OrderJpaEntity> findById(UUID id);
}
