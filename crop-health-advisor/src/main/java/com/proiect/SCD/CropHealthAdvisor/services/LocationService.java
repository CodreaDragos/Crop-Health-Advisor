// src/main/java/com/proiect/SCD/CropHealthAdvisor/services/LocationService.java
package com.proiect.SCD.CropHealthAdvisor.services;

import com.proiect.SCD.CropHealthAdvisor.models.Location;
import com.proiect.SCD.CropHealthAdvisor.models.User;
import com.proiect.SCD.CropHealthAdvisor.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private UserService userService;

    /**
     * Creates a new location or updates an existing one.
     * For updates: loads existing entity and updates only necessary fields to avoid detached entity issues.
     * For creates: validates that a user is associated with the location.
     * Note: Hibernate manages the relationship automatically, avoiding cascade issues.
     */
    public Location save(Location location) {
        boolean isUpdate = location.getId() != null && location.getId() > 0;
        
        if (isUpdate) {
            Optional<Location> existingLocationOpt = locationRepository.findById(location.getId());
            if (existingLocationOpt.isEmpty()) {
                throw new RuntimeException("Location with ID " + location.getId() + " does not exist.");
            }
            
            Location existingLocation = existingLocationOpt.get();
            existingLocation.setName(location.getName());
            existingLocation.setLatitude(location.getLatitude());
            existingLocation.setLongitude(location.getLongitude());
            
            if (location.getUser() != null && location.getUser().getId() != null) {
                Optional<User> userOpt = userService.findById(location.getUser().getId());
                if (userOpt.isPresent()) {
                    existingLocation.setUser(userOpt.get());
                } else {
                    throw new RuntimeException("Specified user does not exist.");
                }
            }
            
            return locationRepository.save(existingLocation);
        } else {
            if (location.getUser() == null || location.getUser().getId() == null) {
                throw new RuntimeException("Location must have an associated user.");
            }
            
            Optional<User> userOpt = userService.findById(location.getUser().getId());
            if (userOpt.isEmpty()) {
                throw new RuntimeException("Specified user does not exist.");
            }
            
            User user = userOpt.get();
            location.setUser(user);
            return locationRepository.save(location);
        }
    }

    /**
     * Retrieves all locations (for admin purposes).
     */
    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    /**
     * Finds all locations belonging to a specific user.
     */
    public List<Location> findByUserId(Long userId) {
        return locationRepository.findByUserId(userId);
    }

    /**
     * Finds a location by ID.
     */
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    /**
     * Deletes a location by ID.
     * @return true if location was deleted, false if not found
     */
    public boolean deleteById(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}