package com.proiect.SCD.CropHealthAdvisor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Aplica regulile CORS pe toate endpoint-urile /api
                .allowedOrigins("http://localhost:5173", "http://localhost:5174") // Permite ambele porturi comune pentru Vite
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Include OPTIONS pentru preflight
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Cache preflight requests pentru 1 oră
    }
}
