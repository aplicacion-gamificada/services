package com.gamified.application.learning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una plantilla de lección asociada a una Unit
 * Esta es una plantilla general, no específica de un learning_point
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    private Integer id;
    private Integer unitId; // NUEVO: Asociación a Unit en lugar de learning_point
    private String title;
    private String contentData;
    // ELIMINADO: sequenceOrder - ahora va en la tabla intermedia learning_point_lesson
    private Integer estimatedReadingTime;
    private Integer isMandatory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // MÉTODOS DEPRECATED - para compatibilidad temporal
    @Deprecated
    public Integer getLearningPointId() {
        // Ya no existe este campo - retornar null para evitar errores
        return null;
    }
    
    @Deprecated
    public Integer getSequenceOrder() {
        // Ya no existe este campo - retornar null para evitar errores
        return null;
    }
} 