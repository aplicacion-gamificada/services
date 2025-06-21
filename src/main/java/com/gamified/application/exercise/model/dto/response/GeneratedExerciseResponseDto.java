package com.gamified.application.exercise.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTOs de respuesta para ejercicios generados por IA
 */
public class GeneratedExerciseResponseDto {

    /**
     * DTO para entregar un ejercicio generado por IA al frontend
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedExerciseDto {
        private Long generatedExerciseId;
        private Integer exerciseTemplateId;
        private String exerciseContent; // JSON parseable que contiene el ejercicio completo
        private String exerciseType;
        private String difficulty;
        private Integer estimatedTimeMinutes;
        private Integer pointsValue;
        private String learningPointTitle;
        private LocalDateTime generatedAt;
        private String aiModelVersion;
    }

    /**
     * DTO para el resultado de evaluación de un intento contra un ejercicio generado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedExerciseAttemptResultDto {
        private Integer attemptId;
        private Long generatedExerciseId;
        private Boolean isCorrect;
        private Double score;
        private Integer pointsEarned;
        private String detailedFeedback; // JSON con feedback específico del ejercicio
        private String correctAnswerExplanation;
        private Integer timeSpentSeconds;
        private Integer attemptNumber;
        private GeneratedExerciseDto nextSuggestedExercise; // Siguiente ejercicio recomendado
    }

    /**
     * DTO para estadísticas de ejercicios generados por IA
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedExerciseStatsDto {
        private Integer totalGeneratedExercises;
        private Integer totalAttempts;
        private Double averageGenerationTime;
        private Double cacheHitRate; // Porcentaje de ejercicios servidos desde el pool
        private String mostUsedAiModel;
        private Integer successfulGenerations;
        private Integer failedGenerations;
    }
}
