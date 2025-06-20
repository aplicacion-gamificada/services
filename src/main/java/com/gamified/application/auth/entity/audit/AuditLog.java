package com.gamified.application.auth.entity.audit;

import com.gamified.application.user.model.entity.User;
import com.gamified.application.shared.model.entity.enums.ActionType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * POJO que registra todas las acciones realizadas por los usuarios
 * para auditoría y trazabilidad del sistema
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = {"id"})
public class AuditLog {

    private Long id;
    private Long userId;
    private ActionType actionType;
    private String entityType;
    private Long entityId;
    private String actionDetails;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime performedAt;
    private String oldValues;
    private String newValues;
    private String description;
    private String requestId;
    private String sessionId;
    private Boolean isSensitive;

    // Objeto relacionado (se carga por separado si es necesario)
    private User user;

    /**
     * Constructor para mapeo desde stored procedures (datos básicos)
     */
    public AuditLog(Long id, Long userId, int actionTypeId, String entityType, Long entityId,
                    String actionDetails, String ipAddress, String userAgent, LocalDateTime performedAt) {
        this.id = id;
        this.userId = userId;
        this.actionType = ActionType.fromId(actionTypeId);
        this.entityType = entityType;
        this.entityId = entityId;
        this.actionDetails = actionDetails;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.performedAt = performedAt;
    }

    /**
     * Constructor completo para mapeo desde stored procedures
     */
    public AuditLog(Long id, Long userId, int actionTypeId, String entityType, Long entityId,
                    String actionDetails, String ipAddress, String userAgent, LocalDateTime performedAt,
                    String oldValues, String newValues, String description, String requestId,
                    String sessionId, Boolean isSensitive) {
        this.id = id;
        this.userId = userId;
        this.actionType = ActionType.fromId(actionTypeId);
        this.entityType = entityType;
        this.entityId = entityId;
        this.actionDetails = actionDetails;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.performedAt = performedAt;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.description = description;
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.isSensitive = isSensitive;
    }

    /**
     * Constructor para acciones básicas
     */
    public AuditLog(Long userId, ActionType actionType, String entityType, Long entityId,
                    String description, String ipAddress, String userAgent) {
        this.userId = userId;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.performedAt = LocalDateTime.now();
    }

    /**
     * Constructor para acciones con detalles completos
     */
    public AuditLog(Long userId, ActionType actionType, String entityType, Long entityId,
                    String actionDetails, String oldValues, String newValues,
                    String ipAddress, String userAgent, String requestId, String sessionId) {
        this.userId = userId;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.actionDetails = actionDetails;
        this.oldValues = oldValues;
        this.newValues = newValues;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.requestId = requestId;
        this.sessionId = sessionId;
        this.performedAt = LocalDateTime.now();
    }

    /**
     * Verifica si la acción es sensible desde el punto de vista de seguridad
     */
    public boolean isSensitiveAction() {
        return Boolean.TRUE.equals(this.isSensitive) ||
                this.actionType == ActionType.LOGIN ||
                this.actionType == ActionType.LOGOUT ||
                this.actionType == ActionType.PASSWORD_CHANGE ||
                this.actionType == ActionType.PASSWORD_RESET ||
                this.actionType == ActionType.FAILED_LOGIN ||
                this.actionType == ActionType.ACCOUNT_ACTIVATION ||
                this.actionType == ActionType.ACCOUNT_DEACTIVATION;
    }

    /**
     * Marca la acción como sensible
     */
    public void markAsSensitive() {
        this.isSensitive = true;
    }

    /**
     * Obtiene el nombre del usuario que realizó la acción
     */
    public String getUserFullName() {
        return this.user != null ? this.user.getFullName() : "Usuario desconocido";
    }

    /**
     * Obtiene el email del usuario que realizó la acción
     */
    public String getUserEmail() {
        return this.user != null ? this.user.getEmail() : "Email desconocido";
    }

    /**
     * Obtiene una descripción legible de la acción
     */
    public String getActionDescription() {
        if (this.description != null && !this.description.trim().isEmpty()) {
            return this.description;
        }

        StringBuilder desc = new StringBuilder();
        if (this.actionType != null) {
            desc.append(this.actionType.getDescription());
        }

        if (this.entityType != null) {
            desc.append(" en ").append(this.entityType);
        }

        if (this.entityId != null) {
            desc.append(" (ID: ").append(this.entityId).append(")");
        }

        return desc.toString();
    }

    /**
     * Verifica si hubo cambios en los valores
     */
    public boolean hasValueChanges() {
        return (this.oldValues != null && !this.oldValues.trim().isEmpty()) ||
                (this.newValues != null && !this.newValues.trim().isEmpty());
    }

    /**
     * Obtiene el ID numérico del tipo de acción para la base de datos
     */
    public Integer getActionTypeId() {
        return this.actionType != null ? this.actionType.getId() : null;
    }
}