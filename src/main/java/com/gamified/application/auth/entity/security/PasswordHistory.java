package com.gamified.application.auth.entity.security;

import java.sql.Timestamp;

/**
 * POJO para mantener un historial de contraseñas
 * Previene que los usuarios reutilicen contraseñas recientes
 */
public class PasswordHistory {

    private Long id;
    private Long userId;
    private String passwordHash;
    private Timestamp changedAt;
    private Boolean changedByAdmin;
    private String ipAddress;
    private String userAgent;

    public PasswordHistory(Long id, Long userId, String passwordHash, Timestamp changedAt,
                           Boolean changedByAdmin, String ipAddress, String userAgent) {
        this.id = id;
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.changedAt = changedAt;
        this.changedByAdmin = changedByAdmin;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    /**
     * Verifica si el cambio fue realizado por un administrador
     */
    public boolean wasChangedByAdmin() {
        return Boolean.TRUE.equals(this.changedByAdmin);
    }

    /**
     * Verifica si la contraseña es reciente (menos de 90 días)
     */
    public boolean isRecent() {
        return this.changedAt != null && this.changedAt.toLocalDateTime().isAfter(java.time.LocalDateTime.now().minusDays(90));
    }

    /**
     * Obtiene la antigüedad de la contraseña en días
     */
    public long getAgeInDays() {
        if (this.changedAt == null) {
            return 0;
        }
        return java.time.Duration.between(this.changedAt.toLocalDateTime(), java.time.LocalDateTime.now()).toDays();
    }

    /**
     * Verifica si la contraseña debe ser considerada para prevención de reutilización
     */
    public boolean shouldPreventReuse() {
        return isRecent(); // Solo prevenir reutilización de contraseñas recientes
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Timestamp getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Timestamp changedAt) {
        this.changedAt = changedAt;
    }

    public Boolean getChangedByAdmin() {
        return changedByAdmin;
    }

    public void setChangedByAdmin(Boolean changedByAdmin) {
        this.changedByAdmin = changedByAdmin;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}