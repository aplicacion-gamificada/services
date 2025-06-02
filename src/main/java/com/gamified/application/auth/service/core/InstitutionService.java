package com.gamified.application.auth.service.core;

import com.gamified.application.auth.dto.response.InstitutionResponseDto;

import java.util.List;

/**
 * Servicio para operaciones relacionadas con instituciones
 */
public interface InstitutionService {

    /**
     * Obtiene todas las instituciones activas
     * @return Lista de instituciones activas
     */
    List<InstitutionResponseDto.InstitutionSummaryDto> getAllActiveInstitutions();

    /**
     * Obtiene una institución por su ID
     * @param institutionId ID de la institución
     * @return DTO con detalles de la institución
     */
    InstitutionResponseDto.InstitutionDetailDto getInstitutionById(Long institutionId);

    /**
     * Busca instituciones por nombre
     * @param query Texto a buscar en el nombre
     * @param limit Límite de resultados
     * @return Lista de instituciones que coinciden con la búsqueda
     */
    List<InstitutionResponseDto.InstitutionSummaryDto> searchInstitutions(String query, int limit);
} 