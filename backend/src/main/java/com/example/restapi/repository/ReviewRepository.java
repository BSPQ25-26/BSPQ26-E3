package com.example.restapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.restapi.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByItem_IdOrderByCreatedAtDescIdDesc(Long itemId);
}
