package com.gamified.application.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTOs para respuestas relacionadas con instituciones
 */
public class InstitutionResponseDto {

    /**
     * DTO para resumen de institución (listados)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstitutionSummaryDto {
        private Long id;
        private String name;
        private String code;
        private String city;
        private String province;
        private String type;
    }

    /**
     * DTO para detalles completos de una institución
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstitutionDetailDto {
        private Long id;
        private String name;
        private String code;
        private String type;
        private String address;
        private String city;
        private String province;
        private String postalCode;
        private String country;
        private String phone;
        private String email;
        private String website;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private boolean active;
    }
} 