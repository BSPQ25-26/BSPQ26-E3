package com.example.restapi.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import com.example.restapi.dto.CartRequest;
import com.example.restapi.dto.CartResponse;
import com.example.restapi.model.Category;
import com.example.restapi.model.Item;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.CartItemRepository;
import com.example.restapi.repository.CartRepository;
import com.example.restapi.repository.CategoryRepository;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.ProfileRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

//this is to ensure that the test uses the application-test.properties configuration (in our case is a H2 in-memory database)
// we are testing the Controller / Service / Repository / Database
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("Cart Integration Tests")
class CartServiceIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private com.example.restapi.repository.ReceiptRepository receiptRepository;
    @Autowired private com.example.restapi.repository.SaleRepository saleRepository;

    private static final Logger log = LoggerFactory.getLogger(CartServiceIntegrationTest.class);

    private UUID buyerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        // Clean up in safe order: sales → receipts → cart_items → carts → items → profiles → categories
        saleRepository.deleteAll();
        receiptRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        itemRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();
        buyerId = UUID.randomUUID();

        Category category = categoryRepository.save(new Category("Plants", "Test category"));

        Profile seller = profileRepository.save(new Profile(UUID.randomUUID(), "seller", "000000000"));
        // Buyer profile required by CartService.checkout() to verify the buyer exists
        profileRepository.save(new Profile(buyerId, "buyer", "111111111"));

        Item item = new Item("Test Plant", "A test plant", 9.99, null, 100, category, true);
        item.setSeller(seller);
        itemId = itemRepository.save(item).getId();
    }

    @AfterEach
    void tearDown() {
        saleRepository.deleteAll();
        receiptRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        itemRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();
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

        assertEquals(200, addResponse.getStatusCode().value());
        assertNotNull(addResponse.getBody());
        assertEquals(1, addResponse.getBody().getItems().size());

        // Step 2: Verify cart
        ResponseEntity<CartResponse> getResponse = restTemplate.getForEntity(
            "/api/carts/" + buyerId,
            CartResponse.class
        );

        assertEquals(200, getResponse.getStatusCode().value());
        assertNotNull(getResponse.getBody());

        // Step 3: Checkout
        com.example.restapi.dto.PaymentRequest paymentRequest = new com.example.restapi.dto.PaymentRequest();
        paymentRequest.setCardNumber("1234-5678-9012-3456");
        paymentRequest.setExpiryDate("12/25");
        paymentRequest.setCvv("123");
        paymentRequest.setCardHolder("John Doe");

        ResponseEntity<com.example.restapi.dto.ReceiptResponse> checkoutResponse = restTemplate.postForEntity(
            "/api/carts/" + buyerId + "/checkout",
            paymentRequest,
            com.example.restapi.dto.ReceiptResponse.class
        );

        assertEquals(200, checkoutResponse.getStatusCode().value());
        assertNotNull(checkoutResponse.getBody());
        assertEquals(1, checkoutResponse.getBody().getItems().size());
        log.info("testAddItemAndCheckout passed: checkout completed for buyerId={}", buyerId);
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

        assertEquals(200, removeResponse.getStatusCode().value());
        assertNotNull(removeResponse.getBody());
        assertTrue(removeResponse.getBody().getItems().isEmpty());
        assertEquals(0.0, removeResponse.getBody().getTotal(), 0.01);
        log.info("testRemoveItemFromCart passed: item removed from cart for buyerId={}", buyerId);
    }
}
