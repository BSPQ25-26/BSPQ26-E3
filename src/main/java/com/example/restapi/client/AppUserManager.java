package com.example.restapi.client;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.restapi.model.AppUser;

public class AppUserManager {

    private static final String USER_CONTROLLER_URL_TEMPLATE = "http://%s:%s/api/users";
    private final String userControllerUrl;
    private final RestTemplate restTemplate;

    public AppUserManager(String hostname, String port) {
        this.userControllerUrl = String.format(USER_CONTROLLER_URL_TEMPLATE, hostname, port);
        this.restTemplate = new RestTemplate();
    }

    public void registerUser(AppUser user) {
        ResponseEntity<AppUser> response = restTemplate.postForEntity(userControllerUrl, user, AppUser.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("User registered successfully.");
        } else {
            System.out.println("Failed to register user. Status code: " + response.getStatusCode());
        }
    }

    public List<AppUser> getAllUsers() {
        ResponseEntity<AppUser[]> response = restTemplate.getForEntity(userControllerUrl, AppUser[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return List.of(response.getBody());
        }

        System.out.println("Failed to retrieve users. Status code: " + response.getStatusCode());
        return List.of();
    }

    public void deleteUser(String userId) {
        try {
            restTemplate.delete(userControllerUrl + "/" + userId);
            System.out.println("User deleted successfully.");
        } catch (RestClientException exception) {
            System.out.println("Failed to delete user. " + exception.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.exit(0);
        }

        String hostname = args[0];
        String port = args[1];

        AppUserManager appUserManager = new AppUserManager(hostname, port);
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        while (true) {
            System.out.println("1. Register user");
            System.out.println("2. List users");
            System.out.println("3. Delete user");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter phone: ");
                    String phone = scanner.nextLine();
                    System.out.print("Enter mail: ");
                    String mail = scanner.nextLine();
                    appUserManager.registerUser(new AppUser(phone, mail));
                    break;
                case 2:
                    List<AppUser> users = appUserManager.getAllUsers();
                    for (AppUser user : users) {
                        System.out.println("ID: " + user.getId());
                        System.out.println("Created at: " + user.getCreatedAt());
                        System.out.println("Phone: " + user.getPhone());
                        System.out.println("Mail: " + user.getMail());
                        System.out.println("---------------------------");
                    }
                    break;
                case 3:
                    System.out.print("Enter user ID to delete: ");
                    String userId = scanner.nextLine();
                    appUserManager.deleteUser(userId);
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
