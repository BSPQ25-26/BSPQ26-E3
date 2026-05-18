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

/**
 *
 * Service layer for user authentication and profile management.
 *
 * Acts as a remote facade that delegates identity operations to Supabase Auth
 * while keeping local profile data in the application database.
 */
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

    /**
     * Constructs AppUserService.
     * @param profileRepository Repository for local user profiles.
     */
    public AppUserService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Registers a new user in Supabase Auth and creates a local profile.
     * @param req Registration data (email, password, username, phone).
     * @return The persisted local Profile.
     * @throws RuntimeException if Supabase signup fails or the username is already taken.
     */
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

    /**
     * Authenticates a user against Supabase and returns session data.
     * @param email    User email.
     * @param password User password.
     * @return AuthResponse containing the access token and local profile.
     * @throws RuntimeException if credentials are invalid or the email is not confirmed.
     */
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

    /**
     * Requests Supabase to resend the email-confirmation message.
     * @param email Target email address.
     * @throws RuntimeException if the request fails.
     */
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

    /**
     * Retrieves a public profile by username.
     * @param email    Optional email filter (currently unused).
     * @param username The unique username to look up.
     * @return Optional containing the public UserProfileResponse.
     */
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

    /**
     * Returns every registered user profile.
     * @return List of Profile entities.
     */
    public List<Profile> getAllUsers() {
        return profileRepository.findAll();
    }

    /**
     * Retrieves a profile by its primary key.
     * @param id The profile UUID.
     * @return Optional containing the Profile, or empty if not found.
     */
    public Optional<Profile> getUserById(UUID id) {
        return profileRepository.findById(id);
    }

    /**
     * Applies a partial update to an existing profile.
     * @param id      UUID of the profile to update.
     * @param updates Map of field names to new values (supports "username", "phone").
     * @return The updated Profile entity.
     * @throws RuntimeException if the profile does not exist.
     */
    public Profile updatePartOfUser(UUID id, Map<String, Object> updates) {
        Profile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile not found with id: " + id));
        partialUpdate(profile, updates);
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

    /**
     * Triggers a password-reset email via Supabase.
     * @param email User email.
     * @throws RuntimeException if the request fails.
     */
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

    /**
     * Changes a user's password after verifying the current one.
     * @param userId          UUID of the user.
     * @param email           User email.
     * @param currentPassword The existing password.
     * @param newPassword     The desired new password.
     * @throws RuntimeException if the current password is invalid or the update fails.
     */
    public void changePassword(UUID userId, String email, String currentPassword, String newPassword) {
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

    /**
     * Deletes a user both from Supabase Auth and the local database.
     * @param id UUID of the user to remove.
     * @throws RuntimeException if the local profile is not found.
     */
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
        if (response.containsKey("user") && response.get("user") != null) {
            Map<String, Object> user = (Map<String, Object>) response.get("user");
            return (String) user.get("id");
        }
        return (String) response.get("id");
    }
}
