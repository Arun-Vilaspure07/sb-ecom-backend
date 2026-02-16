package com.ecommerce.project.controller;

import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseControllerTest {

    @MockitoBean
    protected JwtUtils jwtUtils;

    @MockitoBean
    protected UserDetailsServiceImpl userDetailsService;
}