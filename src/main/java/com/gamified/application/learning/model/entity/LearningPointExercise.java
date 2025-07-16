package com.gamified.application.learning.model.entity;

import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.GeneratedExercise;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa la asignación de un ejercicio específico a un learning point de un estudiante
 * Tabla intermedia entre learning_point y exercise templates, con ejercicios generados opcionales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPointExercise {
    private Integer id;
    private Integer learningPointId; // FK a learning_point
    private Long generatedExerciseId; // FK a generated_exercise (NULL si aún no se genera)
    private Integer exerciseTemplateId; // FK a exercise (plantilla)
    private Integer sequenceOrder; // Orden específico para este learning_point
    private Integer isCompleted; // 0 = no completado, 1 = completado
    private LocalDateTime assignedAt; // Cuándo se asignó
    private LocalDateTime completedAt; // Cuándo se completó (NULL si no completado)
    private Integer isActive; // 1 = activo, 0 = inactivo
    
    // Relaciones virtuales
    private LearningPoint learningPoint;
    private GeneratedExercise generatedExercise;
    private Exercise exerciseTemplate;
    
    // Métodos de conveniencia
    public boolean isCompleted() {
        return isCompleted != null && isCompleted == 1;
    }
    
    public boolean isActive() {
        return isActive == null || isActive == 1;
    }
    
    public boolean hasGeneratedExercise() {
        return generatedExerciseId != null;
    }
} 