package com.example.restapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.restapi.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
