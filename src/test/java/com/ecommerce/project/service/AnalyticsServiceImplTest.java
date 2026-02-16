package com.ecommerce.project.service;

import com.ecommerce.project.payload.AnalyticsResponse;
import com.ecommerce.project.repositories.OrderRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    // ---------- SUCCESS CASE (revenue exists) ----------

    @Test
    void getAnalyticsData_success() {
        // Mock repository responses
        when(productRepository.count()).thenReturn(10L);
        when(orderRepository.count()).thenReturn(25L);
        when(orderRepository.getTotalRevenue()).thenReturn(15000.50);

        AnalyticsResponse response = analyticsService.getAnalyticsData();

        assertNotNull(response);
        assertEquals("10", response.getProductCount());
        assertEquals("25", response.getTotalOrders());
        assertEquals("15000.5", response.getTotalRevenue());

        verify(productRepository, times(1)).count();
        verify(orderRepository, times(1)).count();
        verify(orderRepository, times(1)).getTotalRevenue();
    }

    // ---------- EDGE CASE (revenue is null) ----------

    @Test
    void getAnalyticsData_revenueIsNull() {
        when(productRepository.count()).thenReturn(0L);
        when(orderRepository.count()).thenReturn(0L);
        when(orderRepository.getTotalRevenue()).thenReturn(null);

        AnalyticsResponse response = analyticsService.getAnalyticsData();

        assertNotNull(response);
        assertEquals("0", response.getProductCount());
        assertEquals("0", response.getTotalOrders());
        assertEquals("0.0", response.getTotalRevenue());
    }
}