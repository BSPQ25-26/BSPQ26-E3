package com.example.restapi.repository;

import com.example.restapi.dto.PostRequest;
import com.example.restapi.model.Item;
import com.example.restapi.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Item> findByAuthorId(java.util.UUID sellerId);
}
