package com.gamified.application.auth.repository.core;

import com.gamified.application.shared.model.entity.Role;
import com.gamified.application.shared.repository.BaseRepository;

import java.util.Optional;

/**
 * Repositorio para operaciones en la tabla 'role'
 */
public interface RoleRepository extends BaseRepository<Role, Byte> {
    /**
     * Busca un rol por su nombre
     * @param name Nombre del rol
     * @return Rol si existe, empty si no
     */
    Optional<Role> findByName(String name);
} 