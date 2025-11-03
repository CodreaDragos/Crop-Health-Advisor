// src/main/java/com/proiect/SCD/CropHealthAdvisor/controllers/UserController.java
package com.proiect.SCD.CropHealthAdvisor.controllers;

import com.proiect.SCD.CropHealthAdvisor.models.User;
import com.proiect.SCD.CropHealthAdvisor.services.UserService;
import jakarta.validation.Valid; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. CREATE (Inregistrare / Sign-Up)
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
        // @Valid forteaza validarea regulilor @NotBlank, @Email etc.
        try {
            User savedUser = userService.save(user);
            // Returneaza 201 Created
            return new ResponseEntity<>(savedUser, HttpStatus.CREATED); 
        } catch (Exception e) {
            // In cazul in care email-ul nu este unic (eroare SQL)
            return new ResponseEntity<>(null, HttpStatus.CONFLICT); 
        }
    }

    // 2.5. READ (Citeste dupa Email) - TREBUIE SA FIE INAINTE DE /{id} pentru a evita conflictul
    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 2. READ (Citeste dupa ID)
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok) // Daca gaseste, returneaza 200 OK
                .orElse(ResponseEntity.notFound().build()); // Daca nu gaseste, returneaza 404 Not Found
    }

    // 3. UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        User updatedUser = userService.update(id, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        }
        return ResponseEntity.notFound().build();
    }

    // 4. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.deleteById(id)) {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
        return ResponseEntity.notFound().build(); // 404 Not Found
    }
}