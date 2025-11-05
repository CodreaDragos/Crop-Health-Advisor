package com.proiect.SCD.CropHealthAdvisor.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service for obtaining weather data (temperature, precipitation)
* Uses OpenWeatherMap API (free up to 1000 requests/day)
 */
@Service
public class WeatherService {
    
    @Value("${openweathermap.api.key:}")
    private String openWeatherMapApiKey;
    
    private final WebClient webClient;
    
    public WeatherService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openweathermap.org")
                .build();
    }
    
    /**
     * Get temperature and precipitation for a location
     * @param lat Latitude
     * @param lon Longitude
     * @return Map with "temperature" (in °C) and "precipitation" (in mm)
     */
    public Mono<Map<String, Double>> getWeatherData(double lat, double lon) {
        // If no API key, return mock data
        if (openWeatherMapApiKey == null || openWeatherMapApiKey.isEmpty()) {
            return Mono.just(Map.of(
                "temperature", Math.random() * 30.0 + 5.0,
                "precipitation", Math.random() * 80.0
            ));
        }
        
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/weather")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", openWeatherMapApiKey)
                            .queryParam("units", "metric")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        // Extract temperature (in °C)
                        Map<String, Object> main = (Map<String, Object>) response.get("main");
                        Double temperature = null;
                        if (main != null && main.get("temp") != null) {
                            temperature = ((Number) main.get("temp")).doubleValue();
                        }
                        
                        // Extract precipitation (if exists)
                        Map<String, Object> rain = (Map<String, Object>) response.get("rain");
                        Double precipitation = 0.0;
                        if (rain != null) {
                            // Can be "1h" or "3h" depending on API
                            if (rain.get("1h") != null) {
                                precipitation = ((Number) rain.get("1h")).doubleValue();
                            } else if (rain.get("3h") != null) {
                                precipitation = ((Number) rain.get("3h")).doubleValue() / 3.0; // Average per hour
                            }
                        }
                        
                        // Extract precipitation from "snow" if exists
                        Map<String, Object> snow = (Map<String, Object>) response.get("snow");
                        if (snow != null && snow.get("1h") != null) {
                            precipitation += ((Number) snow.get("1h")).doubleValue();
                        }
                        
                        return Map.of(
                            "temperature", temperature != null ? temperature : (Math.random() * 30.0 + 5.0),
                            "precipitation", Math.max(0.0, precipitation)
                        );
                    })
                    .onErrorResume(error -> {
                        System.err.println("Error fetching weather data from OpenWeatherMap: " + error.getMessage());
                        // Fallback to mock
                        return Mono.just(Map.of(
                            "temperature", Math.random() * 30.0 + 5.0,
                            "precipitation", Math.random() * 80.0
                        ));
                    });
        } catch (Exception e) {
            System.err.println("Error building weather request: " + e.getMessage());
            return Mono.just(Map.of(
                "temperature", Math.random() * 30.0 + 5.0,
                "precipitation", Math.random() * 80.0
            ));
        }
    }
}

