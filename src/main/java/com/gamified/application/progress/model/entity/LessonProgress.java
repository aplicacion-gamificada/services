package com.gamified.application.progress.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa el progreso de una lección específica por estudiante
 * (Tabla virtual - no existe en BD, se maneja en memoria/cache)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgress {
    
    private Integer studentProfileId;
    private Integer lessonId;
    private Integer learningPointId;
    private boolean isCompleted;
    private LocalDateTime completedAt;
    private Integer timeSpentMinutes;
} 