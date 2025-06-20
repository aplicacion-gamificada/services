package com.gamified.application.learning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa una lección dentro de un learning point
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {
    private Integer id;
    private Integer learningPointId;
    private String title;
    private String contentData;
    private Integer sequenceOrder;
    private Integer estimatedReadingTime;
    private Integer isMandatory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 