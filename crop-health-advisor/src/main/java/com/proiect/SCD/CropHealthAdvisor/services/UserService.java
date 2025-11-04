// src/main/java/com/proiect/SCD/CropHealthAdvisor/services/UserService.java
package com.proiect.SCD.CropHealthAdvisor.services;

import com.proiect.SCD.CropHealthAdvisor.models.User;
import com.proiect.SCD.CropHealthAdvisor.models.Role;
import com.proiect.SCD.CropHealthAdvisor.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Creates a new user or registers an existing one.
     * Automatically sets role to USER if not specified.
     * TODO: Add password encryption (e.g., BCryptPasswordEncoder)
     */
    public User save(User user) {
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        return userRepository.save(user);
    }

    /**
     * Retrieves all users (for admin/debug purposes).
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Finds a user by email address.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Finds a user by ID.
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Updates an existing user.
     * Only updates non-null fields from updatedUser.
     * Password is only updated if provided and not empty.
     */
    public User update(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(updatedUser.getPassword());
            }
            return userRepository.save(user);
        }).orElse(null);
    }

    /**
     * Deletes a user by ID.
     * @return true if user was deleted, false if not found
     */
    public boolean deleteById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}