package com.ecommerce.project.controller;

import com.ecommerce.project.payload.*;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.StripeService;
import com.ecommerce.project.util.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private StripeService stripeService;

    @MockitoBean
    private AuthUtil authUtil;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    // ---------- PLACE ORDER ----------
    @Test
    void orderProducts_shouldReturnCreated() throws Exception {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setAddressId(1L);

        OrderDTO orderDTO = new OrderDTO();

        Mockito.when(authUtil.loggedInEmail())
                .thenReturn("user@test.com");

        Mockito.when(orderService.placeOrder(
                        Mockito.anyString(),
                        Mockito.anyLong(),
                        Mockito.anyString(),
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any(),
                        Mockito.any()))
                .thenReturn(orderDTO);

        mockMvc.perform(post("/api/order/users/payments/{paymentMethod}", "COD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // ---------- STRIPE CLIENT SECRET ----------
    @Test
    void createStripeClientSecret_shouldReturnCreated() throws Exception {
        StripePaymentDto stripePaymentDto = new StripePaymentDto();

        PaymentIntent paymentIntent = Mockito.mock(PaymentIntent.class);
        Mockito.when(paymentIntent.getClientSecret())
                .thenReturn("secret_123");

        Mockito.when(stripeService.paymentIntent(Mockito.any()))
                .thenReturn(paymentIntent);

        mockMvc.perform(post("/api/order/stripe-client-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stripePaymentDto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("secret_123"));
    }

    // ---------- GET ALL ORDERS (ADMIN) ----------
    @Test
    void getAllOrders_shouldReturnOk() throws Exception {
        OrderResponse response = new OrderResponse();

        Mockito.when(orderService.getAllOrders(
                        Mockito.anyInt(),
                        Mockito.anyInt(),
                        Mockito.anyString(),
                        Mockito.anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk());
    }

    // ---------- GET ALL ORDERS (SELLER) ----------
    @Test
    void getAllSellerOrders_shouldReturnOk() throws Exception {
        OrderResponse response = new OrderResponse();

        Mockito.when(orderService.getAllSellerOrders(
                        Mockito.anyInt(),
                        Mockito.anyInt(),
                        Mockito.anyString(),
                        Mockito.anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/seller/orders"))
                .andExpect(status().isOk());
    }

    // ---------- UPDATE ORDER STATUS (ADMIN) ----------
    @Test
    void updateOrderStatus_shouldReturnOk() throws Exception {
        OrderStatusUpdateDto statusDto = new OrderStatusUpdateDto();
        statusDto.setStatus("DELIVERED");

        OrderDTO orderDTO = new OrderDTO();

        Mockito.when(orderService.updateOrder(
                        Mockito.eq(1L),
                        Mockito.eq("DELIVERED")))
                .thenReturn(orderDTO);

        mockMvc.perform(put("/api/admin/orders/{orderId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isOk());
    }

    // ---------- UPDATE ORDER STATUS (SELLER) ----------
    @Test
    void updateOrderStatusBySeller_shouldReturnOk() throws Exception {
        OrderStatusUpdateDto statusDto = new OrderStatusUpdateDto();
        statusDto.setStatus("SHIPPED");

        OrderDTO orderDTO = new OrderDTO();

        Mockito.when(orderService.updateOrderStatusBySeller(
                        Mockito.eq(1L),
                        Mockito.eq("SHIPPED")))
                .thenReturn(orderDTO);

        mockMvc.perform(put("/api/seller/orders/{orderId}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isOk());
    }
}