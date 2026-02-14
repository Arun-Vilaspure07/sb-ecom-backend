package com.ecommerce.project.security.services;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsImplTest {

    @Test
    void shouldBuildUserDetailsFromUser() {
        // Arrange
        Role role = new Role();
        role.setRoleName(AppRole.ROLE_USER);

        User user = new User();
        user.setUserId(1L);
        user.setUserName("john_doe");
        user.setEmail("john@example.com");
        user.setPassword("password123");
        user.setRoles(Set.of(role));

        // Act
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        // Assert
        assertThat(userDetails.getId()).isEqualTo(1L);
        assertThat(userDetails.getUsername()).isEqualTo("john_doe");
        assertThat(userDetails.getEmail()).isEqualTo("john@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("password123");

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldReturnTrueForAccountStatusMethods() {
        UserDetailsImpl userDetails =
                new UserDetailsImpl(1L, "user", "email", "pass", List.of());

        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void equalsShouldReturnTrueForSameId() {
        UserDetailsImpl user1 =
                new UserDetailsImpl(1L, "user1", "a", "p", List.of());

        UserDetailsImpl user2 =
                new UserDetailsImpl(1L, "user2", "b", "p", List.of());

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void equalsShouldReturnFalseForDifferentId() {
        UserDetailsImpl user1 =
                new UserDetailsImpl(1L, "user1", "a", "p", List.of());

        UserDetailsImpl user2 =
                new UserDetailsImpl(2L, "user2", "b", "p", List.of());

        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    void equalsShouldReturnFalseForNullAndDifferentClass() {
        UserDetailsImpl user =
                new UserDetailsImpl(1L, "user", "a", "p", List.of());

        assertThat(user).isNotEqualTo(null);
        assertThat(user).isNotEqualTo("some-string");
    }

    @Test
    void shouldReturnAuthoritiesCorrectly() {
        GrantedAuthority authority = () -> "ROLE_ADMIN";

        UserDetailsImpl userDetails =
                new UserDetailsImpl(1L, "admin", "admin@mail.com", "pass",
                        List.of(authority));

        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }
}