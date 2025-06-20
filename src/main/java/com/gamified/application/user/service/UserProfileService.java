package com.gamified.application.user.service;

import com.gamified.application.user.model.dto.request.UserRequestDto;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import com.gamified.application.user.model.dto.response.UserResponseDto;

import java.util.List;

/**
 * Servicio para la gestión de perfiles de usuario
 */
public interface UserProfileService {
    
    /**
     * Obtiene el perfil de un estudiante
     * @param userId ID del usuario
     * @return Perfil del estudiante
     */
    UserResponseDto.StudentResponseDto getStudentProfile(Long userId);
    
    /**
     * Obtiene el perfil de un profesor
     * @param userId ID del usuario
     * @return Perfil del profesor
     */
    UserResponseDto.TeacherResponseDto getTeacherProfile(Long userId);
    
    /**
     * Obtiene el perfil de un tutor
     * @param userId ID del usuario
     * @return Perfil del tutor
     */
    UserResponseDto.GuardianResponseDto getGuardianProfile(Long userId);
    
    /**
     * Actualiza el perfil de un estudiante
     * @param userId ID del usuario
     * @param updateRequest Datos a actualizar
     * @return Perfil actualizado
     */
    UserResponseDto.StudentResponseDto updateStudentProfile(Long userId, UserRequestDto.StudentUpdateRequestDto updateRequest);
    
    /**
     * Actualiza el perfil de un profesor
     * @param userId ID del usuario
     * @param updateRequest Datos a actualizar
     * @return Perfil actualizado
     */
    UserResponseDto.TeacherResponseDto updateTeacherProfile(Long userId, UserRequestDto.TeacherUpdateRequestDto updateRequest);
    
    /**
     * Actualiza el perfil de un tutor
     * @param userId ID del usuario
     * @param updateRequest Datos a actualizar
     * @return Perfil actualizado
     */
    UserResponseDto.GuardianResponseDto updateGuardianProfile(Long userId, UserRequestDto.GuardianUpdateRequestDto updateRequest);
    
    /**
     * Actualiza la foto de perfil de un usuario
     * @param userId ID del usuario
     * @param profilePictureUrl URL de la nueva foto
     * @return Respuesta con resultado de la operación
     */
    CommonResponseDto updateProfilePicture(Long userId, String profilePictureUrl);
    
    /**
     * Actualiza la contraseña de un usuario
     * @param userId ID del usuario
     * @param updateRequest Datos para actualizar la contraseña
     * @return Respuesta con resultado de la operación
     */
    CommonResponseDto updatePassword(Long userId, UserRequestDto.PasswordUpdateRequestDto updateRequest);
    
    /**
     * Desactiva una cuenta de usuario
     * @param userId ID del usuario
     * @return Respuesta con resultado de la operación
     */
    CommonResponseDto deactivateAccount(Long userId);
    
    /**
     * Obtiene los estudiantes asociados a un tutor
     * @param guardianUserId ID del usuario tutor
     * @return Lista de perfiles de estudiantes
     */
    List<UserResponseDto.StudentResponseDto> getStudentsByGuardian(Long guardianUserId);

    /**
     * Obtiene los estudiantes asociados a un tutor por profile ID
     * @param guardianProfileId ID del perfil del tutor
     * @return Lista de perfiles de estudiantes
     */
    List<UserResponseDto.StudentResponseDto> getStudentsByGuardianProfile(Long guardianProfileId);
    
    /**
     * Busca usuarios por término de búsqueda
     * @param searchTerm Término de búsqueda
     * @param roleFilter Filtro de rol (opcional)
     * @param limit Límite de resultados
     * @return Lista de perfiles que coinciden
     */
    List<UserResponseDto.BasicUserResponseDto> searchUsers(String searchTerm, String roleFilter, int limit);

    // ==================== MÉTODOS PARA ADMINISTRACIÓN INSTITUCIONAL ====================

    /**
     * Obtiene conteo de usuarios por rol en una institución
     * @param institutionId ID de la institución
     * @return Map con conteos por rol
     */
    java.util.Map<String, Integer> getUserCountsByRoleForInstitution(Long institutionId);

    /**
     * Obtiene usuarios de una institución con filtros
     * @param institutionId ID de la institución
     * @param role Filtro por rol (opcional)
     * @param unassigned Solo estudiantes sin guardián (opcional)
     * @return Lista de usuarios básicos
     */
    List<UserResponseDto.BasicUserResponseDto> getUsersByInstitution(Long institutionId, String role, Boolean unassigned);

    /**
     * Obtiene estudiantes sin guardián de una institución
     * @param institutionId ID de la institución
     * @return Lista de estudiantes sin guardián
     */
    List<UserResponseDto.StudentResponseDto> getUnassignedStudentsByInstitution(Long institutionId);

    /**
     * Obtiene guardianes disponibles de una institución
     * @param institutionId ID de la institución
     * @return Lista de guardianes con información de capacidad
     */
    List<UserResponseDto.GuardianResponseDto> getAvailableGuardiansByInstitution(Long institutionId);

    /**
     * Asigna un guardián a un estudiante
     * @param studentProfileId ID del perfil del estudiante
     * @param guardianProfileId ID del perfil del guardián
     * @return true si la asignación fue exitosa
     */
    boolean assignGuardianToStudent(Long studentProfileId, Long guardianProfileId);

    /**
     * Reasigna un guardián a un estudiante
     * @param studentProfileId ID del perfil del estudiante
     * @param previousGuardianProfileId ID del guardián anterior
     * @param newGuardianProfileId ID del nuevo guardián
     * @return true si la reasignación fue exitosa
     */
    boolean reassignGuardianToStudent(Long studentProfileId, Long previousGuardianProfileId, Long newGuardianProfileId);
} 