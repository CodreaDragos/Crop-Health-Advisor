package com.proiect.SCD.CropHealthAdvisor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "reports")
public class Reports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @DecimalMin(value = "-1.0", message = "NDVI cannot be below -1.0")
    @DecimalMax(value = "1.0", message = "NDVI cannot exceed 1.0")
    private Double ndviValue;
    
    @DecimalMin(value = "-50.0", message = "Temperature cannot be below -50°C")
    @DecimalMax(value = "60.0", message = "Temperature cannot exceed 60°C")
    private Double temperatureValue;
    
    @PositiveOrZero(message = "Precipitation cannot be negative")
    private Double precipitationValue;

    @Column(columnDefinition = "TEXT")
    private String aiInterpretation;

    @NotNull(message = "Report date is required")
    private LocalDateTime reportDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    @JsonIgnoreProperties({"reports"}) // Prevents circular reference
    private Location location;
    
    // Additional metrics not stored in DB but included in JSON response
    // These are set programmatically before serialization
    @Transient
    @JsonProperty("eviValue")
    private Double eviValue;
    
    @Transient
    @JsonProperty("ndwiValue")
    private Double ndwiValue;
    
    @Transient
    @JsonProperty("soilMoisture")
    private Double soilMoisture;
    
    @Transient
    @JsonProperty("cloudCover")
    private Double cloudCover;
    
    @Transient
    @JsonProperty("evapotranspiration")
    private Double evapotranspiration;
}