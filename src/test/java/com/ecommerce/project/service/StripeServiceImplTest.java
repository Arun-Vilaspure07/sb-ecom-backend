package com.ecommerce.project.service;

import com.ecommerce.project.payload.StripePaymentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;


@Service
@Profile("test")
class StripeServiceImplTest implements StripeService {

//    @Override
//    public void charge(...) {
//        // no-op or fake response
//    }

    @Override
    public PaymentIntent paymentIntent(StripePaymentDto stripePaymentDto) throws StripeException {
        PaymentIntent intent = new PaymentIntent();
        intent.setId("pi_test_123");
        intent.setStatus("succeeded");
        return intent;
    }
}