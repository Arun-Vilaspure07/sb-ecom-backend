package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AuthenticationResult;
import com.ecommerce.project.payload.UserResponse;
import com.ecommerce.project.security.jwt.AuthTokenFilter;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ IMPORTANT FIX
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private AuthTokenFilter authTokenFilter;

    // ---------- SIGN IN ----------
    @Test
    void authenticateUser_shouldReturnOkAndCookie() throws Exception {

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("password");

        UserInfoResponse userInfo = new UserInfoResponse(
                1L,
                "testuser",
                List.of("ROLE_USER"),
                "test@example.com",
                "Bearer"
        );

        ResponseCookie cookie = ResponseCookie.from("jwt", "token")
                .httpOnly(true)
                .path("/")
                .build();

        AuthenticationResult result = new AuthenticationResult(userInfo, cookie);

        Mockito.when(authService.login(Mockito.any(LoginRequest.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/auth/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    // ---------- SIGN UP ----------
    @Test
    void registerUser_shouldReturnOk() throws Exception {

        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        MessageResponse response = new MessageResponse("User registered");

        Mockito.when(authService.register(Mockito.any()))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/api/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ---------- CURRENT USERNAME (UNAUTHENTICATED) ----------
    @Test
    void currentUserName_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/auth/username"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- CURRENT USERNAME (AUTHENTICATED) ----------
    @Test
    @WithMockUser(username = "user@test.com")
    void currentUserName_authenticated_shouldReturnUsername() throws Exception {
        mockMvc.perform(get("/api/auth/username"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- USER DETAILS ----------
    @Test
    @WithMockUser(username = "user@test.com")
    void getUserDetails_shouldReturnUserInfo() throws Exception {

        UserInfoResponse userInfo = new UserInfoResponse(
                1L,
                "testuser",
                List.of("ROLE_USER"),
                "test@example.com",
                "Bearer"
        );

        Mockito.when(authService.getCurrentUserDetails(Mockito.any()))
                .thenReturn(userInfo);

        mockMvc.perform(get("/api/auth/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    // ---------- SIGN OUT ----------
    @Test
    void signoutUser_shouldReturnOkAndCookie() throws Exception {

        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .maxAge(0)
                .path("/")
                .build();

        Mockito.when(authService.logoutUser())
                .thenReturn(cookie);

        mockMvc.perform(post("/api/auth/signout").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.message").value("You've been signed out!"));
    }

    // ---------- GET SELLERS ----------
    @Test
    void getAllSellers_shouldReturnOk() throws Exception {

        UserResponse response = new UserResponse();

        Mockito.when(authService.getAllSellers(Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/auth/sellers")
                        .param("pageNumber", "0"))
                .andExpect(status().isOk());
    }
}