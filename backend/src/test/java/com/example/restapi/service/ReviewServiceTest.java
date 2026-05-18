package com.example.restapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.restapi.dto.ReviewRequest;
import com.example.restapi.dto.ReviewResponse;
import com.example.restapi.model.Item;
import com.example.restapi.model.Profile;
import com.example.restapi.model.Review;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.ProfileRepository;
import com.example.restapi.repository.ReviewRepository;

import jakarta.persistence.EntityManager;

@DisplayName("ReviewService Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private EntityManager entityManager;

    private ReviewService reviewService;
    private Item item;
    private Profile author;
    private Review review;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reviewService = new ReviewService(reviewRepository, itemRepository, profileRepository, entityManager);

        item = new Item();
        item.setId(10L);
        item.setName("Fern");

        author = new Profile(UUID.randomUUID(), "alice", "111");

        review = new Review();
        review.setId(1L);
        review.setItem(item);
        review.setAuthor(author);
        review.setRating(5);
        review.setComment("Great plant");
    }

    @Test
    @DisplayName("should return item reviews ordered by repository")
    void getReviewsByItemIdReturnsMappedReviews() {
        when(itemRepository.existsById(10L)).thenReturn(true);
        when(reviewRepository.findByItem_IdOrderByCreatedAtDescIdDesc(10L)).thenReturn(List.of(review));

        List<ReviewResponse> responses = reviewService.getReviewsByItemId(10L);

        assertEquals(1, responses.size());
        assertEquals(10L, responses.get(0).getItemId());
        assertEquals("alice", responses.get(0).getAuthorUsername());
        verify(reviewRepository).findByItem_IdOrderByCreatedAtDescIdDesc(10L);
    }

    @Test
    @DisplayName("should reject missing item when listing reviews")
    void getReviewsByItemIdRejectsMissingItem() {
        when(itemRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.getReviewsByItemId(99L));

        assertEquals("Item not found", error.getMessage());
        verify(reviewRepository, never()).findByItem_IdOrderByCreatedAtDescIdDesc(any());
    }

    @Test
    @DisplayName("should create review successfully")
    void createReviewCreatesAndMapsResponse() {
        ReviewRequest request = new ReviewRequest();
        request.setAuthorId(author.getId());
        request.setRating(4);
        request.setComment("  Looks healthy and arrived fast.  ");

        Review saved = new Review();
        saved.setId(2L);
        saved.setItem(item);
        saved.setAuthor(author);
        saved.setRating(4);
        saved.setComment("Looks healthy and arrived fast.");

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(profileRepository.findById(author.getId())).thenReturn(Optional.of(author));
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        ReviewResponse response = reviewService.createReview(10L, request);

        assertEquals(2L, response.getId());
        assertEquals(10L, response.getItemId());
        assertEquals(author.getId(), response.getAuthorId());
        assertEquals("alice", response.getAuthorUsername());
        assertEquals(4, response.getRating());
        assertEquals("Looks healthy and arrived fast.", response.getComment());
        verify(entityManager).flush();
        verify(entityManager).refresh(saved);
    }

    @Test
    @DisplayName("should reject rating outside one to five")
    void createReviewRejectsInvalidRating() {
        ReviewRequest request = new ReviewRequest();
        request.setAuthorId(author.getId());
        request.setRating(6);
        request.setComment("Too high");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(10L, request));

        assertEquals("Rating must be between 1 and 5", error.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("should reject missing author")
    void createReviewRejectsMissingAuthor() {
        ReviewRequest request = new ReviewRequest();
        request.setAuthorId(author.getId());
        request.setRating(5);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(profileRepository.findById(author.getId())).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> reviewService.createReview(10L, request));

        assertEquals("Author not found", error.getMessage());
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("should store null when comment is blank")
    void createReviewNormalizesBlankComment() {
        ReviewRequest request = new ReviewRequest();
        request.setAuthorId(author.getId());
        request.setRating(5);
        request.setComment("   ");

        Review saved = new Review();
        saved.setId(3L);
        saved.setItem(item);
        saved.setAuthor(author);
        saved.setRating(5);
        saved.setComment(null);

        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(profileRepository.findById(author.getId())).thenReturn(Optional.of(author));
        when(reviewRepository.save(any(Review.class))).thenReturn(saved);

        ReviewResponse response = reviewService.createReview(10L, request);

        assertNull(response.getComment());
    }
}
