package com.gamified.application.learning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad que representa una unidad dentro de un módulo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    private Integer id;
    private Integer moduleId;
    private String title;
    private String description;
    private Integer sequence;
    private Integer status;
} 