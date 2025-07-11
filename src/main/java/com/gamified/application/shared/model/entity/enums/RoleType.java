package com.gamified.application.shared.model.entity.enums;

/**
 * Enum que define los roles disponibles en el sistema
 * Solo permite: Student, Teacher y Guardian
 */
public enum RoleType {
    ADMIN((byte) 1, "ADMIN", "Administrador del sistema"),
    TEACHER((byte) 2, "TEACHER", "Profesor o educador"),
    STUDENT((byte) 3, "STUDENT", "Estudiante del sistema"),
    GUARDIAN((byte) 4, "GUARDIAN", "Tutor o apoderado");

    private final Byte id;
    private final String code;
    private final String description;

    RoleType(Byte id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public Byte getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static RoleType fromId(Byte id) {
        for (RoleType type : RoleType.values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No RoleType with id " + id);
    }

    public static RoleType fromCode(String code) {
        for (RoleType type : RoleType.values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No RoleType with code " + code);
    }
}

