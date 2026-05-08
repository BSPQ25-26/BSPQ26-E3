package com.example.restapi.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ReceiptResponse {
    private Long receiptId;
    private UUID buyerId;
    private String receiptNumber;
    private Double totalAmount;
    private String paymentStatus;
    private String orderStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime processingStartedAt;
    private OffsetDateTime deliveryStartedAt;
    private OffsetDateTime completedAt;
    private OffsetDateTime cancelledAt;
    private Long remainingTimeSeconds;
    private List<ReceiptItemResponse> items;

    public ReceiptResponse(Long receiptId, UUID buyerId, String receiptNumber, Double totalAmount, 
                          String paymentStatus, String orderStatus, OffsetDateTime createdAt, 
                          OffsetDateTime processingStartedAt, OffsetDateTime deliveryStartedAt,
                          OffsetDateTime completedAt, OffsetDateTime cancelledAt,
                          Long remainingTimeSeconds, List<ReceiptItemResponse> items) {
        this.receiptId = receiptId;
        this.buyerId = buyerId;
        this.receiptNumber = receiptNumber;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.orderStatus = orderStatus;
        this.createdAt = createdAt;
        this.processingStartedAt = processingStartedAt;
        this.deliveryStartedAt = deliveryStartedAt;
        this.completedAt = completedAt;
        this.cancelledAt = cancelledAt;
        this.remainingTimeSeconds = remainingTimeSeconds;
        this.items = items;
    }

    // Getters
    public Long getReceiptId() { return receiptId; }
    public UUID getBuyerId() { return buyerId; }
    public String getReceiptNumber() { return receiptNumber; }
    public Double getTotalAmount() { return totalAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getOrderStatus() { return orderStatus; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getProcessingStartedAt() { return processingStartedAt; }
    public OffsetDateTime getDeliveryStartedAt() { return deliveryStartedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public OffsetDateTime getCancelledAt() { return cancelledAt; }
    public Long getRemainingTimeSeconds() { return remainingTimeSeconds; }
    public List<ReceiptItemResponse> getItems() { return items; }
}
