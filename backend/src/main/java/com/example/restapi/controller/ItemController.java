package com.example.restapi.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.restapi.dto.ItemResponse;
import com.example.restapi.model.Item;
import com.example.restapi.service.ItemService;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/items")
@Tag(name = "Item Controller", description = "API for managing items/plants")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(itemService.getItemById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<ItemResponse>> getActiveItems() {
        return ResponseEntity.ok(itemService.getActiveItems());
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody Item item, @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            System.out.println("DEBUG: Received userId: " + userId);  // ← Agrega
            UUID sellerId;
            if (userId != null && !userId.isEmpty()) {
                sellerId = UUID.fromString(userId);
            } else {
                return ResponseEntity.badRequest().build();
            }
            System.out.println("DEBUG: Creating item with sellerId: " + sellerId);  // ← Agrega
            return ResponseEntity.ok(itemService.createItem(item, sellerId));
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR (BadRequest): " + e.getMessage());  // ← Agrega
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("ERROR (500): " + e.getMessage());  // ← Agrega
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item itemDetails) {
        try {
            return ResponseEntity.ok(itemService.updateItem(id, itemDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            itemService.deleteItem(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}