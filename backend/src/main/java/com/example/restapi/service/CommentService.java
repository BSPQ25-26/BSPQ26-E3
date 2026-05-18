package com.example.restapi.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.restapi.dto.CommentRequest;
import com.example.restapi.dto.CommentResponse;
import com.example.restapi.model.Comment;
import com.example.restapi.model.Post;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.CommentRepository;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.ProfileRepository;

import jakarta.persistence.EntityManager;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ProfileRepository profileRepository;
    private final EntityManager entityManager;

    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            ProfileRepository profileRepository,
            EntityManager entityManager) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post not found");
        }
        return commentRepository.findByPost_IdOrderByCreatedAtAscIdAsc(postId).stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request) {
        if (request.getAuthorId() == null) {
            throw new IllegalArgumentException("Author not found");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Comment content is required");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        Profile author = profileRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(request.getContent().trim());

        Comment saved = commentRepository.save(comment);
        entityManager.flush();
        entityManager.refresh(saved);
        return CommentResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteComment(Long commentId, UUID requesterId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        if (!comment.getAuthor().getId().equals(requesterId)) {
            throw new IllegalArgumentException("Not authorized to delete this comment");
        }
        commentRepository.deleteById(commentId);
    }
}
