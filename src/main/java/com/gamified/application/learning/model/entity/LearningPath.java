package com.gamified.application.learning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un camino de aprendizaje personalizado para un estudiante
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPath {
    private Integer id;
    private Integer studentProfileId;
    private Integer adaptiveInterventionId;
    private Integer currentLearningPointId;
    private Integer unitsId;
    private BigDecimal completionPercentage;
    private BigDecimal difficultyAdjustment;
    private Integer isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}