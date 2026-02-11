package com.ecommerce.project.controller;

import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import com.ecommerce.project.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldSignupSuccessfully() throws Exception {

        when(authService.register(any(SignupRequest.class)))
                .thenReturn(ResponseEntity.ok(new MessageResponse("User registered")));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "username": "user",
                          "password": "password",
                          "email": "user@test.com",
                          "role": ["user"]
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/username"))
                .andExpect(status().isUnauthorized());
    }
}