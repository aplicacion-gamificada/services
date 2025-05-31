package com.gamified.application.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class UserResponseDto {
    /**
     * Resumen de usuario para listados
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserSummaryDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String profilePictureUrl;
        private String initials;

        // Información básica del rol y institución
        private String roleName;
        private String roleCode;
        private String institutionName;

        // Estado de la cuenta
        private Boolean emailVerified;
        private Boolean accountActive;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        // Indicadores de estado
        private String accountStatus; // "active", "inactive", "pending_verification"
        private Integer daysSinceLastLogin;
    }

    /**
     * DTO con información completa del usuario autenticado
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserInfoDto {

        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String profilePictureUrl;
        private String initials;

        // Información del rol
        private AuthResponseDto.RoleInfoDto role;

        // Información de la institución
        private AuthResponseDto.InstitutionInfoDto institution;

        // Estado de la cuenta
        private Boolean emailVerified;
        private Boolean accountActive;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        // Configuraciones básicas del usuario
        private AuthResponseDto.UserPreferencesDto preferences;
    }

    /**
     * DTO de respuesta para listado de usuarios
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserListResponseDto {

        private List<UserSummaryDto> users;
        private PaginationDto pagination;
        private FilterSummaryDto appliedFilters;
    }

    /**
     * DTO de respuesta para operaciones de usuario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserOperationResponseDto {

        private Boolean success;
        private String message;
        private String operation; // "create", "update", "activate", "deactivate"

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime processedAt;

        private UserInfoDto user; // Usuario afectado
        private List<String> warnings; // Advertencias si las hay

        // Información adicional de la operación
        private String performedBy; // Email del usuario que realizó la operación
        private String operationDetails;
    }

    /**
     * DTO de perfil completo del usuario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserProfileResponseDto {

        private UserInfoDto userInfo;
        private List<SessionResponseDto.SessionInfoResponseDto> activeSessions;
        private UserStatsDto statistics;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastProfileUpdate;

        // Configuraciones de privacidad
        private PrivacySettingsDto privacySettings;
    }

    /**
     * Resumen de filtros aplicados
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class FilterSummaryDto {
        private String searchTerm;
        private String roleName;
        private String institutionName;
        private String statusFilter;
        private String emailVerifiedFilter;
        private String sortBy;
        private String sortDirection;
        private Integer totalFiltersApplied;
    }

    /**
     * Información de paginación
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class PaginationDto {
        private Integer currentPage;
        private Integer pageSize;
        private Integer totalPages;
        private Long totalElements;
        private Boolean hasNext;
        private Boolean hasPrevious;
        private Integer startElement;
        private Integer endElement;
    }

    /**
     * Configuraciones de privacidad
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class PrivacySettingsDto {
        @Builder.Default
        private Boolean showEmail = false;

        @Builder.Default
        private Boolean showLastLogin = true;

        @Builder.Default
        private Boolean allowProfileView = true;

        @Builder.Default
        private Boolean shareStatistics = false;
    }

    /**
     * Estadísticas del usuario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserStatsDto {
        private Integer totalLogins;
        private Integer totalSessions;
        private Integer averageSessionMinutes;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime firstLogin;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLogin;

        private Integer daysActive;
        private String mostActiveTimeOfDay;
    }
}
