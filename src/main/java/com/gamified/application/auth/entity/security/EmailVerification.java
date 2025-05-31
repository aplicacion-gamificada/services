package com.gamified.application.auth.entity.security;

import com.gamified.application.auth.entity.User;
import lombok.*;
import java.time.LocalDateTime;

/**
 * POJO para gestionar las verificaciones de email
 * Permite múltiples intentos y seguimiento detallado
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = {"id", "verificationToken"})
public class EmailVerification {

    private Long id;
    private Long userId;
    private String email;
    private String verificationToken;
    private LocalDateTime expiresAt;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
    private Integer attemptCount;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime createdAt;
    private String ipAddress;
    private String userAgent;

    // Objeto relacionado (se carga por separado si es necesario)
    private User user;

    /**
     * Constructor para mapeo desde stored procedures (datos básicos)
     */
    public EmailVerification(Long id, Long userId, String email, String verificationToken,
                             LocalDateTime expiresAt, Boolean isVerified, Integer attemptCount) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.verificationToken = verificationToken;
        this.expiresAt = expiresAt;
        this.isVerified = isVerified;
        this.attemptCount = attemptCount != null ? attemptCount : 0;
    }

    /**
     * Constructor completo para mapeo desde stored procedures
     */
    public EmailVerification(Long id, Long userId, String email, String verificationToken,
                             LocalDateTime expiresAt, Boolean isVerified, LocalDateTime verifiedAt,
                             Integer attemptCount, LocalDateTime lastAttemptAt, LocalDateTime createdAt,
                             String ipAddress, String userAgent) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.verificationToken = verificationToken;
        this.expiresAt = expiresAt;
        this.isVerified = isVerified;
        this.verifiedAt = verifiedAt;
        this.attemptCount = attemptCount != null ? attemptCount : 0;
        this.lastAttemptAt = lastAttemptAt;
        this.createdAt = createdAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    /**
     * Constructor personalizado
     */
    public EmailVerification(Long userId, String email, String verificationToken,
                             LocalDateTime expiresAt, String ipAddress, String userAgent) {
        this.userId = userId;
        this.email = email;
        this.verificationToken = verificationToken;
        this.expiresAt = expiresAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isVerified = false;
        this.attemptCount = 0;
    }

    /**
     * Verifica si el token está expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
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
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * Registra un intento de verificación
     */
    public void recordAttempt() {
        this.attemptCount = (this.attemptCount != null ? this.attemptCount : 0) + 1;
        this.lastAttemptAt = LocalDateTime.now();
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
        return java.time.Duration.between(LocalDateTime.now(), this.expiresAt).toHours();
    }

    /**
     * Verifica si el email coincide con el usuario actual
     */
    public boolean emailMatchesUser() {
        return this.user != null &&
                this.email != null &&
                this.email.equalsIgnoreCase(this.user.getEmail());
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
}