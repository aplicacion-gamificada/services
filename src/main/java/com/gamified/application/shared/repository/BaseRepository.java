package com.gamified.application.shared.repository;

import java.util.List;
import java.util.Optional;

/**
 * Interface base para todos los repositorios
 * @param <T> Tipo de entidad
 * @param <ID> Tipo de ID primario
 */
public interface BaseRepository<T, ID> {
    /**
     * Busca una entidad por su ID
     * @param id ID de la entidad
     * @return Optional con la entidad si se encuentra, empty si no
     */
    Optional<T> findById(ID id);
    
    /**
     * Guarda una entidad (insert o update)
     * @param entity Entidad a guardar
     * @return Resultado de la operación con la entidad guardada
     */
    Result<T> save(T entity);
    
    /**
     * Elimina una entidad por su ID
     * @param id ID de la entidad a eliminar
     * @return Resultado con true si se eliminó correctamente
     */
    Result<Boolean> delete(ID id);
    
    /**
     * Obtiene todas las entidades
     * @return Lista de entidades
     */
    List<T> findAll();
} 