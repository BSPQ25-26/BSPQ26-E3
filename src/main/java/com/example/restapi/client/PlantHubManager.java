package com.example.restapi.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.restapi.model.PlantHub;

public class PlantHubManager {

    private String PlantHub_CONTROLLER_URL_TEMPLATE = "http://%s:%s/api/PlantHubs";
    private final String PlantHub_CONTROLLER_URL;
    private final RestTemplate restTemplate;

    public PlantHubManager(String hostname, String port) {
        PlantHub_CONTROLLER_URL = String.format(PlantHub_CONTROLLER_URL_TEMPLATE, hostname, port);
        this.restTemplate = new RestTemplate();
    }

    public void registerPlantHub(PlantHub PlantHub) {
        ResponseEntity<Void> response = restTemplate.postForEntity(PlantHub_CONTROLLER_URL, PlantHub, Void.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("PlantHub registered successfully.");
        } else {
            System.out.println("Failed to register PlantHub. Status code: " + response.getStatusCode());
        }
    }

    public List<PlantHub> getAllPlantHubs() {
        ResponseEntity<PlantHub[]> response = restTemplate.getForEntity(PlantHub_CONTROLLER_URL, PlantHub[].class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return List.of(response.getBody());
        } else {
            System.out.println("Failed to retrieve PlantHubs. Status code: " + response.getStatusCode());
            return List.of();
        }
    }

    public void deletePlantHub(String PlantHubId) {
        try {
            restTemplate.delete(PlantHub_CONTROLLER_URL + "/" + PlantHubId);
            System.out.println("PlantHub deleted successfully.");
        } catch (RestClientException e)
        {
            System.out.println("Failed to delete PlantHub. " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.exit(0);
        }

        String hostname = args[0];
        String port = args[1];

        PlantHubManager PlantHubManager = new PlantHubManager(hostname, port);
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        while (true) {
            System.out.println("1. Register PlantHub");
            System.out.println("2. List All PlantHubs");
            System.out.println("3. Delete PlantHub");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter PlantHub title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter PlantHub author: ");
                    String author = scanner.nextLine();
                    System.out.print("Enter PlantHub ISBN: ");
                    String isbn = scanner.nextLine();
                    PlantHub PlantHub = new PlantHub(title, author, isbn);
                    PlantHubManager.registerPlantHub(PlantHub);
                    break;
                case 2:
                    List<PlantHub> PlantHubs = PlantHubManager.getAllPlantHubs();
                    for (PlantHub b : PlantHubs) {
                        System.out.println("ID: " + b.getId());
                        System.out.println("Title: " + b.getTitle());
                        System.out.println("Author: " + b.getAuthor());
                        System.out.println("ISBN: " + b.getIsbn());
                        System.out.println("---------------------------");
                    }
                    break;
                case 3:
                    System.out.print("Enter PlantHub ID to delete: ");
                    String PlantHubId = scanner.nextLine();
                    PlantHubManager.deletePlantHub(PlantHubId);
                    break;
                case 4:
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
