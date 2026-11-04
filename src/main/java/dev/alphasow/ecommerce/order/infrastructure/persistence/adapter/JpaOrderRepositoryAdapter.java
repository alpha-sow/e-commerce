package dev.alphasow.ecommerce.order.infrastructure.persistence.adapter;

import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.domain.model.Order;
import dev.alphasow.ecommerce.order.domain.model.OrderId;
import dev.alphasow.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import dev.alphasow.ecommerce.order.infrastructure.persistence.mapper.OrderMapper;
import dev.alphasow.ecommerce.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class JpaOrderRepositoryAdapter implements OrderRepository {

  private final SpringDataOrderRepository repository;
  private final OrderMapper mapper;

  public JpaOrderRepositoryAdapter(SpringDataOrderRepository repository, OrderMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Order> findById(OrderId id) {
    return repository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  @Transactional
  public void save(Order order) {
    Optional<OrderJpaEntity> existing = repository.findById(order.getId().value());

    if (existing.isPresent()) {
      mapper.updateEntity(order, existing.get());
    } else {
      repository.save(mapper.toJpaEntity(order));
    }
  }
}
