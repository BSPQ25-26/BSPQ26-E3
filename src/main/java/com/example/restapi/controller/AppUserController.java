package com.example.restapi.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.restapi.dto.AuthResponse;
import com.example.restapi.dto.LoginRequest;
import com.example.restapi.dto.RegisterRequest;
import com.example.restapi.model.Profile;
import com.example.restapi.service.AppUserService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Controller", description = "API for managing users via Supabase Auth")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<Profile> getAllUsers() {
        return appUserService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> getUserById(@PathVariable UUID id) {
        return appUserService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Profile> register(@RequestBody RegisterRequest req) {
        try {
            return ResponseEntity.ok(appUserService.register(req));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(appUserService.login(request.getEmail(), request.getPassword()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Profile> updateUser(@PathVariable UUID id, @RequestBody Profile userDetails) {
        try {
            return ResponseEntity.ok(appUserService.updateUser(id, userDetails));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        try {
            appUserService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
