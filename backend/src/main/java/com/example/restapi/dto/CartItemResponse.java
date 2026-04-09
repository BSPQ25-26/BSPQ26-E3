package com.example.restapi.dto;

public class CartItemResponse {
    private Long itemId;
    private String title;
    private Double amount;
    private Integer quantity;

    public CartItemResponse(Long itemId, String title, Double amount, Integer quantity) {
        this.itemId = itemId;
        this.title = title;
        this.amount = amount;
        this.quantity = quantity;
    }

    public Long getItemId() { return itemId; }
    public String getTitle() { return title; }
    public Double getAmount() { return amount; }
    public Integer getQuantity() { return quantity; }
}
