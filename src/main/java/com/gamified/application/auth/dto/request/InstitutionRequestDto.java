package com.gamified.application.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs para solicitudes relacionadas con instituciones
 */
public class InstitutionRequestDto {

    /**
     * DTO para registro de institución
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstitutionRegistrationRequestDto {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        private String name;
        
        @NotBlank(message = "La dirección es obligatoria")
        private String address;
        
        @NotBlank(message = "La ciudad es obligatoria")
        private String city;
        
        private String state;
        
        @NotBlank(message = "El país es obligatorio")
        private String country;
        
        private String postalCode;
        
        @NotBlank(message = "El teléfono es obligatorio")
        private String phone;
        
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato válido")
        private String email;
        
        private String website;
        
        private String logoUrl;
    }
    
    /**
     * DTO para actualización de institución
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstitutionUpdateRequestDto {
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
        private String name;
        
        private String address;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        private String phone;
        
        @Email(message = "El email debe tener un formato válido")
        private String email;
        
        private String website;
        private String logoUrl;
        private Boolean status;
    }
} 