package com.gamified.application.auth.service.user;

import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;

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
     * Busca usuarios por término de búsqueda
     * @param searchTerm Término de búsqueda
     * @param roleFilter Filtro de rol (opcional)
     * @param limit Límite de resultados
     * @return Lista de perfiles que coinciden
     */
    List<UserResponseDto.BasicUserResponseDto> searchUsers(String searchTerm, String roleFilter, int limit);
} 