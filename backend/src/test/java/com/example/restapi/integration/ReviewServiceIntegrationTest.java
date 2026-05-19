package com.example.restapi.integration;

import static org.junit.jupiter.api.Assertions.*;

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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.example.restapi.dto.ReviewRequest;
import com.example.restapi.dto.ReviewResponse;
import com.example.restapi.model.Category;
import com.example.restapi.model.Item;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.CategoryRepository;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.ProfileRepository;
import com.example.restapi.repository.ReviewRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Review Integration Tests")
class ReviewServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceIntegrationTest.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Long itemId;
    private UUID reviewerId;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();
        itemRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.save(new Category("Succulents", "Succulent plants"));
        Profile seller = profileRepository.save(new Profile(UUID.randomUUID(), "item-seller", "000000001"));
        Profile reviewer = profileRepository.save(new Profile(UUID.randomUUID(), "reviewer-user", "000000002"));
        reviewerId = reviewer.getId();

        Item item = new Item("Aloe Vera", "Medicinal plant", 12.50, null, 20, category, true);
        item.setSeller(seller);
        itemId = itemRepository.save(item).getId();

        log.info("setUp complete: itemId={}, reviewerId={}", itemId, reviewerId);
    }

    @AfterEach
    void tearDown() {
        reviewRepository.deleteAll();
        itemRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/items/{itemId}/reviews creates a review and returns 201")
    void createReviewReturns201() {
        ReviewRequest request = new ReviewRequest();
        request.setAuthorId(reviewerId);
        request.setRating(4);
        request.setComment("Great plant!");

        ResponseEntity<ReviewResponse> response = restTemplate.postForEntity(
                "/api/items/" + itemId + "/reviews", request, ReviewResponse.class);

        assertEquals(201, response.getStatusCode().value());
        ReviewResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());
        assertEquals(4, body.getRating());
        assertEquals("Great plant!", body.getComment());
        assertEquals(reviewerId, body.getAuthorId());
        log.info("createReviewReturns201 passed: reviewId={}", body.getId());
    }

    @Test
    @DisplayName("GET /api/items/{itemId}/reviews returns all reviews for the item")
    void getReviewsByItemReturnsCreatedReview() {
        ReviewRequest request = new ReviewRequest();
        request.setAuthorId(reviewerId);
        request.setRating(5);
        request.setComment("Excellent!");
        restTemplate.postForEntity("/api/items/" + itemId + "/reviews", request, ReviewResponse.class);

        ResponseEntity<ReviewResponse[]> response = restTemplate.getForEntity(
                "/api/items/" + itemId + "/reviews", ReviewResponse[].class);

        assertEquals(200, response.getStatusCode().value());
        ReviewResponse[] body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.length);
        assertEquals(5, body[0].getRating());
        assertEquals("Excellent!", body[0].getComment());
        log.info("getReviewsByItemReturnsCreatedReview passed: {} review(s) found", body.length);
    }

    @Test
    @DisplayName("GET /api/items/{itemId}/reviews returns empty list when no reviews exist")
    void getReviewsForItemWithNoReviewsReturnsEmptyList() {
        ResponseEntity<ReviewResponse[]> response = restTemplate.getForEntity(
                "/api/items/" + itemId + "/reviews", ReviewResponse[].class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
        log.info("getReviewsForItemWithNoReviewsReturnsEmptyList passed");
    }

    @Test
    @DisplayName("DELETE /api/items/{itemId}/reviews/{reviewId} removes the review")
    void deleteReviewRemovesIt() {
        ReviewRequest request = new ReviewRequest();
        request.setAuthorId(reviewerId);
        request.setRating(3);
        request.setComment("Decent plant");
        Long reviewId = restTemplate.postForEntity(
                "/api/items/" + itemId + "/reviews", request, ReviewResponse.class)
                .getBody().getId();

        restTemplate.delete("/api/items/" + itemId + "/reviews/" + reviewId + "?requesterId=" + reviewerId);

        ResponseEntity<ReviewResponse[]> listResponse = restTemplate.getForEntity(
                "/api/items/" + itemId + "/reviews", ReviewResponse[].class);
        assertEquals(200, listResponse.getStatusCode().value());
        assertEquals(0, listResponse.getBody().length);
        log.info("deleteReviewRemovesIt passed: reviewId={}", reviewId);
    }
}
