package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un ejercicio específico generado por IA
 * Mapea a la tabla 'generated_exercise' en la base de datos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedExercise {
    private Long id;
    private Integer exerciseTemplateId; // FK a la tabla 'exercise' (plantilla)
    private String generatedContentJson; // Contenido JSON completo devuelto por la IA
    private String correctAnswerHash; // Hash SHA256 de la respuesta correcta para validaciones rápidas
    private String generationPrompt; // Prompt exacto enviado a la IA
    private String aiModelVersion; // Versión del modelo AI usado (ej: 'gpt-4-turbo-2024-04-09')
    private LocalDateTime createdAt;
    
    // Relación virtual con la plantilla del ejercicio
    private Exercise exerciseTemplate;
}
