package com.gamified.application.exercise.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs de response para operaciones de Exercise
 */
public class ExerciseResponseDto {

    /**
     * DTO para el siguiente ejercicio disponible
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextExerciseDto {
        private Integer exerciseId;
        private Integer learningPointId;
        private String learningPointTitle;
        private String exerciseTypeId;
        private String exerciseTypeName;
        private String title;
        private String questionText;
        private List<String> possibleAnswers; // Parsed from JSON
        private String difficulty;
        private List<String> hints; // Parsed from JSON
        private Integer estimatedTimeMinutes;
        private Boolean hasAttempts; // If student has attempted this exercise
        private Integer previousAttempts;
    }

    /**
     * DTO para el resultado de un intento
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptResultDto {
        private Integer attemptId;
        private Integer exerciseId;
        private Boolean isCorrect;
        private Double score;
        private String feedback; // Parsed feedback
        private String correctAnswer;
        private Integer timeSpentSeconds;
        private Integer attemptNumber;
        private LocalDateTime submittedAt;
        private NextExerciseDto nextExercise; // Suggested next exercise
    }

    /**
     * DTO para historial de intentos
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptHistoryDto {
        private Integer attemptId;
        private Integer exerciseId;
        private String exerciseTitle;
        private String exerciseTypeName;
        private String difficulty;
        private Boolean isCorrect;
        private Double score;
        private Integer timeSpentSeconds;
        private Integer attemptNumber;
        private LocalDateTime submittedAt;
        
        // Learning context
        private Integer learningPointId;
        private String learningPointTitle;
    }

    /**
     * DTO para ejercicios completados
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompletedExerciseDto {
        private Integer exerciseId;
        private String exerciseTitle;
        private String exerciseTypeName;
        private String difficulty;
        private Integer totalAttempts;
        private Double bestScore;
        private Boolean isCompleted;
        private LocalDateTime completedAt;
        
        // Learning context
        private Integer learningPointId;
        private String learningPointTitle;
        
        // Performance metrics
        private Integer averageTimeSeconds;
        private Double averageScore;
    }

    /**
     * DTO para estadísticas de ejercicios del estudiante
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentExerciseStatsDto {
        private Integer totalExercisesAttempted;
        private Integer totalExercisesCompleted;
        private Double averageScore;
        private Integer totalTimeSpentMinutes;
        private String preferredDifficulty;
        private List<ExerciseTypeStatsDto> exerciseTypeStats;
    }

    /**
     * DTO para estadísticas por tipo de ejercicio
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseTypeStatsDto {
        private Integer exerciseTypeId;
        private String exerciseTypeName;
        private Integer totalAttempts;
        private Integer totalCompleted;
        private Double averageScore;
        private String strongestDifficulty;
    }
} 