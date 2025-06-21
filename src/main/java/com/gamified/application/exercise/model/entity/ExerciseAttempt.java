package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un intento de ejercicio por parte de un estudiante
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAttempt {
    private Integer id;
    private Integer exerciseId; // Ahora actúa como exercise_template_id (FK a la plantilla)
    private Integer studentProfileId;
    private Long generatedExerciseId; // FK al ejercicio específico generado por IA
    private String submittedAnswer;
    private Boolean isCorrect;
    private Integer timeSpentSeconds;
    private Integer hintsUsed;
    private Double score;
    private String feedback; // JSON with detailed feedback
    private Integer attemptNumber; // Track multiple attempts
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
    
    // Relaciones virtuales
    private GeneratedExercise generatedExercise;
    private Exercise exerciseTemplate;
} 