package dev.alphasow.ecommerce.order.application.command;

import java.util.UUID;

public record ConfirmOrderCommand(UUID orderId) {}
