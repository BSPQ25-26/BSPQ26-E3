package com.example.restapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.restapi.dto.CommentRequest;
import com.example.restapi.dto.CommentResponse;
import com.example.restapi.model.Comment;
import com.example.restapi.model.Post;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.CommentRepository;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.ProfileRepository;

import jakarta.persistence.EntityManager;

@DisplayName("CommentService Tests")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private EntityManager entityManager;

    private CommentService commentService;
    private Post post;
    private Profile author;
    private Comment comment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        commentService = new CommentService(commentRepository, postRepository, profileRepository, entityManager);

        post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");

        author = new Profile(UUID.randomUUID(), "bob", "222");

        comment = new Comment();
        comment.setId(10L);
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent("Nice post!");
    }

    @Test
    @DisplayName("should return comments for existing post")
    void getCommentsByPostIdReturnsMappedComments() {
        when(postRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByPost_IdOrderByCreatedAtAscIdAsc(1L)).thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getCommentsByPostId(1L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getPostId());
        assertEquals("bob", responses.get(0).getAuthorUsername());
        assertEquals("Nice post!", responses.get(0).getContent());
        verify(commentRepository).findByPost_IdOrderByCreatedAtAscIdAsc(1L);
    }

    @Test
    @DisplayName("should reject listing comments for missing post")
    void getCommentsByPostIdRejectsMissingPost() {
        when(postRepository.existsById(99L)).thenReturn(false);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.getCommentsByPostId(99L));

        assertEquals("Post not found", error.getMessage());
        verify(commentRepository, never()).findByPost_IdOrderByCreatedAtAscIdAsc(any());
    }

    @Test
    @DisplayName("should create comment successfully")
    void createCommentCreatesAndMapsResponse() {
        CommentRequest request = new CommentRequest();
        request.setAuthorId(author.getId());
        request.setContent("  Great content!  ");

        Comment saved = new Comment();
        saved.setId(20L);
        saved.setPost(post);
        saved.setAuthor(author);
        saved.setContent("Great content!");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(profileRepository.findById(author.getId())).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentResponse response = commentService.createComment(1L, request);

        assertEquals(20L, response.getId());
        assertEquals(1L, response.getPostId());
        assertEquals(author.getId(), response.getAuthorId());
        assertEquals("bob", response.getAuthorUsername());
        assertEquals("Great content!", response.getContent());
        verify(entityManager).flush();
        verify(entityManager).refresh(saved);
    }

    @Test
    @DisplayName("should reject blank comment content")
    void createCommentRejectsBlankContent() {
        CommentRequest request = new CommentRequest();
        request.setAuthorId(author.getId());
        request.setContent("   ");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(1L, request));

        assertEquals("Comment content is required", error.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("should reject null content")
    void createCommentRejectsNullContent() {
        CommentRequest request = new CommentRequest();
        request.setAuthorId(author.getId());
        request.setContent(null);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(1L, request));

        assertEquals("Comment content is required", error.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("should reject missing author when creating comment")
    void createCommentRejectsMissingAuthor() {
        CommentRequest request = new CommentRequest();
        request.setAuthorId(null);
        request.setContent("Some content");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(1L, request));

        assertEquals("Author not found", error.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("should reject creating comment for missing post")
    void createCommentRejectsMissingPost() {
        CommentRequest request = new CommentRequest();
        request.setAuthorId(author.getId());
        request.setContent("Some content");

        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.createComment(99L, request));

        assertEquals("Post not found", error.getMessage());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("should delete comment when requester is author")
    void deleteCommentSucceedsForAuthor() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(10L, author.getId());

        verify(commentRepository).deleteById(10L);
    }

    @Test
    @DisplayName("should reject deletion by non-author")
    void deleteCommentRejectsNonAuthor() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        UUID otherId = UUID.randomUUID();
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.deleteComment(10L, otherId));

        assertEquals("Not authorized to delete this comment", error.getMessage());
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("should reject deletion of non-existent comment")
    void deleteCommentRejectsMissingComment() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> commentService.deleteComment(999L, author.getId()));

        assertEquals("Comment not found", error.getMessage());
        verify(commentRepository, never()).deleteById(any());
    }
}
