package com.gamified.application.progress.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa el learning path de un estudiante
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