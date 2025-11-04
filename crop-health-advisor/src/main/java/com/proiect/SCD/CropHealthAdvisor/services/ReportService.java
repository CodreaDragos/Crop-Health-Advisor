package com.proiect.SCD.CropHealthAdvisor.services;

import com.proiect.SCD.CropHealthAdvisor.models.Reports;
import com.proiect.SCD.CropHealthAdvisor.models.Location;
import com.proiect.SCD.CropHealthAdvisor.repositories.ReportRepository;
import com.proiect.SCD.CropHealthAdvisor.services.SatelliteDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.List;
import com.proiect.SCD.CropHealthAdvisor.repositories.LocationRepository;

@Service
public class ReportService {

    @Autowired
    private SatelliteDataService satelliteDataService;
    
    @Autowired
    private GeminiAIService geminiAIService;
    
    @Autowired
    private LocationRepository locationRepository;
    
    @Autowired
    private ReportRepository reportRepository;
    /**
     * Generates a new crop health report for a location.
     * Fetches satellite data from Sentinel Hub API, generates AI interpretation,
     * and saves the report to the database.
     * 
     * @param locationId The ID of the location to generate report for
     * @return Mono containing the generated report
     */
    public Mono<Reports> generateAndSaveReport(Long locationId) {
        return Mono.fromCallable(() -> locationRepository.findById(locationId))
            .flatMap(locationOpt -> {
                if (locationOpt.isEmpty()) {
                    return Mono.error(new RuntimeException("Location with ID " + locationId + " not found."));
                }
                
                Location location = locationOpt.get();
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                
                // Fetch real-time satellite data from Sentinel Hub API
                return satelliteDataService.getSatelliteMetrics(lat, lon)
                    .flatMap(metrics -> {
                        double ndviValue = metrics.getNdvi();
                        double eviValue = metrics.getEvi();
                        double ndwiValue = metrics.getNdwi();
                        double temperatureValue = metrics.getTemperature(); // LST - Land Surface Temperature
                        double precipitationValue = metrics.getPrecipitation(); // Estimated from satellite indices
                        double soilMoistureValue = metrics.getSoilMoisture(); // Soil moisture (%)
                        double cloudCoverValue = metrics.getCloudCover(); // Cloud cover (%)
                        double evapotranspirationValue = metrics.getEvapotranspiration(); // Evapotranspiration (mm/day)
                        
                        // Create AI prompt for comprehensive crop analysis
                        String prompt = String.format(
                                "Ești un expert în agricultură și monitorizare a culturilor prin date satelitare. " +
                                "Analizează în detaliu următoarele date satelitare pentru o locație agricolă și oferă o analiză comprehensivă:\n\n" +
                                "📊 DATE SATELITARE:\n\n" +
                                "INDICI DE VEGETAȚIE:\n" +
                                "- NDVI (Normalized Difference Vegetation Index): %.3f\n" +
                                "  Interval: -1.0 (vegetație mortă/sol gol) → 1.0 (vegetație foarte sănătoasă)\n" +
                                "- EVI (Enhanced Vegetation Index): %.3f\n" +
                                "  Indică vegetație mai robust, mai puțin sensibil la influența solului\n" +
                                "- NDWI (Normalized Difference Water Index): %.3f\n" +
                                "  Indică disponibilitatea apei în vegetație și sol (-1.0 uscat → 1.0 umed)\n\n" +
                                "CONDIȚII DE MEDIU:\n" +
                                "- Temperatura solului (LST - Land Surface Temperature): %.1f°C\n" +
                                "  NOTĂ IMPORTANTĂ: Aceasta este temperatura suprafeței solului, NU temperatura aerului\n" +
                                "  Interval normal pentru sol: 5°C - 50°C (variază după sezon și tipul culturii)\n" +
                                "- Umiditate solului: %.1f%%\n" +
                                "  Derivă din NDWI: valori ridicate = sol mai umed\n" +
                                "- Precipitații estimate: %.1fmm\n" +
                                "  Estimare bazată pe indici satelitari (NDWI și alți indici)\n" +
                                "- Evapotranspirație: %.2f mm/zi\n" +
                                "  Estimare bazată pe NDVI și temperatura solului\n" +
                                "- Acoperire nori: %.1f%%\n" +
                                "  Estimare din diferența NDVI-EVI\n\n" +
                                "📝 CERINȚE PENTRU RĂSPUNSUL TĂU:\n" +
                                "Fă o ANALIZĂ DETALIATĂ și COMPREHENSIVĂ (minimum 10-15 rânduri) care include:\n" +
                                "1. Evaluare generală a stării culturii bazată pe TOATE indicile (NDVI, EVI, NDWI)\n" +
                                "2. Analiza impactului temperaturii solului asupra dezvoltării culturilor\n" +
                                "3. Evaluarea umidității solului și disponibilității apei\n" +
                                "4. Analiza balanței dintre evapotranspirație și precipitații\n" +
                                "5. Impactul acoperirii norilor asupra calității datelor\n" +
                                "6. Identificarea problemelor potențiale și riscurilor\n" +
                                "7. Recomandări SPECIFICE și PRIORITIZATE pentru acțiuni (ex: irigație, fertilizare, tratamente, monitorizare)\n" +
                                "8. Plan de acțiune pe termen scurt și mediu\n" +
                                "9. Sfaturi practice pentru îmbunătățirea condițiilor culturilor\n\n" +
                                "Fii detaliat, precis și oferă sfaturi practice bazate pe toate datele disponibile. " +
                                "Răspunde în română, într-un stil profesional dar accesibil, fără prefixe sau formate JSON.",
                                ndviValue,
                                eviValue,
                                ndwiValue,
                                temperatureValue,
                                soilMoistureValue,
                                precipitationValue,
                                evapotranspirationValue,
                                cloudCoverValue);

                        return geminiAIService.getInterpretation(prompt)
                            .map(aiInterpretation -> {
                                Reports report = new Reports();
                                report.setNdviValue(ndviValue);
                                report.setTemperatureValue(temperatureValue);
                                report.setPrecipitationValue(precipitationValue);
                                
                                // Additional metrics (@Transient - not saved in DB, but included in JSON response)
                                report.setEviValue(eviValue);
                                report.setNdwiValue(ndwiValue);
                                report.setSoilMoisture(soilMoistureValue);
                                report.setCloudCover(cloudCoverValue);
                                report.setEvapotranspiration(evapotranspirationValue);
                                
                                report.setAiInterpretation(aiInterpretation);
                                report.setReportDate(LocalDateTime.now());
                                report.setLocation(location);
                                
                                return report;
                            });
                    });
            })
            .flatMap(report -> Mono.fromCallable(() -> reportRepository.save(report)));
    }    
    /**
     * Retrieves all reports for a specific location.
     * Populates additional transient metrics (@Transient fields) with current data from Sentinel Hub API.
     * These metrics are not stored in DB but included in JSON responses for better UI display.
     */
    public List<Reports> getReportsByLocationId(Long locationId) {
        List<Reports> reports = reportRepository.findByLocationId(locationId);
        
        if (!reports.isEmpty() && reports.get(0).getLocation() != null) {
            Location location = reports.get(0).getLocation();
            
            try {
                var metrics = satelliteDataService.getSatelliteMetrics(
                    location.getLatitude(),
                    location.getLongitude()
                ).block(java.time.Duration.ofSeconds(30));
                
                if (metrics != null) {
                    reports.forEach(report -> {
                        report.setEviValue(metrics.getEvi());
                        report.setNdwiValue(metrics.getNdwi());
                        report.setSoilMoisture(metrics.getSoilMoisture());
                        report.setCloudCover(metrics.getCloudCover());
                        report.setEvapotranspiration(metrics.getEvapotranspiration());
                    });
                }
            } catch (Exception e) {
                System.err.println("Error populating additional metrics for location " + locationId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return reports;
    }
    
    /**
     * Retrieves all reports (for admin purposes).
     */
    public List<Reports> findAll() {
        return reportRepository.findAll();
    }

    /**
     * Finds a report by ID.
     */
    public java.util.Optional<Reports> findById(Long id) {
        return reportRepository.findById(id);
    }

    /**
     * Creates a new report or updates an existing one.
     * For updates: loads existing entity and updates fields to avoid detached entity issues.
     * Location can be changed but coordinates remain fixed for the selected location.
     */
    public Reports save(Reports report) {
        boolean isUpdate = report.getId() != null && report.getId() > 0;
        
        if (isUpdate) {
            java.util.Optional<Reports> existingReportOpt = reportRepository.findById(report.getId());
            if (existingReportOpt.isEmpty()) {
                throw new RuntimeException("Report with ID " + report.getId() + " does not exist.");
            }
            
            Reports existingReport = existingReportOpt.get();
            existingReport.setNdviValue(report.getNdviValue());
            existingReport.setTemperatureValue(report.getTemperatureValue());
            existingReport.setPrecipitationValue(report.getPrecipitationValue());
            existingReport.setAiInterpretation(report.getAiInterpretation());
            existingReport.setReportDate(report.getReportDate());
            
            if (report.getLocation() != null && report.getLocation().getId() != null) {
                java.util.Optional<Location> locationOpt = locationRepository.findById(report.getLocation().getId());
                if (locationOpt.isPresent()) {
                    existingReport.setLocation(locationOpt.get());
                } else {
                    throw new RuntimeException("Specified location does not exist.");
                }
            }
            
            return reportRepository.save(existingReport);
        } else {
            if (report.getLocation() == null || report.getLocation().getId() == null) {
                throw new RuntimeException("Report must have an associated location.");
            }
            
            java.util.Optional<Location> locationOpt = locationRepository.findById(report.getLocation().getId());
            if (locationOpt.isEmpty()) {
                throw new RuntimeException("Specified location does not exist.");
            }
            
            Location location = locationOpt.get();
            report.setLocation(location);
            
            if (report.getReportDate() == null) {
                report.setReportDate(LocalDateTime.now());
            }
            
            return reportRepository.save(report);
        }
    }

    /**
     * Deletes a report by ID.
     * @return true if report was deleted, false if not found
     */
    public boolean deleteById(Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return true;
        }
        return false;
    }

}