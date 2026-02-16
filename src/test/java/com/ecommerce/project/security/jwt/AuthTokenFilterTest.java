package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validJwt_shouldSetAuthentication() throws Exception {
        String jwt = "valid-jwt";
        String username = "testuser";

        UserDetails userDetails =
                new User(username, "password", Collections.emptyList());

        when(jwtUtils.getJwtFromCookies(request)).thenReturn(jwt);
        when(jwtUtils.validateJwtToken(jwt)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(jwt)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertEquals(username, authentication.getName());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_noJwt_shouldContinueFilterChain() throws Exception {
        when(jwtUtils.getJwtFromCookies(request)).thenReturn(null);
        when(jwtUtils.getJwtFromHeader(request)).thenReturn(null);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidJwt_shouldNotSetAuthentication() throws Exception {
        String jwt = "invalid-jwt";

        when(jwtUtils.getJwtFromCookies(request)).thenReturn(jwt);
        when(jwtUtils.validateJwtToken(jwt)).thenReturn(false);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_exceptionThrown_shouldStillContinueFilterChain() throws Exception {
        when(jwtUtils.getJwtFromCookies(request))
                .thenThrow(new RuntimeException("JWT error"));

        authTokenFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}