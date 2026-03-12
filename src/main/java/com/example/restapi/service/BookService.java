package com.example.restapi.service;

import com.example.restapi.model.PlantHub;
import com.example.restapi.repository.PlantHubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlantHubService {

    private final PlantHubRepository PlantHubRepository;

    @Autowired
    public PlantHubService(PlantHubRepository PlantHubRepository) {
        this.PlantHubRepository = PlantHubRepository;
    }

    public List<PlantHub> getAllPlantHubs() {
        return PlantHubRepository.findAll();
    }

    public Optional<PlantHub> getPlantHubById(Long id) {
        return PlantHubRepository.findById(id);
    }

    public PlantHub createPlantHub(PlantHub PlantHub) {
        return PlantHubRepository.save(PlantHub);
    }

    public PlantHub updatePlantHub(Long id, PlantHub PlantHubDetails) {
        Optional<PlantHub> optionalPlantHub = PlantHubRepository.findById(id);
        if (optionalPlantHub.isPresent()) {
            PlantHub PlantHub = optionalPlantHub.get();
            PlantHub.setTitle(PlantHubDetails.getTitle());
            PlantHub.setAuthor(PlantHubDetails.getAuthor());
            return PlantHubRepository.save(PlantHub);
        } else {
            throw new RuntimeException("PlantHub not found");
        }
    }

    public void deletePlantHub(Long id) {
        if (PlantHubRepository.existsById(id)) {
            PlantHubRepository.deleteById(id);
        } else {
            throw new RuntimeException("PlantHub not found with id: " + id);
        }
    }
}