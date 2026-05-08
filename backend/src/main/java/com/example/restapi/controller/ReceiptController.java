package com.example.restapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.ReceiptResponse;
import com.example.restapi.service.ReceiptService;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<ReceiptResponse>> getReceiptsByBuyerId(@PathVariable String buyerId) {
        try {
            UUID uuid = UUID.fromString(buyerId);
            List<ReceiptResponse> receipts = receiptService.getReceiptsByBuyerId(uuid);
            return ResponseEntity.ok(receipts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{receiptId}")
    public ResponseEntity<ReceiptResponse> getReceiptById(@PathVariable Long receiptId) {
        try {
            ReceiptResponse receipt = receiptService.getReceiptById(receiptId);
            return ResponseEntity.ok(receipt);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{receiptId}/cancel")
    public ResponseEntity<ReceiptResponse> cancelOrder(@PathVariable Long receiptId) {
        try {
            ReceiptResponse receipt = receiptService.cancelOrder(receiptId);
            return ResponseEntity.ok(receipt);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
