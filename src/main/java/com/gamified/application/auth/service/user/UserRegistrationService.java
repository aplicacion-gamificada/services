package com.gamified.application.auth.service.user;

import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.entity.composite.CompleteStudent;
import com.gamified.application.auth.entity.composite.CompleteTeacher;
import com.gamified.application.auth.entity.composite.CompleteGuardian;

/**
 * Servicio para el registro y creación de usuarios
 */
public interface UserRegistrationService {
    
    /**
     * Registra un nuevo estudiante
     * @param studentRequest Datos del estudiante
     * @return Respuesta con el estudiante creado
     */
    UserResponseDto.StudentResponseDto registerStudent(UserRequestDto.StudentRegistrationRequestDto studentRequest);
    
    /**
     * Registra un nuevo profesor
     * @param teacherRequest Datos del profesor
     * @return Respuesta con el profesor creado
     */
    UserResponseDto.TeacherResponseDto registerTeacher(UserRequestDto.TeacherRegistrationRequestDto teacherRequest);
    
    /**
     * Registra un nuevo tutor/apoderado
     * @param guardianRequest Datos del tutor
     * @return Respuesta con el tutor creado
     */
    UserResponseDto.GuardianResponseDto registerGuardian(UserRequestDto.GuardianRegistrationRequestDto guardianRequest);
    
    /**
     * Asocia un estudiante a un tutor
     * @param associationRequest Datos de la asociación
     * @return Respuesta con resultado de la operación
     */
    CommonResponseDto associateStudentToGuardian(UserRequestDto.StudentGuardianAssociationRequestDto associationRequest);
    
    /**
     * Verifica si un email está disponible para registro
     * @param email Email a verificar
     * @return true si está disponible
     */
    boolean isEmailAvailable(String email);
    
    /**
     * Verifica si un nombre de usuario está disponible para registro
     * @param username Nombre de usuario a verificar
     * @return true si está disponible
     */
    boolean isUsernameAvailable(String username);
    
    /**
     * Convierte una entidad de usuario completo en DTO de respuesta
     * @param completeStudent Estudiante completo
     * @return DTO de respuesta de estudiante
     */
    UserResponseDto.StudentResponseDto mapToStudentResponseDto(CompleteStudent completeStudent);
    
    /**
     * Convierte una entidad de usuario completo en DTO de respuesta
     * @param completeTeacher Profesor completo
     * @return DTO de respuesta de profesor
     */
    UserResponseDto.TeacherResponseDto mapToTeacherResponseDto(CompleteTeacher completeTeacher);
    
    /**
     * Convierte una entidad de usuario completo en DTO de respuesta
     * @param completeGuardian Tutor completo
     * @return DTO de respuesta de tutor
     */
    UserResponseDto.GuardianResponseDto mapToGuardianResponseDto(CompleteGuardian completeGuardian);
} 