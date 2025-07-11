package com.gamified.application.shared.model.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Eventos de dominio para el sistema de logros
 */
public class DomainEvent {

    /**
     * Evento base abstracto
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public abstract static class BaseDomainEvent {
        private String eventId = UUID.randomUUID().toString();
        private LocalDateTime occurredAt = LocalDateTime.now();
        private String eventType;
        private Integer userId;
        private Map<String, Object> metadata;
    }

    /**
     * Evento cuando se completa un ejercicio
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseCompletedEvent extends BaseDomainEvent {
        private Integer exerciseId;
        private Integer studentProfileId;
        private Integer learningPointId;
        private String difficulty;
        private Boolean isCorrect;
        private Double score;
        private Integer timeSpentSeconds;
        private Integer hintsUsed;
        private Integer attemptNumber;
        private String exerciseType;
    }

    /**
     * Evento cuando se actualiza una racha
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreakUpdatedEvent extends BaseDomainEvent {
        private Integer studentProfileId;
        private String streakType; // "daily", "weekly", "exercise", "perfect_score"
        private Integer currentStreak;
        private Integer previousStreak;
        private Boolean isNewRecord;
        private LocalDateTime streakStartDate;
    }

    /**
     * Evento cuando se completa un learning point
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningPointCompletedEvent extends BaseDomainEvent {
        private Integer learningPointId;
        private Integer studentProfileId;
        private String learningPointName;
        private Integer totalExercisesCompleted;
        private Double averageScore;
        private Integer totalTimeSpent;
        private String masteryLevel; // "beginner", "intermediate", "advanced", "mastered"
    }

    /**
     * Evento cuando se desbloquea un logro
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AchievementUnlockedEvent extends BaseDomainEvent {
        private Integer achievementId;
        private Integer studentProfileId;
        private String achievementName;
        private String achievementType;
        private Integer pointsAwarded;
        private String rarityTier;
        private String triggerReason;
    }

    /**
     * Evento de sesión de estudio
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudySessionCompletedEvent extends BaseDomainEvent {
        private Integer studentProfileId;
        private Integer sessionDurationMinutes;
        private Integer exercisesCompleted;
        private Integer correctAnswers;
        private Integer incorrectAnswers;
        private Double averageScore;
        private LocalDateTime sessionStartTime;
        private LocalDateTime sessionEndTime;
    }

    /**
     * Evento de mejora en rendimiento
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceImprovedEvent extends BaseDomainEvent {
        private Integer studentProfileId;
        private String improvementType; // "accuracy", "speed", "consistency"
        private Double previousValue;
        private Double currentValue;
        private Double improvementPercentage;
        private String timeFrame; // "weekly", "monthly", "overall"
        private String subject;
    }
} 