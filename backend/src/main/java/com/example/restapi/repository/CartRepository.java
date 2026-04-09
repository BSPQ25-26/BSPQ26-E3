package com.example.restapi.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.restapi.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByOwnerId(UUID ownerId);
}
