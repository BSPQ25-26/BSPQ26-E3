package com.example.restapi.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.restapi.dto.CartRequest;
import com.example.restapi.dto.CartResponse;
import com.example.restapi.service.CartService;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{ownerId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable String ownerId) {
        UUID owner = UUID.fromString(ownerId);
        CartResponse resp = cartService.getCart(owner);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{ownerId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable String ownerId, @RequestBody CartRequest request) {
        UUID owner = UUID.fromString(ownerId);
        CartResponse resp = cartService.addItemToCart(owner, request.getItemId(), request.getQuantity());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{ownerId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable String ownerId, @PathVariable Long itemId) {
        UUID owner = UUID.fromString(ownerId);
        CartResponse resp = cartService.removeItemFromCart(owner, itemId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{ownerId}/clear")
    public ResponseEntity<CartResponse> clearCart(@PathVariable String ownerId) {
        UUID owner = UUID.fromString(ownerId);
        CartResponse resp = cartService.clearCart(owner);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{ownerId}/checkout")
    public ResponseEntity<CartResponse> checkout(@PathVariable String ownerId) {
        UUID owner = UUID.fromString(ownerId);
        CartResponse resp = cartService.checkout(owner);
        return ResponseEntity.ok(resp);
    }
}
