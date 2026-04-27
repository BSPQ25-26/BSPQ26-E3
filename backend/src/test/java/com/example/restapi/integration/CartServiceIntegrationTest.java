package com.example.restapi.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.example.restapi.dto.CartRequest;
import com.example.restapi.dto.CartResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Cart Integration Tests")
class CartServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private UUID buyerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        buyerId = UUID.randomUUID();
        itemId = 1L;
    }

    @Test
    @DisplayName("should add item to cart and checkout successfully")
    void testAddItemAndCheckout() {
        // Step 1: Add item to cart
        CartRequest request = new CartRequest();
        request.setItemId(itemId);
        request.setQuantity(2);

        ResponseEntity<CartResponse> addResponse = restTemplate.postForEntity(
            "/api/carts/" + buyerId + "/items",
            request,
            CartResponse.class
        );

        assertEquals(200, addResponse.getStatusCodeValue());
        assertNotNull(addResponse.getBody());
        assertNotNull(addResponse.getBody().getItems());
        assertEquals(1, addResponse.getBody().getItems().size());

        // Step 2: Verify cart
        ResponseEntity<CartResponse> getResponse = restTemplate.getForEntity(
            "/api/carts/" + buyerId,
            CartResponse.class
        );

        assertEquals(200, getResponse.getStatusCodeValue());
        assertNotNull(getResponse.getBody());

        // Step 3: Checkout
        ResponseEntity<CartResponse> checkoutResponse = restTemplate.postForEntity(
            "/api/carts/" + buyerId + "/checkout",
            null,
            CartResponse.class
        );

        assertEquals(200, checkoutResponse.getStatusCodeValue());
        assertNotNull(checkoutResponse.getBody());
        assertTrue(checkoutResponse.getBody().getItems().isEmpty());
    }

    @Test
    @DisplayName("should remove item from cart")
    void testRemoveItemFromCart() {
        // Step 1: Add item
        CartRequest request = new CartRequest();
        request.setItemId(itemId);
        request.setQuantity(1);

        restTemplate.postForEntity(
            "/api/carts/" + buyerId + "/items",
            request,
            CartResponse.class
        );

        // Step 2: Remove item
        ResponseEntity<CartResponse> removeResponse = restTemplate.exchange(
            "/api/carts/" + buyerId + "/items/" + itemId,
            org.springframework.http.HttpMethod.DELETE,
            null,
            CartResponse.class
        );

        assertEquals(200, removeResponse.getStatusCodeValue());
        assertNotNull(removeResponse.getBody());
        assertTrue(removeResponse.getBody().getItems().isEmpty());
        assertEquals(0.0, removeResponse.getBody().getTotal(), 0.01);
    }
}
