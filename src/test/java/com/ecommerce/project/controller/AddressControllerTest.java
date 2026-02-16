package com.ecommerce.project.controller;

import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.util.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
class AddressControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private AuthUtil authUtil;

    // ---------- CREATE ADDRESS ----------
    @Test
    @WithMockUser
    void createAddress_shouldReturnCreated() throws Exception {
        AddressDTO addressDTO = new AddressDTO();
        User user = new User();

        Mockito.when(authUtil.loggedInUser()).thenReturn(user);
        Mockito.when(addressService.createAddress(Mockito.any(), Mockito.any()))
                .thenReturn(addressDTO);

        mockMvc.perform(post("/api/addresses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressDTO)))
                .andExpect(status().isCreated());
    }

    // ---------- GET ALL ADDRESSES ----------
    @Test
    @WithMockUser
    void getAddresses_shouldReturnOk() throws Exception {
        Mockito.when(addressService.getAddresses())
                .thenReturn(List.of(new AddressDTO()));

        mockMvc.perform(get("/api/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- GET ADDRESS BY ID ----------
    @Test
    @WithMockUser
    void getAddressById_shouldReturnOk() throws Exception {
        Mockito.when(addressService.getAddressesById(1L))
                .thenReturn(new AddressDTO());

        mockMvc.perform(get("/api/addresses/{id}", 1L))
                .andExpect(status().isOk());
    }

    // ---------- GET USER ADDRESSES ----------
    @Test
    @WithMockUser
    void getUserAddresses_shouldReturnOk() throws Exception {
        User user = new User();

        Mockito.when(authUtil.loggedInUser()).thenReturn(user);
        Mockito.when(addressService.getUserAddresses(user))
                .thenReturn(List.of(new AddressDTO()));

        mockMvc.perform(get("/api/users/addresses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ---------- UPDATE ADDRESS ----------
    @Test
    @WithMockUser
    void updateAddress_shouldReturnOk() throws Exception {
        AddressDTO addressDTO = new AddressDTO();

        Mockito.when(addressService.updateAddress(Mockito.eq(1L), Mockito.any()))
                .thenReturn(addressDTO);

        mockMvc.perform(put("/api/addresses/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addressDTO)))
                .andExpect(status().isOk());
    }

    // ---------- DELETE ADDRESS ----------
    @Test
    @WithMockUser
    void deleteAddress_shouldReturnOk() throws Exception {
        Mockito.when(addressService.deleteAddress(1L))
                .thenReturn("Address deleted");

        mockMvc.perform(delete("/api/addresses/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Address deleted"));
    }
}