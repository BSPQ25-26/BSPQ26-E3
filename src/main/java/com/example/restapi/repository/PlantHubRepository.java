package com.example.restapi.repository;
import com.example.restapi.model.PlantHub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface PlantHubRepository extends JpaRepository<PlantHub, Long> {
}