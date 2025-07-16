package com.gamified.application.learning.repository;

import com.gamified.application.learning.model.entity.LearningPointLesson;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de la tabla intermedia learning_point_lesson
 * Maneja la asignación de lecciones específicas a learning points de estudiantes
 */
public interface LearningPointLessonRepository {
    
    /**
     * Asigna una lección template a un learning point específico
     */
    Integer assignLessonToLearningPoint(LearningPointLesson assignment);
    
    /**
     * Obtiene todas las lecciones asignadas a un learning point, ordenadas por sequence_order
     */
    List<LearningPointLesson> findByLearningPointId(Integer learningPointId);
    
    /**
     * Obtiene una lección específica asignada por ID
     */
    Optional<LearningPointLesson> findById(Integer id);
    
    /**
     * Actualiza una asignación de lección (ej: marcar como completado, asignar lección generada)
     */
    boolean updateAssignment(LearningPointLesson assignment);
    
    /**
     * Marca una lección como completada para un learning point
     */
    boolean markAsCompleted(Integer learningPointId, Integer lessonTemplateId);
    
    /**
     * Asigna una lección generada específica a una asignación existente
     */
    boolean assignGeneratedLesson(Integer assignmentId, Long generatedLessonId);
    
    /**
     * Cuenta lecciones completadas en un learning point
     */
    Integer countCompletedLessonsByLearningPoint(Integer learningPointId);
    
    /**
     * Cuenta total de lecciones asignadas a un learning point
     */
    Integer countTotalLessonsByLearningPoint(Integer learningPointId);
    
    /**
     * Obtiene la siguiente lección pendiente para un learning point
     */
    Optional<LearningPointLesson> findNextPendingLesson(Integer learningPointId);
    
    /**
     * Elimina una asignación (marca como inactiva)
     */
    boolean deleteAssignment(Integer id);
    
    /**
     * Reordena las lecciones de un learning point
     */
    boolean reorderLessons(Integer learningPointId, List<Integer> lessonTemplateIds);
} 