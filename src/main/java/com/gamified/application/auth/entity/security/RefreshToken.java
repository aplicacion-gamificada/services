package com.gamified.application.auth.entity.security;

import com.gamified.application.user.model.entity.User;
import lombok.*;
import java.time.LocalDateTime;
import java.sql.Timestamp;

/**
 * POJO para gestionar los refresh tokens de los usuarios
 * Permite invalidar tokens específicos y gestionar múltiples sesiones
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"token"})
@EqualsAndHashCode(of = {"id", "token"})
public class RefreshToken {

    private Long id;
    private String token;
    private Long userId;
    private Timestamp expiresAt;
    private Boolean isRevoked;
    private LocalDateTime revokedAt;
    private String revokedReason;
    private Timestamp createdAt;
    private LocalDateTime lastUsedAt;
    private String ipAddress;
    private String userAgent;
    private String deviceInfo;
    private String sessionName;
    private Boolean isValid;

    // Objeto relacionado (se carga por separado si es necesario)
    private User user;

    /**
     * Constructor para mapeo desde stored procedures (datos básicos)
     */
    public RefreshToken(Long id, String token, Long userId, Timestamp expiresAt,
                        Boolean isRevoked, Timestamp createdAt) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.isRevoked = isRevoked;
        this.createdAt = createdAt;
    }

    /**
     * Constructor completo para mapeo desde stored procedures
     */
    public RefreshToken(Long id, String token, Long userId, Timestamp expiresAt,
                        Boolean isRevoked, LocalDateTime revokedAt, String revokedReason,
                        Timestamp createdAt, LocalDateTime lastUsedAt, String ipAddress,
                        String userAgent, String deviceInfo, String sessionName) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.isRevoked = isRevoked;
        this.revokedAt = revokedAt;
        this.revokedReason = revokedReason;
        this.createdAt = createdAt;
        this.lastUsedAt = lastUsedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceInfo = deviceInfo;
        this.sessionName = sessionName;
    }

    /**
     * Constructor personalizado
     */
    public RefreshToken(String token, Long userId, Timestamp expiresAt,
                        String ipAddress, String userAgent, String deviceInfo) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceInfo = deviceInfo;
        this.isRevoked = false;
    }

    /**
     * Verifica si el token está expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt.toLocalDateTime());
    }

    /**
     * Verifica si el token está revocado
     */
    public boolean isRevoked() {
        return Boolean.TRUE.equals(this.isRevoked);
    }

    /**
     * Verifica si el token es válido (no expirado y no revocado)
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked();
    }

    /**
     * Revoca el token
     */
    public void revoke(String reason) {
        this.isRevoked = true;
        this.revokedAt = LocalDateTime.now();
        this.revokedReason = reason;
    }

    /**
     * Marca el token como usado
     */
    public void markAsUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Verifica si el token ha sido usado recientemente (últimos 5 minutos)
     */
    public boolean isRecentlyUsed() {
        return this.lastUsedAt != null &&
                this.lastUsedAt.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    /**
     * Obtiene información resumida de la sesión
     */
    public String getSessionInfo() {
        if (sessionName != null && !sessionName.trim().isEmpty()) {
            return sessionName;
        }

        StringBuilder info = new StringBuilder();
        if (deviceInfo != null && !deviceInfo.trim().isEmpty()) {
            info.append(deviceInfo);
        } else if (userAgent != null && !userAgent.trim().isEmpty()) {
            // Extraer información básica del user agent
            if (userAgent.contains("Mobile")) {
                info.append("Dispositivo Móvil");
            } else if (userAgent.contains("Chrome")) {
                info.append("Chrome");
            } else if (userAgent.contains("Firefox")) {
                info.append("Firefox");
            } else if (userAgent.contains("Safari")) {
                info.append("Safari");
            } else {
                info.append("Navegador");
            }
        }

        if (ipAddress != null) {
            info.append(" (").append(ipAddress).append(")");
        }

        return info.toString();
    }

    /**
     * Calcula los días restantes antes de la expiración
     */
    public long getDaysUntilExpiration() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), this.expiresAt.toLocalDateTime()).toDays();
    }

    /**
     * Calcula las horas restantes antes de la expiración
     */
    public long getHoursUntilExpiration() {
        if (isExpired()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), this.expiresAt.toLocalDateTime()).toHours();
    }
}