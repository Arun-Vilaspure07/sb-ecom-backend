package com.ecommerce.project.service;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AuthenticationResult;
import com.ecommerce.project.payload.UserDTO;
import com.ecommerce.project.payload.UserResponse;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.services.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("password");

        UserDetailsImpl userDetails =
                new UserDetailsImpl(1L, "user", "user@test.com", "pass", List.of());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        ResponseCookie cookie = ResponseCookie.from("jwt", "token").build();
        when(jwtUtils.generateJwtCookie(userDetails)).thenReturn(cookie);

        AuthenticationResult result = authService.login(request);

        assertNotNull(result);
        assertNotNull(result.getResponse());
        assertEquals("user", result.getResponse().getUsername());
    }

    @Test
    void shouldReturnCurrentUserDetails() {
        UserDetailsImpl userDetails =
                new UserDetailsImpl(1L, "user", "user@test.com", "pass", List.of());

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        UserInfoResponse response = authService.getCurrentUserDetails(authentication);

        assertNotNull(response);
        assertEquals("user", response.getUsername());
    }

    @Test
    void shouldRegisterSellerUser() {
        SignupRequest request = new SignupRequest();
        request.setUsername("seller");
        request.setEmail("seller@test.com");
        request.setPassword("password");
        request.setRole(Set.of("seller"));

        Role sellerRole = new Role();
        sellerRole.setRoleName(AppRole.ROLE_SELLER);

        when(userRepository.existsByUserName("seller")).thenReturn(false);
        when(userRepository.existsByEmail("seller@test.com")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(AppRole.ROLE_SELLER))
                .thenReturn(Optional.of(sellerRole));

        ResponseEntity<MessageResponse> response = authService.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRegisterUserWithUnknownRole_DefaultsToUser() {
        SignupRequest request = new SignupRequest();
        request.setUsername("user2");
        request.setEmail("user2@test.com");
        request.setPassword("password");
        request.setRole(Set.of("random"));

        Role userRole = new Role();
        userRole.setRoleName(AppRole.ROLE_USER);

        when(userRepository.existsByUserName("user2")).thenReturn(false);
        when(userRepository.existsByEmail("user2@test.com")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encodedPass");
        when(roleRepository.findByRoleName(AppRole.ROLE_USER))
                .thenReturn(Optional.of(userRole));

        ResponseEntity<MessageResponse> response = authService.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldThrowExceptionWhenRoleNotFound() {
        SignupRequest request = new SignupRequest();
        request.setUsername("user");
        request.setEmail("user@test.com");
        request.setPassword("password");

        when(userRepository.existsByUserName("user")).thenReturn(false);
        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(roleRepository.findByRoleName(AppRole.ROLE_USER))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        RuntimeException.class,
                        () -> authService.register(request)
                );

        assertEquals(AuthServiceImpl.ERROR_ROLE_IS_NOT_FOUND, exception.getMessage());
    }

    @Test
    void shouldGetAllSellers() {
        User user = new User();
        user.setUserId(1L);
        user.setUserName("seller");

        Page<User> page =
                new org.springframework.data.domain.PageImpl<>(List.of(user));

        when(userRepository.findByRoleName(eq(AppRole.ROLE_SELLER), any()))
                .thenReturn(page);

        when(modelMapper.map(any(User.class), eq(UserDTO.class)))
                .thenReturn(new UserDTO());

        UserResponse response =
                authService.getAllSellers(org.springframework.data.domain.PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }
}