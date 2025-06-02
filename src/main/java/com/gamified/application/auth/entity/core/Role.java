package com.gamified.application.auth.entity.core;

import java.sql.Timestamp;

/**
 * POJO que representa los roles del sistema
 * Solo permite: Student, Teacher, Guardian
 */
public class Role {

    private Byte id;
    private String name;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Getters and Setters

    public Byte getId() {
        return id;
    }

    public void setId(Byte id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods for role checking
    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(this.name);
    }

    public boolean isTeacher() {
        return "TEACHER".equalsIgnoreCase(this.name);
    }

    public boolean isGuardian() {
        return "GUARDIAN".equalsIgnoreCase(this.name);
    }

    public String getRoleCode() {
        return this.name;
    }

    /**
     * Obtiene la descripción del tipo de rol
     */
    public String getRoleTypeDescription() {
        return this.description;
    }

    /**
     * Válida que el rol tenga los datos mínimos requeridos
     */
    public boolean isValid() {
        return name != null && description != null && !description.trim().isEmpty();
    }
}