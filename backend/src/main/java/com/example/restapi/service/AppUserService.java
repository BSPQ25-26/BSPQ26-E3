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
import com.example.restapi.dto.UserProfileResponse;
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

        if (profileRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already taken: " + req.getUsername());
        }

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
            String errorBody = e.getResponseBodyAsString();
            if (errorBody.contains("email_not_confirmed")) {
                throw new RuntimeException("EMAIL_NOT_CONFIRMED");
            }
            throw new RuntimeException("Supabase login failed: " + errorBody);
        }

        String accessToken = (String) response.get("access_token");
        String tokenType = (String) response.get("token_type");
        Object expiresInRaw = response.get("expires_in");
        int expiresIn = expiresInRaw instanceof Number ? ((Number) expiresInRaw).intValue() : 3600;

        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) response.get("user");
        UUID userId = UUID.fromString((String) userMap.get("id"));
        String userEmail = (String) userMap.get("email");

        Profile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setTokenType(tokenType);
        authResponse.setExpiresIn(expiresIn);
        authResponse.setEmail(userEmail);
        authResponse.setProfile(profile);
        return authResponse;
    }

    public void resendConfirmation(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseAnonKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("type", "signup");
        body.put("email", email);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForObject(supabaseUrl + "/auth/v1/resend", request, Map.class);
        } catch (HttpClientErrorException e) {
            log.error("Supabase resend error {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to resend confirmation email: " + e.getResponseBodyAsString());
        }
    }

    public Optional<UserProfileResponse> getDisplayProfile(String email, String username) {
        Optional<Profile> profileOpt = Optional.empty();

        if (username != null && !username.isBlank()) {
            profileOpt = profileRepository.findByUsername(username);
        }

        return profileOpt.map(profile -> {
            UserProfileResponse dto = new UserProfileResponse();
            dto.setUsername(profile.getUsername());
            dto.setPhone(profile.getPhone());
            dto.setCreatedAt(profile.getCreatedAt());
            return dto;
        });
    }

    public List<Profile> getAllUsers() {
        return profileRepository.findAll();
    }

    public Optional<Profile> getUserById(UUID id) {
        return profileRepository.findById(id);
    }

    public Profile updatePartOfUser(UUID id, Map<String, Object> updates) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
        partialUpdate(profile ,updates);
        return profileRepository.save(profile);
    }

    private void partialUpdate(Profile userDetails, Map<String, Object> updates){
        if(updates.containsKey("username")){
            userDetails.setUsername((String) updates.get("username"));
        }

        if(updates.containsKey("phone")){
            userDetails.setPhone((String) updates.get("phone"));
        }
        profileRepository.save(userDetails);
    }

    public void resetPassword(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseAnonKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.postForObject(supabaseUrl + "/auth/v1/recover", request, Map.class);
        } catch (HttpClientErrorException e) {
            log.error("Supabase recover error {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send reset email: " + e.getResponseBodyAsString());
        }
    }

    public void changePassword(UUID userId, String email, String currentPassword, String newPassword) {
        // Verify current password
        HttpHeaders verifyHeaders = new HttpHeaders();
        verifyHeaders.set("apikey", supabaseAnonKey);
        verifyHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> loginBody = new HashMap<>();
        loginBody.put("email", email);
        loginBody.put("password", currentPassword);

        HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(loginBody, verifyHeaders);
        try {
            restTemplate.postForObject(
                    supabaseUrl + "/auth/v1/token?grant_type=password", loginRequest, Map.class);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("INVALID_CURRENT_PASSWORD");
        }

        // Update password via admin API
        HttpHeaders adminHeaders = new HttpHeaders();
        adminHeaders.set("apikey", supabaseServiceRoleKey);
        adminHeaders.set("Authorization", "Bearer " + supabaseServiceRoleKey);
        adminHeaders.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> updateBody = new HashMap<>();
        updateBody.put("password", newPassword);

        HttpEntity<Map<String, String>> updateRequest = new HttpEntity<>(updateBody, adminHeaders);
        try {
            restTemplate.exchange(
                    supabaseUrl + "/auth/v1/admin/users/" + userId,
                    HttpMethod.PUT,
                    updateRequest,
                    Map.class);
        } catch (HttpClientErrorException e) {
            log.error("Supabase password update error {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to update password: " + e.getResponseBodyAsString());
        }
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
