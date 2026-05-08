package com.example.restapi.dto;

public class ReceiptItemResponse {
    private Long itemId;
    private String itemName;
    private Double unitPrice;
    private Integer quantity;
    private Double subtotal;

    public ReceiptItemResponse(Long itemId, String itemName, Double unitPrice, Integer quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice * quantity;
    }

    // Getters
    public Long getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public Double getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    public Double getSubtotal() { return subtotal; }
}
