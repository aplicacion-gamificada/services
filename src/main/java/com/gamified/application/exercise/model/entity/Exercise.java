package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un ejercicio generado para un learning point
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Exercise {
    private Integer id;
    private Integer learningPointId;
    private Integer exerciseTypeId;
    private String title;
    private String questionText;
    private String correctAnswer;
    private String possibleAnswers; // JSON array with options
    private String difficulty;
    private String metadata; // JSON with additional exercise data
    private String hints; // JSON array with hints
    private Integer estimatedTimeMinutes;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 