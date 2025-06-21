package com.gamified.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración para las funcionalidades de IA en ejercicios
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ExerciseAIConfig {

    /**
     * Bean para RestTemplate usado en las llamadas a Azure OpenAI
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
