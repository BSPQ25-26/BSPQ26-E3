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

/**
 *
 * Service layer for blog-post management.
 *
 * Provides CRUD operations for posts, author resolution and category
 * linkage.  This remote facade is consumed by the REST controllers.
 */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final EntityManager entityManager;

    /**
     * Constructs PostService.
     * @param postRepository Data-access object for Post entities.
     * @param entityManager  JPA EntityManager used for flush/refresh operations.
     */
    @Autowired
    public PostService(PostRepository postRepository, EntityManager entityManager) {
        this.postRepository = postRepository;
        this.entityManager = entityManager;
    }

    /**
     * Retrieves a single post by id.
     * @param id Post primary key.
     * @return PostResponse DTO.
     * @throws OpenApiResourceNotFoundException if the post does not exist.
     */
    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Post id : " + id));
        return PostResponse.fromEntity(post);
    }

    /**
     * Returns all posts ordered by creation date (newest first).
     * @return List of PostResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Returns every post written by a specific author.
     * @param authorId UUID of the author.
     * @return List of PostResponse DTOs.
     */
    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByAuthor(UUID authorId) {
        return postRepository.findByAuthor_Id(authorId).stream()
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Persists a new post.
     *
     * Flushes and refreshes the entity so that the returned DTO contains
     * fully loaded author and category names.
     *
     * @param post The post to create (title must not be blank).
     * @return PostResponse DTO of the saved post.
     * @throws IllegalArgumentException if the title is missing or blank.
     */
    @Transactional
    public PostResponse createPost(Post post) {
        if (post.getTitle() == null || post.getTitle().isBlank()) {
            throw new IllegalArgumentException("Post title is required");
        }
        Post saved = postRepository.save(post);
        entityManager.flush();
        entityManager.refresh(saved);
        return PostResponse.fromEntity(saved);
    }

    /**
     * Deletes a post.
     * @param id Post primary key.
     * @throws OpenApiResourceNotFoundException if the post does not exist.
     */
    @Transactional
    public void deletePost(Long id) {
        postRepository.findById(id)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Post id : " + id));
        postRepository.deleteById(id);
    }

    /**
     * Applies a partial update to an existing post.
     * @param id      Post primary key.
     * @param updates Map of fields to update (supports "title", "content", "categoryId").
     * @return PostResponse DTO of the updated post.
     * @throws RuntimeException if the post is not found.
     */
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
