package com.gamified.application.auth.entity.enums;


/**
 * Enum para tipos de acciones de auditoría
 */
public enum ActionType {
    CREATE(1, "create", "Crear"),
    READ(2, "read", "Leer"),
    UPDATE(3, "update", "Actualizar"),
    DELETE(4, "delete", "Eliminar"),
    LOGIN(5, "login", "Iniciar sesión"),
    LOGOUT(6, "logout", "Cerrar sesión"),
    PASSWORD_CHANGE(7, "password_change", "Cambio de contraseña"),
    PASSWORD_RESET(8, "password_reset", "Reseteo de contraseña"),
    EMAIL_VERIFICATION(9, "email_verification", "Verificación de email"),
    PROFILE_UPDATE(10, "profile_update", "Actualización de perfil"),
    ACCOUNT_ACTIVATION(11, "account_activation", "Activación de cuenta"),
    ACCOUNT_DEACTIVATION(12, "account_deactivation", "Desactivación de cuenta"),
    FAILED_LOGIN(13, "failed_login", "Intento de login fallido"),
    OTHER(14, "other", "Otra acción");

    private final int id;
    private final String code;
    private final String description;

    ActionType(int id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static ActionType fromId(int id) {
        for (ActionType action : values()) {
            if (action.id == id) {
                return action;
            }
        }
        throw new IllegalArgumentException("ID de acción no válido: " + id);
    }

    public static ActionType fromCode(String code) {
        for (ActionType action : values()) {
            if (action.code.equalsIgnoreCase(code)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Código de acción no válido: " + code);
    }
}
