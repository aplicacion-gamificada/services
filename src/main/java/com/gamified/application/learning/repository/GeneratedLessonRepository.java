package com.gamified.application.learning.repository;

import com.gamified.application.learning.model.entity.GeneratedLesson;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de GeneratedLesson
 * Maneja las lecciones específicas generadas por IA
 */
public interface GeneratedLessonRepository {
    
    /**
     * Guarda una nueva lección generada
     */
    Long save(GeneratedLesson generatedLesson);
    
    /**
     * Busca una lección generada por ID
     */
    Optional<GeneratedLesson> findById(Long id);
    
    /**
     * Busca lecciones generadas por template ID
     */
    List<GeneratedLesson> findByLessonTemplateId(Integer lessonTemplateId);
    
    /**
     * Busca lecciones generadas recientes por template (para reutilización)
     */
    List<GeneratedLesson> findRecentByLessonTemplateId(Integer lessonTemplateId, int limit);
    
    /**
     * Actualiza una lección generada
     */
    boolean update(GeneratedLesson generatedLesson);
    
    /**
     * Elimina una lección generada
     */
    boolean delete(Long id);
    
    /**
     * Cuenta lecciones generadas por template
     */
    Integer countByLessonTemplateId(Integer lessonTemplateId);
} 