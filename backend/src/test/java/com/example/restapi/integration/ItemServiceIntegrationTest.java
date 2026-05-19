package com.example.restapi.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.example.restapi.dto.ItemResponse;
import com.example.restapi.model.Category;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.CategoryRepository;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.ProfileRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Item Integration Tests")
class ItemServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceIntegrationTest.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private CategoryRepository categoryRepository;

    private UUID sellerId;
    private String categoryName;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();

        categoryName = "Indoor Plants";
        categoryRepository.save(new Category(categoryName, "Indoor plants category"));

        Profile seller = profileRepository.save(new Profile(UUID.randomUUID(), "item-seller", "000000000"));
        sellerId = seller.getId();

        log.info("setUp complete: sellerId={}, category={}", sellerId, categoryName);
    }

    @AfterEach
    void tearDown() {
        itemRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/items returns empty list when no items exist")
    void getAllItemsReturnsEmptyList() {
        ResponseEntity<ItemResponse[]> response = restTemplate.getForEntity("/api/items", ItemResponse[].class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
        log.info("getAllItemsReturnsEmptyList passed");
    }

    @Test
    @DisplayName("POST /api/items creates an item and GET /api/items/{id} retrieves it")
    void createItemAndRetrieveById() {
        Long itemId = createItemViaApi("Monstera", 24.99, true);
        assertNotNull(itemId);

        ResponseEntity<ItemResponse> response = restTemplate.getForEntity("/api/items/" + itemId, ItemResponse.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Monstera", response.getBody().getTitle());
        assertEquals(categoryName, response.getBody().getCategoryName());
        log.info("createItemAndRetrieveById passed: itemId={}", itemId);
    }

    @Test
    @DisplayName("GET /api/items/active returns only items with status=true")
    void getActiveItemsReturnsOnlyActiveOnes() {
        createItemViaApi("Active Plant", 10.00, true);
        createItemViaApi("Inactive Plant", 5.00, false);

        ResponseEntity<ItemResponse[]> response = restTemplate.getForEntity("/api/items/active", ItemResponse[].class);

        assertEquals(200, response.getStatusCode().value());
        ItemResponse[] body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.length);
        assertEquals("Active Plant", body[0].getTitle());
        assertTrue(body[0].getStatus());
        log.info("getActiveItemsReturnsOnlyActiveOnes passed: {} active item(s) returned", body.length);
    }

    @Test
    @DisplayName("DELETE /api/items/{id} removes the item")
    void deleteItemRemovesIt() {
        Long itemId = createItemViaApi("Cactus", 8.99, true);

        restTemplate.delete("/api/items/" + itemId);

        ResponseEntity<ItemResponse> response = restTemplate.getForEntity("/api/items/" + itemId, ItemResponse.class);
        assertEquals(404, response.getStatusCode().value());
        log.info("deleteItemRemovesIt passed: itemId={}", itemId);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Long createItemViaApi(String name, double price, boolean active) {
        Map<String, Object> body = new HashMap<>();
        body.put("title", name);
        body.put("description", "Test description");
        body.put("amount", price);
        body.put("quantity", 10);
        body.put("status", active);
        Map<String, Object> cat = new HashMap<>();
        cat.put("name", categoryName);
        body.put("category", cat);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", sellerId.toString());
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity("/api/items", request, Map.class);
        assertEquals(200, response.getStatusCode().value(), "Item creation failed for: " + name);
        return ((Number) response.getBody().get("id")).longValue();
    }
}
