package dev.alphasow.ecommerce.order.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record AddOrderItemCommand(
    UUID orderId, UUID productId, int quantity, BigDecimal unitPrice) {}
