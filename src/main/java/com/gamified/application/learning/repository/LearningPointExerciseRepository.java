package com.gamified.application.learning.repository;

import com.gamified.application.learning.model.entity.LearningPointExercise;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de la tabla intermedia learning_point_exercise
 * Maneja la asignación de ejercicios específicos a learning points de estudiantes
 */
public interface LearningPointExerciseRepository {
    
    /**
     * Asigna un ejercicio template a un learning point específico
     */
    Integer assignExerciseToLearningPoint(LearningPointExercise assignment);
    
    /**
     * Obtiene todos los ejercicios asignados a un learning point, ordenados por sequence_order
     */
    List<LearningPointExercise> findByLearningPointId(Integer learningPointId);
    
    /**
     * Obtiene un ejercicio específico asignado por ID
     */
    Optional<LearningPointExercise> findById(Integer id);
    
    /**
     * Actualiza una asignación de ejercicio (ej: marcar como completado, asignar ejercicio generado)
     */
    boolean updateAssignment(LearningPointExercise assignment);
    
    /**
     * Marca un ejercicio como completado para un learning point
     */
    boolean markAsCompleted(Integer learningPointId, Integer exerciseTemplateId);
    
    /**
     * Asigna un ejercicio generado específico a una asignación existente
     */
    boolean assignGeneratedExercise(Integer assignmentId, Long generatedExerciseId);
    
    /**
     * Cuenta ejercicios completados en un learning point
     */
    Integer countCompletedExercisesByLearningPoint(Integer learningPointId);
    
    /**
     * Cuenta total de ejercicios asignados a un learning point
     */
    Integer countTotalExercisesByLearningPoint(Integer learningPointId);
    
    /**
     * Obtiene el siguiente ejercicio pendiente para un learning point
     */
    Optional<LearningPointExercise> findNextPendingExercise(Integer learningPointId);
    
    /**
     * Elimina una asignación (marca como inactiva)
     */
    boolean deleteAssignment(Integer id);
    
    /**
     * Reordena los ejercicios de un learning point
     */
    boolean reorderExercises(Integer learningPointId, List<Integer> exerciseTemplateIds);
} 