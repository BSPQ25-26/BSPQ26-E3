package com.example.restapi.dto;

import java.time.OffsetDateTime;

public class SaleResponse {
    private Long saleId;
    private String itemName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private OffsetDateTime createdAt;

    public SaleResponse(Long saleId, String itemName, Integer quantity, Double unitPrice, Double totalPrice, OffsetDateTime createdAt) {
        this.saleId = saleId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getSaleId() { return saleId; }
    public String getItemName() { return itemName; }
    public Integer getQuantity() { return quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public Double getTotalPrice() { return totalPrice; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
