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
public Mono<Reports> generateAndSaveReport(Long locationId) {
        
        // 1. Caută Locația salvată în baza de date
        return Mono.fromCallable(() -> locationRepository.findById(locationId))
            .flatMap(locationOpt -> {
                if (locationOpt.isEmpty()) {
                    // Dacă Locația nu există, aruncăm o eroare
                    return Mono.error(new RuntimeException("Locația cu ID-ul " + locationId + " nu a fost găsită."));
                }
                
                Location location = locationOpt.get();
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                
                // 2. Apelul către Sentinel Hub API pentru date reale
                return satelliteDataService.getSatelliteMetrics(lat, lon)
                    .flatMap(metrics -> {
                        // Folosim datele reale din Sentinel Hub
                        double ndviValue = metrics.getNdvi();
                        double eviValue = metrics.getEvi();
                        double ndwiValue = metrics.getNdwi();
                        double temperatureValue = metrics.getTemperature(); // LST - temperatura solului
                        double precipitationValue = metrics.getPrecipitation(); // Precipitații estimate din sateliți
                        double soilMoistureValue = metrics.getSoilMoisture(); // Umiditate solului (%)
                        double cloudCoverValue = metrics.getCloudCover(); // Acoperire nori (%)
                        double evapotranspirationValue = metrics.getEvapotranspiration(); // Evapotranspirație (mm/zi)
                        
                        // 3. Creare prompt pentru AI - Simplificat, lasă AI-ul să analizeze toate datele
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
                                // 4. Creare și populare Report
                                Reports report = new Reports();
                                // Setează valorile calculate:
                                report.setNdviValue(ndviValue);
                                report.setTemperatureValue(temperatureValue);
                                report.setPrecipitationValue(precipitationValue);
                                
                                // Setează metricile suplimentare (@Transient - nu sunt salvate în BD, dar sunt în JSON)
                                report.setEviValue(eviValue);
                                report.setNdwiValue(ndwiValue);
                                report.setSoilMoisture(soilMoistureValue);
                                report.setCloudCover(cloudCoverValue);
                                report.setEvapotranspiration(evapotranspirationValue);
                                
                                report.setAiInterpretation(aiInterpretation);
                                report.setReportDate(LocalDateTime.now());
                                report.setLocation(location); // <-- CHEIE STRĂINĂ
                                
                                return report;
                            });
                    });
            })
            .flatMap(report -> Mono.fromCallable(() -> reportRepository.save(report))); // 5. Salvare în BD
    }    
    /**
     * ✅ METRICE OBȚINUTE DIN SENTINEL HUB API
     * 
     * Aceste valori sunt acum obținute direct de la Sentinel Hub Statistical API:
     * 
     * 1. NDVI (Normalized Difference Vegetation Index):
     *    - Calculat direct în eval script: NDVI = (NIR - Red) / (NIR + Red)
     *    - Folosește benzi Sentinel-2 (B04 = Red, B08 = NIR)
     *    - Interval valid: -1.0 (sol gol/apă) până la 1.0 (vegetație foarte sănătoasă)
     * 
     * 2. EVI (Enhanced Vegetation Index):
     *    - Calculat în eval script pentru analiză mai avansată
     *    - Mai puțin sensibil la influența solului decât NDVI
     * 
     * 3. NDWI (Normalized Difference Water Index):
     *    - Calculat în eval script pentru estimarea umidității
     *    - Utilitar pentru detectarea stresului hidric al culturilor
     * 
     * 4. Temperatură:
     *    - Momentan folosește mock (TODO: integrare cu OpenWeatherMap sau API termic)
     * 
     * 5. Precipitații:
     *    - Momentan folosește mock (TODO: integrare cu OpenWeatherMap sau API precipitații)
     * 
     * NOTĂ: Metodele calculateNDVI, calculateTemperature, calculatePrecipitation au fost eliminate
     * deoarece datele sunt acum obținute direct de la Sentinel Hub.
     */
    public List<Reports> getReportsByLocationId(Long locationId) {
        List<Reports> reports = reportRepository.findByLocationId(locationId);
        
        // Pentru rapoartele existente, populăm metricile suplimentare (@Transient)
        // folosind datele actuale de la Sentinel Hub
        if (!reports.isEmpty() && reports.get(0).getLocation() != null) {
            Location location = reports.get(0).getLocation();
            
            try {
                // Recalculează metricile actuale din Sentinel Hub API
                // Folosim blocking pentru a popula datele înainte de return
                var metrics = satelliteDataService.getSatelliteMetrics(
                    location.getLatitude(),
                    location.getLongitude()
                ).block(java.time.Duration.ofSeconds(30));
                
                if (metrics != null) {
                    // Populează metricile suplimentare pentru toate rapoartele din listă
                    // (toate rapoartele sunt pentru aceeași locație, deci folosim aceleași metrici)
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
                // Lasă câmpurile null dacă nu putem recalcula - frontend-ul va afișa N/A
            }
        }
        
        return reports;
    }
    
    // DELETE (Sterge un raport)
    public boolean deleteById(Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return true;
        }
        return false;
    }

}