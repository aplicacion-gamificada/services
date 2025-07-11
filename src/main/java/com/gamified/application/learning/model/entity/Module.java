package com.gamified.application.learning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa un módulo dentro de una especialización
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Module {
    private Integer id;
    private Integer specializationId;
    private String title;
    private String description;
    private Integer sequence;
    private Integer status;
} 