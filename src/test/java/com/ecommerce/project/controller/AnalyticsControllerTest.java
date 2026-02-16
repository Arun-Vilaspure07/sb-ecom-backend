package com.ecommerce.project.controller;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
class AnalyticsControllerTest extends BaseControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void getAnalytics_shouldReturnOk() throws Exception {
        AnalyticsResponse response = new AnalyticsResponse();

        Mockito.when(analyticsService.getAnalyticsData())
                .thenReturn(response);

        mockMvc.perform(get("/api/admin/app/analytics"))
                .andExpect(status().isOk());
    }
}