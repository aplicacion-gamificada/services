package com.gamified.application.user.service;

import com.gamified.application.user.model.dto.response.UserResponseDto;

import java.util.List;

/**
 * Servicio específico para operaciones relacionadas con profesores
 */
public interface TeacherService {
    
    /**
     * Obtiene todos los usuarios relacionados con un teacher
     * Esto incluye estudiantes de sus classrooms y sus guardianes
     * 
     * @param teacherUserId ID del usuario teacher
     * @return TeacherRelatedUsersDto con todos los usuarios relacionados
     */
    UserResponseDto.TeacherRelatedUsersDto getRelatedUsers(Long teacherUserId);
    
    /**
     * Obtiene los classrooms de un teacher
     * 
     * @param teacherUserId ID del usuario teacher
     * @return Lista de classrooms del teacher
     */
    List<UserResponseDto.ClassroomDto> getTeacherClassrooms(Long teacherUserId);
    
    /**
     * Obtiene los estudiantes de un classroom específico
     * Verificando que el classroom pertenezca al teacher
     * 
     * @param teacherUserId ID del usuario teacher
     * @param classroomId ID del classroom
     * @return Lista de estudiantes con información de sus guardianes
     */
    List<UserResponseDto.StudentWithGuardianDto> getStudentsByClassroom(Long teacherUserId, Long classroomId);
    
    /**
     * Obtiene estadísticas del teacher
     * 
     * @param teacherUserId ID del usuario teacher
     * @return Estadísticas del teacher
     */
    UserResponseDto.TeacherStatsDto getTeacherStats(Long teacherUserId);
    
    /**
     * Verifica si un classroom pertenece a un teacher
     * 
     * @param teacherUserId ID del usuario teacher
     * @param classroomId ID del classroom
     * @return true si el classroom pertenece al teacher
     */
    boolean verifyClassroomOwnership(Long teacherUserId, Long classroomId);
} 