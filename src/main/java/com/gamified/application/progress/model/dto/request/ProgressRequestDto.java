package com.gamified.application.progress.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTOs de request para el módulo Progress
 */
public class ProgressRequestDto {

    /**
     * DTO para crear un learning path
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateLearningPathDto {
        
        @NotNull(message = "El ID del estudiante es requerido")
        @Positive(message = "El ID del estudiante debe ser positivo")
        private Integer studentProfileId;
        
        @NotNull(message = "El ID de la unidad es requerido")
        @Positive(message = "El ID de la unidad debe ser positivo")
        private Integer unitsId;
        
        @NotNull(message = "El ID del learning point inicial es requerido")
        @Positive(message = "El ID del learning point inicial debe ser positivo")
        private Integer startingLearningPointId;
        
        // Configuración opcional de dificultad
        private BigDecimal difficultyAdjustment;
    }

    /**
     * DTO para completar una lección
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompleteLessonDto {
        
        @NotNull(message = "El ID del estudiante es requerido")
        @Positive(message = "El ID del estudiante debe ser positivo")
        private Integer studentProfileId;
        
        @NotNull(message = "El ID de la lección es requerido")
        @Positive(message = "El ID de la lección debe ser positivo")
        private Integer lessonId;
        
        // Tiempo opcional que pasó estudiando
        private Integer timeSpentMinutes;
    }
} 