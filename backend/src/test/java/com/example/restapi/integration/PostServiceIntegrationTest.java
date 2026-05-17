package com.example.restapi.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.example.restapi.dto.PostRequest;
import com.example.restapi.dto.PostResponse;
import com.example.restapi.model.Category;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.CategoryRepository;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.ProfileRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Post Integration Tests")
class PostServiceIntegrationTest {

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private PostRepository postRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private CategoryRepository categoryRepository;

    private UUID authorId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();

        Profile author = profileRepository.save(new Profile(UUID.randomUUID(), "forum-author", "111111111"));
        authorId = author.getId();
        categoryId = categoryRepository.save(new Category("Indoor", "Indoor plants")).getId();
    }

    @AfterEach
    void tearDown() {
        postRepository.deleteAll();
        profileRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/posts creates and returns a post with author and category names")
    void createPost() {
        PostRequest req = new PostRequest();
        req.setAuthorId(authorId);
        req.setTitle("Hello forum");
        req.setContent("First post on the new community tab.");
        req.setCategoryId(categoryId);

        ResponseEntity<PostResponse> response = restTemplate.postForEntity("/api/posts", req, PostResponse.class);

        assertEquals(201, response.getStatusCode().value());
        PostResponse body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.getId());
        assertEquals("Hello forum", body.getTitle());
        assertEquals("forum-author", body.getAuthorUsername());
        assertEquals("Indoor", body.getCategoryName());
    }

    @Test
    @DisplayName("POST /api/posts with blank title returns 400")
    void createPostRejectsBlankTitle() {
        PostRequest req = new PostRequest();
        req.setAuthorId(authorId);
        req.setTitle("   ");
        req.setCategoryId(categoryId);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/posts", req, String.class);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    @DisplayName("GET /api/posts returns posts ordered by createdAt desc")
    void feedIsOrderedNewestFirst() throws Exception {
        // create two posts with a delay to guarantee distinct timestamps
        for (String t : new String[]{"older", "newer"}) {
            PostRequest req = new PostRequest();
            req.setAuthorId(authorId);
            req.setTitle(t);
            req.setCategoryId(categoryId);
            restTemplate.postForEntity("/api/posts", req, PostResponse.class);
            Thread.sleep(15);
        }

        ResponseEntity<PostResponse[]> response = restTemplate.getForEntity("/api/posts", PostResponse[].class);
        assertEquals(200, response.getStatusCode().value());
        PostResponse[] feed = response.getBody();
        assertNotNull(feed);
        assertEquals(2, feed.length);
        assertEquals("newer", feed[0].getTitle());
        assertEquals("older", feed[1].getTitle());
    }

    @Test
    @DisplayName("GET /api/posts/by-author/{id} returns only that author's posts")
    void postsByAuthor() {
        Profile other = profileRepository.save(new Profile(UUID.randomUUID(), "other-user", "222222222"));

        PostRequest mine = new PostRequest();
        mine.setAuthorId(authorId);
        mine.setTitle("Mine");
        mine.setCategoryId(categoryId);
        restTemplate.postForEntity("/api/posts", mine, PostResponse.class);

        PostRequest theirs = new PostRequest();
        theirs.setAuthorId(other.getId());
        theirs.setTitle("Theirs");
        theirs.setCategoryId(categoryId);
        restTemplate.postForEntity("/api/posts", theirs, PostResponse.class);

        ResponseEntity<PostResponse[]> response = restTemplate.getForEntity(
                "/api/posts/by-author/" + authorId, PostResponse[].class);
        assertEquals(200, response.getStatusCode().value());
        PostResponse[] body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.length);
        assertEquals("Mine", body[0].getTitle());
    }

    @Test
    @DisplayName("PATCH /api/posts/{id} updates title and content")
    void patchUpdatesFields() {
        PostRequest req = new PostRequest();
        req.setAuthorId(authorId);
        req.setTitle("original");
        req.setCategoryId(categoryId);
        ResponseEntity<PostResponse> created = restTemplate.postForEntity("/api/posts", req, PostResponse.class);
        Long postId = created.getBody().getId();

        ResponseEntity<PostResponse> patched = restTemplate.exchange(
                "/api/posts/" + postId,
                HttpMethod.PATCH,
                new org.springframework.http.HttpEntity<>(Map.of("title", "edited", "content", "new body")),
                PostResponse.class);

        assertEquals(200, patched.getStatusCode().value());
        assertEquals("edited", patched.getBody().getTitle());
        assertEquals("new body", patched.getBody().getContent());
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} removes the post")
    void deleteRemovesPost() {
        PostRequest req = new PostRequest();
        req.setAuthorId(authorId);
        req.setTitle("to-delete");
        req.setCategoryId(categoryId);
        Long postId = restTemplate.postForEntity("/api/posts", req, PostResponse.class).getBody().getId();

        restTemplate.delete("/api/posts/" + postId);

        assertTrue(postRepository.findById(postId).isEmpty());
    }
}
