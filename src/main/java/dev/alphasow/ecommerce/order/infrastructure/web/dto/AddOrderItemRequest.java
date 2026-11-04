package dev.alphasow.ecommerce.order.infrastructure.web.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record AddOrderItemRequest(
    @NotNull UUID productId,
    @Positive int quantity,
    @NotNull @DecimalMin("0.0") BigDecimal unitPrice) {}
