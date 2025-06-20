package com.gamified.application.learning.repository;

import com.gamified.application.learning.model.entity.StemArea;
import com.gamified.application.learning.model.entity.Specialization;
import com.gamified.application.learning.model.entity.Unit;
import com.gamified.application.learning.model.entity.LearningPoint;
import com.gamified.application.learning.model.entity.Lesson;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de la estructura de contenido educativo
 */
public interface LearningRepository {
    
    // ===================================================================
    // STEM AREAS
    // ===================================================================
    
    /**
     * Obtiene todas las áreas STEM activas
     * @return Lista de áreas STEM
     */
    List<StemArea> findAllActiveStemAreas();
    
    /**
     * Busca un área STEM por ID
     * @param stemAreaId ID del área STEM
     * @return Área STEM si existe
     */
    Optional<StemArea> findStemAreaById(Integer stemAreaId);
    
    // ===================================================================
    // SPECIALIZATIONS
    // ===================================================================
    
    /**
     * Obtiene todas las especializaciones de un área STEM
     * @param stemAreaId ID del área STEM
     * @return Lista de especializaciones
     */
    List<Specialization> findSpecializationsByStemArea(Integer stemAreaId);
    
    /**
     * Busca una especialización por ID
     * @param specializationId ID de la especialización
     * @return Especialización si existe
     */
    Optional<Specialization> findSpecializationById(Integer specializationId);
    
    // ===================================================================
    // MODULES
    // ===================================================================
    
         /**
      * Obtiene todos los módulos de una especialización
      * @param specializationId ID de la especialización
      * @return Lista de módulos ordenados por secuencia
      */
     List<com.gamified.application.learning.model.entity.Module> findModulesBySpecialization(Integer specializationId);
     
     /**
      * Busca un módulo por ID
      * @param moduleId ID del módulo
      * @return Módulo si existe
      */
     Optional<com.gamified.application.learning.model.entity.Module> findModuleById(Integer moduleId);
    
    // ===================================================================
    // UNITS
    // ===================================================================
    
    /**
     * Obtiene todas las unidades de un módulo
     * @param moduleId ID del módulo
     * @return Lista de unidades ordenadas por secuencia
     */
    List<Unit> findUnitsByModule(Integer moduleId);
    
    /**
     * Busca una unidad por ID
     * @param unitId ID de la unidad
     * @return Unidad si existe
     */
    Optional<Unit> findUnitById(Integer unitId);
    
    // ===================================================================
    // LEARNING POINTS
    // ===================================================================
    
    /**
     * Obtiene todos los learning points de una unidad
     * @param unitId ID de la unidad
     * @return Lista de learning points ordenados por secuencia
     */
    List<LearningPoint> findLearningPointsByUnit(Integer unitId);
    
    /**
     * Busca un learning point por ID
     * @param learningPointId ID del learning point
     * @return Learning point si existe
     */
    Optional<LearningPoint> findLearningPointById(Integer learningPointId);
    
    // ===================================================================
    // LESSONS
    // ===================================================================
    
    /**
     * Obtiene todas las lecciones de un learning point
     * @param learningPointId ID del learning point
     * @return Lista de lecciones ordenadas por secuencia
     */
    List<Lesson> findLessonsByLearningPoint(Integer learningPointId);
    
    /**
     * Busca una lección por ID
     * @param lessonId ID de la lección
     * @return Lección si existe
     */
    Optional<Lesson> findLessonById(Integer lessonId);
    
    /**
     * Busca la lección anterior a una dada
     * @param learningPointId ID del learning point
     * @param currentSequence Secuencia actual
     * @return Lección anterior si existe
     */
    Optional<Lesson> findPreviousLesson(Integer learningPointId, Integer currentSequence);
    
    /**
     * Busca la lección siguiente a una dada
     * @param learningPointId ID del learning point
     * @param currentSequence Secuencia actual
     * @return Lección siguiente si existe
     */
    Optional<Lesson> findNextLesson(Integer learningPointId, Integer currentSequence);
    
    // ===================================================================
    // CONTEOS (para DTOs)
    // ===================================================================
    
    /**
     * Cuenta las especializaciones de un área STEM
     * @param stemAreaId ID del área STEM
     * @return Número de especializaciones
     */
    Integer countSpecializationsByStemArea(Integer stemAreaId);
    
    /**
     * Cuenta los módulos de una especialización
     * @param specializationId ID de la especialización
     * @return Número de módulos
     */
    Integer countModulesBySpecialization(Integer specializationId);
    
    /**
     * Cuenta las unidades de un módulo
     * @param moduleId ID del módulo
     * @return Número de unidades
     */
    Integer countUnitsByModule(Integer moduleId);
    
    /**
     * Cuenta los learning points de una unidad
     * @param unitId ID de la unidad
     * @return Número de learning points
     */
    Integer countLearningPointsByUnit(Integer unitId);
    
    /**
     * Cuenta las lecciones de un learning point
     * @param learningPointId ID del learning point
     * @return Número de lecciones
     */
    Integer countLessonsByLearningPoint(Integer learningPointId);
    
    /**
     * Cuenta los ejercicios de un learning point
     * @param learningPointId ID del learning point
     * @return Número de ejercicios
     */
    Integer countExercisesByLearningPoint(Integer learningPointId);
} 