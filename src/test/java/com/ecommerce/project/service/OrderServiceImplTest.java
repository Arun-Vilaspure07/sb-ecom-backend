package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.kafka.event.OrderCreatedEvent;
import com.ecommerce.project.kafka.producer.OrderEventProducer;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.payload.OrderResponse;
import com.ecommerce.project.repositories.*;
import com.ecommerce.project.util.AuthUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private CartRepository cartRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private CartService cartService;
    @Mock private ModelMapper modelMapper;
    @Mock private ProductRepository productRepository;
    @Mock private AuthUtil authUtil;
    @Mock private OrderEventProducer orderEventProducer;

    @Test
    void placeOrder_success() {
        String email = "test@mail.com";
        Long addressId = 1L;

        User user = new User();
        user.setUserId(10L);

        Product product = new Product();
        product.setProductId(100L);
        product.setQuantity(10);
        product.setUser(user);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setProductPrice(500.0);
        cartItem.setDiscount(0.0);

        Cart cart = new Cart();
        cart.setCartId(1L);
        cart.setTotalPrice(1000.0);
        cart.setCartItems(List.of(cartItem));

        Address address = new Address();
        address.setAddressId(addressId);

        Order savedOrder = new Order();
        savedOrder.setOrderId(99L);
        savedOrder.setTotalAmount(1000.0);
        savedOrder.setOrderStatus("Accepted");

        when(cartRepository.findCartByEmail(email)).thenReturn(cart);
        when(authUtil.loggedInUser()).thenReturn(user);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderItemRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // ✅ FIX: initialize orderItems to avoid NullPointerException
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setOrderItems(new ArrayList<>());

        when(modelMapper.map(any(Order.class), eq(OrderDTO.class)))
                .thenReturn(orderDTO);

        when(modelMapper.map(any(OrderItem.class), eq(OrderItemDTO.class)))
                .thenReturn(new OrderItemDTO());

        OrderDTO result = orderService.placeOrder(
                email, addressId,
                "CARD", "STRIPE", "pid", "SUCCESS", "OK"
        );

        assertNotNull(result);

        verify(orderEventProducer, times(1))
                .sendOrderCreatedEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void placeOrder_cartNotFound() {
        when(cartRepository.findCartByEmail(anyString())).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.placeOrder(
                        "x@mail.com", 1L,
                        "CARD", "PG", "id", "OK", "MSG"
                )
        );
    }

    @Test
    void placeOrder_addressNotFound() {
        Cart cart = new Cart();
        cart.setCartItems(List.of(new CartItem()));

        when(cartRepository.findCartByEmail(anyString())).thenReturn(cart);
        when(authUtil.loggedInUser()).thenReturn(new User());
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.placeOrder(
                        "test@mail.com", 1L,
                        "CARD", "PG", "id", "OK", "MSG"
                )
        );
    }

    @Test
    void placeOrder_cartEmpty() {
        Cart cart = new Cart();
        cart.setCartItems(new ArrayList<>());

        when(cartRepository.findCartByEmail(anyString())).thenReturn(cart);
        when(authUtil.loggedInUser()).thenReturn(new User());
        when(addressRepository.findById(anyLong()))
                .thenReturn(Optional.of(new Address()));
        when(orderRepository.save(any(Order.class)))
                .thenReturn(new Order());

        assertThrows(APIException.class, () ->
                orderService.placeOrder(
                        "test@mail.com", 1L,
                        "CARD", "PG", "id", "OK", "MSG"
                )
        );
    }

    @Test
    void getAllOrders_success() {
        Page<Order> page = new PageImpl<>(List.of(new Order()));

        when(orderRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class)))
                .thenReturn(new OrderDTO());

        OrderResponse response = orderService.getAllOrders(0, 10, "orderId", "asc");

        assertEquals(1, response.getContent().size());
    }

    @Test
    void updateOrder_success() {
        Order order = new Order();
        order.setOrderStatus("OLD");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(modelMapper.map(any(Order.class), eq(OrderDTO.class)))
                .thenReturn(new OrderDTO());

        OrderDTO dto = orderService.updateOrder(1L, "SHIPPED");

        assertNotNull(dto);
    }

    @Test
    void updateOrderStatusBySeller_cancelNotAllowed() {
        assertThrows(IllegalStateException.class, () ->
                orderService.updateOrderStatusBySeller(1L, "CANCELLED")
        );
    }
}