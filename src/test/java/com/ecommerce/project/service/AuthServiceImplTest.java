package com.ecommerce.project.service;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private ModelMapper modelMapper;

    /* ---------------- REGISTER TESTS ---------------- */

    @Test
    void shouldFailWhenUsernameAlreadyExists() {
        SignupRequest request = new SignupRequest();
        request.setUsername("user");
        request.setEmail("user@test.com");

        when(userRepository.existsByUserName("user")).thenReturn(true);

        ResponseEntity<MessageResponse> response = authService.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        MessageResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(AuthServiceImpl.ERROR_USERNAME_IS_ALREADY_TAKEN, body.getMessage()
        );
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() {
        SignupRequest request = new SignupRequest();
        request.setUsername("user");
        request.setEmail("user@test.com");

        when(userRepository.existsByUserName("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        ResponseEntity<MessageResponse> response = authService.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        MessageResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(AuthServiceImpl.ERROR_EMAIL_IS_ALREADY_IN_USE, body.getMessage()
        );
    }

    @Test
    void shouldRegisterUserWithDefaultRole() {
        SignupRequest request = new SignupRequest();
        request.setUsername("user");
        request.setEmail("user@test.com");
        request.setPassword("password");

        Role roleUser = new Role();
        roleUser.setRoleName(AppRole.ROLE_USER);

        when(userRepository.existsByUserName("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(AppRole.ROLE_USER))
                .thenReturn(Optional.of(roleUser));

        ResponseEntity<MessageResponse> response = authService.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User registered successfully!", response.getBody().getMessage());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRegisterAdminUser() {
        SignupRequest request = new SignupRequest();
        request.setUsername("admin");
        request.setEmail("admin@test.com");
        request.setPassword("password");
        request.setRole(Set.of("admin"));

        Role adminRole = new Role();
        adminRole.setRoleName(AppRole.ROLE_ADMIN);

        when(userRepository.existsByUserName("admin")).thenReturn(false);
        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(AppRole.ROLE_ADMIN))
                .thenReturn(Optional.of(adminRole));

        ResponseEntity<MessageResponse> response = authService.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any(User.class));
    }

    /* ---------------- LOGOUT TEST ---------------- */

    @Test
    void shouldLogoutUser() {
        ResponseCookie cookie = ResponseCookie.from("jwt", "").build();

        when(jwtUtils.getCleanJwtCookie()).thenReturn(cookie);

        ResponseCookie result = authService.logoutUser();

        assertNotNull(result);
    }
}