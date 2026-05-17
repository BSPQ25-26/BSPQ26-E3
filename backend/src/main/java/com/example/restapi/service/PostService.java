package com.example.restapi.service;

import com.example.restapi.model.Category;
import com.example.restapi.model.Post;
import com.example.restapi.repository.PostRepository;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PostService {

    private PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Post id : "+id));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.findById(id)
                .orElseThrow(()-> new OpenApiResourceNotFoundException("Post id : "+ id));
        postRepository.deleteById(id);
    }

    public Post updatePost(Long id, Map<String, Object> updates) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        partialUpdate(post ,updates);
        return postRepository.save(post);
    }

    private void partialUpdate(Post postDetails, Map<String, Object> updates){
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
