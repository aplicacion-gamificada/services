package com.gamified.application.learning.service;

import com.gamified.application.learning.model.dto.response.LearningResponseDto;

import java.util.List;

/**
 * Servicio para operaciones del módulo Learning
 */
public interface LearningService {
    
    /**
     * Obtiene todas las áreas STEM disponibles
     * @return Lista de áreas STEM
     */
    List<LearningResponseDto.StemAreaDto> getAllStemAreas();
    
    /**
     * Obtiene todas las especializaciones de un área STEM
     * @param stemAreaId ID del área STEM
     * @return Lista de especializaciones
     */
    List<LearningResponseDto.SpecializationDto> getSpecializationsByStemArea(Integer stemAreaId);
    
    /**
     * Obtiene todos los módulos de una especialización
     * @param specializationId ID de la especialización
     * @return Lista de módulos
     */
    List<LearningResponseDto.ModuleDto> getModulesBySpecialization(Integer specializationId);
    
    /**
     * Obtiene todas las unidades de un módulo
     * @param moduleId ID del módulo
     * @return Lista de unidades
     */
    List<LearningResponseDto.UnitDto> getUnitsByModule(Integer moduleId);
    
    /**
     * Obtiene todos los learning points de una unidad
     * @param unitId ID de la unidad
     * @return Lista de learning points
     */
    List<LearningResponseDto.LearningPointDto> getLearningPointsByUnit(Integer unitId);
    
    /**
     * Obtiene todas las lecciones de un learning point
     * @param learningPointId ID del learning point
     * @return Lista de lecciones
     */
    List<LearningResponseDto.LessonDto> getLessonsByLearningPoint(Integer learningPointId);
    
    /**
     * Obtiene una lección específica con navegación
     * @param lessonId ID de la lección
     * @return Lección con datos de navegación
     */
    LearningResponseDto.LessonDetailDto getLessonById(Integer lessonId);
} 