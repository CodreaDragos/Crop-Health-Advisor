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

    /**
     * Creates a new location.
     * Location object must contain a valid User reference or user_id.
     */
    @PostMapping
    public ResponseEntity<Location> createLocation(@Valid @RequestBody Location location) {
        Location savedLocation = locationService.save(location);
        return new ResponseEntity<>(savedLocation, HttpStatus.CREATED);
    }

    /**
     * Retrieves all locations (for admin purposes).
     */
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        List<Location> locations = locationService.findAll();
        return ResponseEntity.ok(locations);
    }

    /**
     * Retrieves all locations for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Location>> getLocationsByUserId(@PathVariable Long userId) {
        List<Location> locations = locationService.findByUserId(userId);
        return ResponseEntity.ok(locations);
    }

    /**
     * Retrieves a single location by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Long id) {
        return locationService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing location.
     */
    @PutMapping
    public ResponseEntity<Location> updateLocation(@Valid @RequestBody Location locationDetails) {
        if (locationService.findById(locationDetails.getId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Location updatedLocation = locationService.save(locationDetails);
        return ResponseEntity.ok(updatedLocation);
    }

    /**
     * Deletes a location by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        if (locationService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}