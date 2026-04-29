package com.example.restapi.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.example.restapi.dto.RegisterRequest;
import com.example.restapi.model.Profile;
import com.example.restapi.service.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Integration test: the real AppUserManager client makes HTTP calls to the
 * embedded Spring Boot server started on a random port.  AppUserService is
 * replaced by a Mockito mock so no Supabase connection is needed.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("AppUserManager Integration Tests (client → server)")
class AppUserManagerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AppUserManagerIntegrationTest.class);

    @LocalServerPort
    private int port;

    @MockitoBean
    private AppUserService appUserService;

    private AppUserManager appUserManager;

    @BeforeEach
    void setUp() {
        appUserManager = new AppUserManager("localhost", String.valueOf(port));
    }

    @Test
    @DisplayName("getAllUsers — client receives list of users from real HTTP server")
    void getAllUsers_clientReceivesUsersFromServer() {
        Profile p1 = new Profile(UUID.randomUUID(), "alice", "111000111");
        Profile p2 = new Profile(UUID.randomUUID(), "bob", "222000222");
        when(appUserService.getAllUsers()).thenReturn(List.of(p1, p2));

        List<Profile> result = appUserManager.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).getUsername());
        assertEquals("bob", result.get(1).getUsername());
        log.info("Integration: getAllUsers returned {} user(s) from server on port {}", result.size(), port);
    }

    @Test
    @DisplayName("getAllUsers — client handles empty user list from real HTTP server")
    void getAllUsers_clientHandlesEmptyListFromServer() {
        when(appUserService.getAllUsers()).thenReturn(List.of());

        List<Profile> result = appUserManager.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        log.info("Integration: getAllUsers returned empty list from server on port {}", port);
    }

    @Test
    @DisplayName("registerUser — client receives 400 when service throws and does not crash")
    void registerUser_clientReceivesBadRequest_whenServiceThrows() {
        when(appUserService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Username already taken"));

        RegisterRequest req = new RegisterRequest();
        req.setUsername("taken");
        req.setEmail("taken@example.com");
        req.setPassword("pass");

        // Server maps the RuntimeException to 400 Bad Request.
        // RestTemplate throws HttpClientErrorException which is not caught by registerUser.
        assertThrows(Exception.class, () -> appUserManager.registerUser(req));
        log.info("Integration: registerUser correctly propagated 400 from server on port {}", port);
    }

    @Test
    @DisplayName("deleteUser — client handles 404 gracefully via RestClientException catch")
    void deleteUser_clientHandlesNotFoundGracefully() {
        // No user exists in the mock service; controller returns 404
        doThrow(new RuntimeException("Profile not found"))
                .when(appUserService).deleteUser(any(UUID.class));

        String randomId = UUID.randomUUID().toString();

        // deleteUser catches RestClientException internally — must not propagate
        assertDoesNotThrow(() -> appUserManager.deleteUser(randomId));
        log.info("Integration: deleteUser handled 404 gracefully on port {}", port);
    }
}
