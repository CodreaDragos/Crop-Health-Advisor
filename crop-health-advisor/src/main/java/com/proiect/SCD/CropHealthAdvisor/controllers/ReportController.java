// src/main/java/com/proiect/SCD/CropHealthAdvisor/controllers/ReportController.java
package com.proiect.SCD.CropHealthAdvisor.controllers;

import com.proiect.SCD.CropHealthAdvisor.models.Reports;
import com.proiect.SCD.CropHealthAdvisor.services.ReportService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API is running!");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@Valid @RequestParam double lat, @RequestParam double lon) {
        return ResponseEntity.ok("Test endpoint working! Lat: " + lat + ", Lon: " + lon);
    }

    @GetMapping
    public ResponseEntity<Reports> getFieldData(@Valid @RequestParam Long locationId) {
        try {
            // Converteste Mono la blocking call pentru compatibilitate cu Spring Security
            // Adaugam timeout pentru a preveni blocarea indefinita
            Reports report = reportService.generateAndSaveReport(locationId)
                    .block(java.time.Duration.ofSeconds(60)); // Mărit timeout-ul pentru Sentinel Hub API
            
            if (report != null) {
                // Asigură-te că locația este încărcată (EAGER fetch)
                // și că este inclusă în răspuns (JSON serialization)
                return ResponseEntity.ok(report);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // Log pentru debugging
            e.printStackTrace();
            System.err.println("Error generating report for location " + locationId + ": " + e.getMessage());
            
            // Daca apare o eroare (ex: locatia nu exista), returneaza 400 sau 500
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            if (errorMessage != null && errorMessage.contains("găsită")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.status(500).build();
        }
    }
    // NOU: READ (Citeste istoricul rapoartelor pentru o locatie)
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Reports>> getReportsForLocation(@PathVariable Long locationId) {
        List<Reports> reports = reportService.getReportsByLocationId(locationId);
        if (reports.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reports);
    }
    
    // DELETE (Sterge un raport)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        if (reportService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}