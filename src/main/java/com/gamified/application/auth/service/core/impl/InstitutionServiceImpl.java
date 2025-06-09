package com.gamified.application.auth.service.core.impl;

import com.gamified.application.auth.dto.request.InstitutionRequestDto;
import com.gamified.application.auth.dto.response.InstitutionResponseDto;
import com.gamified.application.auth.entity.core.Institution;
import com.gamified.application.auth.exception.ResourceNotFoundException;
import com.gamified.application.auth.repository.core.InstitutionRepository;
import com.gamified.application.auth.repository.interfaces.Result;
import com.gamified.application.auth.service.core.InstitutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de instituciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;

    @Override
    public List<InstitutionResponseDto.InstitutionSummaryDto> getAllActiveInstitutions() {
        log.info("Obteniendo todas las instituciones activas");
        List<Institution> institutions = institutionRepository.findAllActive();
        
        // Mapear a DTO de respuesta
        return institutions.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionResponseDto.InstitutionDetailDto getInstitutionById(Long institutionId) {
        log.info("Buscando institución con ID: {}", institutionId);
        
        return institutionRepository.findById(institutionId)
                .map(this::mapToDetailDto)
                .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
    }

    @Override
    public List<InstitutionResponseDto.InstitutionSummaryDto> searchInstitutions(String query, int limit) {
        log.info("Buscando instituciones con query: '{}', limit: {}", query, limit);
        
        List<Institution> institutions = institutionRepository.searchByName(query, limit);
        
        // Mapear a DTO de respuesta
        return institutions.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public InstitutionResponseDto.InstitutionDetailDto registerInstitution(
            InstitutionRequestDto.InstitutionRegistrationRequestDto requestDto) {
        log.info("Registrando nueva institución con nombre: {}", requestDto.getName());
        
        // Verificar si ya existe una institución con el mismo nombre
        institutionRepository.findByName(requestDto.getName()).ifPresent(institution -> {
            throw new IllegalArgumentException("Ya existe una institución con el nombre: " + requestDto.getName());
        });
        
        // Crear entidad Institution a partir del DTO
        Institution institution = Institution.builder()
                .name(requestDto.getName())
                .address(requestDto.getAddress())
                .city(requestDto.getCity())
                .state(requestDto.getState())
                .country(requestDto.getCountry())
                .postalCode(requestDto.getPostalCode())
                .phone(requestDto.getPhone())
                .email(requestDto.getEmail())
                .website(requestDto.getWebsite())
                .logoUrl(requestDto.getLogoUrl())
                .status(true) // Por defecto activa
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Guardar la institución
        Result<Institution> result = institutionRepository.save(institution);
        
        if (!result.isSuccess()) {
            log.error("Error al registrar institución: {}", result.getErrorMessage());
            throw new RuntimeException("Error al registrar la institución: " + result.getErrorMessage());
        }
        
        Institution savedInstitution = result.getData();
        log.info("Institución registrada exitosamente con ID: {}", savedInstitution.getId());
        
        // Mapear a DTO de respuesta
        return mapToDetailDto(savedInstitution);
    }
    
    // Métodos privados para mapeo de entidad a DTO
    
    private InstitutionResponseDto.InstitutionSummaryDto mapToSummaryDto(Institution institution) {
        return InstitutionResponseDto.InstitutionSummaryDto.builder()
                .id(institution.getId())
                .name(institution.getName())
                .city(institution.getCity())
                .province(institution.getState())
                .build();
    }
    
    private InstitutionResponseDto.InstitutionDetailDto mapToDetailDto(Institution institution) {
        return InstitutionResponseDto.InstitutionDetailDto.builder()
                .id(institution.getId())
                .name(institution.getName())
                .address(institution.getAddress())
                .city(institution.getCity())
                .province(institution.getState())
                .country(institution.getCountry())
                .postalCode(institution.getPostalCode())
                .phone(institution.getPhone())
                .email(institution.getEmail())
                .website(institution.getWebsite())
                .createdAt(institution.getCreatedAt())
                .updatedAt(institution.getUpdatedAt())
                .active(institution.isActive())
                .build();
    }
} 