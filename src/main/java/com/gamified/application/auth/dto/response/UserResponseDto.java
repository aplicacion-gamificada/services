package com.gamified.application.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.sql.Date;

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

    /**
     * Respuesta básica para búsqueda de usuarios
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class BasicUserResponseDto {
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String profilePictureUrl;
        private String roleName;
        private String roleCode;
        private String institutionName;
        private Boolean status;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }
    
    /**
     * Respuesta específica para perfil de estudiante
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class StudentResponseDto {
        // Información básica del usuario
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String profilePictureUrl;
        
        // Información específica del estudiante
        private Long studentProfileId;
        private String username;
        private Date birth_date;
        private Integer pointsAmount;
        private Long guardianProfileId;
        private String guardianName;
        private String guardianEmail;
        
        // Información de institución y rol
        private String roleName;
        private String institutionName;
        
        // Estado de la cuenta
        private Boolean status;
        private Boolean emailVerified;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        // Información académica
        private Integer level;
        private List<AchievementSummaryDto> recentAchievements;
    }
    
    /**
     * Respuesta específica para perfil de profesor
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class TeacherResponseDto {
        // Información básica del usuario
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String profilePictureUrl;
        
        // Información específica del profesor
        private Long teacherProfileId;
        private Byte stemAreaId;
        private String stemAreaName;
        
        // Información de institución y rol
        private String roleName;
        private String institutionName;
        
        // Estado de la cuenta
        private Boolean status;
        private Boolean emailVerified;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        // Información académica
        private Integer classroomsCount;
        private Integer studentsCount;
    }
    
    /**
     * Respuesta específica para perfil de tutor
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class GuardianResponseDto {
        // Información básica del usuario
        private Long id;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String profilePictureUrl;
        
        // Información específica del tutor
        private Long guardianProfileId;
        private String phone;
        
        // Información de institución y rol
        private String roleName;
        private String institutionName;
        
        // Estado de la cuenta
        private Boolean status;
        private Boolean emailVerified;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        // Información de estudiantes asociados
        private Integer studentsCount;
        private List<StudentBasicInfoDto> students;
    }
    
    /**
     * Información básica de estudiante para listar en perfil de tutor
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class StudentBasicInfoDto {
        private Long id;
        private Long studentProfileId;
        private String username;
        private String fullName;
        private Date birth_date;
        private Integer pointsAmount;
    }
    
    /**
     * Resumen de logro para listar en perfil de estudiante
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class AchievementSummaryDto {
        private Long id;
        private String name;
        private String description;
        private Integer pointsValue;
        private String rarityTier;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime earnedAt;
    }
}
