package com.gamified.application.auth.controller;

import com.gamified.application.auth.dto.request.InstitutionRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.InstitutionResponseDto;
import com.gamified.application.auth.service.core.InstitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para operaciones relacionadas con instituciones
 */
@RestController
@RequestMapping("/api/institutions")
@RequiredArgsConstructor
@Slf4j
public class InstitutionController {

    private final InstitutionService institutionService;

    /**
     * Obtiene todas las instituciones activas
     * @return Lista de instituciones
     */
    @GetMapping
    public ResponseEntity<List<InstitutionResponseDto.InstitutionSummaryDto>> getAllActiveInstitutions() {
        log.info("GET /api/institutions - Obteniendo todas las instituciones activas");
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
        log.info("GET /api/institutions/{} - Obteniendo institución por ID", institutionId);
        
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
        log.info("GET /api/institutions/search?query={}&limit={} - Buscando instituciones", query, limit);
        
        List<InstitutionResponseDto.InstitutionSummaryDto> institutions = 
                institutionService.searchInstitutions(query, limit);
        return ResponseEntity.ok(institutions);
    }
    
    /**
     * Registra una nueva institución
     * @param requestDto Datos de la institución a registrar
     * @return La institución registrada
     */
    @PostMapping
    public ResponseEntity<CommonResponseDto<InstitutionResponseDto.InstitutionDetailDto>> registerInstitution(
            @Valid @RequestBody InstitutionRequestDto.InstitutionRegistrationRequestDto requestDto) {
        log.info("POST /api/institutions - Registrando institución: {}", requestDto.getName());
        
        try {
            InstitutionResponseDto.InstitutionDetailDto registeredInstitution =
                    institutionService.registerInstitution(requestDto);
            
            CommonResponseDto<InstitutionResponseDto.InstitutionDetailDto> response = CommonResponseDto.<InstitutionResponseDto.InstitutionDetailDto>builder()
                    .success(true)
                    .message("Institución registrada exitosamente")
                    .timestamp(LocalDateTime.now())
                    .data(registeredInstitution)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // En caso de nombre duplicado u otros errores de validación
            log.warn("Error al registrar institución: {}", e.getMessage());
            
            CommonResponseDto<InstitutionResponseDto.InstitutionDetailDto> response = CommonResponseDto.<InstitutionResponseDto.InstitutionDetailDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // Otros errores no esperados
            log.error("Error inesperado al registrar institución", e);
            
            CommonResponseDto<InstitutionResponseDto.InstitutionDetailDto> response = CommonResponseDto.<InstitutionResponseDto.InstitutionDetailDto>builder()
                    .success(false)
                    .message("Error interno al procesar la solicitud")
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 