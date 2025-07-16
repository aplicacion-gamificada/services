package com.gamified.application.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuración para las funcionalidades de IA en ejercicios con Azure AI Foundry
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ExerciseAIConfig {

    @Value("${azure.ai.foundry.timeout:60}")
    private Integer timeoutSeconds;

    /**
     * Bean para RestTemplate usado en las llamadas a Azure AI Foundry
     * Configurado con timeouts y configuraciones optimizadas
     */
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        
        // Configurar timeouts
        factory.setConnectTimeout(Duration.ofSeconds(10)); // 10 segundos para conectar
        factory.setConnectionRequestTimeout(Duration.ofSeconds(5)); // 5 segundos para obtener conexión del pool
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds)); // Timeout configurable para leer respuesta
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        return restTemplate;
    }

    /**
     * Bean para RestTemplate específico para operaciones de health check
     * Con timeouts más cortos para verificaciones rápidas
     */
    @Bean("healthCheckRestTemplate")
    public RestTemplate healthCheckRestTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        
        // Timeouts más cortos para health checks
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setConnectionRequestTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(10));
        
        return new RestTemplate(factory);
    }
}
