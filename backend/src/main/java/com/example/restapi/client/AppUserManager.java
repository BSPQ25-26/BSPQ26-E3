package com.example.restapi.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.restapi.dto.RegisterRequest;
import com.example.restapi.model.Profile;

public class AppUserManager {

    private static final Logger log = LoggerFactory.getLogger(AppUserManager.class);
    private static final String USER_CONTROLLER_URL_TEMPLATE = "http://%s:%s/api/users";
    private final String userControllerUrl;
    private final RestTemplate restTemplate;

    public AppUserManager(String hostname, String port) {
        this.userControllerUrl = String.format(USER_CONTROLLER_URL_TEMPLATE, hostname, port);
        this.restTemplate = new RestTemplate();
    }

    public void registerUser(RegisterRequest req) {
        ResponseEntity<Profile> response = restTemplate.postForEntity(userControllerUrl, req, Profile.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("User registered successfully.");
        } else {
            log.warn("Failed to register user. Status code: {}", response.getStatusCode());
        }
    }

    public List<Profile> getAllUsers() {
        ResponseEntity<Profile[]> response = restTemplate.getForEntity(userControllerUrl, Profile[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return List.of(response.getBody());
        }

        log.warn("Failed to retrieve users. Status code: {}", response.getStatusCode());
        return List.of();
    }

    public void deleteUser(String userId) {
        try {
            restTemplate.delete(userControllerUrl + "/" + userId);
            log.info("User deleted successfully.");
        } catch (RestClientException exception) {
            log.error("Failed to delete user: {}", exception.getMessage());
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

        Logger cliLog = LoggerFactory.getLogger(AppUserManager.class);
        while (true) {
            cliLog.info("1. Register user");
            cliLog.info("2. List users");
            cliLog.info("3. Delete user");
            cliLog.info("4. Exit");
            cliLog.info("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    cliLog.info("Enter username: ");
                    String username = scanner.nextLine();
                    cliLog.info("Enter phone: ");
                    String phone = scanner.nextLine();
                    cliLog.info("Enter email: ");
                    String email = scanner.nextLine();
                    cliLog.info("Enter password: ");
                    String password = scanner.nextLine();
                    RegisterRequest req = new RegisterRequest();
                    req.setUsername(username);
                    req.setPhone(phone);
                    req.setEmail(email);
                    req.setPassword(password);
                    appUserManager.registerUser(req);
                    break;
                case 2:
                    List<Profile> users = appUserManager.getAllUsers();
                    for (Profile user : users) {
                        cliLog.info("ID: {}", user.getId());
                        cliLog.info("Username: {}", user.getUsername());
                        cliLog.info("Created at: {}", user.getCreatedAt());
                        cliLog.info("---------------------------");
                    }
                    break;
                case 3:
                    cliLog.info("Enter user ID to delete: ");
                    String userId = scanner.nextLine();
                    appUserManager.deleteUser(userId);
                    break;
                case 4:
                    scanner.close();
                    System.exit(0);
                default:
                    cliLog.warn("Invalid choice. Please try again.");
            }
        }
    }
}
