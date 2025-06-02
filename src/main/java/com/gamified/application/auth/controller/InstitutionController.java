package com.gamified.application.auth.controller;

import com.gamified.application.auth.dto.response.InstitutionResponseDto;
import com.gamified.application.auth.service.core.InstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para operaciones relacionadas con instituciones
 */
@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
public class InstitutionController {

    private final InstitutionService institutionService;

    /**
     * Obtiene todas las instituciones activas
     * @return Lista de instituciones
     */
    @GetMapping
    public ResponseEntity<List<InstitutionResponseDto.InstitutionSummaryDto>> getAllActiveInstitutions() {
        List<InstitutionResponseDto.InstitutionSummaryDto> institutions = 
                institutionService.getAllActiveInstitutions();
        return ResponseEntity.ok(institutions);
    }

    /**
     * Obtiene una institución por su ID
     * @param institutionId ID de la institución
     * @return Institución encontrada
     */
    @GetMapping("/{institutionId}")
    public ResponseEntity<InstitutionResponseDto.InstitutionDetailDto> getInstitutionById(
            @PathVariable Long institutionId) {
        
        InstitutionResponseDto.InstitutionDetailDto institution = 
                institutionService.getInstitutionById(institutionId);
        return ResponseEntity.ok(institution);
    }

    /**
     * Busca instituciones por nombre
     * @param query Texto a buscar
     * @param limit Límite de resultados
     * @return Lista de instituciones que coinciden con la búsqueda
     */
    @GetMapping("/search")
    public ResponseEntity<List<InstitutionResponseDto.InstitutionSummaryDto>> searchInstitutions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<InstitutionResponseDto.InstitutionSummaryDto> institutions = 
                institutionService.searchInstitutions(query, limit);
        return ResponseEntity.ok(institutions);
    }
} 