package com.gamified.application.auth.controller;

import com.gamified.application.institution.model.dto.request.InstitutionRequestDto;
import com.gamified.application.institution.model.dto.response.InstitutionResponseDto;
import com.gamified.application.institution.controller.InstitutionController;
import com.gamified.application.institution.service.InstitutionService;
import com.gamified.application.config.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test completo para InstitutionController basado en GUIA_TESTING_DETALLADA.md
 * 
 * Secciones de test:
 * 1. Consulta de Instituciones
 * 2. Creación de Instituciones
 */
@WebMvcTest(InstitutionController.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@DisplayName("InstitutionController - Tests Completos")
class InstitutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InstitutionService institutionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ================================
    // 1. CONSULTA DE INSTITUCIONES
    // ================================
    
    @Nested
    @DisplayName("1. Consulta de Instituciones")
    class InstitutionConsultationTests {

        @Test
        @DisplayName("GET /api/institutions - Should return all active institutions")
        void testGetAllActiveInstitutions_Success() throws Exception {
            // Given
            List<InstitutionResponseDto.InstitutionSummaryDto> mockInstitutions = Arrays.asList(
                    InstitutionResponseDto.InstitutionSummaryDto.builder()
                            .id(1L)
                            .name("Universidad Nacional")
                            .code("UNAL")
                            .city("Bogotá")
                            .province("Cundinamarca")
                            .type("Universidad")
                            .build(),
                    InstitutionResponseDto.InstitutionSummaryDto.builder()
                            .id(2L)
                            .name("Instituto Tecnológico")
                            .code("ITEC")
                            .city("Medellín")
                            .province("Antioquia")
                            .type("Instituto")
                            .build()
            );

            when(institutionService.getAllActiveInstitutions()).thenReturn(mockInstitutions);

            // When & Then
            mockMvc.perform(get("/api/institutions"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Universidad Nacional"))
                    .andExpect(jsonPath("$[0].code").value("UNAL"))
                    .andExpect(jsonPath("$[0].city").value("Bogotá"))
                    .andExpect(jsonPath("$[0].province").value("Cundinamarca"))
                    .andExpect(jsonPath("$[0].type").value("Universidad"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("Instituto Tecnológico"))
                    .andExpect(jsonPath("$[1].code").value("ITEC"))
                    .andExpect(jsonPath("$[1].city").value("Medellín"))
                    .andExpect(jsonPath("$[1].province").value("Antioquia"))
                    .andExpect(jsonPath("$[1].type").value("Instituto"));

            verify(institutionService, times(1)).getAllActiveInstitutions();
        }

        @Test
        @DisplayName("GET /api/institutions - Should return empty list when no institutions")
        void testGetAllActiveInstitutions_EmptyList() throws Exception {
            // Given
            when(institutionService.getAllActiveInstitutions()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/institutions"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(institutionService, times(1)).getAllActiveInstitutions();
        }

        @Test
        @DisplayName("GET /api/institutions/{institutionId} - Should return institution details")
        void testGetInstitutionById_Success() throws Exception {
            // Given
            Long institutionId = 1L;
            InstitutionResponseDto.InstitutionDetailDto mockInstitution = InstitutionResponseDto.InstitutionDetailDto.builder()
                    .id(1L)
                    .name("Universidad Nacional")
                    .code("UNAL")
                    .type("Universidad")
                    .address("Carrera 45 # 26-85")
                    .city("Bogotá")
                    .province("Cundinamarca")
                    .country("Colombia")
                    .postalCode("111321")
                    .phone("+57-1-3165000")
                    .email("info@unal.edu.co")
                    .website("https://unal.edu.co")
                    .description("Universidad Nacional de Colombia")
                    .active(true)
                    .createdAt(LocalDateTime.now().minusYears(5))
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(institutionService.getInstitutionById(institutionId)).thenReturn(mockInstitution);

            // When & Then
            mockMvc.perform(get("/api/institutions/{institutionId}", institutionId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("Universidad Nacional"))
                    .andExpect(jsonPath("$.address").value("Carrera 45 # 26-85"))
                    .andExpect(jsonPath("$.city").value("Bogotá"))
                    .andExpect(jsonPath("$.province").value("Cundinamarca"))
                    .andExpect(jsonPath("$.country").value("Colombia"))
                    .andExpect(jsonPath("$.postalCode").value("111321"))
                    .andExpect(jsonPath("$.phone").value("+57-1-3165000"))
                    .andExpect(jsonPath("$.email").value("info@unal.edu.co"))
                    .andExpect(jsonPath("$.website").value("https://unal.edu.co"))
                    .andExpect(jsonPath("$.description").value("Universidad Nacional de Colombia"))
                    .andExpect(jsonPath("$.active").value(true))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());

            verify(institutionService, times(1)).getInstitutionById(institutionId);
        }

        @Test
        @DisplayName("GET /api/institutions/{institutionId} - Should return 404 when institution not found")
        void testGetInstitutionById_NotFound() throws Exception {
            // Given
            Long institutionId = 999L;
            when(institutionService.getInstitutionById(institutionId))
                    .thenThrow(new RuntimeException("Institution not found"));

            // When & Then
            mockMvc.perform(get("/api/institutions/{institutionId}", institutionId))
                    .andExpect(status().isNotFound());

            verify(institutionService, times(1)).getInstitutionById(institutionId);
        }

        @Test
        @DisplayName("GET /api/institutions/search - Should search institutions by name")
        void testSearchInstitutions_Success() throws Exception {
            // Given
            String query = "Nacional";
            int limit = 5;
            List<InstitutionResponseDto.InstitutionSummaryDto> mockResults = Arrays.asList(
                    InstitutionResponseDto.InstitutionSummaryDto.builder()
                            .id(1L)
                            .name("Universidad Nacional")
                            .code("UNAL")
                            .city("Bogotá")
                            .province("Cundinamarca")
                            .type("Universidad")
                            .build()
            );

            when(institutionService.searchInstitutions(query, limit)).thenReturn(mockResults);

            // When & Then
            mockMvc.perform(get("/api/institutions/search")
                            .param("query", query)
                            .param("limit", String.valueOf(limit)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("Universidad Nacional"));

            verify(institutionService, times(1)).searchInstitutions(query, limit);
        }

        @Test
        @DisplayName("GET /api/institutions/search - Should use default limit when not provided")
        void testSearchInstitutions_DefaultLimit() throws Exception {
            // Given
            String query = "Universidad";
            when(institutionService.searchInstitutions(anyString(), anyInt())).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/institutions/search")
                            .param("query", query))
                    .andExpect(status().isOk());

            verify(institutionService, times(1)).searchInstitutions(query, 10); // Default limit
        }

        @Test
        @DisplayName("GET /api/institutions/search - Should return 400 when query is missing")
        void testSearchInstitutions_MissingQuery() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/institutions/search"))
                    .andExpect(status().isBadRequest());

            verify(institutionService, never()).searchInstitutions(anyString(), anyInt());
        }

        @Test
        @DisplayName("GET /api/institutions/search - Should return empty list when no matches")
        void testSearchInstitutions_NoResults() throws Exception {
            // Given
            String query = "NonExistentInstitution";
            when(institutionService.searchInstitutions(anyString(), anyInt())).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/institutions/search")
                            .param("query", query))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(institutionService, times(1)).searchInstitutions(anyString(), anyInt());
        }
    }

    // ================================
    // 2. CREACIÓN DE INSTITUCIONES
    // ================================
    
    @Nested
    @DisplayName("2. Creación de Instituciones")
    class InstitutionCreationTests {

        @Test
        @DisplayName("POST /api/institutions - Should register new institution successfully")
        void testRegisterInstitution_Success() throws Exception {
            // Given
            InstitutionRequestDto.InstitutionRegistrationRequestDto requestDto = InstitutionRequestDto.InstitutionRegistrationRequestDto.builder()
                    .name("Instituto de Ciencias")
                    .address("Calle 123 # 45-67")
                    .city("Cali")
                    .state("Valle del Cauca")
                    .country("Colombia")
                    .postalCode("760001")
                    .phone("+57-2-5551234")
                    .email("contacto@instituto.edu.co")
                    .website("https://instituto.edu.co")
                    .logoUrl("https://instituto.edu.co/logo.png")
                    .build();

            InstitutionResponseDto.InstitutionDetailDto registeredInstitution = InstitutionResponseDto.InstitutionDetailDto.builder()
                    .id(123L)
                    .name("Instituto de Ciencias")
                    .code("ICIE")
                    .type("Instituto")
                    .address("Calle 123 # 45-67")
                    .city("Cali")
                    .province("Valle del Cauca")
                    .country("Colombia")
                    .postalCode("760001")
                    .phone("+57-2-5551234")
                    .email("contacto@instituto.edu.co")
                    .website("https://instituto.edu.co")
                    .description("Instituto de Ciencias")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(institutionService.registerInstitution(any(InstitutionRequestDto.InstitutionRegistrationRequestDto.class)))
                    .thenReturn(registeredInstitution);

            // When & Then
            mockMvc.perform(post("/api/institutions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Institución registrada exitosamente"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.data.id").value(123))
                    .andExpect(jsonPath("$.data.name").value("Instituto de Ciencias"))
                    .andExpect(jsonPath("$.data.address").value("Calle 123 # 45-67"))
                    .andExpect(jsonPath("$.data.city").value("Cali"))
                    .andExpect(jsonPath("$.data.province").value("Valle del Cauca"))
                    .andExpect(jsonPath("$.data.country").value("Colombia"))
                    .andExpect(jsonPath("$.data.postalCode").value("760001"))
                    .andExpect(jsonPath("$.data.phone").value("+57-2-5551234"))
                    .andExpect(jsonPath("$.data.email").value("contacto@instituto.edu.co"))
                    .andExpect(jsonPath("$.data.website").value("https://instituto.edu.co"))
                    .andExpect(jsonPath("$.data.active").value(true))
                    .andExpect(jsonPath("$.data.createdAt").exists())
                    .andExpect(jsonPath("$.data.updatedAt").exists());

            verify(institutionService, times(1)).registerInstitution(any(InstitutionRequestDto.InstitutionRegistrationRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/institutions - Should fail with duplicate name")
        void testRegisterInstitution_DuplicateName() throws Exception {
            // Given
            InstitutionRequestDto.InstitutionRegistrationRequestDto requestDto = InstitutionRequestDto.InstitutionRegistrationRequestDto.builder()
                    .name("Universidad Nacional") // Existing name
                    .city("Cali")
                    .country("Colombia")
                    .build();

            when(institutionService.registerInstitution(any(InstitutionRequestDto.InstitutionRegistrationRequestDto.class)))
                    .thenThrow(new IllegalArgumentException("Ya existe una institución con ese nombre"));

            // When & Then
            mockMvc.perform(post("/api/institutions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Ya existe una institución con ese nombre"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.data").doesNotExist());

            verify(institutionService, times(1)).registerInstitution(any(InstitutionRequestDto.InstitutionRegistrationRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/institutions - Should handle validation errors")
        void testRegisterInstitution_ValidationErrors() throws Exception {
            // Given - Request with invalid data
            InstitutionRequestDto.InstitutionRegistrationRequestDto invalidRequest = InstitutionRequestDto.InstitutionRegistrationRequestDto.builder()
                    // Missing required name field
                    .email("invalid-email") // Invalid email format
                    .website("not-a-url") // Invalid URL format
                    .build();

            // When & Then
            mockMvc.perform(post("/api/institutions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());

            verify(institutionService, never()).registerInstitution(any(InstitutionRequestDto.InstitutionRegistrationRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/institutions - Should handle unexpected errors")
        void testRegisterInstitution_UnexpectedError() throws Exception {
            // Given
            InstitutionRequestDto.InstitutionRegistrationRequestDto requestDto = InstitutionRequestDto.InstitutionRegistrationRequestDto.builder()
                    .name("Test Institution")
                    .city("Test City")
                    .country("Test Country")
                    .build();

            when(institutionService.registerInstitution(any(InstitutionRequestDto.InstitutionRegistrationRequestDto.class)))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            mockMvc.perform(post("/api/institutions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Error interno al procesar la solicitud"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.data").doesNotExist());

            verify(institutionService, times(1)).registerInstitution(any(InstitutionRequestDto.InstitutionRegistrationRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/institutions - Should register institution with minimal required fields")
        void testRegisterInstitution_MinimalFields() throws Exception {
            // Given
            InstitutionRequestDto.InstitutionRegistrationRequestDto requestDto = InstitutionRequestDto.InstitutionRegistrationRequestDto.builder()
                    .name("Minimal Institution")
                    .city("City")
                    .country("Country")
                    .build();

            InstitutionResponseDto.InstitutionDetailDto registeredInstitution = InstitutionResponseDto.InstitutionDetailDto.builder()
                    .id(124L)
                    .name("Minimal Institution")
                    .code("MINI")
                    .type("Instituto")
                    .city("City")
                    .country("Country")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(institutionService.registerInstitution(any(InstitutionRequestDto.InstitutionRegistrationRequestDto.class)))
                    .thenReturn(registeredInstitution);

            // When & Then
            mockMvc.perform(post("/api/institutions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(124))
                    .andExpect(jsonPath("$.data.name").value("Minimal Institution"))
                    .andExpect(jsonPath("$.data.city").value("City"))
                    .andExpect(jsonPath("$.data.country").value("Country"))
                    .andExpect(jsonPath("$.data.active").value(true))
                    .andExpect(jsonPath("$.data.createdAt").exists())
                    .andExpect(jsonPath("$.data.updatedAt").exists());

            verify(institutionService, times(1)).registerInstitution(any(InstitutionRequestDto.InstitutionRegistrationRequestDto.class));
        }
    }
}