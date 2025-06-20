package com.gamified.application.exercise.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs de request para operaciones de Exercise
 */
public class ExerciseRequestDto {

    /**
     * DTO para enviar respuesta a un ejercicio
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubmitAttemptDto {
        @NotNull(message = "Exercise ID es obligatorio")
        private Integer exerciseId;
        
        @NotNull(message = "Student Profile ID es obligatorio")
        private Integer studentProfileId;
        
        @NotBlank(message = "La respuesta es obligatoria")
        private String submittedAnswer;
        
        @Min(value = 0, message = "El tiempo empleado debe ser positivo")
        private Integer timeSpentSeconds;
        
        @Min(value = 0, message = "El número de pistas usadas debe ser positivo")
        private Integer hintsUsed;
    }

    /**
     * DTO para solicitar el siguiente ejercicio
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextExerciseRequestDto {
        @NotNull(message = "Student Profile ID es obligatorio")
        private Integer studentProfileId;
        
        @NotNull(message = "Learning Point ID es obligatorio")
        private Integer learningPointId;
        
        private String preferredDifficulty; // "easy", "medium", "hard"
        private String excludeExerciseTypes; // JSON array of exercise type IDs to exclude
    }
} 