package com.example.restapi.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.restapi.model.Item;
    

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByCategoryId(Long categoryId);
    List<Item> findBySellerId(java.util.UUID sellerId);
    List<Item> findByStatusTrue();
}

