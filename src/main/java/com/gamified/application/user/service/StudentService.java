package com.gamified.application.user.service;

import com.gamified.application.user.model.dto.response.StudentResponseDto;

/**
 * Servicio para operaciones específicas de estudiantes
 */
public interface StudentService {
    
    /**
     * Obtiene la especialización asignada al estudiante a través de su classroom
     * @param studentProfileId ID del perfil del estudiante
     * @return Especialización asignada del estudiante
     */
    StudentResponseDto.AssignedSpecializationDto getAssignedSpecialization(Integer studentProfileId);
    
    /**
     * Obtiene el progreso del estudiante en una especialización específica
     * @param studentProfileId ID del perfil del estudiante
     * @param specializationId ID de la especialización
     * @return Progreso del estudiante en la especialización
     */
    StudentResponseDto.SpecializationProgressDto getSpecializationProgress(Integer studentProfileId, Integer specializationId);
    
    /**
     * Obtiene el progreso del estudiante en un classroom específico
     * @param studentProfileId ID del perfil del estudiante
     * @param classroomId ID del classroom
     * @return Progreso del estudiante en el classroom
     */
    StudentResponseDto.ClassroomProgressDto getClassroomProgress(Integer studentProfileId, Integer classroomId);
} 