package com.example.restapi.service;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.restapi.dto.UserProfileResponse;
import com.example.restapi.model.AppUser;
import com.example.restapi.repository.AppUserRepository;

@Service
public class AppUserService {

    private static final String DISPLAY_PROFILE_QUERY = """
            select
                coalesce(
                    nullif(au.raw_user_meta_data ->> 'username', ''),
                    nullif(to_jsonb(p) ->> 'username', ''),
                    :fallbackUsername
                ) as username,
                coalesce(
                    nullif(au.email, ''),
                    nullif(to_jsonb(p) ->> 'email', ''),
                    :fallbackEmail
                ) as email,
                coalesce(
                    nullif(au.phone, ''),
                    nullif(to_jsonb(p) ->> 'phone', ''),
                    :fallbackPhone
                ) as phone,
                au.created_at as created_at
            from auth.users au
            left join public.profiles p on p.id = au.id
            where lower(au.email) = lower(:email)
            limit 1
            """;

    private final AppUserRepository appUserRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AppUserService(AppUserRepository appUserRepository, NamedParameterJdbcTemplate jdbcTemplate) {
        this.appUserRepository = appUserRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }

    public AppUser createUser(AppUser user) {
        appUserRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            throw new RuntimeException("A user with this email already exists");
        });

        appUserRepository.findByUsername(user.getUsername()).ifPresent(existingUser -> {
            throw new RuntimeException("A user with this username already exists");
        });

        return appUserRepository.save(user);
    }

    public Optional<AppUser> login(String username, String password) {
        return appUserRepository.findByUsernameAndPassword(username, password);
    }

    public Optional<UserProfileResponse> getDisplayProfile(String email, String username) {
        Optional<AppUser> fallbackUser = findFallbackUser(email, username);

        if (email != null && !email.isBlank()) {
            try {
                MapSqlParameterSource parameters = new MapSqlParameterSource()
                        .addValue("email", email)
                        .addValue("fallbackUsername", fallbackUser.map(AppUser::getUsername).orElse(username))
                        .addValue("fallbackEmail", fallbackUser.map(AppUser::getEmail).orElse(email))
                        .addValue("fallbackPhone", fallbackUser.map(AppUser::getPhone).orElse(null));

                List<UserProfileResponse> results = jdbcTemplate.query(DISPLAY_PROFILE_QUERY, parameters,
                        (resultSet, rowNum) -> mapUserProfile(resultSet.getString("username"),
                                resultSet.getString("email"),
                                resultSet.getString("phone"),
                                toOffsetDateTime(resultSet.getObject("created_at"))));

                if (!results.isEmpty()) {
                    return Optional.of(results.get(0));
                }
            } catch (DataAccessException exception) {
                // Fallback to the application user table if auth/profiles cannot be queried.
            }
        }

        return fallbackUser.map(this::mapAppUserProfile);
    }

    public AppUser updateUser(Long id, AppUser userDetails) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setPhone(userDetails.getPhone());
        user.setEmail(userDetails.getEmail());
        user.setUsername(userDetails.getUsername());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }
        return appUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }

        appUserRepository.deleteById(id);
    }

    private Optional<AppUser> findFallbackUser(String email, String username) {
        if (email != null && !email.isBlank()) {
            Optional<AppUser> userByEmail = appUserRepository.findByEmail(email);
            if (userByEmail.isPresent()) {
                return userByEmail;
            }
        }

        if (username != null && !username.isBlank()) {
            return appUserRepository.findByUsername(username);
        }

        return Optional.empty();
    }

    private UserProfileResponse mapAppUserProfile(AppUser appUser) {
        return mapUserProfile(appUser.getUsername(), appUser.getEmail(), appUser.getPhone(), appUser.getCreatedAt());
    }

    private UserProfileResponse mapUserProfile(String username, String email, String phone, OffsetDateTime createdAt) {
        UserProfileResponse response = new UserProfileResponse();
        response.setUsername(username);
        response.setEmail(email);
        response.setPhone(phone);
        response.setCreatedAt(createdAt);
        return response;
    }

    private OffsetDateTime toOffsetDateTime(Object value) {
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime;
        }

        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant().atOffset(OffsetDateTime.now().getOffset());
        }

        return null;
    }
}
