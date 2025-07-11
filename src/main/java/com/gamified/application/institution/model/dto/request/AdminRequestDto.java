package com.gamified.application.institution.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs para peticiones de operaciones administrativas
 */
public class AdminRequestDto {

    /**
     * Request para asignar guardián a estudiante
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignGuardianRequestDto {
        
        @NotNull(message = "El ID del perfil de estudiante es obligatorio")
        private Long studentProfileId;
        
        @NotNull(message = "El ID del perfil de guardián es obligatorio")
        private Long guardianProfileId;
        
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        private String notes; // Notas adicionales para la asignación
        
        private Boolean notifyGuardian; // Si enviar notificación al guardián
        private Boolean notifyStudent; // Si enviar notificación al estudiante
    }

    /**
     * Request para reasignar guardián (con auditoría)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReassignGuardianRequestDto {
        
        @NotNull(message = "El ID del perfil de estudiante es obligatorio")
        private Long studentProfileId;
        
        @NotNull(message = "El ID del nuevo guardián es obligatorio")
        private Long newGuardianProfileId;
        
        private Long previousGuardianProfileId; // Para auditoría
        
        @NotNull(message = "El motivo de la reasignación es obligatorio")
        @Size(min = 10, max = 1000, message = "El motivo debe tener entre 10 y 1000 caracteres")
        private String reason; // Motivo del cambio
        
        private Boolean notifyPreviousGuardian; // Notificar al guardián anterior
        private Boolean notifyNewGuardian; // Notificar al nuevo guardián
        private Boolean notifyStudent; // Notificar al estudiante
    }

    /**
     * Request para búsqueda de usuarios con filtros
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSearchRequestDto {
        
        @Size(min = 2, max = 100, message = "El término de búsqueda debe tener entre 2 y 100 caracteres")
        private String searchTerm; // Búsqueda en nombre, apellido, email o username
        
        private String role; // Filtro por rol: STUDENT, TEACHER, GUARDIAN, ADMIN
        private Boolean status; // true = activo, false = inactivo, null = todos
        private Boolean emailVerified; // true = verificado, false = no verificado, null = todos
        private Boolean unassigned; // Solo para estudiantes: sin guardián asignado
        
        // Paginación
        private Integer page = 0;
        private Integer size = 20;
        private String sortBy = "createdAt"; // Campo para ordenar
        private String sortDirection = "desc"; // asc o desc
    }

    /**
     * Request para actualización masiva de usuarios
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BulkUserUpdateRequestDto {
        
        @NotNull(message = "La lista de IDs de usuario es obligatoria")
        private Long[] userIds;
        
        private Boolean status; // Activar/desactivar usuarios
        private Boolean emailVerified; // Marcar como email verificado
        
        @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
        private String notes; // Notas sobre la operación masiva
    }
} 