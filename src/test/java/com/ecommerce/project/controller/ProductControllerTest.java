package com.ecommerce.project.controller;

import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest extends BaseControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;



    // ---------- ADD PRODUCT (ADMIN) ----------
    @Test
    void addProduct_shouldReturnCreated() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setProductName("iPhone");

        Mockito.when(productService.addProduct(Mockito.eq(1L), Mockito.any(ProductDTO.class)))
                .thenReturn(dto);

        mockMvc.perform(post("/api/admin/categories/{categoryId}/product", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("iPhone"));
    }

    // ---------- GET ALL PRODUCTS ----------
    @Test
    void getAllProducts_shouldReturnOk() throws Exception {
        ProductResponse response = new ProductResponse();
        response.setContent(List.of(new ProductDTO()));

        Mockito.when(productService.getAllProducts(
                        Mockito.anyInt(), Mockito.anyInt(),
                        Mockito.anyString(), Mockito.anyString(),
                        Mockito.any(), Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/public/products"))
                .andExpect(status().isOk());
    }

    // ---------- GET PRODUCTS BY CATEGORY ----------
    @Test
    void getProductsByCategory_shouldReturnOk() throws Exception {
        ProductResponse response = new ProductResponse();
        response.setContent(List.of(new ProductDTO()));

        Mockito.when(productService.searchByCategory(
                        Mockito.eq(1L),
                        Mockito.anyInt(), Mockito.anyInt(),
                        Mockito.anyString(), Mockito.anyString()))
                .thenReturn(response);

        mockMvc.perform(get("/api/public/categories/{categoryId}/products", 1L))
                .andExpect(status().isOk());
    }

    // ---------- UPDATE PRODUCT (ADMIN) ----------
    @Test
    void updateProduct_shouldReturnOk() throws Exception {
        ProductDTO dto = new ProductDTO();
        dto.setProductName("Updated");

        Mockito.when(productService.updateProduct(
                        Mockito.eq(1L),
                        Mockito.any(ProductDTO.class),
                        Mockito.eq("ADMIN")))
                .thenReturn(dto);

        mockMvc.perform(put("/api/admin/products/{productId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Updated"));
    }

    // ---------- DELETE PRODUCT (ADMIN) ----------
    @Test
    void deleteProduct_shouldReturnOk() throws Exception {
        ProductDTO dto = new ProductDTO();

        Mockito.when(productService.deleteProduct(1L, "ADMIN"))
                .thenReturn(dto);

        mockMvc.perform(delete("/api/admin/products/{productId}", 1L))
                .andExpect(status().isOk());
    }
}