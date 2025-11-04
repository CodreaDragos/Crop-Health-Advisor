package com.proiect.SCD.CropHealthAdvisor.controllers;

import com.proiect.SCD.CropHealthAdvisor.dto.JwtAuthResponse;
import com.proiect.SCD.CropHealthAdvisor.dto.LoginRequest;
import com.proiect.SCD.CropHealthAdvisor.models.User;
import com.proiect.SCD.CropHealthAdvisor.jwt.JwtTokenProvider;
import com.proiect.SCD.CropHealthAdvisor.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    /**
     * Authenticates user and returns JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authentication using NoOpPasswordEncoder (no encryption)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate token
            String jwt = tokenProvider.generateToken(authentication);

            return ResponseEntity.ok(new JwtAuthResponse(jwt));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email sau parolă incorectă.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Eroare la autentificare: " + e.getMessage());
        }
    }

    /**
     * Registers a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody User user) {
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            return new ResponseEntity<>("Email-ul este deja folosit!", HttpStatus.BAD_REQUEST);
        }
        
        // Password is saved as plain text due to NoOpPasswordEncoder
        userService.save(user); 

        return new ResponseEntity<>("Utilizatorul s-a înregistrat cu succes!", HttpStatus.OK);
    }
}