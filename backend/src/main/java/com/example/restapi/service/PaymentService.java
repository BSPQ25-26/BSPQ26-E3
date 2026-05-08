package com.example.restapi.service;

import org.springframework.stereotype.Service;

import com.example.restapi.dto.PaymentRequest;

@Service
public class PaymentService {

    // Simulates a payment process without actually charging anything
    // Returns true if payment is successful, false otherwise
     
    public boolean processPayment(PaymentRequest paymentRequest, Double amount) {
        if (paymentRequest == null || amount == null || amount <= 0) {
            return false;
        }

        // Validate card number (simmulation)
        if (!isValidCardNumber(paymentRequest.getCardNumber())) {
            return false;
        }
        if (!isValidExpiryDate(paymentRequest.getExpiryDate())) {
            return false;
        }
        if (!isValidCVV(paymentRequest.getCvv())) {
            return false;
        }
        if (paymentRequest.getCardHolder() == null || paymentRequest.getCardHolder().trim().isEmpty()) {
            return false;
        }

        // Simulate successful payment (90% success rate for testing)
        return Math.random() < 0.9;
    }

    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }
        // Remove spaces and dashes
        String cleaned = cardNumber.replaceAll("[\\s-]", "");
        // Check if only digits and length is 13-19
        return cleaned.matches("\\d{13,19}");
    }

    private boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return false;
        }
        // Expected format: MM/YY
        return expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2}$");
    }

    private boolean isValidCVV(String cvv) {
        if (cvv == null || cvv.isEmpty()) {
            return false;
        }
        // CVV be 3-4 digits
        return cvv.matches("\\d{3,4}");
    }
}
