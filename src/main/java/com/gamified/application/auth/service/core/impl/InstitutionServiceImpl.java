package com.gamified.application.auth.service.core.impl;

import com.gamified.application.auth.dto.response.InstitutionResponseDto;
import com.gamified.application.auth.service.core.InstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del servicio de instituciones
 */
@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<InstitutionResponseDto.InstitutionSummaryDto> getAllActiveInstitutions() {
        // TODO: Implementar lógica para obtener todas las instituciones activas usando SP
        // Ejemplo de implementación temporal con datos de prueba
        return List.of(
            InstitutionResponseDto.InstitutionSummaryDto.builder()
                .id(1L)
                .name("Universidad Nacional")
                .code("UN-001")
                .city("Ciudad A")
                .province("Provincia A")
                .type("Universidad")
                .build(),
            InstitutionResponseDto.InstitutionSummaryDto.builder()
                .id(2L)
                .name("Colegio San José")
                .code("CSJ-002")
                .city("Ciudad B")
                .province("Provincia B")
                .type("Colegio")
                .build()
        );
    }

    @Override
    public InstitutionResponseDto.InstitutionDetailDto getInstitutionById(Long institutionId) {
        // TODO: Implementar lógica para obtener una institución por ID usando SP
        // Ejemplo de implementación temporal con datos de prueba
        return InstitutionResponseDto.InstitutionDetailDto.builder()
                .id(institutionId)
                .name("Universidad Nacional")
                .code("UN-001")
                .type("Universidad")
                .address("Calle Principal 123")
                .city("Ciudad A")
                .province("Provincia A")
                .postalCode("12345")
                .country("País A")
                .phone("123-456-7890")
                .email("contacto@universidad.edu")
                .website("www.universidad.edu")
                .description("Universidad pública con amplia oferta educativa")
                .active(true)
                .build();
    }

    @Override
    public List<InstitutionResponseDto.InstitutionSummaryDto> searchInstitutions(String query, int limit) {
        // TODO: Implementar lógica para buscar instituciones por nombre usando SP
        // Ejemplo de implementación temporal con datos de prueba
        return List.of(
            InstitutionResponseDto.InstitutionSummaryDto.builder()
                .id(1L)
                .name("Universidad Nacional " + query)
                .code("UN-001")
                .city("Ciudad A")
                .province("Provincia A")
                .type("Universidad")
                .build()
        );
    }
} 