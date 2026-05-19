package com.example.restapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.restapi.dto.CommentRequest;
import com.example.restapi.dto.CommentResponse;
import com.example.restapi.model.Comment;
import com.example.restapi.model.Post;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.CommentRepository;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.ProfileRepository;

@DisplayName("CommentService Tests")
class CommentServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CommentServiceTest.class);

    @Mock private CommentRepository commentRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    private Profile author;
    private Post post;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        author = new Profile(UUID.randomUUID(), "alice", "111");
        post = new Post();
        post.setId(1L);
        post.setTitle("Test post");
    }

    @Nested
    @DisplayName("createComment")
    class CreateCommentTests {

        @Test
        @DisplayName("should create and return a comment")
        void createsCommentSuccessfully() {
            CommentRequest request = new CommentRequest();
            request.setAuthorId(author.getId());
            request.setPostId(1L);
            request.setContent("Great post!");

            Comment saved = new Comment();
            saved.setId(10L);
            saved.setContent("Great post!");
            saved.setAuthor(author);
            saved.setPost(post);

            when(profileRepository.findById(author.getId())).thenReturn(Optional.of(author));
            when(postRepository.findById(1L)).thenReturn(Optional.of(post));
            when(commentRepository.save(any(Comment.class))).thenReturn(saved);

            CommentResponse response = commentService.createComment(request);

            assertNotNull(response);
            assertEquals(10L, response.getId());
            assertEquals("Great post!", response.getContent());
            assertEquals(author.getId(), response.getAuthorId());
            assertFalse(response.isEdited());
            log.info("createsCommentSuccessfully passed: commentId={}", response.getId());
        }

        @Test
        @DisplayName("should throw when author not found")
        void throwsWhenAuthorNotFound() {
            CommentRequest request = new CommentRequest();
            request.setAuthorId(UUID.randomUUID());
            request.setPostId(1L);
            request.setContent("Hello");

            when(profileRepository.findById(any())).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> commentService.createComment(request));
            verify(commentRepository, never()).save(any());
            log.info("throwsWhenAuthorNotFound passed");
        }

        @Test
        @DisplayName("should throw when post not found")
        void throwsWhenPostNotFound() {
            CommentRequest request = new CommentRequest();
            request.setAuthorId(author.getId());
            request.setPostId(99L);
            request.setContent("Hello");

            when(profileRepository.findById(author.getId())).thenReturn(Optional.of(author));
            when(postRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> commentService.createComment(request));
            verify(commentRepository, never()).save(any());
            log.info("throwsWhenPostNotFound passed");
        }
    }

    @Nested
    @DisplayName("updateComment")
    class UpdateCommentTests {

        @Test
        @DisplayName("should update content and mark as edited")
        void updatesContentAndSetsEditedFlag() {
            Comment comment = new Comment();
            comment.setId(5L);
            comment.setContent("Original");
            comment.setAuthor(author);
            comment.setPost(post);

            when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));
            when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

            CommentResponse response = commentService.updateComment(5L, author.getId(), "Updated content");

            assertEquals("Updated content", response.getContent());
            assertTrue(response.isEdited());
            log.info("updatesContentAndSetsEditedFlag passed");
        }

        @Test
        @DisplayName("should throw SecurityException when requester is not the author")
        void throwsSecurityExceptionForWrongRequester() {
            Comment comment = new Comment();
            comment.setId(5L);
            comment.setContent("Original");
            comment.setAuthor(author);

            when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

            UUID otherId = UUID.randomUUID();
            assertThrows(SecurityException.class,
                    () -> commentService.updateComment(5L, otherId, "Hacked content"));
            verify(commentRepository, never()).save(any());
            log.info("throwsSecurityExceptionForWrongRequester passed");
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteCommentTests {

        @Test
        @DisplayName("should delete comment when requester is the author")
        void deletesCommentSuccessfully() {
            Comment comment = new Comment();
            comment.setId(7L);
            comment.setAuthor(author);

            when(commentRepository.findById(7L)).thenReturn(Optional.of(comment));

            commentService.deleteComment(7L, author.getId());

            verify(commentRepository).delete(comment);
            log.info("deletesCommentSuccessfully passed");
        }

        @Test
        @DisplayName("should throw SecurityException when requester is not the author")
        void throwsSecurityExceptionForWrongRequester() {
            Comment comment = new Comment();
            comment.setId(7L);
            comment.setAuthor(author);

            when(commentRepository.findById(7L)).thenReturn(Optional.of(comment));

            UUID otherId = UUID.randomUUID();
            assertThrows(SecurityException.class,
                    () -> commentService.deleteComment(7L, otherId));
            verify(commentRepository, never()).delete(any());
            log.info("throwsSecurityExceptionForWrongRequester passed");
        }
    }

    @Nested
    @DisplayName("getCommentsByPost")
    class GetCommentsByPostTests {

        @Test
        @DisplayName("should return all comments for a post")
        void returnsCommentsForPost() {
            Comment c1 = new Comment();
            c1.setId(1L);
            c1.setContent("First comment");
            c1.setAuthor(author);

            Comment c2 = new Comment();
            c2.setId(2L);
            c2.setContent("Second comment");
            c2.setAuthor(author);

            when(commentRepository.findByPostId(1L)).thenReturn(List.of(c1, c2));

            List<CommentResponse> result = commentService.getCommentsByPost(1L);

            assertEquals(2, result.size());
            assertEquals("First comment", result.get(0).getContent());
            assertEquals("Second comment", result.get(1).getContent());
            log.info("returnsCommentsForPost passed: {} comments", result.size());
        }

        @Test
        @DisplayName("should return empty list when post has no comments")
        void returnsEmptyListForNoComments() {
            when(commentRepository.findByPostId(99L)).thenReturn(List.of());

            List<CommentResponse> result = commentService.getCommentsByPost(99L);

            assertTrue(result.isEmpty());
            log.info("returnsEmptyListForNoComments passed");
        }
    }
}
