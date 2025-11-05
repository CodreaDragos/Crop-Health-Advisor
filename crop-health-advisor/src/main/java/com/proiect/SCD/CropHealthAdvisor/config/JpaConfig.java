package com.proiect.SCD.CropHealthAdvisor.config;

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {

    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            // Disable schema dropping to avoid foreign key errors
            hibernateProperties.put("hibernate.hbm2ddl.auto", "validate");
            hibernateProperties.put("hibernate.schema-generation.script.action", "none");
        };
    }
}

