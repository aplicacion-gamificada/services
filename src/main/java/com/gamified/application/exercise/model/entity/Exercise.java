package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una plantilla de ejercicio según el modelo UML actualizado
 * Esta es una plantilla general asociada a una Unit, no a un learning_point específico
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exercise {
    private Integer id;
    private Integer exerciseTypeId;
    private Integer competencyId;
    private Integer difficultyLevelId;
    private String title;
    private String description;
    private String instructions;
    private Integer unitId; // NUEVO: Asociación a Unit en lugar de learning_point
    private Integer estimatedTime;
    private Integer pointsValue;
    // ELIMINADO: sequenceOrder - ahora va en la tabla intermedia learning_point_exercise
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer promptTemplateId;
    
    private String exerciseConfig; // Campo: exercise_config - Configuración JSON del ejercicio
    private String exerciseSubtype; // Campo: exercise_subtype - Subtipo específico (ej: "multiple-choice-images")
    private String generationRules; // Campo: generation_rules - Reglas específicas para generación con IA
    private String difficultyParameters; // Campo: difficulty_parameters - Parámetros personalizados de dificultad
    
    // Relaciones virtuales
    private PromptTemplate promptTemplate;
    private ExerciseType exerciseType;
    
    // Propiedades derivadas para compatibilidad con código existente
    public String getDifficulty() {
        return difficultyLevelId != null ? "level_" + difficultyLevelId : "medium";
    }
    
    public Integer getEstimatedTimeMinutes() {
        return estimatedTime;
    }
    
    // MÉTODO DEPRECATED - para compatibilidad temporal
    @Deprecated
    public Integer getLearningPointId() {
        // Ya no existe este campo - retornar null para evitar errores
        return null;
    }
    
    // MÉTODO DEPRECATED - para compatibilidad temporal  
    @Deprecated
    public Integer getSequenceOrder() {
        // Ya no existe este campo - retornar null para evitar errores
        return null;
    }
    
    /**
     * Obtiene la configuración del ejercicio como objeto JSON parseado
     * @return Map con la configuración o mapa vacío si no hay configuración
     */
    public java.util.Map<String, Object> getExerciseConfigAsMap() {
        if (exerciseConfig == null || exerciseConfig.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(exerciseConfig, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
    
    /**
     * Obtiene las reglas de generación como objeto JSON parseado
     * @return Map con las reglas o mapa vacío si no hay reglas
     */
    public java.util.Map<String, Object> getGenerationRulesAsMap() {
        if (generationRules == null || generationRules.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(generationRules, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
    
    /**
     * Obtiene los parámetros de dificultad como objeto JSON parseado
     * @return Map con los parámetros o mapa vacío si no hay parámetros
     */
    public java.util.Map<String, Object> getDifficultyParametersAsMap() {
        if (difficultyParameters == null || difficultyParameters.trim().isEmpty()) {
            return new java.util.HashMap<>();
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(difficultyParameters, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
        } catch (Exception e) {
            return new java.util.HashMap<>();
        }
    }
} 