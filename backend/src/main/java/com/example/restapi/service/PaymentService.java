package com.example.restapi.service;

import org.springframework.stereotype.Service;

import com.example.restapi.dto.PaymentRequest;

/**
 *
 * Service layer that simulates payment processing.
 *
 * Validates card data (number, expiry, CVV, holder name) and returns a
 * probabilistic success result.  This remote facade allows the checkout
 * flow to remain agnostic of the real payment provider.
 */
@Service
public class PaymentService {

    /**
     * Validates payment details and simulates a charge.
     *
     * The simulation has a 90 % success rate when all fields are valid.
     *
     * @param paymentRequest DTO containing card holder, number, expiry and CVV.
     * @param amount         Monetary amount to charge (must be > 0).
     * @return true if the simulated payment succeeds, false otherwise.
     */
    public boolean processPayment(PaymentRequest paymentRequest, Double amount) {
        if (paymentRequest == null || amount == null || amount <= 0) {
            return false;
        }

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

    /**
     * Checks whether a card number looks valid.
     * @param cardNumber Raw card number (may contain spaces or dashes).
     * @return true if the cleaned number has 13-19 digits.
     */
    private boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }
        String cleaned = cardNumber.replaceAll("[\\s-]", "");
        return cleaned.matches("\\d{13,19}");
    }

    /**
     * Validates the expiry date format.
     * @param expiryDate String expected in MM/YY format.
     * @return true if the format matches MM/YY.
     */
    private boolean isValidExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.isEmpty()) {
            return false;
        }
        return expiryDate.matches("^(0[1-9]|1[0-2])/\\d{2}$");
    }

    /**
     * Validates the CVV/CVC code.
     * @param cvv Security code.
     * @return true if the CVV consists of 3 or 4 digits.
     */
    private boolean isValidCVV(String cvv) {
        if (cvv == null || cvv.isEmpty()) {
            return false;
        }
        return cvv.matches("\\d{3,4}");
    }
}
