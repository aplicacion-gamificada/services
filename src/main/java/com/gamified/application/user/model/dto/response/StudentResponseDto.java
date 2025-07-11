package com.gamified.application.user.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs de respuesta para operaciones de estudiantes
 */
public class StudentResponseDto {
    
    /**
     * DTO para la especialización asignada al estudiante
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedSpecializationDto {
        private Integer specializationId;
        private String specializationTitle;
        private String specializationDescription;
        private Integer stemAreaId;
        private String stemAreaTitle;
        private Integer classroomId;
        private String classroomName;
        private Integer totalModules;
        private Integer completedModules;
        private Double progressPercentage;
        private LocalDateTime enrolledAt;
    }
    
    /**
     * DTO para el progreso del estudiante en una especialización
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecializationProgressDto {
        private Integer specializationId;
        private String specializationTitle;
        private Integer totalModules;
        private Integer completedModules;
        private Integer totalUnits;
        private Integer completedUnits;
        private Integer totalLessons;
        private Integer completedLessons;
        private Double overallProgress;
        private List<ModuleProgressDto> modules;
        private NextLearningItemDto nextLearningItem;
        private LocalDateTime lastActivity;
    }
    
    /**
     * DTO para el progreso del estudiante en un classroom
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomProgressDto {
        private Integer classroomId;
        private String classroomName;
        private Integer specializationId;
        private String specializationTitle;
        private Double overallProgress;
        private Integer totalModules;
        private Integer completedModules;
        private List<ModuleProgressDto> modules;
        private NextLearningItemDto nextLearningItem;
        private LocalDateTime lastActivity;
    }
    
    /**
     * DTO para el progreso en un módulo específico
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleProgressDto {
        private Integer moduleId;
        private String moduleTitle;
        private String moduleDescription;
        private Integer sequence;
        private Integer totalUnits;
        private Integer completedUnits;
        private Double progressPercentage;
        private Boolean isCompleted;
        private Boolean isUnlocked;
        private LocalDateTime completedAt;
    }
    
    /**
     * DTO para el siguiente elemento de aprendizaje recomendado
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextLearningItemDto {
        private String itemType; // "MODULE", "UNIT", "LESSON"
        private Integer itemId;
        private String itemTitle;
        private String itemDescription;
        private String navigationPath; // Ruta para navegar al elemento
        private Boolean isRecommended;
        private String recommendationReason;
    }
} 