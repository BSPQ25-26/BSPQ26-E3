package com.example.restapi.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restapi.dto.ReviewRequest;
import com.example.restapi.dto.ReviewResponse;
import com.example.restapi.model.Item;
import com.example.restapi.model.Profile;
import com.example.restapi.model.Review;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.ProfileRepository;
import com.example.restapi.repository.ReviewRepository;

import jakarta.persistence.EntityManager;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final ProfileRepository profileRepository;
    private final EntityManager entityManager;

    public ReviewService(
            ReviewRepository reviewRepository,
            ItemRepository itemRepository,
            ProfileRepository profileRepository,
            EntityManager entityManager) {
        this.reviewRepository = reviewRepository;
        this.itemRepository = itemRepository;
        this.profileRepository = profileRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByItemId(Long itemId) {
        validateItemExists(itemId);
        return reviewRepository.findByItem_IdOrderByCreatedAtDescIdDesc(itemId).stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewResponse createReview(Long itemId, ReviewRequest request) {
        if (request.getAuthorId() == null) {
            throw new IllegalArgumentException("Author not found");
        }
        validateRating(request.getRating());

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        Profile author = profileRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        Review review = new Review();
        review.setItem(item);
        review.setAuthor(author);
        review.setRating(request.getRating());
        review.setComment(normalizeComment(request.getComment()));

        Review saved = reviewRepository.save(review);
        entityManager.flush();
        entityManager.refresh(saved);
        return ReviewResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteReview(Long reviewId, UUID requesterId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        if (!review.getAuthor().getId().equals(requesterId)) {
            throw new SecurityException("Not authorized to delete this review");
        }
        reviewRepository.delete(review);
    }

    private void validateItemExists(Long itemId) {
        if (!itemRepository.existsById(itemId)) {
            throw new IllegalArgumentException("Item not found");
        }
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private String normalizeComment(String comment) {
        if (comment == null) {
            return null;
        }
        String trimmed = comment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
