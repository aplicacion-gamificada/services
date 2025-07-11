package com.gamified.application.learning.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs de respuesta para el módulo Learning
 */
public class LearningResponseDto {

    /**
     * DTO para área STEM
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StemAreaDto {
        private Integer id;
        private String title;
        private String description;
        private Integer status;
        private Integer specializationsCount;
    }

    /**
     * DTO para especialización
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecializationDto {
        private Integer id;
        private Integer stemAreaId;
        private String stemAreaTitle;
        private String title;
        private String description;
        private Integer status;
        private Integer modulesCount;
    }

    /**
     * DTO para módulo
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModuleDto {
        private Integer id;
        private Integer specializationId;
        private String specializationTitle;
        private String title;
        private String description;
        private Integer sequence;
        private Integer status;
        private Integer unitsCount;
    }

    /**
     * DTO para unidad
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitDto {
        private Integer id;
        private Integer moduleId;
        private String moduleTitle;
        private String title;
        private String description;
        private Integer sequence;
        private Integer status;
        private Integer learningPointsCount;
        private LocalDateTime createdAt;
    }

    /**
     * DTO para learning point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningPointDto {
        private Integer id;
        private Integer learningPathId;
        private String unitTitle;
        private String title;
        private String description;
        private Integer sequenceOrder;
        private Integer estimatedDuration;
        private BigDecimal difficultyWeight;
        private BigDecimal masteryThreshold;
        private Boolean isPrerequisite;
        private String unlockCriteria;
        private Integer status;
        private Integer lessonsCount;
        private Integer exercisesCount;
        private LocalDateTime createdAt;
    }

    /**
     * DTO para lección
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonDto {
        private Integer id;
        private Integer learningPointId;
        private String learningPointTitle;
        private String title;
        private String contentData;
        private Integer sequenceOrder;
        private Integer estimatedReadingTime;
        private Boolean isMandatory;
        private LocalDateTime createdAt;
    }

    /**
     * DTO completo de lección con datos adicionales
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonDetailDto {
        private Integer id;
        private Integer learningPointId;
        private String learningPointTitle;
        private String title;
        private String contentData;
        private Integer sequenceOrder;
        private Integer estimatedReadingTime;
        private Boolean isMandatory;
        private LocalDateTime createdAt;
        
        // Navegación
        private LessonDto previousLesson;
        private LessonDto nextLesson;
        
        // Progreso
        private Boolean isCompleted;
        private LocalDateTime completedAt;
    }
} 