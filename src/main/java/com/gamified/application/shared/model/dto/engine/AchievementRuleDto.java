package com.gamified.application.shared.model.dto.engine;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTOs para reglas de Achievement estructuradas en JSON
 * Versión 1.0 del esquema de reglas
 */
public class AchievementRuleDto {

    /**
     * Esquema base para reglas de achievement
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleSchema {
        @NotBlank(message = "Version is required")
        @Pattern(regexp = "^1\\.[0-9]+$", message = "Version must follow format 1.x")
        private String version;
        
        @NotBlank(message = "Rule type is required")
        private String ruleType;
        
        @NotNull(message = "Conditions are required")
        @Valid
        private List<RuleCondition> conditions;
        
        @Valid
        private RuleMetadata metadata;
    }

    /**
     * Condición individual dentro de una regla
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "conditionType")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = ExerciseCondition.class, name = "EXERCISE"),
        @JsonSubTypes.Type(value = StreakCondition.class, name = "STREAK"),
        @JsonSubTypes.Type(value = TimeCondition.class, name = "TIME"),
        @JsonSubTypes.Type(value = PerformanceCondition.class, name = "PERFORMANCE"),
        @JsonSubTypes.Type(value = CompositeCondition.class, name = "COMPOSITE")
    })
    public abstract static class RuleCondition {
        @NotBlank(message = "Condition type is required")
        private String conditionType;
        
        @NotBlank(message = "Operator is required")
        private String operator; // "AND", "OR", "NOT"
        
        private Integer priority; // Para orden de evaluación
    }

    /**
     * Condición basada en ejercicios completados
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExerciseCondition extends RuleCondition {
        @NotNull(message = "Exercise count is required")
        private Integer requiredCount;
        
        private String difficulty; // "easy", "medium", "hard"
        private List<Integer> learningPointIds;
        private List<Integer> exerciseTypeIds;
        private Integer timeFrameDays; // Período de tiempo para completar
        private Double minimumAccuracy; // Precisión mínima requerida
        
        @Builder
        public ExerciseCondition(String conditionType, String operator, Integer priority,
                               Integer requiredCount, String difficulty, List<Integer> learningPointIds,
                               List<Integer> exerciseTypeIds, Integer timeFrameDays, Double minimumAccuracy) {
            super(conditionType, operator, priority);
            this.requiredCount = requiredCount;
            this.difficulty = difficulty;
            this.learningPointIds = learningPointIds;
            this.exerciseTypeIds = exerciseTypeIds;
            this.timeFrameDays = timeFrameDays;
            this.minimumAccuracy = minimumAccuracy;
        }
    }

    /**
     * Condición basada en rachas de actividad
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreakCondition extends RuleCondition {
        @NotNull(message = "Streak length is required")
        private Integer requiredStreakLength;
        
        private String streakType; // "daily", "weekly", "exercise", "perfect_score"
        private Integer minimumActivityPerDay;
        
        @Builder
        public StreakCondition(String conditionType, String operator, Integer priority,
                             Integer requiredStreakLength, String streakType, Integer minimumActivityPerDay) {
            super(conditionType, operator, priority);
            this.requiredStreakLength = requiredStreakLength;
            this.streakType = streakType;
            this.minimumActivityPerDay = minimumActivityPerDay;
        }
    }

    /**
     * Condición basada en tiempo/velocidad
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeCondition extends RuleCondition {
        private Integer maxTimeSeconds; // Tiempo máximo para completar
        private Integer minTimeSeconds; // Tiempo mínimo (para evitar trampa)
        private String timeType; // "per_exercise", "total_session", "average"
        
        @Builder
        public TimeCondition(String conditionType, String operator, Integer priority,
                           Integer maxTimeSeconds, Integer minTimeSeconds, String timeType) {
            super(conditionType, operator, priority);
            this.maxTimeSeconds = maxTimeSeconds;
            this.minTimeSeconds = minTimeSeconds;
            this.timeType = timeType;
        }
    }

    /**
     * Condición basada en rendimiento/puntuación
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceCondition extends RuleCondition {
        private Double minimumScore; // Puntuación mínima
        private Double minimumAverage; // Promedio mínimo
        private Integer minimumAttempts; // Número mínimo de intentos
        private String performanceType; // "score", "accuracy", "improvement"
        
        @Builder
        public PerformanceCondition(String conditionType, String operator, Integer priority,
                                  Double minimumScore, Double minimumAverage, Integer minimumAttempts,
                                  String performanceType) {
            super(conditionType, operator, priority);
            this.minimumScore = minimumScore;
            this.minimumAverage = minimumAverage;
            this.minimumAttempts = minimumAttempts;
            this.performanceType = performanceType;
        }
    }

    /**
     * Condición compuesta que combina otras condiciones
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompositeCondition extends RuleCondition {
        @NotNull(message = "Sub-conditions are required")
        @Valid
        private List<RuleCondition> subConditions;
        
        private String logicalOperator; // "ALL", "ANY", "NONE"
        
        @Builder
        public CompositeCondition(String conditionType, String operator, Integer priority,
                                List<RuleCondition> subConditions, String logicalOperator) {
            super(conditionType, operator, priority);
            this.subConditions = subConditions;
            this.logicalOperator = logicalOperator;
        }
    }

    /**
     * Metadatos adicionales para la regla
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleMetadata {
        private String description;
        private String category;
        private Integer difficulty; // 1-10
        private List<String> tags;
        private Map<String, Object> customProperties;
        private String createdBy;
        private String lastModifiedBy;
    }

    /**
     * Respuesta de evaluación de regla
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleEvaluationResult {
        private Boolean passed;
        private String ruleName;
        private String ruleType;
        private List<ConditionResult> conditionResults;
        private Map<String, Object> contextData;
        private String failureReason;
        private Double completionPercentage;
    }

    /**
     * Resultado de evaluación de condición individual
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConditionResult {
        private String conditionType;
        private Boolean passed;
        private String description;
        private Map<String, Object> actualValues;
        private Map<String, Object> requiredValues;
    }
} 