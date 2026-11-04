package dev.alphasow.ecommerce.order.infrastructure.web;

import dev.alphasow.ecommerce.order.application.command.*;
import dev.alphasow.ecommerce.order.application.usecase.*;
import dev.alphasow.ecommerce.order.infrastructure.web.dto.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/order")
public class OrderController {
  private final CreateOrder createOrder;
  private final AddOrderItem addOrderItem;
  private final ConfirmOrder confirmOrder;
  private final CancelOrder cancelOrder;

  public OrderController(
      CreateOrder createOrder,
      AddOrderItem addOrderItem,
      ConfirmOrder confirmOrder,
      CancelOrder cancelOrder) {
    this.createOrder = createOrder;
    this.addOrderItem = addOrderItem;
    this.confirmOrder = confirmOrder;
    this.cancelOrder = cancelOrder;
  }

  @PostMapping
  public ResponseEntity<Void> create(@Valid @RequestBody CreateOrderRequest request) {
    UUID id = createOrder.execute(new CreateOrderCommand(request.customerId()));

    return ResponseEntity.created(URI.create("/api/order/" + id)).build();
  }

  @PostMapping("/{orderId}/items")
  public ResponseEntity<Void> addItem(
      @PathVariable UUID orderId, @Valid @RequestBody AddOrderItemRequest request) {
    addOrderItem.execute(
        new AddOrderItemCommand(
            orderId, request.productId(), request.quantity(), request.unitPrice()));

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{orderId}/confirm")
  public ResponseEntity<Void> confirm(@PathVariable UUID orderId) {
    confirmOrder.execute(new ConfirmOrderCommand(orderId));

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<Void> cancel(@PathVariable UUID orderId) {
    cancelOrder.execute(new CancelOrderCommand(orderId));

    return ResponseEntity.noContent().build();
  }
}
