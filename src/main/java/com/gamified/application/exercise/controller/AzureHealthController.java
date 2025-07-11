package com.gamified.application.exercise.controller;

import com.gamified.application.exercise.service.AzureAiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para verificar el estado de salud de Azure AI Foundry
 * y obtener métricas de rendimiento
 */
@RestController
@RequestMapping("/azure-ai")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Azure AI Foundry Health", description = "Endpoints para verificar el estado de Azure AI Foundry")
public class AzureHealthController {

    private final AzureAiClient azureAiClient;

    /**
     * Verifica el estado de salud de Azure AI Foundry
     */
    @GetMapping("/health")
    @Operation(summary = "Verificar estado de salud de Azure AI Foundry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.info("Verificando estado de salud de Azure AI Foundry");
        
        try {
            Map<String, Object> health = azureAiClient.healthCheck();
            
            String status = (String) health.get("status");
            if ("UP".equals(status)) {
                return ResponseEntity.ok(health);
            } else {
                return ResponseEntity.status(503).body(health); // Service Unavailable
            }
            
        } catch (Exception e) {
            log.error("Error verificando estado de Azure AI Foundry", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * Obtiene métricas de rendimiento de Azure AI Foundry
     */
    @GetMapping("/metrics")
    @Operation(summary = "Obtener métricas de rendimiento de Azure AI Foundry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        log.info("Obteniendo métricas de Azure AI Foundry");
        
        try {
            Map<String, Object> metrics = azureAiClient.getPerformanceMetrics();
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Error obteniendo métricas de Azure AI Foundry", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * Prueba la generación de un ejercicio simple
     */
    @PostMapping("/test-generation")
    @Operation(summary = "Probar generación de ejercicio con Azure AI Foundry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testGeneration(
            @RequestParam(defaultValue = "Genera un ejercicio simple de suma de números enteros para nivel básico") 
            String testPrompt) {
        
        log.info("Probando generación de ejercicio con Azure AI Foundry");
        
        try {
            long startTime = System.currentTimeMillis();
            String result = azureAiClient.generateExerciseContent(testPrompt);
            long endTime = System.currentTimeMillis();
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "prompt", testPrompt,
                "result", result,
                "response_time_ms", endTime - startTime,
                "result_length", result.length(),
                "timestamp", System.currentTimeMillis()
            ));
            
        } catch (Exception e) {
            log.error("Error probando generación con Azure AI Foundry", e);
            return ResponseEntity.status(500).body(Map.of(
                "status", "ERROR",
                "prompt", testPrompt,
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * Obtiene información de configuración (sin datos sensibles)
     */
    @GetMapping("/config")
    @Operation(summary = "Obtener información de configuración de Azure AI Foundry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        log.info("Obteniendo configuración de Azure AI Foundry");
        
        try {
            Map<String, Object> config = Map.of(
                "is_configured", azureAiClient.isConfigured(),
                "endpoint_configured", azureAiClient.getPerformanceMetrics().get("deployment_name") != null,
                "deployment_name", azureAiClient.getPerformanceMetrics().get("deployment_name"),
                "model_version", azureAiClient.getPerformanceMetrics().get("model_version"),
                "max_tokens", azureAiClient.getPerformanceMetrics().get("max_tokens"),
                "temperature", azureAiClient.getPerformanceMetrics().get("temperature"),
                "timeout_seconds", azureAiClient.getPerformanceMetrics().get("timeout_seconds"),
                "generation_enabled", azureAiClient.getPerformanceMetrics().get("generation_enabled"),
                "fallback_enabled", azureAiClient.getPerformanceMetrics().get("fallback_enabled")
            );
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            log.error("Error obteniendo configuración de Azure AI Foundry", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
} 