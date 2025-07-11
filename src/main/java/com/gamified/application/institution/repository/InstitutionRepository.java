package com.gamified.application.institution.repository;

import com.gamified.application.institution.model.entity.Institution;
import com.gamified.application.shared.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones en la tabla 'institution'
 */
public interface InstitutionRepository extends BaseRepository<Institution, Long> {
    /**
     * Busca una institución por su nombre
     * @param name Nombre de la institución
     * @return Institución si existe, empty si no
     */
    Optional<Institution> findByName(String name);
    
    /**
     * Busca instituciones por localización
     * @param country País
     * @param state Estado/Provincia (opcional)
     * @param city Ciudad (opcional)
     * @return Lista de instituciones que coinciden
     */
    List<Institution> findByLocation(String country, String state, String city);
    
    /**
     * Busca instituciones activas
     * @return Lista de instituciones activas
     */
    List<Institution> findActiveInstitutions();
    
    /**
     * Actualiza el estado de una institución
     * @param institutionId ID de la institución
     * @param active Nuevo estado (activo/inactivo)
     * @return Resultado de la operación
     */
    boolean updateStatus(Long institutionId, boolean active);

    /**
     * Encuentra todas las instituciones activas
     * @return Lista de instituciones activas
     */
    List<Institution> findAllActive();
    
    /**
     * Encuentra una institución por su ID
     * @param id ID de la institución
     * @return Optional con la institución encontrada o vacío si no existe
     */
    Optional<Institution> findById(Long id);
    
    /**
     * Busca instituciones por nombre
     * @param query Texto a buscar en el nombre
     * @param limit Límite de resultados
     * @return Lista de instituciones que coinciden con la búsqueda
     */
    List<Institution> searchByName(String query, int limit);
} 