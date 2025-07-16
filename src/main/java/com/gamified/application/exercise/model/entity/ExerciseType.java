package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un tipo de ejercicio actualizada según el nuevo esquema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseType {
    private Integer id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
        
    private String supportedSubtypes; // Campo: supported_subtypes - JSON con subtipos soportados
    private String defaultConfig; // Campo: default_config - Configuración por defecto en JSON
    private String validationRules; // Campo: validation_rules - Reglas de validación en JSON
    private String generationStrategy; // Campo: generation_strategy - Estrategia de generación del ejercicio
    
    // private String difficultyLevel; // ELIMINADO - la dificultad es específica de cada ejercicio
    // private Integer status; // ELIMINADO - se deriva de created_at y otros timestamps
    
    /**
     * Obtiene los subtipos soportados como lista
     * @return Lista de subtipos o lista vacía si no hay subtipos
     */
    public java.util.List<String> getSupportedSubtypesAsList() {
        if (supportedSubtypes == null || supportedSubtypes.trim().isEmpty()) {
            return new java.util.ArrayList<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(supportedSubtypes, new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>() {});
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Obtiene la configuración por defecto como mapa
     * @return Map con la configuración o mapa vacío si no hay configuración
     */
    public java.util.Map<String, Object> getDefaultConfigAsMap() {
        if (defaultConfig == null || defaultConfig.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(defaultConfig, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
    
    /**
     * Obtiene las reglas de validación como mapa
     * @return Map con las reglas o mapa vacío si no hay reglas
     */
    public java.util.Map<String, Object> getValidationRulesAsMap() {
        if (validationRules == null || validationRules.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(validationRules, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
} 