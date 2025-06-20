package com.gamified.application.clasroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa la inscripción de un estudiante en un aula
 * Mapea la tabla 'enrollment' de la base de datos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    
    private Integer id;
    private Integer classroomId;
    private Integer studentProfileId;
    private LocalDateTime joinedAt;
    private Integer status;
} 