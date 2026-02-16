package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuthUtil authUtil;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addProductToCart_success() {
        Long productId = 1L;
        Integer quantity = 2;

        // Mock user (IMPORTANT: previously null caused issues)
        User user = new User();
        user.setUserId(1L);

        Cart cart = new Cart();
        cart.setCartId(10L);
        cart.setTotalPrice(0.0);

        Product product = new Product();
        product.setProductId(productId);
        product.setProductName("Phone");
        product.setQuantity(10);
        product.setSpecialPrice(500.0);
        product.setDiscount(10.0);

        // AuthUtil must always be stubbed
        when(authUtil.loggedInEmail()).thenReturn("test@mail.com");
        when(authUtil.loggedInUser()).thenReturn(user);

        // Cart does not exist initially
        when(cartRepository.findCartByEmail(anyString())).thenReturn(null);

        // save() should return the saved cart (Mockito best practice)
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(cartItemRepository.findCartItemByProductIdAndCartId(any(), eq(productId)))
                .thenReturn(null);

        when(modelMapper.map(any(Cart.class), eq(CartDTO.class))).thenReturn(new CartDTO());

        CartDTO result = cartService.addProductToCart(productId, quantity);

        assertNotNull(result);

        // save() is called TWICE: once in createCart(), once after adding product
        verify(cartRepository, times(2)).save(any(Cart.class));
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void addProductToCart_productNotFound() {
        when(authUtil.loggedInEmail()).thenReturn("test@mail.com");
        when(cartRepository.findCartByEmail(anyString())).thenReturn(new Cart());
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.addProductToCart(1L, 1)
        );
    }

    @Test
    void getAllCarts_noCarts() {
        when(cartRepository.findAll()).thenReturn(List.of());

        APIException ex = assertThrows(
                APIException.class,
                () -> cartService.getAllCarts()
        );

        assertEquals("No cart exists", ex.getMessage());
    }

    @Test
    void getCart_cartNotFound() {
        when(cartRepository.findCartByEmailAndCartId(anyString(), anyLong()))
                .thenReturn(null);

        assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.getCart("test@mail.com", 1L)
        );
    }

    @Test
    void updateProductQuantityInCart_negativeQuantity() {
        Cart cart = new Cart();
        cart.setCartId(1L);

        Product product = new Product();
        product.setQuantity(10);
        product.setSpecialPrice(100.0);

        CartItem cartItem = new CartItem();
        cartItem.setQuantity(1);

        when(authUtil.loggedInEmail()).thenReturn("test@mail.com");
        when(cartRepository.findCartByEmail(anyString())).thenReturn(cart);
        when(cartRepository.findById(anyLong())).thenReturn(Optional.of(cart));
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(cartItemRepository.findCartItemByProductIdAndCartId(anyLong(), anyLong()))
                .thenReturn(cartItem);

        APIException ex = assertThrows(
                APIException.class,
                () -> cartService.updateProductQuantityInCart(1L, -5)
        );

        assertEquals("The resulting quantity cannot be negative.", ex.getMessage());
    }

    @Test
    void deleteProductFromCart_success() {
        Cart cart = new Cart();
        cart.setTotalPrice(1000.0);

        Product product = new Product();
        product.setProductName("Laptop");

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setProductPrice(500.0);
        cartItem.setQuantity(2);

        when(cartRepository.findById(anyLong())).thenReturn(Optional.of(cart));
        when(cartItemRepository.findCartItemByProductIdAndCartId(anyLong(), anyLong()))
                .thenReturn(cartItem);

        String result = cartService.deleteProductFromCart(1L, 1L);

        assertTrue(result.contains("removed from the cart"));
        verify(cartItemRepository)
                .deleteCartItemByProductIdAndCartId(anyLong(), anyLong());
    }

    @Test
    void createOrUpdateCartWithItems_newCart() {
        CartItemDTO dto = new CartItemDTO();
        dto.setProductId(1L);
        dto.setQuantity(2);

        Product product = new Product();
        product.setSpecialPrice(100.0);
        product.setDiscount(0.0);

        User user = new User();
        user.setUserId(1L);

        when(authUtil.loggedInEmail()).thenReturn("test@mail.com");
        when(authUtil.loggedInUser()).thenReturn(user);

        // New cart scenario
        when(cartRepository.findCartByEmail(anyString())).thenReturn(null);

        // IMPORTANT: save() must return the cart to avoid NPE
        when(cartRepository.save(any(Cart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        String result = cartService.createOrUpdateCartWithItems(List.of(dto));

        assertEquals("Cart created/updated with the new items successfully", result);
        verify(cartItemRepository).save(any(CartItem.class));
    }
}