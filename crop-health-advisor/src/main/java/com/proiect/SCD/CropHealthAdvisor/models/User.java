package com.proiect.SCD.CropHealthAdvisor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email; 
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size; 
import lombok.Data;
import lombok.NoArgsConstructor; 
import lombok.AllArgsConstructor; 
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor 
@AllArgsConstructor 
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username-ul este obligatoriu")
    @Size(min = 3, max = 45, message = "Username-ul trebuie sa aiba intre 3 si 45 de caractere")
    private String username;

    @NotBlank(message = "Email-ul este obligatoriu")
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
           message = "Formatul email-ului este invalid (ex: a@b.c)")

    @Column(unique = true)
    private String email;

    @NotBlank(message = "Parola este obligatorie")
    @Size(min = 8, message = "Parola trebuie sa aiba minim 8 caractere")
    private String password;

    // Lista de locatii asociate utilizatorului
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Location> locations;
}