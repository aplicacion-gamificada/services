package com.gamified.application.auth.entity.enums;


/**
 * Enum que define los roles disponibles en el sistema
 * Solo permite: Student, Teacher y Guardian
 */
public enum RoleType {
    STUDENT("student", "Estudiante del sistema"),
    TEACHER("teacher", "Docente/Profesor del sistema"),
    GUARDIAN("guardian", "Tutor/Apoderado del estudiante");

    private final String code;
    private final String description;

    RoleType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RoleType fromCode(String code) {
        for (RoleType role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Rol no válido: " + code);
    }

    public static RoleType fromId(Long id) {
        if (id == null) return null;

        switch (id.intValue()) {
            case 1: return STUDENT;
            case 2: return TEACHER;
            case 3: return GUARDIAN;
            default: throw new IllegalArgumentException("ID de rol no válido: " + id);
        }
    }

    public Long getId() {
        switch (this) {
            case STUDENT: return 1L;
            case TEACHER: return 2L;
            case GUARDIAN: return 3L;
            default: return null;
        }
    }
}
