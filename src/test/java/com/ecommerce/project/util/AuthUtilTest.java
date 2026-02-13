package com.ecommerce.project.util;

import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthUtilTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthUtil authUtil = new AuthUtil(userRepository);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // ---------- loggedInEmail (SUCCESS) ----------
    @Test
    void loggedInEmail_shouldReturnEmail() {

        // Arrange
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("testuser", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new User();
        user.setUserId(1L);
        user.setUserName("testuser");
        user.setEmail("test@example.com");

        when(userRepository.findByUserName("testuser"))
                .thenReturn(Optional.of(user));

        // Act
        String email = authUtil.loggedInEmail();

        // Assert
        assertEquals("test@example.com", email);
    }

    // ---------- loggedInEmail (USER NOT FOUND) ----------
    @Test
    void loggedInEmail_shouldThrowException_whenUserNotFound() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("unknown", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userRepository.findByUserName("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authUtil.loggedInEmail());
    }

    // ---------- loggedInUserId ----------
    @Test
    void loggedInUserId_shouldReturnUserId() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("testuser", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new User();
        user.setUserId(10L);
        user.setUserName("testuser");

        when(userRepository.findByUserName("testuser"))
                .thenReturn(Optional.of(user));

        Long userId = authUtil.loggedInUserId();

        assertEquals(10L, userId);
    }

    // ---------- loggedInUser ----------
    @Test
    void loggedInUser_shouldReturnUser() {

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("testuser", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = new User();
        user.setUserId(5L);
        user.setUserName("testuser");
        user.setEmail("user@test.com");

        when(userRepository.findByUserName("testuser"))
                .thenReturn(Optional.of(user));

        User result = authUtil.loggedInUser();

        assertNotNull(result);
        assertEquals("user@test.com", result.getEmail());
    }
}