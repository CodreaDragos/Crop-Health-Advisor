package com.proiect.SCD.CropHealthAdvisor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT")
    private Long id;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Minimum latitude is -90")
    @DecimalMax(value = "90.0", message = "Maximum latitude is 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Minimum longitude is -180")
    @DecimalMax(value = "180.0", message = "Maximum longitude is 180")
    private Double longitude;

    @NotBlank(message = "Location name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"locations", "password"}) // Prevents circular reference and excludes password
    private User user;
    
    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"location"}) // Prevents circular serialization
    private List<Reports> reports;
}