package com.gamified.application.clasroom.service;

import com.gamified.application.clasroom.model.dto.request.ClassroomRequestDto;
import com.gamified.application.clasroom.model.dto.response.ClassroomResponseDto;

import java.util.List;

/**
 * Servicio para operaciones de gestión de aulas
 */
public interface ClassroomService {
    
    // ===================================================================
    // CLASSROOM MANAGEMENT
    // ===================================================================
    
    /**
     * Crea un nuevo aula para un profesor
     * @param teacherUserId ID del usuario profesor
     * @param request Datos del aula a crear
     * @return Aula creada
     */
    ClassroomResponseDto.ClassroomDto createClassroom(Long teacherUserId, ClassroomRequestDto.CreateClassroomRequestDto request);
    
    /**
     * Obtiene todas las aulas de un profesor
     * @param teacherUserId ID del usuario profesor
     * @return Lista de aulas del profesor
     */
    List<ClassroomResponseDto.ClassroomDto> getTeacherClassrooms(Long teacherUserId);
    
    /**
     * Obtiene detalles completos de un aula (incluyendo estudiantes)
     * @param teacherUserId ID del usuario profesor
     * @param classroomId ID del aula
     * @return Detalles del aula con estudiantes
     */
    ClassroomResponseDto.ClassroomDetailDto getClassroomDetail(Long teacherUserId, Integer classroomId);
    
    /**
     * Actualiza un aula
     * @param teacherUserId ID del usuario profesor
     * @param classroomId ID del aula
     * @param request Datos a actualizar
     * @return Aula actualizada
     */
    ClassroomResponseDto.ClassroomDto updateClassroom(Long teacherUserId, Integer classroomId, ClassroomRequestDto.UpdateClassroomRequestDto request);
    
    /**
     * Desactiva un aula (soft delete)
     * @param teacherUserId ID del usuario profesor
     * @param classroomId ID del aula
     * @return true si se desactivó correctamente
     */
    boolean deactivateClassroom(Long teacherUserId, Integer classroomId);
    
    // ===================================================================
    // STUDENT ENROLLMENT
    // ===================================================================
    
    /**
     * Inscribe un estudiante en un aula
     * @param teacherUserId ID del usuario profesor
     * @param classroomId ID del aula
     * @param request Datos de la inscripción
     * @return Resultado de la inscripción
     */
    ClassroomResponseDto.EnrollmentResponseDto enrollStudent(Long teacherUserId, Integer classroomId, ClassroomRequestDto.EnrollStudentRequestDto request);
    
    /**
     * Obtiene los estudiantes de un aula
     * @param teacherUserId ID del usuario profesor
     * @param classroomId ID del aula
     * @return Lista de estudiantes del aula
     */
    List<ClassroomResponseDto.StudentInClassroomDto> getClassroomStudents(Long teacherUserId, Integer classroomId);
    
    /**
     * Desinscribe un estudiante de un aula
     * @param teacherUserId ID del usuario profesor
     * @param classroomId ID del aula
     * @param studentProfileId ID del perfil del estudiante
     * @return true si se desinscribió correctamente
     */
    boolean unenrollStudent(Long teacherUserId, Integer classroomId, Integer studentProfileId);
    
    // ===================================================================
    // STUDENT OPERATIONS (for students)
    // ===================================================================
    
    /**
     * Obtiene las aulas en las que está inscrito un estudiante
     * @param studentUserId ID del usuario estudiante
     * @return Lista de aulas del estudiante
     */
    List<ClassroomResponseDto.ClassroomDto> getStudentClassrooms(Long studentUserId);
    
    // ===================================================================
    // STATISTICS AND ANALYTICS
    // ===================================================================
    
    /**
     * Obtiene estadísticas de un aula
     * @param teacherUserId ID del usuario profesor
     * @param classroomId ID del aula
     * @return Estadísticas del aula
     */
    ClassroomResponseDto.ClassroomStatsDto getClassroomStats(Long teacherUserId, Integer classroomId);
    
    // ===================================================================
    // VALIDATION METHODS
    // ===================================================================
    
    /**
     * Verifica si un aula pertenece a un profesor
     * @param teacherUserId ID del usuario profesor
     * @param classroomId ID del aula
     * @return true si el aula pertenece al profesor
     */
    boolean verifyClassroomOwnership(Long teacherUserId, Integer classroomId);
    
    /**
     * Verifica si un estudiante está inscrito en un aula
     * @param classroomId ID del aula
     * @param studentProfileId ID del perfil del estudiante
     * @return true si está inscrito
     */
    boolean isStudentEnrolled(Integer classroomId, Integer studentProfileId);
} 