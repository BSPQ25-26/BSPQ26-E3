package com.example.restapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.ReviewRequest;
import com.example.restapi.dto.ReviewResponse;
import com.example.restapi.service.ReviewService;

@RestController
@RequestMapping("/api/items/{itemId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviewsByItemId(@PathVariable Long itemId) {
        return ResponseEntity.ok(reviewService.getReviewsByItemId(itemId));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long itemId, @RequestBody ReviewRequest request) {
        ReviewResponse created = reviewService.createReview(itemId, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long itemId,
            @PathVariable Long reviewId,
            @RequestParam UUID requesterId) {
        reviewService.deleteReview(reviewId, requesterId);
        return ResponseEntity.noContent().build();
    }
}
