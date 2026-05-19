package com.example.restapi.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.example.restapi.dto.CommentRequest;
import com.example.restapi.dto.CommentResponse;
import com.example.restapi.model.Category;
import com.example.restapi.model.Post;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.CategoryRepository;
import com.example.restapi.repository.CommentRepository;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.ProfileRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Comment Integration Tests")
class CommentServiceIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceIntegrationTest.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private CommentRepository commentRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private CategoryRepository categoryRepository;

    private UUID authorId;
    private Long postId;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.save(new Category("General", "General discussion"));
        Profile author = profileRepository.save(new Profile(UUID.randomUUID(), "comment-author", "000000003"));
        authorId = author.getId();

        Post post = new Post();
        post.setTitle("Test Post");
        post.setContent("Post content for comment integration tests");
        post.setAuthor(author);
        post.setCategory(category);
        postId = postRepository.save(post).getId();

        log.info("setUp complete: authorId={}, postId={}", authorId, postId);
    }

    @AfterEach
    void tearDown() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/comments creates a comment on a post")
    void createCommentReturns200() {
        CommentRequest request = new CommentRequest();
        request.setContent("This is a test comment");
        request.setAuthorId(authorId);
        request.setPostId(postId);

        ResponseEntity<CommentResponse> response = restTemplate.postForEntity(
                "/api/comments", request, CommentResponse.class);

        assertEquals(200, response.getStatusCode().value());
        CommentResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());
        assertEquals("This is a test comment", body.getContent());
        assertEquals(authorId, body.getAuthorId());
        assertFalse(body.isEdited());
        log.info("createCommentReturns200 passed: commentId={}", body.getId());
    }

    @Test
    @DisplayName("GET /api/comments/post/{postId} returns all comments for a post")
    void getCommentsByPostReturnsCreatedComment() {
        CommentRequest request = new CommentRequest();
        request.setContent("Hello from integration test");
        request.setAuthorId(authorId);
        request.setPostId(postId);
        restTemplate.postForEntity("/api/comments", request, CommentResponse.class);

        ResponseEntity<CommentResponse[]> response = restTemplate.getForEntity(
                "/api/comments/post/" + postId, CommentResponse[].class);

        assertEquals(200, response.getStatusCode().value());
        CommentResponse[] body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.length);
        assertEquals("Hello from integration test", body[0].getContent());
        log.info("getCommentsByPostReturnsCreatedComment passed: {} comment(s) found", body.length);
    }

    @Test
    @DisplayName("PUT /api/comments/{id} updates content and marks comment as edited")
    void updateCommentChangesContentAndSetsEditedFlag() {
        CommentRequest request = new CommentRequest();
        request.setContent("Original content");
        request.setAuthorId(authorId);
        request.setPostId(postId);
        Long commentId = restTemplate.postForEntity("/api/comments", request, CommentResponse.class)
                .getBody().getId();

        Map<String, String> updateBody = Map.of(
                "requesterId", authorId.toString(),
                "content", "Updated content");
        ResponseEntity<CommentResponse> updated = restTemplate.exchange(
                "/api/comments/" + commentId,
                HttpMethod.PUT,
                new HttpEntity<>(updateBody),
                CommentResponse.class);

        assertEquals(200, updated.getStatusCode().value());
        assertEquals("Updated content", updated.getBody().getContent());
        assertTrue(updated.getBody().isEdited());
        log.info("updateCommentChangesContentAndSetsEditedFlag passed: commentId={}", commentId);
    }

    @Test
    @DisplayName("DELETE /api/comments/{id} removes the comment from the post")
    void deleteCommentRemovesIt() {
        CommentRequest request = new CommentRequest();
        request.setContent("To be deleted");
        request.setAuthorId(authorId);
        request.setPostId(postId);
        Long commentId = restTemplate.postForEntity("/api/comments", request, CommentResponse.class)
                .getBody().getId();

        restTemplate.delete("/api/comments/" + commentId + "?requesterId=" + authorId);

        ResponseEntity<CommentResponse[]> listResponse = restTemplate.getForEntity(
                "/api/comments/post/" + postId, CommentResponse[].class);
        assertEquals(200, listResponse.getStatusCode().value());
        assertEquals(0, listResponse.getBody().length);
        log.info("deleteCommentRemovesIt passed: commentId={}", commentId);
    }
}
