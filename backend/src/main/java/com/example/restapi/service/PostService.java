package com.example.restapi.service;

import com.example.restapi.model.Post;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.PostRepository;
import com.example.restapi.repository.ProfileRepository;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PostService {

    private PostRepository postRepository;
    private ProfileRepository profileRepository;

    @Autowired
    public PostService(PostRepository postRepository, ProfileRepository profileRepository) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new OpenApiResourceNotFoundException("Post id : "+id));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post createPost(Post post) {
        Profile profile = post.getAuthor();//profileRepository.findById(post.getAuthor().getId())
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.findById(id)
                .orElseThrow(()-> new OpenApiResourceNotFoundException("Post id : "+ id));
        postRepository.deleteById(id);
    }
}
