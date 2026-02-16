package com.ecommerce.project.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthEntryPointJwtTest {

    private final AuthEntryPointJwt authEntryPointJwt = new AuthEntryPointJwt();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void commence_shouldReturnUnauthorizedResponseWithJsonBody() throws Exception {
        // mocks
        HttpServletRequest request = mock(HttpServletRequest.class);
        AuthenticationException authException = mock(AuthenticationException.class);

        when(request.getServletPath()).thenReturn("/api/test");
        when(authException.getMessage()).thenReturn("Full authentication is required");

        // real response (no mocking output stream!)
        MockHttpServletResponse response = new MockHttpServletResponse();

        // execute
        authEntryPointJwt.commence(request, response, authException);

        // assertions
        assertEquals(401, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());

        Map<String, Object> body =
                objectMapper.readValue(response.getContentAsString(), Map.class);

        assertEquals(401, body.get("status"));
        assertEquals("Unauthorized", body.get("error"));
        assertEquals("Full authentication is required", body.get("message"));
        assertEquals("/api/test", body.get("path"));
    }
}