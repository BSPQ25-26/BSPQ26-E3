package com.example.restapi.dto;

import java.util.UUID;

public class PostRequest {

    private UUID authorId;
    private String title;
    private String content;
    private Long categoryId;

    public PostRequest() {}

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
