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

// CREATE / UPDATE
    public Location save(Location location) {
        // Asigurăm că User-ul este atașat (opțional, dar bun)
        if (location.getUser() != null && location.getUser().getId() != null) {
            Optional<User> userOpt = userService.findById(location.getUser().getId());
            if (userOpt.isPresent()) {
                location.setUser(userOpt.get());
            } else {
                // Aruncă o excepție sau tratează eroarea: User-ul nu există
                throw new RuntimeException("Utilizatorul specificat nu există.");
            }
        }
        
        // Asigurăm că relația Bidirecțională (dacă o folosești) e setată corect
        location.getUser().getLocations().add(location); // <-- Asigură-te că adaugi locația la lista user-ului
        
        return locationRepository.save(location);
    }

    // READ (Toate locatiile unui user - necesită o metodă în Repository)
    public List<Location> findByUserId(Long userId) {
        // Presupunem ca adaugi in LocationRepository o metoda: List<Location> findByUserId(Long userId);
        return locationRepository.findByUserId(userId);
    }

    // READ (Dupa ID-ul locatiei)
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    // DELETE
    public boolean deleteById(Long id) {
        if (locationRepository.existsById(id)) {
            locationRepository.deleteById(id);
            return true;
        }
        return false;
    }
}