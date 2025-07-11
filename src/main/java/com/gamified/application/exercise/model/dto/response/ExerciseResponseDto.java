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
     * Estructura basada en GUIA.md para el frontend
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextExerciseDto {
        private Long generatedExerciseId; // ID del ejercicio generado (para tracking)
        private Integer exerciseTemplateId; // ID de la plantilla base
        private String exerciseType; // Tipo de ejercicio (ej. "drag_and_drop")
        private String renderVariant; // Variante de renderizado (ej. "drag-to-sort")
        private String difficultyLevel; // Nivel de dificultad
        
        // Configuración para el componente frontend
        private ExerciseConfigDto config;
        
        // Contenido generado por la IA
        private ExerciseContentDto content;
        
        // Metadatos adicionales
        private Integer learningPointId;
        private String learningPointTitle;
        private Boolean hasAttempts; // Si el estudiante ya intentó ejercicios de esta plantilla
        private Integer previousAttempts;
    }
    
    /**
     * DTO para la configuración del ejercicio (parte de NextExerciseDto)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseConfigDto {
        private Boolean showTimer;
        private Integer maxTime; // En segundos
        private Boolean allowPartialScore;
        private Integer itemCount; // Para ejercicios con múltiples elementos
        private Boolean shuffleItems;
        private Boolean showHints;
        private Boolean allowRetry;
    }
    
    /**
     * DTO para el contenido del ejercicio (parte de NextExerciseDto)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseContentDto {
        private String title;
        private String instructions;
        private Object options; // Puede ser List<String>, Map, etc. dependiendo del tipo
        private Object correctAnswer; // Estructura variable según el tipo de ejercicio
        private String explanation; // Explicación de la respuesta correcta
        private List<String> hints; // Pistas disponibles
        private String imageUrl; // URL de imagen si aplica
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