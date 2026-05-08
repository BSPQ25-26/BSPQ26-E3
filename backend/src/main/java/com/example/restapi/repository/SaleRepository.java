package com.example.restapi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.restapi.model.Sale;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findBySellerId(UUID sellerId);
}
