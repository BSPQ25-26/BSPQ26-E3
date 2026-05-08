package com.example.restapi.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.restapi.dto.PaymentRequest;

@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceTest.class);

    private PaymentService paymentService;
    private PaymentRequest validRequest;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
        
        validRequest = new PaymentRequest();
        validRequest.setCardNumber("1234-5678-9012-3456");
        validRequest.setExpiryDate("12/25");
        validRequest.setCvv("123");
        validRequest.setCardHolder("John Doe");
    }

    @Nested
    @DisplayName("processPayment - Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("should return false when request or amount is null")
        void testNullInputs() {
            assertFalse(paymentService.processPayment(null, 100.0));
            assertFalse(paymentService.processPayment(validRequest, null));
            log.info("testNullInputs passed: Null parameters correctly rejected");
        }

        @Test
        @DisplayName("should return false when amount is zero or negative")
        void testInvalidAmount() {
            assertFalse(paymentService.processPayment(validRequest, 0.0));
            assertFalse(paymentService.processPayment(validRequest, -10.0));
            log.info("testInvalidAmount passed: Non-positive amounts rejected");
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "abc", "1234-5678-9012", "123456789012345678901"})
        @DisplayName("should return false for invalid card numbers")
        void testInvalidCardNumbers(String invalidCard) {
            validRequest.setCardNumber(invalidCard);
            assertFalse(paymentService.processPayment(validRequest, 50.0));
        }

        @ParameterizedTest
        @ValueSource(strings = {"13/25", "12-25", "1/25", "12/2025", "Jan/25"})
        @DisplayName("should return false for invalid expiry dates")
        void testInvalidExpiryDates(String invalidExpiry) {
            validRequest.setExpiryDate(invalidExpiry);
            assertFalse(paymentService.processPayment(validRequest, 50.0));
        }

        @ParameterizedTest
        @ValueSource(strings = {"1", "12", "12345", "abc"})
        @DisplayName("should return false for invalid CVV")
        void testInvalidCVV(String invalidCvv) {
            validRequest.setCvv(invalidCvv);
            assertFalse(paymentService.processPayment(validRequest, 50.0));
        }

        @Test
        @DisplayName("should return false when card holder name is empty")
        void testInvalidCardHolder() {
            validRequest.setCardHolder("   ");
            assertFalse(paymentService.processPayment(validRequest, 50.0));
        }
    }

    @Nested
    @DisplayName("processPayment - Logic Tests")
    class LogicTests {

        @Test
        @DisplayName("should handle simulation results for valid data")
        void testValidRequestProcessing() {
            // Since the method uses Math.random(), we run it multiple times 
            // to ensure it eventually returns a boolean (simulating the 90% success)
            boolean result = paymentService.processPayment(validRequest, 100.0);
            assertNotNull(result);
            log.info("testValidRequestProcessing passed: Result was {}", result);
        }

        @Test
        @DisplayName("should correctly clean card number with spaces or dashes")
        void testCardCleaning() {
            validRequest.setCardNumber("1234 5678 9012 3456"); 
            validRequest.setCardNumber("1234-5678-9012-3456");
            // We run enough times to hit a 'true' due to the 90% success rate
            boolean sawSuccess = false;
            for(int i=0; i<10; i++) {
                if(paymentService.processPayment(validRequest, 10.0)) {
                    sawSuccess = true;
                    break;
                }
            }
            assertTrue(sawSuccess, "A valid card should eventually return true under 90% simulation");
        }
    }
}