package dev.alphasow.ecommerce.order.infrastructure.configuration;

import dev.alphasow.ecommerce.order.application.port.out.OrderRepository;
import dev.alphasow.ecommerce.order.application.usecase.*;
import dev.alphasow.ecommerce.order.infrastructure.persistence.adapter.JpaOrderRepositoryAdapter;
import dev.alphasow.ecommerce.order.infrastructure.persistence.mapper.OrderMapper;
import dev.alphasow.ecommerce.order.infrastructure.persistence.repository.SpringDataOrderRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrderConfiguration {
  @Bean
  public OrderMapper orderMapper() {
    return new OrderMapper();
  }

  @Bean
  public OrderRepository orderRepository(
      SpringDataOrderRepository springDataRepository, OrderMapper mapper) {
    return new JpaOrderRepositoryAdapter(springDataRepository, mapper);
  }

  @Bean
  public CreateOrder createOrder(OrderRepository repository) {
    return new CreateOrder(repository);
  }

  @Bean
  public AddOrderItem addOrderItem(OrderRepository repository) {
    return new AddOrderItem(repository);
  }

  @Bean
  public ConfirmOrder confirmOrder(OrderRepository repository) {
    return new ConfirmOrder(repository);
  }

  @Bean
  public CancelOrder cancelOrder(OrderRepository repository) {
    return new CancelOrder(repository);
  }
}
