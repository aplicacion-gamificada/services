package com.gamified.application.progress.service;

import com.gamified.application.progress.model.dto.request.ProgressRequestDto;
import com.gamified.application.progress.model.dto.response.ProgressResponseDto;

/**
 * Servicio para operaciones del módulo Progress
 */
public interface ProgressService {
    
    /**
     * Obtiene el learning path activo de un estudiante
     * @param studentProfileId ID del perfil de estudiante
     * @return Learning path del estudiante
     */
    ProgressResponseDto.LearningPathDto getLearningPathByStudent(Integer studentProfileId);
    
    /**
     * Crea un nuevo learning path para un estudiante
     * @param request Datos para crear el learning path
     * @return Learning path creado
     */
    ProgressResponseDto.LearningPathDto createLearningPath(ProgressRequestDto.CreateLearningPathDto request);
    
    /**
     * Obtiene el progreso actual completo de un estudiante
     * @param studentProfileId ID del perfil de estudiante
     * @return Progreso actual del estudiante
     */
    ProgressResponseDto.CurrentProgressDto getCurrentProgress(Integer studentProfileId);
    
    /**
     * Marca una lección como completada
     * @param studentProfileId ID del perfil de estudiante
     * @param lessonId ID de la lección
     * @param timeSpentMinutes Tiempo empleado en minutos (opcional)
     * @return Resultado de completar la lección
     */
    ProgressResponseDto.LessonCompletionDto completeLessonById(
            Integer studentProfileId, Integer lessonId, Integer timeSpentMinutes);
    
    /**
     * Obtiene el siguiente learning point en el learning path del estudiante
     * @param studentProfileId ID del perfil de estudiante
     * @return Siguiente learning point disponible
     */
    ProgressResponseDto.NextLearningPointDto getNextLearningPoint(Integer studentProfileId);
} 