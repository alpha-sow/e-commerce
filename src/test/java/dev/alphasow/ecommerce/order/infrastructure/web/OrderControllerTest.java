package dev.alphasow.ecommerce.order.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.alphasow.ecommerce.order.application.command.*;
import dev.alphasow.ecommerce.order.application.usecase.*;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean CreateOrder createOrder;
  @MockitoBean AddOrderItem addOrderItem;
  @MockitoBean ConfirmOrder confirmOrder;
  @MockitoBean CancelOrder cancelOrder;

  @Test
  void createReturns201WithLocation() throws Exception {
    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();
    when(createOrder.execute(any(CreateOrderCommand.class))).thenReturn(orderId);

    mockMvc
        .perform(
            post("/api/order")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":\"" + customerId + "\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/order/" + orderId));

    ArgumentCaptor<CreateOrderCommand> captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
    verify(createOrder).execute(captor.capture());
    assertEquals(customerId, captor.getValue().customerId());
  }

  @Test
  void addItemReturns204AndDelegates() throws Exception {
    UUID orderId = UUID.randomUUID();
    UUID productId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/api/order/{id}/items", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":\"" + productId + "\",\"quantity\":2,\"unitPrice\":9.99}"))
        .andExpect(status().isNoContent());

    ArgumentCaptor<AddOrderItemCommand> captor = ArgumentCaptor.forClass(AddOrderItemCommand.class);
    verify(addOrderItem).execute(captor.capture());
    AddOrderItemCommand c = captor.getValue();
    assertEquals(orderId, c.orderId());
    assertEquals(productId, c.productId());
    assertEquals(2, c.quantity());
    assertEquals(0, new BigDecimal("9.99").compareTo(c.unitPrice()));
  }

  @Test
  void addItemWithInvalidQuantityReturns400() throws Exception {
    mockMvc
        .perform(
            post("/api/order/{id}/items", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"productId\":\""
                        + UUID.randomUUID()
                        + "\",\"quantity\":0,\"unitPrice\":9.99}"))
        .andExpect(status().isBadRequest());

    verify(addOrderItem, never()).execute(any());
    verifyNoInteractions(addOrderItem);
  }

  @Test
  void confirmReturns204AndDelegates() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc.perform(post("/api/order/{id}/confirm", orderId)).andExpect(status().isNoContent());

    verify(confirmOrder).execute(new ConfirmOrderCommand(orderId));
  }

  @Test
  void cancelReturns204AndDelegates() throws Exception {
    UUID orderId = UUID.randomUUID();

    mockMvc.perform(post("/api/order/{id}/cancel", orderId)).andExpect(status().isNoContent());

    verify(cancelOrder).execute(new CancelOrderCommand(orderId));
  }
}
