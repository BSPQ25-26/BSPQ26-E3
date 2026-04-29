package com.example.restapi.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.List;
import java.util.UUID;

import com.example.restapi.dto.RegisterRequest;
import com.example.restapi.model.Profile;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@DisplayName("AppUserManager Unit Tests")
class AppUserManagerTest {

    private static final Logger log = LoggerFactory.getLogger(AppUserManagerTest.class);

    private AppUserManager appUserManager;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        appUserManager = new AppUserManager("localhost", "8080");
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(appUserManager, "restTemplate");
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    // ── getAllUsers ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        @Test
        @DisplayName("returns list of users when server responds 200")
        void getAllUsers_returnsUsers_onSuccess() throws Exception {
            Profile profile = new Profile(UUID.randomUUID(), "alice", "111222333");
            String json = objectMapper.writeValueAsString(new Profile[]{profile});

            mockServer.expect(requestTo("http://localhost:8080/api/users"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

            List<Profile> result = appUserManager.getAllUsers();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("alice", result.get(0).getUsername());
            mockServer.verify();
            log.info("getAllUsers_returnsUsers_onSuccess: got {} user(s)", result.size());
        }

        @Test
        @DisplayName("returns empty list when server returns empty array")
        void getAllUsers_returnsEmptyList_whenNoUsers() throws Exception {
            mockServer.expect(requestTo("http://localhost:8080/api/users"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

            List<Profile> result = appUserManager.getAllUsers();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            mockServer.verify();
        }
    }

    // ── registerUser ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("registerUser")
    class RegisterUserTests {

        @Test
        @DisplayName("completes without exception when server returns 200")
        void registerUser_success() throws Exception {
            Profile created = new Profile(UUID.randomUUID(), "bob", "999888777");
            String json = objectMapper.writeValueAsString(created);

            mockServer.expect(requestTo("http://localhost:8080/api/users"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

            RegisterRequest req = new RegisterRequest();
            req.setUsername("bob");
            req.setPhone("999888777");
            req.setEmail("bob@example.com");
            req.setPassword("secret");

            assertDoesNotThrow(() -> appUserManager.registerUser(req));
            mockServer.verify();
            log.info("registerUser_success passed");
        }

        @Test
        @DisplayName("propagates exception when server returns 400")
        void registerUser_throwsException_onBadRequest() {
            mockServer.expect(requestTo("http://localhost:8080/api/users"))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withBadRequest());

            RegisterRequest req = new RegisterRequest();
            req.setUsername("duplicate");
            req.setEmail("dup@example.com");
            req.setPassword("pass");

            // RestTemplate throws HttpClientErrorException for 4xx — not caught by registerUser
            assertThrows(Exception.class, () -> appUserManager.registerUser(req));
            mockServer.verify();
        }
    }

    // ── deleteUser ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("completes without exception when server returns 204")
        void deleteUser_success() {
            String userId = UUID.randomUUID().toString();

            mockServer.expect(requestTo("http://localhost:8080/api/users/" + userId))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withNoContent());

            assertDoesNotThrow(() -> appUserManager.deleteUser(userId));
            mockServer.verify();
            log.info("deleteUser_success passed for userId={}", userId);
        }

        @Test
        @DisplayName("handles 404 gracefully — RestClientException is caught internally")
        void deleteUser_handlesNotFound_gracefully() {
            String userId = UUID.randomUUID().toString();

            mockServer.expect(requestTo("http://localhost:8080/api/users/" + userId))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

            // deleteUser catches RestClientException — must not propagate
            assertDoesNotThrow(() -> appUserManager.deleteUser(userId));
            mockServer.verify();
        }

        @Test
        @DisplayName("handles 500 server error gracefully")
        void deleteUser_handlesServerError_gracefully() {
            String userId = UUID.randomUUID().toString();

            mockServer.expect(requestTo("http://localhost:8080/api/users/" + userId))
                    .andExpect(method(HttpMethod.DELETE))
                    .andRespond(withServerError());

            assertDoesNotThrow(() -> appUserManager.deleteUser(userId));
            mockServer.verify();
        }
    }
}
