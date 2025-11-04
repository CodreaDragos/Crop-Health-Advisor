package com.proiect.SCD.CropHealthAdvisor.controllers;

import com.proiect.SCD.CropHealthAdvisor.services.SatelliteDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for generating satellite images (NDVI maps, etc.).
 */
@RestController
@RequestMapping("/api/satellite")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class SatelliteImageController {

    @Autowired
    private SatelliteDataService satelliteDataService;
    
    /**
     * Test endpoint to verify controller is detected.
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("SatelliteImageController is working!");
    }

    /**
     * Generates an NDVI image for a location.
     * @param lat Latitude
     * @param lon Longitude
     * @param width Image width (default: 512)
     * @param height Image height (default: 512)
     * @return PNG image with colored NDVI map
     */
    @GetMapping("/image/ndvi")
    public ResponseEntity<byte[]> getNDVIImage(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "512") int width,
            @RequestParam(defaultValue = "512") int height
    ) {
        System.out.println("=== NDVI Image Request ===");
        System.out.println("Lat: " + lat + ", Lon: " + lon + ", Width: " + width + ", Height: " + height);
        
        try {
            // Use Sentinel Hub Process API to generate NDVI image
            byte[] imageData = satelliteDataService.generateNDVIImage(lat, lon, width, height)
                    .block(java.time.Duration.ofSeconds(30));
            
            System.out.println("Image data length: " + (imageData != null ? imageData.length : "null"));
            
            if (imageData != null && imageData.length > 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(imageData.length);
                headers.set("Content-Disposition", "inline; filename=ndvi-map.png");
                
                System.out.println("Returning image with size: " + imageData.length);
                return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
            } else {
                System.err.println("Image data is null or empty");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error generating NDVI image: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

