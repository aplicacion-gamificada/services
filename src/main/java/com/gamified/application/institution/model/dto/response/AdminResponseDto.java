package com.gamified.application.institution.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para respuestas de operaciones administrativas
 */
public class AdminResponseDto {

    /**
     * Resumen de usuarios de una institución
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstitutionUsersSummaryDto {
        private Long institutionId;
        private String institutionName;
        private Integer totalStudents;
        private Integer studentsWithGuardian;
        private Integer studentsWithoutGuardian;
        private Integer totalGuardians;
        private Integer totalTeachers;
        private Integer totalAdmins;
        private LocalDateTime lastUpdated;
    }

    /**
     * Resumen básico de usuario para listas administrativas
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummaryDto {
        private Long userId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String roleName;
        private Boolean status;
        private Boolean emailVerified;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;
    }

    /**
     * Estudiante disponible para asignación de guardián
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentForAssignmentDto {
        private Long userId;
        private Long studentProfileId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String username;
        private LocalDate birthDate;
        private Integer pointsAmount;
        private GuardianBasicInfoDto currentGuardian; // null si no tiene
        private LocalDateTime enrollmentDate;
        private Boolean needsGuardian;
    }

    /**
     * Guardián disponible para asignación
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuardianForAssignmentDto {
        private Long userId;
        private Long guardianProfileId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private Integer currentStudentsCount;
        private Integer maxStudentsRecommended; // Sugerencia: máximo 3-5 estudiantes
        private List<StudentBasicInfoDto> currentStudents;
        private Boolean availableForNewAssignments;
    }

    /**
     * Información básica de guardián
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuardianBasicInfoDto {
        private Long guardianProfileId;
        private String fullName;
        private String email;
        private String phone;
    }

    /**
     * Información básica de estudiante
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentBasicInfoDto {
        private Long studentProfileId;
        private String fullName;
        private String username;
        private Integer pointsAmount;
    }

    /**
     * Respuesta completa de estudiante (reutilizando del shared)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentResponseDto {
        private Long userId;
        private Long studentProfileId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String username;
        private LocalDate birthDate;
        private Integer pointsAmount;
        private String roleName;
        private Boolean status;
        private Boolean emailVerified;
        private LocalDateTime createdAt;
        private GuardianBasicInfoDto guardian;
    }

    /**
     * Estadísticas institucionales
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstitutionStatisticsDto {
        private Long institutionId;
        private String institutionName;
        
        // Estadísticas de usuarios
        private Integer totalUsers;
        private Integer activeUsers;
        private Integer inactiveUsers;
        
        // Estadísticas por rol
        private Integer totalStudents;
        private Integer totalTeachers;
        private Integer totalGuardians;
        private Integer totalAdmins;
        
        // Estadísticas de relaciones
        private Integer studentsWithGuardian;
        private Integer studentsWithoutGuardian;
        private Double guardianAssignmentPercentage;
        
        // Estadísticas de actividad
        private Integer usersLoggedInLast30Days;
        private Integer newUsersThisMonth;
        private LocalDateTime lastActivity;
        
        // Metadata
        private LocalDateTime generatedAt;
        private String generatedBy;
    }
} 