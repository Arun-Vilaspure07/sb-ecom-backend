package com.ecommerce.project.controller;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.util.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private AuthUtil authUtil;

    // needed to satisfy Spring Security context
    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    // ---------- CREATE / UPDATE CART ----------
    @Test
    void createOrUpdateCart_shouldReturnCreated() throws Exception {
        List<CartItemDTO> items = List.of(new CartItemDTO());

        Mockito.when(cartService.createOrUpdateCartWithItems(items))
                .thenReturn("Cart updated");

        mockMvc.perform(post("/api/cart/create")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(items)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Cart updated"));
    }

    // ---------- ADD PRODUCT ----------
    @Test
    void addProductToCart_shouldReturnCreated() throws Exception {
        Mockito.when(cartService.addProductToCart(1L, 2))
                .thenReturn(new CartDTO());

        mockMvc.perform(post("/api/carts/products/1/quantity/2"))
                .andExpect(status().isCreated());
    }

    // ---------- GET ALL CARTS ----------
    @Test
    void getCarts_shouldReturnFound() throws Exception {
        Mockito.when(cartService.getAllCarts())
                .thenReturn(List.of(new CartDTO()));

        mockMvc.perform(get("/api/carts"))
                .andExpect(status().isFound());
    }

    // ---------- GET CART BY USER ----------
    @Test
    void getCartByUser_shouldReturnOk() throws Exception {
        Mockito.when(authUtil.loggedInEmail())
                .thenReturn("test@gmail.com");

        Cart cart = new Cart();
        cart.setCartId(1L);

        Mockito.when(cartRepository.findCartByEmail("test@gmail.com"))
                .thenReturn(cart);

        Mockito.when(cartService.getCart("test@gmail.com", 1L))
                .thenReturn(new CartDTO());

        mockMvc.perform(get("/api/carts/users/cart"))
                .andExpect(status().isOk());
    }

    // ---------- UPDATE CART (ADD) ----------
    @Test
    void updateCartProduct_increment_shouldReturnOk() throws Exception {
        Mockito.when(cartService.updateProductQuantityInCart(1L, 1))
                .thenReturn(new CartDTO());

        mockMvc.perform(put("/api/cart/products/1/quantity/add"))
                .andExpect(status().isOk());
    }

    // ---------- UPDATE CART (DELETE) ----------
    @Test
    void updateCartProduct_delete_shouldReturnOk() throws Exception {
        Mockito.when(cartService.updateProductQuantityInCart(1L, -1))
                .thenReturn(new CartDTO());

        mockMvc.perform(put("/api/cart/products/1/quantity/delete"))
                .andExpect(status().isOk());
    }

    // ---------- DELETE PRODUCT ----------
    @Test
    void deleteProductFromCart_shouldReturnOk() throws Exception {
        Mockito.when(cartService.deleteProductFromCart(1L, 2L))
                .thenReturn("Product removed");

        mockMvc.perform(delete("/api/carts/1/product/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product removed"));
    }
}