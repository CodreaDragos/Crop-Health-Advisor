// src/main/java/com/proiect/SCD/CropHealthAdvisor/services/UserService.java
package com.proiect.SCD.CropHealthAdvisor.services;

import com.proiect.SCD.CropHealthAdvisor.models.User;
import com.proiect.SCD.CropHealthAdvisor.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // CREATE (Inregistrare)
    public User save(User user) {
        // Aici vei adauga logica de criptare a parolei in viitor (ex: BCryptPasswordEncoder)
        return userRepository.save(user);
    }

    // READ (Citeste toti utilizatorii - pentru debug/admin)
    public List<User> findAll() {
        return userRepository.findAll();
    }
    //Gaseste dupa email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // READ (Citeste dupa ID)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // UPDATE (Actualizare)
    public User update(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            return userRepository.save(user);
        }).orElse(null);
    }

    // DELETE (Stergere cont)
    public boolean deleteById(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}