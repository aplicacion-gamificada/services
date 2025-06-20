package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un tipo de ejercicio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseType {
    private Integer id;
    private String name;
    private String description;
    private String difficultyLevel;
    private Integer status;
} 