package com.example.restapi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.restapi.model.AppUser;
import com.example.restapi.repository.AppUserRepository;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<AppUser> getAllUsers() {
        return appUserRepository.findAll();
    }

    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }

    public AppUser createUser(AppUser user) {
        appUserRepository.findByMail(user.getMail()).ifPresent(existingUser -> {
            throw new RuntimeException("A user with this email already exists");
        });

        return appUserRepository.save(user);
    }

    public Optional<AppUser> login(String mail, String phone) {
        return appUserRepository.findByMailAndPhone(mail, phone);
    }

    public AppUser updateUser(Long id, AppUser userDetails) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setPhone(userDetails.getPhone());
        user.setMail(userDetails.getMail());
        return appUserRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!appUserRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }

        appUserRepository.deleteById(id);
    }
}
