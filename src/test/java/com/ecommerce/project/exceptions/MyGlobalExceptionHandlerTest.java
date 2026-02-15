package com.ecommerce.project.exceptions;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
        MyGlobalExceptionHandler.class,
        MyGlobalExceptionHandlerTest.TestController.class
})
@AutoConfigureMockMvc(addFilters = false)
class MyGlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    // --------------------------------------------------
    // 1️⃣ MethodArgumentNotValidException
    // --------------------------------------------------
    @Test
    void shouldHandleMethodArgumentNotValidException() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"));
    }

    // --------------------------------------------------
    // 2️⃣ ResourceNotFoundException
    // --------------------------------------------------
    @Test
    void shouldHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(post("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("User not found with id: 1"));
    }

    // --------------------------------------------------
    // 3️⃣ APIException
    // --------------------------------------------------
    @Test
    void shouldHandleAPIException() throws Exception {
        mockMvc.perform(post("/test/api-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("API error occurred"));
    }

    // --------------------------------------------------
    // Dummy Controller (ONLY for tests)
    // --------------------------------------------------
    @RestController
    static class TestController {

        @PostMapping("/test/validation")
        public void validation(@Valid @RequestBody TestRequest request) {
            // Intentionally empty:
            // Used only to trigger MethodArgumentNotValidException
            // for testing MyGlobalExceptionHandler
        }

        @PostMapping("/test/resource-not-found")
        public void resourceNotFound() {
            throw new ResourceNotFoundException("User", "id", 1L);
        }

        @PostMapping("/test/api-exception")
        public void apiException() {
            throw new APIException("API error occurred");
        }
    }

    static class TestRequest {

        @NotBlank(message = "Name is required")
        private String name;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}