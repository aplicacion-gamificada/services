package com.gamified.application.learning.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un área STEM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StemArea {
    private Integer id;
    private String title;
    private String description;
    private Integer status;
} 