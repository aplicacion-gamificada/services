package com.gamified.application.user.repository.profiles;

import com.gamified.application.user.model.entity.profiles.TeacherProfile;
import com.gamified.application.shared.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones en la tabla 'teacher_profile'
 */
public interface TeacherProfileRepository extends BaseRepository<TeacherProfile, Long> {
    /**
     * Busca un perfil de profesor por ID de usuario
     * @param userId ID del usuario
     * @return Perfil de profesor si existe, empty si no
     */
    Optional<TeacherProfile> findByUserId(Long userId);
    
    /**
     * Verifica el email de un profesor
     * @param teacherProfileId ID del perfil de profesor
     * @return true si se verificó correctamente
     */
    boolean verifyEmail(Long teacherProfileId);
    
    /**
     * Busca profesores por área STEM
     * @param stemAreaId ID del área STEM
     * @return Lista de perfiles de profesores en esa área
     */
    List<TeacherProfile> findByStemAreaId(Byte stemAreaId);
} 