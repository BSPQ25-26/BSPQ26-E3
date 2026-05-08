package com.example.restapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.ReceiptResponse;
import com.example.restapi.service.OrderStateService;
import com.example.restapi.service.ReceiptService;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final ReceiptService receiptService;
    private final OrderStateService orderStateService;

    public PaymentController(ReceiptService receiptService, OrderStateService orderStateService) {
        this.receiptService = receiptService;
        this.orderStateService = orderStateService;
    }

    @GetMapping("/order/{receiptId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long receiptId) {
        try {
            ReceiptResponse receipt = receiptService.getReceiptById(receiptId);
            return ResponseEntity.ok(receipt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new PaymentResponse("ERROR", "Error retrieving order: " + e.getMessage()));
        }
    }

    @PostMapping("/order/{receiptId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long receiptId) {
        try {
            orderStateService.cancelOrder(receiptId);
            ReceiptResponse receipt = receiptService.getReceiptById(receiptId);
            return ResponseEntity.ok(receipt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new PaymentResponse("ERROR", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new PaymentResponse("ERROR", "Error cancelling order: " + e.getMessage()));
        }
    }

    // Simple response class
    public static class PaymentResponse {
        private String status;
        private String message;

        public PaymentResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
    }
}
