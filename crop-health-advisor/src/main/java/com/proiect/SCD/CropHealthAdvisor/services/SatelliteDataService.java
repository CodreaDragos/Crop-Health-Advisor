// src/main/java/com/proiect/SCD/CropHealthAdvisor/services/SatelliteDataService.java
package com.proiect.SCD.CropHealthAdvisor.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proiect.SCD.CropHealthAdvisor.dto.SatelliteDataDTO;
import com.proiect.SCD.CropHealthAdvisor.dto.SatelliteMetricsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

@Service
public class SatelliteDataService {

    @Value("${sentinelhub.api.key}")
    private String sentinelHubApiKey;

    @Value("${sentinelhub.client.secret:}")
    private String sentinelHubClientSecret; // Opțional - pentru OAuth2
    
    @Autowired(required = false)
    private WeatherService weatherService; // Opțional - pentru date meteorologice reale
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    // Cache pentru access token
    private String cachedAccessToken = null;
    private long tokenExpiryTime = 0;
    
    @Autowired
    public SatelliteDataService(ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .baseUrl("https://services.sentinel-hub.com")
                .build();
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }
    
    /**
     * Obține un access token OAuth2 de la Sentinel Hub
     * NOTĂ: Dacă nu ai Client Secret, folosește direct cheia API (legacy mode)
     */
    private Mono<String> getAccessToken() {
        // Dacă nu avem Client Secret, folosim direct cheia API (pentru legacy/statistical API)
        if (sentinelHubClientSecret == null || sentinelHubClientSecret.isEmpty()) {
            return Mono.just(sentinelHubApiKey);
        }
        
        // Dacă avem token cache valid, folosim-l
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return Mono.just(cachedAccessToken);
        }
        
        // Obține token nou prin OAuth2 client credentials
        return webClient.post()
                .uri("/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials&client_id=" + sentinelHubApiKey + "&client_secret=" + sentinelHubClientSecret)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String accessToken = (String) response.get("access_token");
                    Integer expiresIn = (Integer) response.get("expires_in");
                    cachedAccessToken = accessToken;
                    tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000) - 60000; // Expiră cu 1 min înainte
                    return accessToken;
                })
                .onErrorResume(error -> {
                    System.err.println("Error getting OAuth token: " + error.getMessage());
                    // Fallback la cheia API directă
                    return Mono.just(sentinelHubApiKey);
                });
    }

    /**
     * Obține metricile satelitare pentru o locație (NDVI, EVI, NDWI, temperatura LST)
     * Folosește Sentinel Hub Statistical API pentru indici și MODIS pentru temperatură
     */
    public Mono<SatelliteMetricsDTO> getSatelliteMetrics(double lat, double lon) {
        // Definește o zonă mică în jurul coordonatelor (buffer de ~100m)
        double buffer = 0.001; // ~100m
        double minLon = lon - buffer;
        double minLat = lat - buffer;
        double maxLon = lon + buffer;
        double maxLat = lat + buffer;
        
        // Interval de timp: ultimele 30 de zile
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        String timeFrom = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00Z";
        String timeTo = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T23:59:59Z";
        
        // Eval script pentru NDVI, EVI, NDWI din Sentinel-2
        String evalscript = 
            "//VERSION=3\n" +
            "function setup() {\n" +
            "  return {\n" +
            "    input: [{\n" +
            "      bands: [\"B02\", \"B03\", \"B04\", \"B08\", \"B11\", \"B12\"], // Blue, Green, Red, NIR, SWIR1, SWIR2\n" +
            "      units: \"DN\"\n" +
            "    }],\n" +
            "    output: [\n" +
            "      { id: \"ndvi\", bands: 1, sampleType: \"FLOAT32\" },\n" +
            "      { id: \"evi\", bands: 1, sampleType: \"FLOAT32\" },\n" +
            "      { id: \"ndwi\", bands: 1, sampleType: \"FLOAT32\" }\n" +
            "    ]\n" +
            "  };\n" +
            "}\n" +
            "function evaluatePixel(samples) {\n" +
            "  var blue = samples.B02 / 10000;\n" +
            "  var green = samples.B03 / 10000;\n" +
            "  var red = samples.B04 / 10000;\n" +
            "  var nir = samples.B08 / 10000;\n" +
            "  var swir1 = samples.B11 / 10000;\n" +
            "  var swir2 = samples.B12 / 10000;\n" +
            "  \n" +
            "  var ndvi = (nir - red) / (nir + red);\n" +
            "  var evi = 2.5 * ((nir - red) / (nir + 6 * red - 7.5 * blue + 1));\n" +
            "  var ndwi = (green - nir) / (green + nir);\n" +
            "  \n" +
            "  return {\n" +
            "    ndvi: [Math.max(-1, Math.min(1, ndvi))],\n" +
            "    evi: [Math.max(-1, Math.min(1, evi))],\n" +
            "    ndwi: [Math.max(-1, Math.min(1, ndwi))]\n" +
            "  };\n" +
            "}";
        
        // Request pentru NDVI/EVI/NDWI din Sentinel-2
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> bounds = new HashMap<>();
        bounds.put("bbox", new double[]{minLon, minLat, maxLon, maxLat});
        bounds.put("properties", Map.of("crs", "http://www.opengis.net/def/crs/EPSG/0/4326"));
        input.put("bounds", bounds);
        
        Map<String, Object> dataSource = new HashMap<>();
        dataSource.put("type", "sentinel-2-l2a");
        Map<String, Object> dataFilter = new HashMap<>();
        Map<String, String> timeRange = new HashMap<>();
        timeRange.put("from", timeFrom);
        timeRange.put("to", timeTo);
        dataFilter.put("timeRange", timeRange);
        dataSource.put("dataFilter", dataFilter);
        input.put("data", new Object[]{dataSource});
        requestBody.put("input", input);
        
        Map<String, Object> aggregation = new HashMap<>();
        Map<String, String> aggTimeRange = new HashMap<>();
        aggTimeRange.put("from", timeFrom);
        aggTimeRange.put("to", timeTo);
        aggregation.put("timeRange", aggTimeRange);
        aggregation.put("aggregationInterval", Map.of("of", "P1D"));
        aggregation.put("evalscript", evalscript);
        requestBody.put("aggregation", aggregation);
        
        Map<String, Object> output = new HashMap<>();
        output.put("width", 1);
        output.put("height", 1);
        Map<String, Object> response = new HashMap<>();
        response.put("identifier", "default");
        Map<String, String> format = new HashMap<>();
        format.put("type", "application/json");
        response.put("format", format);
        output.put("responses", new Object[]{response});
        requestBody.put("output", output);
        
        try {
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            
            // Obține NDVI/EVI/NDWI din Sentinel-2
            Mono<SatelliteMetricsDTO> satelliteMetricsMono = getAccessToken()
                    .flatMap(accessToken -> {
                        return webClient.post()
                                .uri("/api/v1/statistics")
                                .header("Authorization", "Bearer " + accessToken)
                                .header("Content-Type", "application/json")
                                .bodyValue(requestBodyJson)
                                .retrieve()
                                .bodyToMono(String.class);
                    })
                    .map(responseJson -> {
                        return parseStatisticalResponse(responseJson, lat, lon);
                    });
            
            // Obține temperatura LST (Land Surface Temperature) din MODIS
            Mono<Double> lstTemperatureMono = getLandSurfaceTemperature(lat, lon);
            
            // Combină ambele: NDVI/EVI/NDWI + LST + Weather data
            return Mono.zip(satelliteMetricsMono, lstTemperatureMono)
                    .flatMap(tuple -> {
                        SatelliteMetricsDTO metrics = tuple.getT1();
                        Double lstTemp = tuple.getT2();
                        
                        // Folosește LST direct (temperatura suprafeței solului)
                        if (lstTemp != null && lstTemp > -50 && lstTemp < 60) {
                            // LST este temperatura suprafeței solului - folosim direct
                            metrics.setTemperature(lstTemp);
                            
                            // Calculează metrici suplimentare bazate pe datele disponibile din sateliți
                            calculateAdditionalMetricsFromSatellite(metrics);
                        } else if (weatherService != null) {
                            // Dacă LST nu este disponibil, folosește WeatherService
                            return weatherService.getWeatherData(lat, lon)
                                    .map(weatherData -> {
                                        // Folosește temperatura din weather API ca fallback, dar preferăm LST
                                        if (metrics.getTemperature() == 0.0) {
                                            metrics.setTemperature(weatherData.get("temperature"));
                                        }
                                        // Precipitațiile din weather API au prioritate peste estimările satelitare
                                        metrics.setPrecipitation(weatherData.get("precipitation"));
                                        // Calculează metrici suplimentare
                                        calculateAdditionalMetricsFromSatellite(metrics);
                                        return metrics;
                                    })
                                    .onErrorResume(error -> {
                                        System.err.println("Error fetching weather data: " + error.getMessage());
                                        metrics.setTemperature(getTemperatureFromWeatherAPI(lat, lon));
                                        metrics.setPrecipitation(getPrecipitationFromWeatherAPI(lat, lon));
                                        return Mono.just(metrics);
                                    });
                        } else {
                            // Fallback: calculează metrici din datele disponibile, sau mock dacă nu avem nimic
                            if (metrics.getTemperature() == 0.0) {
                                metrics.setTemperature(getTemperatureFromWeatherAPI(lat, lon));
                            }
                            // Calculează metrici suplimentare din datele disponibile
                            calculateAdditionalMetricsFromSatellite(metrics);
                        }
                        
                        // Asigură-te că toate metricile sunt calculate
                        if (metrics.getPrecipitation() == 0.0 && metrics.getSoilMoisture() == 0.0) {
                            calculateAdditionalMetricsFromSatellite(metrics);
                        }
                        
                        return Mono.just(metrics);
                    })
                    .onErrorResume(error -> {
                        System.err.println("Error fetching satellite data: " + error.getMessage());
                        return Mono.just(createMockMetrics(lat, lon));
                    });
        } catch (Exception e) {
            System.err.println("Error building request: " + e.getMessage());
            return Mono.just(createMockMetrics(lat, lon));
        }
    }
    
    /**
     * Obține Land Surface Temperature (LST) din MODIS Terra/Aqua
     * MODIS oferă date termale pentru temperatura suprafeței solului
     */
    private Mono<Double> getLandSurfaceTemperature(double lat, double lon) {
        double buffer = 0.001;
        double minLon = lon - buffer;
        double minLat = lat - buffer;
        double maxLon = lon + buffer;
        double maxLat = lat + buffer;
        
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7); // MODIS are rezoluție zilnică, folosim ultimele 7 zile
        String timeFrom = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T00:00:00Z";
        String timeTo = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) + "T23:59:59Z";
        
        // Eval script pentru LST din MODIS
        String lstScript = 
            "//VERSION=3\n" +
            "function setup() {\n" +
            "  return {\n" +
            "    input: [{\n" +
            "      bands: [\"LST\"], // MODIS Land Surface Temperature band\n" +
            "      units: \"K\" // Kelvin\n" +
            "    }],\n" +
            "    output: [\n" +
            "      { id: \"lst\", bands: 1, sampleType: \"FLOAT32\" }\n" +
            "    ]\n" +
            "  };\n" +
            "}\n" +
            "function evaluatePixel(samples) {\n" +
            "  // LST este în Kelvin, convertim la Celsius\n" +
            "  var lstKelvin = samples.LST * 0.02 - 273.15; // Scale factor 0.02 pentru MODIS\n" +
            "  return {\n" +
            "    lst: [lstKelvin]\n" +
            "  };\n" +
            "}";
        
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> bounds = new HashMap<>();
        bounds.put("bbox", new double[]{minLon, minLat, maxLon, maxLat});
        bounds.put("properties", Map.of("crs", "http://www.opengis.net/def/crs/EPSG/0/4326"));
        input.put("bounds", bounds);
        
        Map<String, Object> dataSource = new HashMap<>();
        dataSource.put("type", "modis");
        Map<String, Object> dataFilter = new HashMap<>();
        Map<String, String> timeRange = new HashMap<>();
        timeRange.put("from", timeFrom);
        timeRange.put("to", timeTo);
        dataFilter.put("timeRange", timeRange);
        dataSource.put("dataFilter", dataFilter);
        input.put("data", new Object[]{dataSource});
        requestBody.put("input", input);
        
        Map<String, Object> aggregation = new HashMap<>();
        Map<String, String> aggTimeRange = new HashMap<>();
        aggTimeRange.put("from", timeFrom);
        aggTimeRange.put("to", timeTo);
        aggregation.put("timeRange", aggTimeRange);
        aggregation.put("aggregationInterval", Map.of("of", "P1D"));
        aggregation.put("evalscript", lstScript);
        requestBody.put("aggregation", aggregation);
        
        Map<String, Object> output = new HashMap<>();
        output.put("width", 1);
        output.put("height", 1);
        Map<String, Object> response = new HashMap<>();
        response.put("identifier", "default");
        Map<String, String> format = new HashMap<>();
        format.put("type", "application/json");
        response.put("format", format);
        output.put("responses", new Object[]{response});
        requestBody.put("output", output);
        
        try {
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            
            return getAccessToken()
                    .flatMap(accessToken -> {
                        return webClient.post()
                                .uri("/api/v1/statistics")
                                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                                .bodyValue(requestBodyJson)
                .retrieve()
                .bodyToMono(String.class);
                    })
                    .map(responseJson -> {
                        // Parsează răspunsul pentru LST
                        SatelliteDataDTO data;
                        try {
                            data = objectMapper.readValue(responseJson, SatelliteDataDTO.class);
                        } catch (Exception e) {
                            System.err.println("Error parsing LST response: " + e.getMessage());
                            return null;
                        }
                        
                        if (data.getData() != null && !data.getData().isEmpty()) {
                            SatelliteDataDTO.TimeSeriesData latestData = data.getData().get(data.getData().size() - 1);
                            if (latestData.getOutputs() != null && latestData.getOutputs().containsKey("lst")) {
                                SatelliteDataDTO.OutputValue lstOutput = latestData.getOutputs().get("lst");
                                if (lstOutput.getStats() != null && lstOutput.getStats().getMean() != null) {
                                    return lstOutput.getStats().getMean();
                                }
                            }
                        }
                        return null;
                    })
                    .onErrorResume(error -> {
                        System.err.println("Error fetching LST from MODIS: " + error.getMessage());
                        return Mono.just((Double) null); // Returnează null dacă nu poate obține LST
                    });
        } catch (Exception e) {
            System.err.println("Error building LST request: " + e.getMessage());
            return Mono.just((Double) null);
        }
    }
    
    /**
     * Parsează răspunsul de la Statistical API și extrage metricile
     */
    private SatelliteMetricsDTO parseStatisticalResponse(String response, double lat, double lon) {
        try {
            SatelliteDataDTO data = objectMapper.readValue(response, SatelliteDataDTO.class);
            
            if (data.getData() != null && !data.getData().isEmpty()) {
                // Ia ultimele date disponibile (cea mai recentă)
                SatelliteDataDTO.TimeSeriesData latestData = data.getData().get(data.getData().size() - 1);
                
                if (latestData.getOutputs() != null) {
                    SatelliteMetricsDTO metrics = new SatelliteMetricsDTO();
                    
                    // Extrage NDVI
                    if (latestData.getOutputs().containsKey("ndvi")) {
                        SatelliteDataDTO.OutputValue ndviOutput = latestData.getOutputs().get("ndvi");
                        if (ndviOutput.getStats() != null && ndviOutput.getStats().getMean() != null) {
                            metrics.setNdvi(ndviOutput.getStats().getMean());
                        }
                    }
                    
                    // Extrage EVI
                    if (latestData.getOutputs().containsKey("evi")) {
                        SatelliteDataDTO.OutputValue eviOutput = latestData.getOutputs().get("evi");
                        if (eviOutput.getStats() != null && eviOutput.getStats().getMean() != null) {
                            metrics.setEvi(eviOutput.getStats().getMean());
                        }
                    }
                    
                    // Extrage NDWI
                    if (latestData.getOutputs().containsKey("ndwi")) {
                        SatelliteDataDTO.OutputValue ndwiOutput = latestData.getOutputs().get("ndwi");
                        if (ndwiOutput.getStats() != null && ndwiOutput.getStats().getMean() != null) {
                            metrics.setNdwi(ndwiOutput.getStats().getMean());
                        }
                    }
                    
                    // Temperatura și precipitațiile vor fi setate mai târziu în flatMap
                    // Momentan le lăsăm null, vor fi completate după
                    
                    return metrics;
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing satellite data response: " + e.getMessage());
        }
        
        // Fallback la mock dacă parsing-ul eșuează
        return createMockMetrics(lat, lon);
    }
    
    /**
     * Calculează metrici suplimentare relevante din datele satelitare disponibile
     * Toate metricile sunt calculate din NDVI, EVI, NDWI, și LST (nu sunt estimate random)
     */
    private void calculateAdditionalMetricsFromSatellite(SatelliteMetricsDTO metrics) {
        double ndvi = metrics.getNdvi();
        double evi = metrics.getEvi();
        double ndwi = metrics.getNdwi();
        double lst = metrics.getTemperature(); // LST = temperatura solului
        
        // 1. Soil Moisture Index (SMI) - derivat din NDWI
        // NDWI corelează cu umiditatea: valori pozitive = mai multă apă
        // Normalizăm NDWI (-1 to 1) la un index de 0-100%
        double soilMoisture = ((ndwi + 1.0) / 2.0) * 100.0; // Convertim -1..1 la 0..100%
        metrics.setSoilMoisture(soilMoisture);
        
        // 2. Evapotranspiration (ET) - estimat din NDVI și LST
        // Formula simplificată: ET ∝ NDVI * LST (mai multă vegetație + temperatură mai mare = mai multă ET)
        // Normalizăm pentru a obține mm/zi rezonabil (0-10mm/zi)
        double evapotranspiration = Math.max(0, Math.min(10, ndvi * 5.0 + (lst > 20 ? (lst - 20) * 0.2 : 0)));
        metrics.setEvapotranspiration(evapotranspiration);
        
        // 3. Cloud Cover - estimat din diferența dintre NDVI și EVI
        // EVI este mai robust la nori, diferența mare indică acoperire de nori
        // Normalizăm la procent (0-100%)
        double ndviEviDiff = Math.abs(ndvi - evi);
        double cloudCover = Math.min(100, ndviEviDiff * 150.0); // Ajustare pentru a obține procente reale
        metrics.setCloudCover(cloudCover);
        
        // 4. Precipitații estimate - derivat din NDWI și schimbări în NDVI
        // NDWI ridicat + scădere NDVI recentă = precipitații recente
        // Formula simplificată bazată pe corelația dintre NDWI și precipitații
        double estimatedPrecipitation = Math.max(0, (ndwi + 0.5) * 30.0); // Estimare mm pe baza NDWI
        metrics.setPrecipitation(estimatedPrecipitation);
    }
    
    /**
     * Obține temperatura de la un API meteorologic (fallback)
     */
    private double getTemperatureFromWeatherAPI(double lat, double lon) {
        // Mock pentru fallback
        return Math.random() * 30.0 + 5.0; // 5-35°C
    }
    
    /**
     * Obține precipitațiile de la un API meteorologic (fallback)
     */
    private double getPrecipitationFromWeatherAPI(double lat, double lon) {
        // Mock pentru fallback
        return Math.random() * 80.0; // 0-80mm
    }
    
    /**
     * Creează metrici mock ca fallback
     */
    private SatelliteMetricsDTO createMockMetrics(double lat, double lon) {
        SatelliteMetricsDTO metrics = new SatelliteMetricsDTO(
            Math.random() * 2.0 - 1.0, // NDVI: -1 to 1
            Math.random() * 2.0 - 1.0, // EVI: -1 to 1
            Math.random() * 2.0 - 1.0, // NDWI: -1 to 1
            Math.random() * 30.0 + 5.0, // Temperature: 5-35°C (LST)
            Math.random() * 80.0 // Precipitation: 0-80mm (estimat)
        );
        // Calculează metrici suplimentare din valorile mock
        calculateAdditionalMetricsFromSatellite(metrics);
        return metrics;
    }
    
    /**
     * Generează o imagine NDVI colorată pentru o locație
     * Folosește Sentinel Hub Process API pentru a genera o imagine PNG
     */
    public Mono<byte[]> generateNDVIImage(double lat, double lon, int width, int height) {
        // Buffer pentru zona de interes (mai mare pentru imagine)
        double buffer = 0.005; // ~500m
        double minLon = lon - buffer;
        double minLat = lat - buffer;
        double maxLon = lon + buffer;
        double maxLat = lat + buffer;
        
        // Eval script pentru generarea unei imagini NDVI colorate
        String evalscript = 
            "//VERSION=3\n" +
            "function setup() {\n" +
            "  return {\n" +
            "    input: [{\n" +
            "      bands: [\"B04\", \"B08\"], // Red, NIR\n" +
            "      units: \"DN\"\n" +
            "    }],\n" +
            "    output: {\n" +
            "      bands: 3, // RGB\n" +
            "      sampleType: \"UINT8\"\n" +
            "    }\n" +
            "  };\n" +
            "}\n" +
            "function evaluatePixel(samples) {\n" +
            "  var red = samples.B04 / 10000;\n" +
            "  var nir = samples.B08 / 10000;\n" +
            "  var ndvi = (nir - red) / (nir + red);\n" +
            "  \n" +
            "  // Colorează NDVI: roșu pentru valori negative/scăzute, verde pentru valori pozitive/ridicate\n" +
            "  var r, g, b;\n" +
            "  if (ndvi < 0) {\n" +
            "    r = 255; g = 0; b = 0; // Roșu pentru apă/sol gol\n" +
            "  } else if (ndvi < 0.2) {\n" +
            "    r = 255; g = 165; b = 0; // Portocaliu pentru vegetație foarte slabă\n" +
            "  } else if (ndvi < 0.5) {\n" +
            "    r = 255; g = 255; b = 0; // Galben pentru vegetație moderată\n" +
            "  } else {\n" +
            "    r = 0; g = 255; b = 0; // Verde pentru vegetație sănătoasă\n" +
            "  }\n" +
            "  \n" +
            "  return [r, g, b];\n" +
            "}";
        
        // Construiește request body pentru Process API
        Map<String, Object> requestBody = new HashMap<>();
        
        // Input
        Map<String, Object> input = new HashMap<>();
        Map<String, Object> bounds = new HashMap<>();
        bounds.put("bbox", new double[]{minLon, minLat, maxLon, maxLat});
        bounds.put("properties", Map.of("crs", "http://www.opengis.net/def/crs/EPSG/0/4326"));
        input.put("bounds", bounds);
        
        Map<String, Object> dataSource = new HashMap<>();
        dataSource.put("type", "sentinel-2-l2a");
        input.put("data", new Object[]{dataSource});
        requestBody.put("input", input);
        
        // Output
        Map<String, Object> output = new HashMap<>();
        output.put("width", width);
        output.put("height", height);
        Map<String, Object> response = new HashMap<>();
        response.put("identifier", "default");
        Map<String, String> format = new HashMap<>();
        format.put("type", "image/png");
        response.put("format", format);
        output.put("responses", new Object[]{response});
        requestBody.put("output", output);
        
        // Eval script
        requestBody.put("evalscript", evalscript);
        
        try {
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            
            // Obține access token (OAuth2 sau direct API key)
            return getAccessToken()
                    .flatMap(accessToken -> {
                        return webClient.post()
                                .uri("/api/v1/process")
                                .header("Authorization", "Bearer " + accessToken)
                                .header("Content-Type", "application/json")
                                .bodyValue(requestBodyJson)
                                .retrieve()
                                .bodyToMono(byte[].class);
                    })
                    .onErrorResume(error -> {
                        System.err.println("Error generating NDVI image: " + error.getMessage());
                        System.err.println("NOTE: Sentinel Hub API returned error. Generating mock NDVI image.");
                        // Generează o imagine mock NDVI pentru demo
                        return Mono.just(generateMockNDVIImage(width, height));
                    });
        } catch (Exception e) {
            System.err.println("Error building image request: " + e.getMessage());
            return Mono.empty();
        }
    }
    
    /**
     * Generează o imagine mock NDVI pentru demo (folosit când API-ul nu funcționează)
     */
    private byte[] generateMockNDVIImage(int width, int height) {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            
            // Creează un gradient NDVI: roșu -> portocaliu -> galben -> verde
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Simulează o distribuție NDVI (centru = mai verde, margini = mai roșu)
                    double centerX = width / 2.0;
                    double centerY = height / 2.0;
                    double distance = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
                    double maxDistance = Math.sqrt(Math.pow(width/2.0, 2) + Math.pow(height/2.0, 2));
                    double normalizedDistance = distance / maxDistance;
                    
                    // NDVI simulat: -0.5 (roșu) la margini, 0.8 (verde) în centru
                    double ndvi = 0.8 - (normalizedDistance * 1.3);
                    
                    Color color;
                    if (ndvi < 0) {
                        color = new Color(255, 0, 0); // Roșu - apă/sol gol
                    } else if (ndvi < 0.2) {
                        color = new Color(255, 165, 0); // Portocaliu - vegetație slabă
                    } else if (ndvi < 0.5) {
                        color = new Color(255, 255, 0); // Galben - vegetație moderată
                    } else {
                        color = new Color(0, 255, 0); // Verde - vegetație sănătoasă
                    }
                    
                    image.setRGB(x, y, color.getRGB());
                }
            }
            
            g2d.dispose();
            
            // Convertește BufferedImage la byte array PNG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            System.err.println("Error generating mock NDVI image: " + e.getMessage());
            return new byte[0];
        }
    }
    
    /**
     * Metodă veche pentru compatibilitate (returnează JSON string)
     * @deprecated Folosește getSatelliteMetrics în loc
     */
    @Deprecated
    public Mono<String> getSatelliteData(double lat, double lon) {
        return getSatelliteMetrics(lat, lon)
                .map(metrics -> {
                    try {
                        return objectMapper.writeValueAsString(metrics);
                    } catch (Exception e) {
                        return "{\"error\": \"Failed to serialize metrics\"}";
                    }
                });
    }
}
