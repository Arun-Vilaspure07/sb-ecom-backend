package com.ecommerce.project.service;

import com.ecommerce.project.payload.StripePaymentDto;
import com.stripe.model.PaymentIntent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class StripeServiceTest {

    @Autowired
    private StripeService stripeService;

    @Test
    void shouldCreatePaymentIntentSuccessfully() throws Exception {
        StripePaymentDto dto = new StripePaymentDto();
        dto.setAmount(1000L);
        dto.setCurrency("INR");

        PaymentIntent intent = stripeService.paymentIntent(dto);

        assertNotNull(intent);
        assertEquals("pi_test_123", intent.getId());
        assertEquals("succeeded", intent.getStatus());
    }
}