package com.gamified.application.auth.repository.profiles;

import com.gamified.application.auth.entity.profiles.StudentProfile;
import com.gamified.application.auth.repository.interfaces.BaseRepository;
import com.gamified.application.auth.repository.interfaces.Result;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones en la tabla 'student_profile'
 */
public interface StudentProfileRepository extends BaseRepository<StudentProfile, Long> {
    /**
     * Busca un perfil de estudiante por ID de usuario
     * @param userId ID del usuario
     * @return Perfil de estudiante si existe, empty si no
     */
    Optional<StudentProfile> findByUserId(Long userId);
    
    /**
     * Busca un perfil de estudiante por nombre de usuario
     * @param username Nombre de usuario
     * @return Perfil de estudiante si existe, empty si no
     */
    Optional<StudentProfile> findByUsername(String username);
    
    /**
     * Actualiza la cantidad de puntos de un estudiante
     * @param studentProfileId ID del perfil de estudiante
     * @param pointsToAdd Puntos a añadir (negativos para restar)
     * @return Resultado con la nueva cantidad de puntos
     */
    Result<Integer> updatePoints(Long studentProfileId, int pointsToAdd);
    
    /**
     * Busca estudiantes por su tutor
     * @param guardianProfileId ID del perfil de tutor
     * @return Lista de perfiles de estudiantes asociados
     */
    List<StudentProfile> findByGuardianProfileId(Long guardianProfileId);
} 