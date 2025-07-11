package com.gamified.application.progress.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs de respuesta para el módulo Progress
 */
public class ProgressResponseDto {

    /**
     * DTO para el learning path del estudiante
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningPathDto {
        private Integer id;
        private Integer studentProfileId;
        private Integer currentLearningPointId;
        private String currentLearningPointTitle;
        private Integer unitsId;
        private String unitTitle;
        private BigDecimal completionPercentage;
        private BigDecimal difficultyAdjustment;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Información adicional del progreso
        private Integer totalLessons;
        private Integer completedLessons;
        private Integer remainingLessons;
    }

    /**
     * DTO para el progreso actual del estudiante
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentProgressDto {
        private Integer studentProfileId;
        private LearningPathDto learningPath;
        
        // Estadísticas de progreso
        private Integer totalLearningPoints;
        private Integer completedLearningPoints;
        private Integer totalLessons;
        private Integer completedLessons;
        private BigDecimal overallCompletionPercentage;
        
        // Progreso actual
        private LearningPointProgressDto currentLearningPoint;
        private List<LessonProgressDto> currentLessons;
    }

    /**
     * DTO para el progreso de un learning point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningPointProgressDto {
        private Integer learningPointId;
        private String title;
        private String description;
        private Integer totalLessons;
        private Integer completedLessons;
        private BigDecimal completionPercentage;
        private boolean isCompleted;
        private LocalDateTime completedAt;
    }

    /**
     * DTO para el progreso de una lección
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonProgressDto {
        private Integer lessonId;
        private String title;
        private Integer sequenceOrder;
        private boolean isCompleted;
        private LocalDateTime completedAt;
        private Integer timeSpentMinutes;
        private boolean isMandatory;
    }

    /**
     * DTO para la respuesta de completar una lección
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonCompletionDto {
        private Integer lessonId;
        private String lessonTitle;
        private boolean wasAlreadyCompleted;
        private LocalDateTime completedAt;
        
        // Progreso actualizado
        private LearningPointProgressDto learningPointProgress;
        private BigDecimal overallProgress;
        
        // Información de navegación
        private Integer nextLessonId;
        private String nextLessonTitle;
        private boolean allLessonsCompleted;
        private Integer nextLearningPointId;
        private String nextLearningPointTitle;
    }

    /**
     * DTO para el siguiente learning point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NextLearningPointDto {
        private Integer learningPointId;
        private String title;
        private String description;
        private Integer sequenceOrder;
        private Integer estimatedDuration;
        private BigDecimal difficultyWeight;
        private boolean isUnlocked;
        private String unlockCriteria;
        
        // Información de contexto
        private Integer unitId;
        private String unitTitle;
        private Integer totalLessons;
        private List<LessonProgressDto> lessons;
    }
} 