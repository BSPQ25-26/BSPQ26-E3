package com.example.restapi.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.restapi.model.Comment;

public class CommentResponse {

    private Long id;
    private Long postId;
    private UUID authorId;
    private String authorUsername;
    private String content;
    private OffsetDateTime createdAt;

    public CommentResponse() {
    }

    public static CommentResponse fromEntity(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.id = comment.getId();
        response.content = comment.getContent();
        response.createdAt = comment.getCreatedAt();
        if (comment.getPost() != null) {
            response.postId = comment.getPost().getId();
        }
        if (comment.getAuthor() != null) {
            response.authorId = comment.getAuthor().getId();
            response.authorUsername = comment.getAuthor().getUsername();
        }
        return response;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public UUID getAuthorId() { return authorId; }
    public String getAuthorUsername() { return authorUsername; }
    public String getContent() { return content; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
