// src/main/java/com/proiect/SCD/CropHealthAdvisor/controllers/LocationController.java
package com.proiect.SCD.CropHealthAdvisor.controllers;

import com.proiect.SCD.CropHealthAdvisor.models.Location;
import com.proiect.SCD.CropHealthAdvisor.services.LocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    // CREATE (Adauga o noua locatie)
    // Se asteapta ca obiectul Location sa contina si un User in interior (sau un user_id valid)
    @PostMapping
    public ResponseEntity<Location> createLocation(@Valid @RequestBody Location location) {
        Location savedLocation = locationService.save(location);
        return new ResponseEntity<>(savedLocation, HttpStatus.CREATED);
    }

    // READ (Toate locatiile unui utilizator. Presupunem ca avem id-ul user-ului)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Location>> getLocationsByUserId(@PathVariable Long userId) {
        List<Location> locations = locationService.findByUserId(userId);
        return ResponseEntity.ok(locations);
    }

    // READ (O singura locatie)
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        return locationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @PutMapping
    public ResponseEntity<Location> updateLocation(@Valid @RequestBody Location locationDetails) {
        if (locationService.findById(locationDetails.getId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Location updatedLocation = locationService.save(locationDetails);
        return ResponseEntity.ok(updatedLocation);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        if (locationService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}