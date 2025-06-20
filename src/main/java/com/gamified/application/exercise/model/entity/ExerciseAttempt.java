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
    private Integer exerciseId;
    private Integer studentProfileId;
    private String submittedAnswer;
    private Boolean isCorrect;
    private Integer timeSpentSeconds;
    private Integer hintsUsed;
    private Double score;
    private String feedback; // JSON with detailed feedback
    private Integer attemptNumber; // Track multiple attempts
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
} 