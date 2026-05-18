package com.example.restapi.service;

import com.example.restapi.dto.CommentRequest;
import com.example.restapi.dto.CommentResponse;
import com.example.restapi.model.Comment;
import com.example.restapi.model.Item;
import com.example.restapi.model.Post;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.CommentRepository;
import com.example.restapi.repository.ItemRepository;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {

        @Autowired
        private CommentRepository commentRepository;
        @Autowired
        private ProfileRepository profileRepository;
        @Autowired
        private ItemRepository itemRepository;
        @Autowired
        private PostRepository postRepository;

        private CommentResponse toResponse(Comment c) {
                return new CommentResponse(
                                c.getId(),
                                c.getContent(),
                                c.getCreatedAt(),
                                c.getAuthor().getId(),
                                c.getAuthor().getUsername(),
                                c.isEdited());
        }

        public CommentResponse createComment(CommentRequest request) {
                boolean hasItem = request.getItemId() != null;
                boolean hasPost = request.getPostId() != null;

                if ((hasItem && hasPost) || (!hasItem && !hasPost)) {
                        throw new IllegalArgumentException(
                                        "El comentario debe pertenecer a un ítem o a un post, pero no a ambos.");
                }

                Profile author = profileRepository.findById(request.getAuthorId())
                                .orElseThrow(() -> new RuntimeException("Autor no encontrado"));

                Comment comment = new Comment();
                comment.setContent(request.getContent());
                comment.setAuthor(author);

                if (hasItem) {
                        Item item = itemRepository.findById(request.getItemId())
                                        .orElseThrow(() -> new RuntimeException("Ítem no encontrado"));
                        comment.setItem(item);
                } else {
                        Post post = postRepository.findById(request.getPostId())
                                        .orElseThrow(() -> new RuntimeException("Post no encontrado"));
                        comment.setPost(post);
                }

                return toResponse(commentRepository.save(comment));
        }

        public CommentResponse updateComment(Long commentId, UUID requesterId, String newContent) {
                Comment comment = commentRepository.findById(commentId)
                                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

                if (!comment.getAuthor().getId().equals(requesterId)) {
                        throw new SecurityException("No tienes permiso para editar este comentario.");
                }

                comment.setContent(newContent);
                comment.setEdited(true);
                return toResponse(commentRepository.save(comment));
        }

        public void deleteComment(Long commentId, UUID requesterId) {
                Comment comment = commentRepository.findById(commentId)
                                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

                if (!comment.getAuthor().getId().equals(requesterId)) {
                        throw new SecurityException("No tienes permiso para eliminar este comentario.");
                }

                commentRepository.delete(comment);
        }

        public List<CommentResponse> getCommentsByPost(Long postId) {
                return commentRepository.findByPostId(postId).stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }

        public List<CommentResponse> getCommentsByItem(Long itemId) {
                return commentRepository.findByItemId(itemId).stream()
                                .map(this::toResponse)
                                .collect(Collectors.toList());
        }
}