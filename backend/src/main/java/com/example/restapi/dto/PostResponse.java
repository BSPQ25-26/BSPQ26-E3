package com.example.restapi.dto;

import com.example.restapi.model.Post;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PostResponse {

    private Long id;
    private String title;
    private String content;
    private OffsetDateTime createdAt;
    private UUID authorId;
    private String authorUsername;
    private Long categoryId;
    private String categoryName;

    public PostResponse() {}

    public static PostResponse fromEntity(Post post) {
        PostResponse r = new PostResponse();
        r.id = post.getId();
        r.title = post.getTitle();
        r.content = post.getContent();
        r.createdAt = post.getCreatedAt();
        if (post.getAuthor() != null) {
            r.authorId = post.getAuthor().getId();
            r.authorUsername = post.getAuthor().getUsername();
        }
        if (post.getCategory() != null) {
            r.categoryId = post.getCategory().getId();
            r.categoryName = post.getCategory().getName();
        }
        return r;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public UUID getAuthorId() { return authorId; }
    public String getAuthorUsername() { return authorUsername; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
}
