package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.service.OrderService;
import com.ecommerce.project.service.StripeService;
import com.ecommerce.project.util.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;
    private final AuthUtil authUtil;
    private final StripeService stripeService;

    public OrderController(OrderService orderService,
                           AuthUtil authUtil,
                           StripeService stripeService) {
        this.orderService = orderService;
        this.authUtil = authUtil;
        this.stripeService = stripeService;
    }

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod, @RequestBody OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.loggedInEmail();
        log.info("orderRequestDTO DATA: {}", orderRequestDTO);
        OrderDTO order = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/order/stripe-client-secret")
    public ResponseEntity<String> createStripeClientSecret(@RequestBody StripePaymentDto stripePaymentDto) throws StripeException {
        log.info("StripePaymentDTO Received {}", stripePaymentDto);
        PaymentIntent paymentIntent = stripeService.paymentIntent(stripePaymentDto);
        return new ResponseEntity<>(paymentIntent.getClientSecret(), HttpStatus.CREATED);
    }

    @GetMapping("/admin/orders")
    public ResponseEntity<OrderResponse> getAllOrders(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        OrderResponse orderResponse = orderService.getAllOrders(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @GetMapping("/seller/orders")
    public ResponseEntity<OrderResponse> getAllSellerOrders(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_ORDERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder
    ) {
        OrderResponse orderResponse = orderService.getAllSellerOrders(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(orderResponse, HttpStatus.OK);
    }

    @PutMapping("/admin/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(@PathVariable Long orderId,
                                                      @RequestBody OrderStatusUpdateDto orderStatusUpdateDto) {
        OrderDTO order = orderService.updateOrder(orderId, orderStatusUpdateDto.getStatus());
        return new ResponseEntity<>(order, HttpStatus.OK);
    }

    @PutMapping("/seller/orders/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatusBySeller(@PathVariable Long orderId,
                                                      @RequestBody OrderStatusUpdateDto orderStatusUpdateDto) {
        OrderDTO order = orderService.updateOrderStatusBySeller(orderId, orderStatusUpdateDto.getStatus());
        return new ResponseEntity<>(order, HttpStatus.OK);
    }
}
