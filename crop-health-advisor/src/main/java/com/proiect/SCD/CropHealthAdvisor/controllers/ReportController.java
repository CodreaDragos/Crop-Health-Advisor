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

    /**
     * Generates and saves a new crop health report for a location.
     * Fetches real-time satellite data and generates AI interpretation.
     */
    @GetMapping
    public ResponseEntity<Reports> getFieldData(@Valid @RequestParam Long locationId) {
        try {
            Reports report = reportService.generateAndSaveReport(locationId)
                    .block(java.time.Duration.ofSeconds(60));
            
            if (report != null) {
                return ResponseEntity.ok(report);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error generating report for location " + locationId + ": " + e.getMessage());
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage = e.getCause().getMessage();
            }
            if (errorMessage != null && errorMessage.contains("not found")) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Retrieves report history for a specific location.
     */
    @GetMapping("/location/{locationId}")
    public ResponseEntity<List<Reports>> getReportsForLocation(@PathVariable Long locationId) {
        List<Reports> reports = reportService.getReportsByLocationId(locationId);
        if (reports.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(reports);
    }
    
    /**
     * Retrieves all reports (for admin purposes).
     */
    @GetMapping("/all")
    public ResponseEntity<List<Reports>> getAllReports() {
        List<Reports> reports = reportService.findAll();
        return ResponseEntity.ok(reports);
    }

    /**
     * Retrieves a single report by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Reports> getReportById(@PathVariable Long id) {
        return reportService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new report.
     */
    @PostMapping
    public ResponseEntity<Reports> createReport(@Valid @RequestBody Reports report) {
        Reports savedReport = reportService.save(report);
        return new ResponseEntity<>(savedReport, org.springframework.http.HttpStatus.CREATED);
    }

    /**
     * Updates an existing report.
     */
    @PutMapping
    public ResponseEntity<Reports> updateReport(@Valid @RequestBody Reports report) {
        if (reportService.findById(report.getId()).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Reports updatedReport = reportService.save(report);
        return ResponseEntity.ok(updatedReport);
    }

    /**
     * Deletes a report by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        if (reportService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}