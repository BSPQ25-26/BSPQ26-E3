package com.example.restapi.repository;

import com.example.restapi.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthor_Id(UUID authorId);

    List<Post> findAllByOrderByCreatedAtDescIdDesc();
}
