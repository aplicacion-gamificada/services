package com.gamified.application.auth.entity;


import com.gamified.application.auth.entity.enums.RoleType;
import lombok.*;
import java.time.LocalDateTime;


/**
 * POJO que representa los roles del sistema
 * Solo permite: Student, Teacher, Guardian
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = {"id", "name"})
public class Role {

    private Long id;
    private RoleType name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructor personalizado para crear roles con tipo específico
     */
    public Role(RoleType roleType, String description) {
        this.name = roleType;
        this.description = description;
    }

    /**
     * Constructor con ID para mapeo desde stored procedures
     */
    public Role(Long id, String roleName, String description) {
        this.id = id;
        this.name = RoleType.fromCode(roleName);
        this.description = description;
    }

    /**
     * Método para verificar si es rol de estudiante
     */
    public boolean isStudent() {
        return this.name == RoleType.STUDENT;
    }

    /**
     * Método para verificar si es rol de profesor
     */
    public boolean isTeacher() {
        return this.name == RoleType.TEACHER;
    }

    /**
     * Método para verificar si es rol de tutor
     */
    public boolean isGuardian() {
        return this.name == RoleType.GUARDIAN;
    }

    /**
     * Obtiene el código del rol
     */
    public String getRoleCode() {
        return this.name != null ? this.name.getCode() : null;
    }

    /**
     * Obtiene la descripción del tipo de rol
     */
    public String getRoleTypeDescription() {
        return this.name != null ? this.name.getDescription() : null;
    }

    /**
     * Obtiene el ID numérico del rol para la base de datos
     */
    public Long getRoleId() {
        return this.name != null ? this.name.getId() : null;
    }

    /**
     * Válida que el rol tenga los datos mínimos requeridos
     */
    public boolean isValid() {
        return name != null && description != null && !description.trim().isEmpty();
    }
}