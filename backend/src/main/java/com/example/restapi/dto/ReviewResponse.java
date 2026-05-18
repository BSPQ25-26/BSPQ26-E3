package com.example.restapi.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.restapi.model.Review;

public class ReviewResponse {

    private Long id;
    private Long itemId;
    private UUID authorId;
    private String authorUsername;
    private int rating;
    private String comment;
    private OffsetDateTime createdAt;

    public ReviewResponse() {
    }

    public static ReviewResponse fromEntity(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.id = review.getId();
        response.rating = review.getRating();
        response.comment = review.getComment();
        response.createdAt = review.getCreatedAt();
        if (review.getItem() != null) {
            response.itemId = review.getItem().getId();
        }
        if (review.getAuthor() != null) {
            response.authorId = review.getAuthor().getId();
            response.authorUsername = review.getAuthor().getUsername();
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getItemId() {
        return itemId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
