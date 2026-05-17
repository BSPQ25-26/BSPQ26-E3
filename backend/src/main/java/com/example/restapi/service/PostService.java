package com.example.restapi.service;

import com.example.restapi.dto.PostResponse;
import com.example.restapi.model.Category;
import com.example.restapi.model.Post;
import com.example.restapi.repository.PostRepository;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final EntityManager entityManager;

    @Autowired
    public PostService(PostRepository postRepository, EntityManager entityManager) {
        this.postRepository = postRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Post id : " + id));
        return PostResponse.fromEntity(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByAuthor(UUID authorId) {
        return postRepository.findByAuthor_Id(authorId).stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponse createPost(Post post) {
        if (post.getTitle() == null || post.getTitle().isBlank()) {
            throw new IllegalArgumentException("Post title is required");
        }
        Post saved = postRepository.save(post);
        // The Profile/Category attached by the controller are transient stubs with
        // only the id. Flush + refresh to load the real rows so the response can
        // expose authorUsername and categoryName.
        entityManager.flush();
        entityManager.refresh(saved);
        return PostResponse.fromEntity(saved);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.findById(id)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Post id : " + id));
        postRepository.deleteById(id);
    }

    @Transactional
    public PostResponse updatePost(Long id, Map<String, Object> updates) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        partialUpdate(post, updates);
        Post saved = postRepository.save(post);
        if (updates.containsKey("categoryId")) {
            entityManager.flush();
            entityManager.refresh(saved);
        }
        return PostResponse.fromEntity(saved);
    }

    private void partialUpdate(Post postDetails, Map<String, Object> updates) {
        if (updates.containsKey("title")) {
            postDetails.setTitle((String) updates.get("title"));
        }

        if (updates.containsKey("content")) {
            postDetails.setContent((String) updates.get("content"));
        }

        if (updates.containsKey("categoryId")) {
            Object raw = updates.get("categoryId");
            Long categoryId = raw instanceof Number n ? n.longValue() : Long.valueOf(raw.toString());
            Category category = new Category();
            category.setId(categoryId);
            postDetails.setCategory(category);
        }
    }
}
