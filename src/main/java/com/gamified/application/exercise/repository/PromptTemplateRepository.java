package com.gamified.application.exercise.repository;

import com.gamified.application.exercise.model.entity.PromptTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos con plantillas de prompts
 */
public interface PromptTemplateRepository {

    /**
     * Busca una plantilla de prompt por su ID
     * @param id ID de la plantilla
     * @return Optional con la plantilla si existe
     */
    Optional<PromptTemplate> findById(Integer id);

    /**
     * Busca plantillas de prompt por tipo de ejercicio
     * @param exerciseTypeId ID del tipo de ejercicio
     * @return Lista de plantillas para ese tipo de ejercicio
     */
    List<PromptTemplate> findByExerciseTypeId(Integer exerciseTypeId);

    /**
     * Busca una plantilla de prompt por nombre
     * @param name Nombre de la plantilla
     * @return Optional con la plantilla si existe
     */
    Optional<PromptTemplate> findByName(String name);

    /**
     * Obtiene todas las plantillas de prompt activas
     * @return Lista de todas las plantillas
     */
    List<PromptTemplate> findAll();

    /**
     * Guarda una nueva plantilla de prompt
     * @param promptTemplate Plantilla a guardar
     * @return ID de la plantilla guardada
     */
    Integer save(PromptTemplate promptTemplate);

    /**
     * Actualiza una plantilla de prompt existente
     * @param promptTemplate Plantilla a actualizar
     * @return true si se actualizó correctamente
     */
    boolean update(PromptTemplate promptTemplate);

    /**
     * Elimina una plantilla de prompt
     * @param id ID de la plantilla a eliminar
     * @return true si se eliminó correctamente
     */
    boolean delete(Integer id);
} 