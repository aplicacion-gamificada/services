package com.gamified.application.learning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una lección específica generada por IA
 * Mapea a la tabla 'generated_lesson' en la base de datos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedLesson {
    private Long id;
    private Integer lessonTemplateId; // FK a la tabla 'lesson' (plantilla)
    private String generatedContentJson; // Contenido JSON completo devuelto por la IA
    private String generationPrompt; // Prompt usado para generar la lección
    private String aiModelVersion; // Versión del modelo AI usado
    private LocalDateTime createdAt;
    
    // Relación virtual con la plantilla de la lección
    private Lesson lessonTemplate;
} 