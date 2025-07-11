package com.gamified.application.auth.entity.security;

import java.sql.Timestamp;

/**
 * POJO para gestionar las verificaciones de email
 * Permite múltiples intentos y seguimiento detallado
 */
public class EmailVerification {

    private Long id;
    private Long userId;
    private String email;
    private String verificationToken;
    private Timestamp expiresAt;
    private Boolean isVerified;
    private Timestamp verifiedAt;
    private Integer attemptCount;
    private Timestamp lastAttemptAt;
    private Timestamp createdAt;
    private String ipAddress;
    private String userAgent;

    public EmailVerification(Long id, Long userId, String email, String verificationToken,
                             Timestamp expiresAt, Boolean isVerified, Timestamp verifiedAt,
                             Integer attemptCount, Timestamp lastAttemptAt, Timestamp createdAt,
                             String ipAddress, String userAgent) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.verificationToken = verificationToken;
        this.expiresAt = expiresAt;
        this.isVerified = isVerified;
        this.verifiedAt = verifiedAt;
        this.attemptCount = attemptCount;
        this.lastAttemptAt = lastAttemptAt;
        this.createdAt = createdAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    /**
     * Verifica si el token está expirado
     */
    public boolean isExpired() {
        return this.expiresAt != null && this.expiresAt.before(Timestamp.valueOf(java.time.LocalDateTime.now()));
    }

    /**
     * Verifica si ya está verificado
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(this.isVerified);
    }

    /**
     * Verifica si el token es válido
     */
    public boolean isValid() {
        return !isExpired() && !isVerified();
    }

    /**
     * Marca como verificado
     */
    public void markAsVerified() {
        this.isVerified = true;
        this.verifiedAt = Timestamp.valueOf(java.time.LocalDateTime.now());
    }

    /**
     * Registra un intento de verificación
     */
    public void recordAttempt() {
        this.attemptCount = (this.attemptCount != null ? this.attemptCount : 0) + 1;
        this.lastAttemptAt = Timestamp.valueOf(java.time.LocalDateTime.now());
    }

    /**
     * Verifica si se han excedido los intentos máximos
     */
    public boolean hasExceededMaxAttempts() {
        return (this.attemptCount != null ? this.attemptCount : 0) >= 5; // Máximo 5 intentos
    }

    /**
     * Calcula las horas restantes para expiración
     */
    public long getHoursUntilExpiration() {
        if (isExpired()) {
            return 0;
        }
        java.time.Duration duration = java.time.Duration.between(java.time.LocalDateTime.now(), this.expiresAt.toLocalDateTime());
        return duration.toHours();
    }

    /**
     * Obtiene el estado de la verificación como texto
     */
    public String getVerificationStatus() {
        if (isVerified()) {
            return "Verificado";
        } else if (isExpired()) {
            return "Expirado";
        } else if (hasExceededMaxAttempts()) {
            return "Bloqueado por múltiples intentos";
        } else {
            return "Pendiente";
        }
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Timestamp getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Timestamp verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Timestamp getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(Timestamp lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
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