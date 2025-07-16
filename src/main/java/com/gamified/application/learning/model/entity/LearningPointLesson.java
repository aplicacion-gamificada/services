package com.gamified.application.learning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa la asignación de una lección específica a un learning point de un estudiante
 * Tabla intermedia entre learning_point y lesson templates, con lecciones generadas opcionales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPointLesson {
    private Integer id;
    private Integer learningPointId; // FK a learning_point
    private Long generatedLessonId; // FK a generated_lesson (NULL si aún no se genera)
    private Integer lessonTemplateId; // FK a lesson (plantilla)
    private Integer sequenceOrder; // Orden específico para este learning_point
    private Integer isCompleted; // 0 = no completado, 1 = completado
    private LocalDateTime assignedAt; // Cuándo se asignó
    private LocalDateTime completedAt; // Cuándo se completó (NULL si no completado)
    private Integer isActive; // 1 = activo, 0 = inactivo
    
    // Relaciones virtuales
    private LearningPoint learningPoint;
    private GeneratedLesson generatedLesson;
    private Lesson lessonTemplate;
    
    // Métodos de conveniencia
    public boolean isCompleted() {
        return isCompleted != null && isCompleted == 1;
    }
    
    public boolean isActive() {
        return isActive == null || isActive == 1;
    }
    
    public boolean hasGeneratedLesson() {
        return generatedLessonId != null;
    }
} 