package com.ecommerce.project.security;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.AuthEntryPointJwt;
import com.ecommerce.project.security.jwt.AuthTokenFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class WebSecurityConfigTest {

    @Mock
    private AuthEntryPointJwt authEntryPointJwt;

    @Mock
    private AuthTokenFilter authTokenFilter;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    private WebSecurityConfig webSecurityConfig;

    @BeforeEach
    void setUp() throws Exception {
        webSecurityConfig = new WebSecurityConfig(authEntryPointJwt, authTokenFilter);

        setPrivateField(webSecurityConfig, "userPassword", "userPass");
        setPrivateField(webSecurityConfig, "sellerPassword", "sellerPass");
        setPrivateField(webSecurityConfig, "adminPassword", "adminPass");
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testPasswordEncoder() {
        PasswordEncoder encoder = webSecurityConfig.passwordEncoder();
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
        String raw = "test123";
        assertThat(encoder.matches(raw, encoder.encode(raw))).isTrue();
    }

    @Test
    void testAuthenticationManager() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(authManager);

        AuthenticationManager manager = webSecurityConfig.authenticationManager(authConfig);

        assertThat(manager).isNotNull();
        assertThat(manager).isEqualTo(authManager);
    }

    @Test
    void testInitData_createsRolesAndUsers() throws Exception {
        when(roleRepository.findByRoleName(AppRole.ROLE_USER)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(AppRole.ROLE_SELLER)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(AppRole.ROLE_ADMIN)).thenReturn(Optional.empty());

        // Mock save to return role and update findByRoleName
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> {
            Role saved = invocation.getArgument(0);
            when(roleRepository.findByRoleName(saved.getRoleName())).thenReturn(Optional.of(saved));
            return saved;
        });

        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var runner = webSecurityConfig.initData(roleRepository, userRepository, webSecurityConfig.passwordEncoder());
        runner.run();

        verify(roleRepository, times(3)).save(any(Role.class));
        verify(userRepository, times(3)).save(any(User.class));
    }

    /**
     * Provide a test PasswordEncoder bean to avoid loading full Spring context.
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    }
}