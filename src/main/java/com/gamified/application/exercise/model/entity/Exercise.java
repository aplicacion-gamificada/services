package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una plantilla de ejercicio según el modelo UML
 * Esta es una plantilla que se usa para generar ejercicios específicos con IA
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
    private Integer learningPointId;
    private Integer estimatedTime;
    private Integer pointsValue;
    private Integer sequenceOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer promptTemplateId;
    
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
} 