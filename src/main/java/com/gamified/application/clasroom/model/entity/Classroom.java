package com.gamified.application.clasroom.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un aula/clase
 * Mapea la tabla 'classroom' de la base de datos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Classroom {
    
    private Integer id;
    private Integer teacherProfileId;
    private String grade;
    private String section;
    private String year;
    private String name;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer specializationId;
} 