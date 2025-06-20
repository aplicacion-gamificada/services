package com.gamified.application.learning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa un punto de aprendizaje dentro de un learning path
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPoint {
    private Integer id;
    private Integer learningPathId;
    private String title;
    private String description;
    private Integer sequenceOrder;
    private Integer estimatedDuration;
    private BigDecimal difficultyWeight;
    private BigDecimal masteryThreshold;
    private Integer isPrerequisite;
    private String unlockCriteria;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 