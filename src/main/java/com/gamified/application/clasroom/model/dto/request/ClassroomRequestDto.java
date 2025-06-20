package com.gamified.application.clasroom.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs para requests del módulo classroom
 */
public class ClassroomRequestDto {

    /**
     * DTO para crear un nuevo aula
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateClassroomRequestDto {
        
        @NotBlank(message = "El grado es requerido")
        @Size(max = 20, message = "El grado no puede exceder 20 caracteres")
        private String grade;
        
        @NotBlank(message = "La sección es requerida")
        @Size(max = 20, message = "La sección no puede exceder 20 caracteres")
        private String section;
        
        @NotBlank(message = "El año es requerido")
        @Size(max = 9, message = "El año no puede exceder 9 caracteres")
        private String year;
        
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        private String name;
    }

    /**
     * DTO para inscribir un estudiante en un aula
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollStudentRequestDto {
        
        @NotNull(message = "El ID del estudiante es requerido")
        private Integer studentProfileId;
        
        private String notes; // Campo opcional para notas adicionales
    }

    /**
     * DTO para actualizar un aula
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateClassroomRequestDto {
        
        @Size(max = 20, message = "El grado no puede exceder 20 caracteres")
        private String grade;
        
        @Size(max = 20, message = "La sección no puede exceder 20 caracteres")
        private String section;
        
        @Size(max = 9, message = "El año no puede exceder 9 caracteres")
        private String year;
        
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
        private String name;
        
        private Integer status;
    }
} 