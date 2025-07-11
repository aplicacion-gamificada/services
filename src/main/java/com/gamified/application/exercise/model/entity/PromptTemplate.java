package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una plantilla de prompt para generar ejercicios con IA
 * Mapea a la tabla 'prompt_template' en la base de datos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    private Integer id;
    private String name;
    private String templateText; // Texto del prompt con placeholders como {{dificultad}}, {{competencia}}
    private Integer exerciseTypeId; // FK a exercise_type
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relación virtual con el tipo de ejercicio
    private ExerciseType exerciseType;
} 