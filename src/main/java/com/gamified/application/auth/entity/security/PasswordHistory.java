package com.gamified.application.auth.entity.security;

import com.gamified.application.auth.entity.User;
import lombok.*;
import java.time.LocalDateTime;

/**
 * POJO para mantener un historial de contraseñas
 * Previene que los usuarios reutilicen contraseñas recientes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"passwordHash"})
@EqualsAndHashCode(of = {"id"})
public class PasswordHistory {

    private Long id;
    private Long userId;
    private String passwordHash;
    private LocalDateTime createdAt;
    private Boolean changedByAdmin;
    private String ipAddress;
    private String userAgent;

    // Objeto relacionado (se carga por separado si es necesario)
    private User user;

    /**
     * Constructor para mapeo desde stored procedures (datos básicos)
     */
    public PasswordHistory(Long id, Long userId, String passwordHash, LocalDateTime createdAt, Boolean changedByAdmin) {
        this.id = id;
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.changedByAdmin = changedByAdmin;
    }

    /**
     * Constructor completo para mapeo desde stored procedures
     */
    public PasswordHistory(Long id, Long userId, String passwordHash, LocalDateTime createdAt,
                           Boolean changedByAdmin, String ipAddress, String userAgent) {
        this.id = id;
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
        this.changedByAdmin = changedByAdmin;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    /**
     * Constructor personalizado
     */
    public PasswordHistory(Long userId, String passwordHash, String ipAddress, String userAgent) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.changedByAdmin = false;
    }

    /**
     * Constructor para cambio realizado por administrador
     */
    public PasswordHistory(Long userId, String passwordHash, String ipAddress, String userAgent, boolean changedByAdmin) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.changedByAdmin = changedByAdmin;
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
        return this.createdAt != null && this.createdAt.isAfter(LocalDateTime.now().minusDays(90));
    }

    /**
     * Obtiene la antigüedad de la contraseña en días
     */
    public long getAgeInDays() {
        if (this.createdAt == null) {
            return 0;
        }
        return java.time.Duration.between(this.createdAt, LocalDateTime.now()).toDays();
    }

    /**
     * Verifica si la contraseña debe ser considerada para prevención de reutilización
     */
    public boolean shouldPreventReuse() {
        return isRecent(); // Solo prevenir reutilización de contraseñas recientes
    }
}