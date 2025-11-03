package com.proiect.SCD.CropHealthAdvisor.config;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            // Dezactiveaza schema dropping pentru a evita erorile la foreign key-uri
            hibernateProperties.put("hibernate.hbm2ddl.auto", "validate");
            hibernateProperties.put("hibernate.schema-generation.script.action", "none");
        };
    }
}

