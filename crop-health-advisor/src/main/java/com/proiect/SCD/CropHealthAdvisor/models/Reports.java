package com.proiect.SCD.CropHealthAdvisor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
@Table(name = "reports")
public class Reports {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // NDVI trebuie sa fie intre -1 si 1
    @DecimalMin(value = "-1.0", message = "NDVI nu poate fi sub -1.0")
    @DecimalMax(value = "1.0", message = "NDVI nu poate fi peste 1.0")
    private Double ndviValue;
    
    // Temperatura, de exemplu, sa nu fie sub -50 si peste 60 de grade C
    @DecimalMin(value = "-50.0", message = "Temperatura nu poate fi sub -50°C")
    @DecimalMax(value = "60.0", message = "Temperatura nu poate fi peste 60°C")
    private Double temperatureValue;
    
    @PositiveOrZero(message = "Precipitațiile nu pot fi negative")
    private Double precipitationValue;

    @Column(columnDefinition = "TEXT")
    private String aiInterpretation;

    @NotNull(message = "Data raportului este obligatorie")
    private LocalDateTime reportDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Location location;
    
    // Metrici suplimentare care nu sunt salvate în BD, dar sunt incluse în response JSON
    // Acestea vor fi setate programatic înainte de serializare
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