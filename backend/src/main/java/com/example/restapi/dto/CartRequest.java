package com.example.restapi.dto;

public class CartRequest {
    private Long itemId;
    private Integer quantity;

    public CartRequest() {}

    public CartRequest(Long itemId, Integer quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public Long getItemId() { return itemId; }
    public Integer getQuantity() { return quantity; }

    public void setItemId(Long itemId) { this.itemId = itemId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
