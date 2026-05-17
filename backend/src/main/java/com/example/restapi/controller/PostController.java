package com.example.restapi.controller;


import com.example.restapi.dto.PostRequest;
import com.example.restapi.dto.PostResponse;
import com.example.restapi.model.Category;
import com.example.restapi.model.Post;
import com.example.restapi.model.Profile;
import com.example.restapi.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping("/by-author/{authorId}")
    public ResponseEntity<List<PostResponse>> getPostsByAuthor(@PathVariable UUID authorId) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId));
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostRequest req) {
        try {
            Post post = new Post();
            post.setTitle(req.getTitle());
            post.setContent(req.getContent());
            if (req.getAuthorId() != null) {
                Profile author = new Profile();
                author.setId(req.getAuthorId());
                post.setAuthor(author);
            }
            if (req.getCategoryId() != null) {
                Category category = new Category();
                category.setId(req.getCategoryId());
                post.setCategory(category);
            }
            PostResponse created = postService.createPost(post);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@RequestBody Map<String, Object> updates, @PathVariable Long id) {
        try {
            return ResponseEntity.ok(postService.updatePost(id, updates));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
