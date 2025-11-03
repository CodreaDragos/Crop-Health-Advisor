package com.proiect.SCD.CropHealthAdvisor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @NotNull(message = "Latitudinea este obligatorie")
    @DecimalMin(value = "-90.0", message = "Latitudinea minima este -90")
    @DecimalMax(value = "90.0", message = "Latitudinea maxima este 90")
    private Double latitude;

    @NotNull(message = "Longitudinea este obligatorie")
    @DecimalMin(value = "-180.0", message = "Longitudinea minima este -180")
    @DecimalMax(value = "180.0", message = "Longitudinea maxima este 180")
    private Double longitude;

    @NotBlank(message = "Numele locatiei este obligatoriu")
    @Size(min = 3, max = 255, message = "Numele trebuie sa aiba intre 3 si 255 de caractere")
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) // Locatia trebuie sa apartina unui user
    @JsonBackReference
    private User user;
    
    // Lista de rapoarte asociate
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"location"}) // Evită serializarea circulară - ignoră location în Reports
    private List<Reports> reports;
}