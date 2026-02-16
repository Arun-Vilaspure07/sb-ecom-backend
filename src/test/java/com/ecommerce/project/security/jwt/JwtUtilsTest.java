package com.ecommerce.project.security.jwt;

import com.ecommerce.project.security.services.UserDetailsImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseCookie;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    // Base64-encoded secret (must be long enough for HS256)
    private static final String TEST_SECRET =
            "dGVzdHNlY3JldHRlc3RzZWNyZXR0ZXN0c2VjcmV0dGVzdHNlY3JldA==";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();

        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1000 * 60); // 1 min
        ReflectionTestUtils.setField(jwtUtils, "jwtCookie", "jwt-cookie");
    }

    // ---------------- TOKEN GENERATION ----------------

    @Test
    void generateTokenFromUsername_shouldReturnValidToken() {
        String token = jwtUtils.generateTokenFromUsername("testuser");

        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void getUserNameFromJwtToken_shouldReturnUsername() {
        String token = jwtUtils.generateTokenFromUsername("testuser");

        String username = jwtUtils.getUserNameFromJwtToken(token);

        assertEquals("testuser", username);
    }

    // ---------------- VALIDATION ----------------

    @Test
    void validateJwtToken_shouldReturnFalse_forMalformedToken() {
        boolean isValid = jwtUtils.validateJwtToken("invalid.token.value");

        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_shouldReturnFalse_forExpiredToken() {
        // Force immediate expiration
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 0);

        String token = jwtUtils.generateTokenFromUsername("expiredUser");

        boolean isValid = jwtUtils.validateJwtToken(token);

        assertFalse(isValid);
    }

    // ---------------- COOKIES ----------------

    @Test
    void generateJwtCookie_shouldCreateHttpOnlySecureCookie() {
        UserDetailsImpl user = Mockito.mock(UserDetailsImpl.class);
        when(user.getUsername()).thenReturn("testuser");

        ResponseCookie cookie = jwtUtils.generateJwtCookie(user);

        assertEquals("jwt-cookie", cookie.getName());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertNotNull(cookie.getValue());
    }

    @Test
    void getCleanJwtCookie_shouldReturnEmptyCookie() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

        assertEquals("jwt-cookie", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge().getSeconds());
    }

    // ---------------- REQUEST EXTRACTION ----------------

    @Test
    void getJwtFromCookies_shouldReturnToken_whenCookieExists() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("jwt-cookie", "token123");

        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        String token = jwtUtils.getJwtFromCookies(request);

        assertEquals("token123", token);
    }

    @Test
    void getJwtFromCookies_shouldReturnNull_whenCookieMissing() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        String token = jwtUtils.getJwtFromCookies(request);

        assertNull(token);
    }

    @Test
    void getJwtFromHeader_shouldReturnToken_whenBearerPresent() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer my-token");

        String token = jwtUtils.getJwtFromHeader(request);

        assertEquals("my-token", token);
    }

    @Test
    void getJwtFromHeader_shouldReturnNull_whenHeaderMissing() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        String token = jwtUtils.getJwtFromHeader(request);

        assertNull(token);
    }
}