package com.example.restapi.controller;

import com.example.restapi.model.PlantHub;
import com.example.restapi.service.PlantHubService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/PlantHubs")
@Tag(name = "PlantHub Controller", description = "API for managing PlantHubs")
public class PlantHubController {

    @Autowired
    private PlantHubService PlantHubService;

    @GetMapping
    public List<PlantHub> getAllPlantHubs() {
        return PlantHubService.getAllPlantHubs();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantHub> getPlantHubById(@PathVariable Long id) {
        Optional<PlantHub> PlantHub = PlantHubService.getPlantHubById(id);
        return PlantHub.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PlantHub> createPlantHub(@RequestBody PlantHub PlantHub) {
        try {
            PlantHub createdPlantHub = PlantHubService.createPlantHub(PlantHub);
            return ResponseEntity.ok(createdPlantHub);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<PlantHub> updatePlantHub(@PathVariable Long id, @RequestBody PlantHub PlantHubDetails) {
        try {
            PlantHub updatedPlantHub = PlantHubService.updatePlantHub(id, PlantHubDetails);
            return ResponseEntity.ok(updatedPlantHub);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlantHub(@PathVariable Long id) {
        Optional<PlantHub> PlantHub = PlantHubService.getPlantHubById(id);
        if (PlantHub.isPresent()) {
            PlantHubService.deletePlantHub(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
