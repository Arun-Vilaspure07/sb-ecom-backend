package com.ecommerce.project.service;

import com.ecommerce.project.payload.StripePaymentDto;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;


@Service
@Profile("test")
class StripeServiceTestImpl implements StripeService {

    @Override
    public PaymentIntent paymentIntent(StripePaymentDto stripePaymentDto) throws StripeException {
        PaymentIntent intent = new PaymentIntent();
        intent.setId("pi_test_123");
        intent.setStatus("succeeded");
        return intent;
    }
}