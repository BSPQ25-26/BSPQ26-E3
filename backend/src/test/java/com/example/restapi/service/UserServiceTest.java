package com.example.restapi.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.restapi.dto.AuthResponse;
import com.example.restapi.dto.RegisterRequest;
import com.example.restapi.dto.UserProfileResponse;
import com.example.restapi.model.Profile;
import com.example.restapi.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@DisplayName("AppUserService Tests")
public class UserServiceTest {

    private static final Logger log = LoggerFactory.getLogger(UserServiceTest.class);

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private RestTemplate restTemplate;

    private AppUserService userService;
    private UUID testUserId;
    private Profile testProfile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new AppUserService(profileRepository);
        ReflectionTestUtils.setField(userService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(userService, "supabaseUrl", "http://localhost:3000");
        ReflectionTestUtils.setField(userService, "supabaseAnonKey", "test-anon-key");
        ReflectionTestUtils.setField(userService, "supabaseServiceRoleKey", "test-service-key");

        testUserId = UUID.randomUUID();
        testProfile = new Profile(testUserId, "testuser", "1234567890");
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("should return all users")
        void testGetAllUsers() {
            List<Profile> expectedProfiles = List.of(testProfile);
            when(profileRepository.findAll()).thenReturn(expectedProfiles);

            List<Profile> result = userService.getAllUsers();

            assertEquals(expectedProfiles, result);
            verify(profileRepository, times(1)).findAll();
            log.info("testGetAllUsers passed: returned {} user(s)", result.size());
        }

        @Test
        @DisplayName("should return empty list when no users exist")
        void testGetAllUsersEmpty() {
            when(profileRepository.findAll()).thenReturn(List.of());

            List<Profile> result = userService.getAllUsers();

            assertTrue(result.isEmpty());
            verify(profileRepository).findAll();
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return user when it exists")
        void testGetUserById() {
            when(profileRepository.findById(testUserId)).thenReturn(Optional.of(testProfile));

            Optional<Profile> result = userService.getUserById(testUserId);

            assertTrue(result.isPresent());
            assertEquals(testProfile, result.get());
            verify(profileRepository).findById(testUserId);
            log.info("testGetUserById passed: found user '{}'", result.get().getUsername());
        }

        @Test
        @DisplayName("should return empty when user does not exist")
        void testGetUserByIdNotFound() {
            when(profileRepository.findById(testUserId)).thenReturn(Optional.empty());

            Optional<Profile> result = userService.getUserById(testUserId);

            assertFalse(result.isPresent());
            verify(profileRepository).findById(testUserId);
        }
    }

    @Nested
    @DisplayName("updatePartOfUser")
    class UpdateUserTests {

        @Test
        @DisplayName("should update user successfully")
        void testUpdateUserSuccess() {
            Profile updatedProfile = new Profile(testUserId, "newusername", "9876543210");
            Map<String, Object> updates = new HashMap<>();
            updates.put("username", "newusername");
            updates.put("phone", "9876543210");

            when(profileRepository.findById(testUserId)).thenReturn(Optional.of(testProfile));
            when(profileRepository.save(any(Profile.class))).thenReturn(updatedProfile);

            Profile result = userService.updatePartOfUser(testUserId, updates);

            assertEquals("newusername", result.getUsername());
            verify(profileRepository).findById(testUserId);
            verify(profileRepository, atLeastOnce()).save(any(Profile.class));
            log.info("testUpdateUserSuccess passed: updated username='{}'", result.getUsername());
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void testUpdateUserNotFound() {
            Map<String, Object> updates = new HashMap<>();
            updates.put("username", "newusername");
            when(profileRepository.findById(testUserId)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () ->
                userService.updatePartOfUser(testUserId, updates)
            );
            verify(profileRepository).findById(testUserId);
            verify(profileRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getDisplayProfile")
    class GetDisplayProfileTests {

        @Test
        @DisplayName("should return display profile for valid username")
        void testGetDisplayProfileByUsername() {
            when(profileRepository.findByUsername("testuser")).thenReturn(Optional.of(testProfile));

            Optional<UserProfileResponse> result = userService.getDisplayProfile(null, "testuser");

            assertTrue(result.isPresent());
            assertEquals("testuser", result.get().getUsername());
            verify(profileRepository).findByUsername("testuser");
            log.info("testGetDisplayProfileByUsername passed: found profile '{}'", result.get().getUsername());
        }

        @Test
        @DisplayName("should return empty when username not found")
        void testGetDisplayProfileNotFound() {
            when(profileRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            Optional<UserProfileResponse> result = userService.getDisplayProfile(null, "nonexistent");

            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("should return empty for blank username")
        void testGetDisplayProfileBlankUsername() {
            Optional<UserProfileResponse> result = userService.getDisplayProfile(null, "");

            assertFalse(result.isPresent());
            verify(profileRepository, never()).findByUsername(anyString());
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("should delete user successfully")
        void testDeleteUserSuccess() {
            when(profileRepository.existsById(testUserId)).thenReturn(true);

            assertDoesNotThrow(() -> userService.deleteUser(testUserId));
            verify(profileRepository).existsById(testUserId);
            log.info("testDeleteUserSuccess passed: user {} deleted", testUserId);
        }

        @Test
        @DisplayName("should throw exception when user not found")
        void testDeleteUserNotFound() {
            when(profileRepository.existsById(testUserId)).thenReturn(false);

            assertThrows(RuntimeException.class, () -> userService.deleteUser(testUserId));
            verify(profileRepository).existsById(testUserId);
        }
    }
}
