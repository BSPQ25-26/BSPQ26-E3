package com.example.restapi.dto;

import java.util.List;

public class CartResponse {
    private Long cartId;
    private List<CartItemResponse> items;
    private Double total;

    public CartResponse(Long cartId, List<CartItemResponse> items, Double total) {
        this.cartId = cartId;
        this.items = items;
        this.total = total;
    }

    public Long getCartId() { return cartId; }
    public List<CartItemResponse> getItems() { return items; }
    public Double getTotal() { return total; }
}
