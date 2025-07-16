package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una plantilla de prompt para generar ejercicios con IA
 * Mapea a la tabla 'prompt_template' en la base de datos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    private Integer id;
    private String name;
    private String templateText; // Texto del prompt con placeholders como {{dificultad}}, {{competencia}}
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Campo eliminado: exerciseTypeId (ya no hay FK a exercise_type)
    private String exerciseSubtype; // Campo: exercise_subtype - Subtipo específico del ejercicio
    private String generationParameters; // Campo: generation_parameters - Parámetros de generación en JSON
    private String validationRules; // Campo: validation_rules - Reglas de validación específicas
    private String sampleOutput; // Campo: sample_output - Ejemplo de salida esperada
    
    // Relación virtual eliminada (ya no hay FK a exercise_type)
    // private ExerciseType exerciseType;
    
    /**
     * Obtiene los parámetros de generación como mapa
     * @return Map con los parámetros o mapa vacío si no hay parámetros
     */
    public java.util.Map<String, Object> getGenerationParametersAsMap() {
        if (generationParameters == null || generationParameters.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(generationParameters, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
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