package com.ecommerce.project;

import com.ecommerce.project.service.StripeService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class SbEcomApplicationTests {

    @MockitoBean
    private StripeService stripeService;

    @Test
    void contextLoads() {
    }
}


