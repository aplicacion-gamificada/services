package com.gamified.application.user.repository.composite;

import com.gamified.application.user.model.entity.composite.CompleteStudent;
import com.gamified.application.user.model.entity.composite.CompleteTeacher;
import com.gamified.application.user.model.entity.composite.CompleteGuardian;
import com.gamified.application.shared.repository.Result;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio principal para operaciones compuestas que involucran múltiples tablas
 * Este es el repositorio que deben usar los servicios para operaciones completas
 */
public interface CompleteUserRepository {
    /**
     * Crea un estudiante completo (user + student_profile)
     * @param completeStudent Datos completos del estudiante
     * @return Resultado con el estudiante creado
     */
    Result<CompleteStudent> createCompleteStudent(CompleteStudent completeStudent);
    
    /**
     * Crea un profesor completo (user + teacher_profile)
     * @param completeTeacher Datos completos del profesor
     * @return Resultado con el profesor creado
     */
    Result<CompleteTeacher> createCompleteTeacher(CompleteTeacher completeTeacher);
    
    /**
     * Crea un tutor completo (user + guardian_profile)
     * @param completeGuardian Datos completos del tutor
     * @return Resultado con el tutor creado
     */
    Result<CompleteGuardian> createCompleteGuardian(CompleteGuardian completeGuardian);
    
    /**
     * Busca un estudiante completo por ID
     * @param userId ID del usuario
     * @return Estudiante completo si existe, empty si no
     */
    Optional<CompleteStudent> findCompleteStudentById(Long userId);
    
    /**
     * Busca un profesor completo por ID
     * @param userId ID del usuario
     * @return Profesor completo si existe, empty si no
     */
    Optional<CompleteTeacher> findCompleteTeacherById(Long userId);
    
    /**
     * Busca un tutor completo por ID
     * @param userId ID del usuario
     * @return Tutor completo si existe, empty si no
     */
    Optional<CompleteGuardian> findCompleteGuardianById(Long userId);
    
    /**
     * Busca un estudiante completo por nombre de usuario
     * @param username Nombre de usuario
     * @return Estudiante completo si existe, empty si no
     */
    Optional<CompleteStudent> findCompleteStudentByUsername(String username);
    
    /**
     * Busca un usuario completo por email (cualquier tipo)
     * Determina automáticamente si es estudiante, profesor o tutor
     * @param email Email del usuario
     * @return Tipo específico de usuario según el rol
     */
    Optional<Object> findCompleteUserByEmail(String email);
    
    /**
     * Busca estudiantes por tutor
     * @param guardianUserId ID de usuario del tutor
     * @return Lista de estudiantes asociados
     */
    List<CompleteStudent> findStudentsByGuardian(Long guardianUserId);
    
    /**
     * Actualiza un estudiante completo
     * @param completeStudent Datos actualizados del estudiante
     * @return Resultado con el estudiante actualizado
     */
    Result<CompleteStudent> updateCompleteStudent(CompleteStudent completeStudent);
    
    /**
     * Actualiza un profesor completo
     * @param completeTeacher Datos actualizados del profesor
     * @return Resultado con el profesor actualizado
     */
    Result<CompleteTeacher> updateCompleteTeacher(CompleteTeacher completeTeacher);
    
    /**
     * Actualiza un tutor completo
     * @param completeGuardian Datos actualizados del tutor
     * @return Resultado con el tutor actualizado
     */
    Result<CompleteGuardian> updateCompleteGuardian(CompleteGuardian completeGuardian);
} 