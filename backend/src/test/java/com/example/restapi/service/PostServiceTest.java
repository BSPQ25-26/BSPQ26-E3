package com.example.restapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.restapi.model.Post;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.ProfileRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("PostService Tests")
class PostServiceTest {

    private static final Logger log = LoggerFactory.getLogger(PostServiceTest.class);

    @Mock
    private PostRepository postRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private PostService postService;

    private Post testPost;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Title");
        testPost.setContent("Test Content");
    }

    @Nested
    @DisplayName("getAllPosts")
    class GetAllPostsTests {

        @Test
        @DisplayName("should return all posts")
        void testGetAllPosts() {
            when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(testPost));

            List<Post> result = postService.getAllPosts();

            assertEquals(1, result.size());
            assertEquals("Test Title", result.get(0).getTitle());
            verify(postRepository).findAllByOrderByCreatedAtDesc();
            log.info("testGetAllPosts passed: returned {} post(s)", result.size());
        }

        @Test
        @DisplayName("should return empty list when no posts exist")
        void testGetAllPostsEmpty() {
            when(postRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of());

            List<Post> result = postService.getAllPosts();

            assertTrue(result.isEmpty());
            verify(postRepository).findAllByOrderByCreatedAtDesc();
        }
    }

    @Nested
    @DisplayName("getPostById")
    class GetPostByIdTests {

        @Test
        @DisplayName("should return post when found")
        void testGetPostByIdFound() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

            Post result = postService.getPostById(1L);

            assertNotNull(result);
            assertEquals("Test Title", result.getTitle());
            verify(postRepository).findById(1L);
            log.info("testGetPostByIdFound passed: title='{}'", result.getTitle());
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void testGetPostByIdNotFound() {
            when(postRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(Exception.class, () -> postService.getPostById(99L));
            verify(postRepository).findById(99L);
        }
    }

    @Nested
    @DisplayName("createPost")
    class CreatePostTests {

        @Test
        @DisplayName("should save and return the post")
        void testCreatePost() {
            when(postRepository.save(any(Post.class))).thenReturn(testPost);

            Post result = postService.createPost(testPost);

            assertNotNull(result);
            assertEquals("Test Title", result.getTitle());
            verify(postRepository).save(testPost);
            log.info("testCreatePost passed: created post id={}", result.getId());
        }
    }

    @Nested
    @DisplayName("deletePost")
    class DeletePostTests {

        @Test
        @DisplayName("should delete post when found")
        void testDeletePostSuccess() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

            assertDoesNotThrow(() -> postService.deletePost(1L));
            verify(postRepository).deleteById(1L);
            log.info("testDeletePostSuccess passed");
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void testDeletePostNotFound() {
            when(postRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(Exception.class, () -> postService.deletePost(99L));
            verify(postRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("updatePost")
    class UpdatePostTests {

        @Test
        @DisplayName("should update title")
        void testUpdatePostTitle() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenReturn(testPost);

            Post result = postService.updatePost(1L, Map.of("title", "New Title"));

            assertNotNull(result);
            verify(postRepository).findById(1L);
            verify(postRepository, atLeastOnce()).save(any(Post.class));
            log.info("testUpdatePostTitle passed");
        }

        @Test
        @DisplayName("should update content")
        void testUpdatePostContent() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenReturn(testPost);

            Post result = postService.updatePost(1L, Map.of("content", "New Content"));

            assertNotNull(result);
            verify(postRepository, atLeastOnce()).save(any(Post.class));
        }

        @Test
        @DisplayName("should update category")
        void testUpdatePostCategory() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
            when(postRepository.save(any(Post.class))).thenReturn(testPost);

            Post result = postService.updatePost(1L, Map.of("categoryId", 2));

            assertNotNull(result);
            verify(postRepository, atLeastOnce()).save(any(Post.class));
        }

        @Test
        @DisplayName("should throw exception when post not found")
        void testUpdatePostNotFound() {
            when(postRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> postService.updatePost(99L, Map.of("title", "X")));
            verify(postRepository, never()).save(any());
        }
    }
}
