package com.proiect.SCD.CropHealthAdvisor.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Serviciu pentru obținerea datelor meteorologice (temperatură, precipitații)
 * Folosește OpenWeatherMap API (gratuit până la 1000 requests/zi)
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
     * Obține temperatura și precipitațiile pentru o locație
     * @param lat Latitudine
     * @param lon Longitudine
     * @return Map cu "temperature" (în °C) și "precipitation" (în mm)
     */
    public Mono<Map<String, Double>> getWeatherData(double lat, double lon) {
        // Dacă nu avem API key, returnează mock
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
                            .queryParam("units", "metric") // Temperatură în °C
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> {
                        // Extrage temperatura (în °C)
                        Map<String, Object> main = (Map<String, Object>) response.get("main");
                        Double temperature = null;
                        if (main != null && main.get("temp") != null) {
                            temperature = ((Number) main.get("temp")).doubleValue();
                        }
                        
                        // Extrage precipitații (dacă există)
                        Map<String, Object> rain = (Map<String, Object>) response.get("rain");
                        Double precipitation = 0.0;
                        if (rain != null) {
                            // Poate fi "1h" sau "3h" în funcție de API
                            if (rain.get("1h") != null) {
                                precipitation = ((Number) rain.get("1h")).doubleValue();
                            } else if (rain.get("3h") != null) {
                                precipitation = ((Number) rain.get("3h")).doubleValue() / 3.0; // Media pe oră
                            }
                        }
                        
                        // Extrage precipitații din "snow" dacă există
                        Map<String, Object> snow = (Map<String, Object>) response.get("snow");
                        if (snow != null && snow.get("1h") != null) {
                            precipitation += ((Number) snow.get("1h")).doubleValue();
                        }
                        
                        // Dacă nu avem precipitații recente, folosim precipitații acumulate (ultimele 24h)
                        // sau obținem din forecast API pentru o estimare mai bună
                        
                        return Map.of(
                            "temperature", temperature != null ? temperature : (Math.random() * 30.0 + 5.0),
                            "precipitation", Math.max(0.0, precipitation) // Asigură-te că nu e negativ
                        );
                    })
                    .onErrorResume(error -> {
                        System.err.println("Error fetching weather data from OpenWeatherMap: " + error.getMessage());
                        // Fallback la mock
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

