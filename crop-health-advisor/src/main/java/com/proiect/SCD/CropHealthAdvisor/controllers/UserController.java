// src/main/java/com/proiect/SCD/CropHealthAdvisor/controllers/UserController.java
package com.proiect.SCD.CropHealthAdvisor.controllers;

import com.proiect.SCD.CropHealthAdvisor.models.User;
import com.proiect.SCD.CropHealthAdvisor.services.UserService;
import jakarta.validation.Valid; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    /**
     * Retrieves all users (for admin purposes).
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    /**
     * Public user registration endpoint.
     */
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        try {
            User savedUser = userService.save(user);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED); 
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.CONFLICT); 
        }
    }
    
    /**
     * Creates a new user (admin only, requires authentication).
     */
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        try {
            if (userService.findByEmail(user.getEmail()).isPresent()) {
                return new ResponseEntity<>("User with this email already exists!", HttpStatus.CONFLICT);
            }
            
            User savedUser = userService.save(user);
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return new ResponseEntity<>("Email or username already exists in system!", HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating user: " + e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    /**
     * Retrieves user by email.
     * Must be before /{id} to avoid route conflict.
     */
    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing user.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        try {
            var existingUserOpt = userService.findById(id);
            if (!existingUserOpt.isPresent()) {
                return new ResponseEntity<>("User not found!", HttpStatus.NOT_FOUND);
            }
            
            var existingUserByEmail = userService.findByEmail(userDetails.getEmail());
            if (existingUserByEmail.isPresent() && !existingUserByEmail.get().getId().equals(id)) {
                return new ResponseEntity<>("User with this email already exists!", HttpStatus.CONFLICT);
            }
            
            var existingUser = existingUserOpt.get();
            if (userDetails.getPassword() == null || userDetails.getPassword().isEmpty()) {
                userDetails.setPassword(existingUser.getPassword());
            }
            
            User updatedUser = userService.update(id, userDetails);
            if (updatedUser != null) {
                return ResponseEntity.ok(updatedUser);
            }
            
            return new ResponseEntity<>("Error updating user!", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DataIntegrityViolationException e) {
            return new ResponseEntity<>("Email or username already exists in system!", HttpStatus.CONFLICT);
        } catch (Exception e) {
            System.out.println("ERROR updating user: " + e.getMessage());
            e.printStackTrace();
            return new ResponseEntity<>("Error updating user: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Exception handler for validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ResponseEntity<>("Validation error: " + errorMessage, HttpStatus.BAD_REQUEST);
    }

    /**
     * Deletes a user by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}