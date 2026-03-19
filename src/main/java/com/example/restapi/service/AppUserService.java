package com.example.restapi.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.restapi.dto.AuthResponse;
import com.example.restapi.dto.RegisterRequest;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.ProfileRepository;

@Service
public class AppUserService {

    private static final Logger log = LoggerFactory.getLogger(AppUserService.class);

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    private final ProfileRepository profileRepository;
    private final RestTemplate restTemplate;

    public AppUserService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        this.restTemplate = new RestTemplate();
    }

    public Profile register(RegisterRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseAnonKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("email", req.getEmail());
        body.put("password", req.getPassword());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map<String, Object> response;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> r = restTemplate.postForObject(
                    supabaseUrl + "/auth/v1/signup", request, Map.class);
            response = r;
        } catch (HttpClientErrorException e) {
            log.error("Supabase signup error {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Supabase signup failed: " + e.getResponseBodyAsString());
        }

        log.info("Supabase signup response: {}", response);
        String userId = extractUserId(response);

        Profile profile = new Profile(UUID.fromString(userId), req.getUsername(), req.getPhone());
        return profileRepository.save(profile);
    }

    public AuthResponse login(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseAnonKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        Map<String, Object> response;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> r = restTemplate.postForObject(
                    supabaseUrl + "/auth/v1/token?grant_type=password", request, Map.class);
            response = r;
        } catch (HttpClientErrorException e) {
            log.error("Supabase login error {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Supabase login failed: " + e.getResponseBodyAsString());
        }

        String accessToken = (String) response.get("access_token");
        String tokenType = (String) response.get("token_type");
        Object expiresInRaw = response.get("expires_in");
        int expiresIn = expiresInRaw instanceof Number ? ((Number) expiresInRaw).intValue() : 3600;

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) response.get("user");
        UUID userId = UUID.fromString((String) userMap.get("id"));

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setTokenType(tokenType);
        authResponse.setExpiresIn(expiresIn);
        authResponse.setProfile(profile);
        return authResponse;
    }

    public List<Profile> getAllUsers() {
        return profileRepository.findAll();
    }

    public Optional<Profile> getUserById(UUID id) {
        return profileRepository.findById(id);
    }

    public Profile updateUser(UUID id, Profile userDetails) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
        profile.setUsername(userDetails.getUsername());
        return profileRepository.save(profile);
    }

    public void deleteUser(UUID id) {
        if (!profileRepository.existsById(id)) {
            throw new RuntimeException("Profile not found with id: " + id);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseServiceRoleKey);
        headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        restTemplate.exchange(
                supabaseUrl + "/auth/v1/admin/users/" + id,
                HttpMethod.DELETE,
                request,
                Void.class);
        // The profile is deleted automatically by the ON DELETE CASCADE constraint
    }

    @SuppressWarnings("unchecked")
    private String extractUserId(Map<String, Object> response) {
        // When email confirmation is disabled, user data is nested under "user"
        if (response.containsKey("user") && response.get("user") != null) {
            Map<String, Object> user = (Map<String, Object>) response.get("user");
            return (String) user.get("id");
        }
        // When email confirmation is enabled, the response IS the user object
        return (String) response.get("id");
    }
}
