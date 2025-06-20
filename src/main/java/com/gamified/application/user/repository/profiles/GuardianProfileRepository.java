package com.gamified.application.user.repository.profiles;

import com.gamified.application.user.model.entity.profiles.GuardianProfile;
import com.gamified.application.shared.repository.BaseRepository;

import java.util.Optional;

/**
 * Repositorio para operaciones en la tabla 'guardian_profile'
 */
public interface GuardianProfileRepository extends BaseRepository<GuardianProfile, Long> {
    /**
     * Busca un perfil de tutor por ID de usuario
     * @param userId ID del usuario
     * @return Perfil de tutor si existe, empty si no
     */
    Optional<GuardianProfile> findByUserId(Long userId);
    
    /**
     * Actualiza el número de teléfono de un tutor
     * @param guardianProfileId ID del perfil de tutor
     * @param phone Nuevo número de teléfono
     * @return true si se actualizó correctamente
     */
    boolean updatePhone(Long guardianProfileId, String phone);
} 