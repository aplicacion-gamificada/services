package com.gamified.application.learning.service;

import com.gamified.application.learning.model.entity.LearningPointExercise;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestionar la asignación de ejercicios a learning points específicos
 * Maneja la tabla intermedia learning_point_exercise que conecta learning points con exercise templates
 */
public interface LearningPointExerciseService {
    
    /**
     * Asigna un ejercicio template a un learning point con un orden específico
     * @param learningPointId ID del learning point
     * @param exerciseTemplateId ID del exercise template (plantilla)
     * @param sequenceOrder Orden en la secuencia del learning point
     * @return ID de la asignación creada
     */
    Integer assignExerciseToLearningPoint(Integer learningPointId, Integer exerciseTemplateId, Integer sequenceOrder);
    
    /**
     * Asigna un ejercicio completo con todos sus parámetros
     * @param assignment Objeto LearningPointExercise con todos los datos
     * @return ID de la asignación creada
     */
    Integer assignExercise(LearningPointExercise assignment);
    
    /**
     * Asigna un ejercicio generado específico a una asignación existente
     * @param assignmentId ID de la asignación de learning_point_exercise
     * @param generatedExerciseId ID del ejercicio generado
     * @return true si se asignó correctamente
     */
    boolean assignGeneratedExerciseToAssignment(Integer assignmentId, Long generatedExerciseId);
    
    /**
     * Obtiene el siguiente ejercicio pendiente para un learning point de un estudiante
     * @param learningPointId ID del learning point
     * @return Siguiente ejercicio pendiente o vacío si no hay más
     */
    Optional<LearningPointExercise> getNextPendingExercise(Integer learningPointId);
    
    /**
     * Marca un ejercicio como completado en un learning point
     * @param learningPointId ID del learning point
     * @param exerciseTemplateId ID del exercise template
     * @return true si se marcó como completado
     */
    boolean markExerciseAsCompleted(Integer learningPointId, Integer exerciseTemplateId);
    
    /**
     * Obtiene todos los ejercicios asignados a un learning point, ordenados por sequence_order
     * @param learningPointId ID del learning point
     * @return Lista de ejercicios asignados
     */
    List<LearningPointExercise> getExercisesForLearningPoint(Integer learningPointId);
    
    /**
     * Obtiene el progreso de ejercicios en un learning point
     * @param learningPointId ID del learning point
     * @return DTO con estadísticas de progreso
     */
    ProgressDto getLearningPointProgress(Integer learningPointId);
    
    /**
     * Reordena los ejercicios de un learning point
     * @param learningPointId ID del learning point
     * @param exerciseTemplateIds Lista ordenada de IDs de exercise templates
     * @return true si se reordenó correctamente
     */
    boolean reorderExercises(Integer learningPointId, List<Integer> exerciseTemplateIds);
    
    /**
     * DTO para representar progreso en un learning point
     */
    record ProgressDto(
        Integer totalExercises,
        Integer completedExercises,
        Double completionPercentage,
        Optional<LearningPointExercise> nextPending
    ) {}
} 