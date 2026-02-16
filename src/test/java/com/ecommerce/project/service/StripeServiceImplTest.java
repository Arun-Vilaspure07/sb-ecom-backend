package com.ecommerce.project.service;

import com.ecommerce.project.model.Address;
import com.ecommerce.project.payload.StripePaymentDto;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeServiceImplTest {

    private final StripeServiceImpl stripeService = new StripeServiceImpl();

    @Test
    void paymentIntent_existingCustomer_success() throws Exception {
        // -------- Arrange --------
        StripePaymentDto dto = buildDto();

        Customer customer = mock(Customer.class);
        when(customer.getId()).thenReturn("cust_123");

        CustomerSearchResult searchResult = mock(CustomerSearchResult.class);
        when(searchResult.getData()).thenReturn(List.of(customer));

        PaymentIntent paymentIntent = mock(PaymentIntent.class);

        try (
                MockedStatic<Customer> customerMock = mockStatic(Customer.class);
                MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)
        ) {
            customerMock.when(() -> Customer.search(any(CustomerSearchParams.class)))
                    .thenReturn(searchResult);

            paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(paymentIntent);

            // -------- Act --------
            PaymentIntent result = stripeService.paymentIntent(dto);

            // -------- Assert --------
            assertNotNull(result);
        }
    }

    @Test
    void paymentIntent_newCustomer_success() throws Exception {
        // -------- Arrange --------
        StripePaymentDto dto = buildDto();

        Customer newCustomer = mock(Customer.class);
        when(newCustomer.getId()).thenReturn("cust_new");

        CustomerSearchResult emptyResult = mock(CustomerSearchResult.class);
        when(emptyResult.getData()).thenReturn(List.of());

        PaymentIntent paymentIntent = mock(PaymentIntent.class);

        try (
                MockedStatic<Customer> customerMock = mockStatic(Customer.class);
                MockedStatic<PaymentIntent> paymentIntentMock = mockStatic(PaymentIntent.class)
        ) {
            customerMock.when(() -> Customer.search(any(CustomerSearchParams.class)))
                    .thenReturn(emptyResult);

            customerMock.when(() -> Customer.create(any(CustomerCreateParams.class)))
                    .thenReturn(newCustomer);

            paymentIntentMock.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(paymentIntent);

            // -------- Act --------
            PaymentIntent result = stripeService.paymentIntent(dto);

            // -------- Assert --------
            assertNotNull(result);
        }
    }

    // -------- Helper --------
    private StripePaymentDto buildDto() {
        StripePaymentDto dto = new StripePaymentDto();
        dto.setEmail("test@mail.com");
        dto.setName("Test User");
        dto.setAmount(1000L);
        dto.setCurrency("usd");
        dto.setDescription("Order payment");

        Address address = new Address();
        address.setStreet("Street");
        address.setCity("City");
        address.setState("State");
        address.setPincode("12345");
        address.setCountry("US");

        dto.setAddress(address);
        return dto;
    }
}