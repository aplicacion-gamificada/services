package com.gamified.application.clasroom.repository;

import com.gamified.application.clasroom.model.entity.Classroom;
import com.gamified.application.clasroom.model.entity.Enrollment;
import com.gamified.application.clasroom.model.dto.response.ClassroomResponseDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repositorio para operaciones de Classroom y Enrollment
 */
public interface ClassroomRepository {
    
    // ===================================================================
    // CLASSROOM OPERATIONS
    // ===================================================================
    
    /**
     * Crea un nuevo aula
     * @param classroom Datos del aula a crear
     * @return ID del aula creada
     */
    Integer createClassroom(Classroom classroom);
    
    /**
     * Busca un aula por ID
     * @param classroomId ID del aula
     * @return Aula si existe
     */
    Optional<Classroom> findClassroomById(Integer classroomId);
    
    /**
     * Obtiene todas las aulas de un profesor
     * @param teacherProfileId ID del perfil del profesor
     * @return Lista de aulas del profesor
     */
    List<ClassroomResponseDto.ClassroomDto> findClassroomsByTeacher(Integer teacherProfileId);
    
    /**
     * Actualiza un aula
     * @param classroom Datos del aula a actualizar
     * @return true si se actualizó correctamente
     */
    boolean updateClassroom(Classroom classroom);
    
    /**
     * Verifica si un aula pertenece a un profesor
     * @param classroomId ID del aula
     * @param teacherProfileId ID del perfil del profesor
     * @return true si el aula pertenece al profesor
     */
    boolean verifyClassroomOwnership(Integer classroomId, Integer teacherProfileId);
    
    /**
     * Obtiene detalles completos de un aula con sus estudiantes
     * @param classroomId ID del aula
     * @return Detalles del aula con estudiantes
     */
    Optional<ClassroomResponseDto.ClassroomDetailDto> findClassroomDetailById(Integer classroomId);
    
    // ===================================================================
    // ENROLLMENT OPERATIONS
    // ===================================================================
    
    /**
     * Inscribe un estudiante en un aula
     * @param enrollment Datos de la inscripción
     * @return ID de la inscripción creada
     */
    Integer enrollStudent(Enrollment enrollment);
    
    /**
     * Verifica si un estudiante ya está inscrito en un aula
     * @param classroomId ID del aula
     * @param studentProfileId ID del perfil del estudiante
     * @return true si ya está inscrito
     */
    boolean isStudentEnrolled(Integer classroomId, Integer studentProfileId);
    
    /**
     * Obtiene los estudiantes de un aula
     * @param classroomId ID del aula
     * @return Lista de estudiantes en el aula
     */
    List<ClassroomResponseDto.StudentInClassroomDto> findStudentsByClassroom(Integer classroomId);
    
    /**
     * Desinscribe un estudiante de un aula (cambia status a inactivo)
     * @param classroomId ID del aula
     * @param studentProfileId ID del perfil del estudiante
     * @return true si se desinscribió correctamente
     */
    boolean unenrollStudent(Integer classroomId, Integer studentProfileId);
    
    /**
     * Cuenta el número de estudiantes activos en un aula
     * @param classroomId ID del aula
     * @return Número de estudiantes activos
     */
    Integer countActiveStudentsInClassroom(Integer classroomId);
    
    /**
     * Obtiene las aulas en las que está inscrito un estudiante
     * @param studentProfileId ID del perfil del estudiante
     * @return Lista de aulas del estudiante
     */
    List<ClassroomResponseDto.ClassroomDto> findClassroomsByStudent(Integer studentProfileId);
    
    /**
     * Obtiene el nombre completo del estudiante por su profile ID
     * @param studentProfileId ID del perfil del estudiante
     * @return Nombre completo del estudiante
     */
    Optional<String> findStudentNameByProfileId(Integer studentProfileId);
    
    // ===================================================================
    // STATISTICS OPERATIONS
    // ===================================================================
    
    /**
     * Obtiene estadísticas de un aula
     * @param classroomId ID del aula
     * @return Estadísticas del aula
     */
    Optional<ClassroomResponseDto.ClassroomStatsDto> getClassroomStats(Integer classroomId);

    Map<String, Object> getClassroomDataByUserId(int userId);
    List<Map<String, Object>> getClassmatesByUserId(int userId);
}