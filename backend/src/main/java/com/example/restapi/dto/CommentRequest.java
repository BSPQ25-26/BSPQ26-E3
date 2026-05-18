package com.example.restapi.dto;

import java.util.UUID;

public class CommentRequest {

    private UUID authorId;
    private String content;

    public UUID getAuthorId() { return authorId; }
    public void setAuthorId(UUID authorId) { this.authorId = authorId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
