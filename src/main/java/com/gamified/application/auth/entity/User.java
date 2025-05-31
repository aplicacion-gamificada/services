package com.gamified.application.auth.entity;


import com.gamified.application.auth.entity.enums.RoleType;
import lombok.*;
import java.time.LocalDateTime;

/**
 * POJO principal que representa a todos los usuarios del sistema
 * Incluye estudiantes, profesores y tutores
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"password"})
@EqualsAndHashCode(of = {"id", "email"})
public class User {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String profilePictureUrl;
    private Boolean status;
    private Boolean emailVerified;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationExpiresAt;
    private String passwordResetToken;
    private LocalDateTime passwordResetExpiresAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Integer failedLoginAttempts;
    private LocalDateTime accountLockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // IDs de relaciones (para stored procedures)
    private Long roleId;
    private Long institutionId;

    // Objetos relacionados (se cargan por separado si es necesario)
    private Role role;
    private Institution institution;

    /**
     * Constructor para mapeo desde stored procedures (datos básicos)
     */
    public User(Long id, String firstName, String lastName, String email,
                Long roleId, Long institutionId, Boolean status, Boolean emailVerified) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.roleId = roleId;
        this.institutionId = institutionId;
        this.status = status;
        this.emailVerified = emailVerified;
        this.failedLoginAttempts = 0;
    }

    /**
     * Constructor para autenticación (datos necesarios para login)
     */
    public User(Long id, String firstName, String lastName, String email, String password,
                Long roleId, Long institutionId, Boolean status, Boolean emailVerified,
                Integer failedLoginAttempts, LocalDateTime accountLockedUntil,
                LocalDateTime lastLoginAt, String lastLoginIp) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
        this.institutionId = institutionId;
        this.status = status;
        this.emailVerified = emailVerified;
        this.failedLoginAttempts = failedLoginAttempts != null ? failedLoginAttempts : 0;
        this.accountLockedUntil = accountLockedUntil;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
    }

    /**
     * Constructor completo para mapeo desde stored procedures
     */
    public User(Long id, String firstName, String lastName, String email, String password,
                String profilePictureUrl, Boolean status, Boolean emailVerified,
                String emailVerificationToken, LocalDateTime emailVerificationExpiresAt,
                String passwordResetToken, LocalDateTime passwordResetExpiresAt,
                LocalDateTime lastLoginAt, String lastLoginIp, Integer failedLoginAttempts,
                LocalDateTime accountLockedUntil, LocalDateTime createdAt, LocalDateTime updatedAt,
                Long roleId, Long institutionId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.profilePictureUrl = profilePictureUrl;
        this.status = status;
        this.emailVerified = emailVerified;
        this.emailVerificationToken = emailVerificationToken;
        this.emailVerificationExpiresAt = emailVerificationExpiresAt;
        this.passwordResetToken = passwordResetToken;
        this.passwordResetExpiresAt = passwordResetExpiresAt;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
        this.failedLoginAttempts = failedLoginAttempts != null ? failedLoginAttempts : 0;
        this.accountLockedUntil = accountLockedUntil;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.roleId = roleId;
        this.institutionId = institutionId;
    }

    // ===================================================================
    // MÉTODOS DE UTILIDAD PARA ROLES
    // ===================================================================

    /**
     * Verifica si el usuario es estudiante
     */
    public boolean isStudent() {
        if (this.role != null) {
            return this.role.isStudent();
        }
        return this.roleId != null && this.roleId.equals(RoleType.STUDENT.getId());
    }

    /**
     * Verifica si el usuario es profesor
     */
    public boolean isTeacher() {
        if (this.role != null) {
            return this.role.isTeacher();
        }
        return this.roleId != null && this.roleId.equals(RoleType.TEACHER.getId());
    }

    /**
     * Verifica si el usuario es tutor/apoderado
     */
    public boolean isGuardian() {
        if (this.role != null) {
            return this.role.isGuardian();
        }
        return this.roleId != null && this.roleId.equals(RoleType.GUARDIAN.getId());
    }

    /**
     * Obtiene el nombre del rol
     */
    public String getRoleName() {
        if (this.role != null) {
            return this.role.getRoleCode();
        }
        if (this.roleId != null) {
            try {
                return RoleType.fromId(this.roleId).getCode();
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Obtiene el tipo de rol como enum
     */
    public RoleType getRoleType() {
        if (this.role != null) {
            return this.role.getName();
        }
        if (this.roleId != null) {
            try {
                return RoleType.fromId(this.roleId);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    // ===================================================================
    // MÉTODOS DE UTILIDAD PARA ESTADO DE CUENTA
    // ===================================================================

    /**
     * Verifica si la cuenta está activa
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.status);
    }

    /**
     * Verifica si el email está verificado
     */
    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(this.emailVerified);
    }

    /**
     * Verifica si la cuenta está bloqueada
     */
    public boolean isAccountLocked() {
        return this.accountLockedUntil != null && this.accountLockedUntil.isAfter(LocalDateTime.now());
    }

    /**
     * Activa la cuenta del usuario
     */
    public void activateAccount() {
        this.status = true;
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    /**
     * Desactiva la cuenta del usuario
     */
    public void deactivateAccount() {
        this.status = false;
    }

    /**
     * Verifica el email del usuario
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiresAt = null;
    }

    // ===================================================================
    // MÉTODOS PARA GESTIÓN DE INTENTOS DE LOGIN
    // ===================================================================

    /**
     * Registra un intento de login exitoso
     */
    public void recordSuccessfulLogin(String ipAddress) {
        this.lastLoginAt = LocalDateTime.now();
        this.lastLoginIp = ipAddress;
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    /**
     * Registra un intento de login fallido
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts = (this.failedLoginAttempts != null ? this.failedLoginAttempts : 0) + 1;

        // Bloquear cuenta después de 5 intentos fallidos
        if (this.failedLoginAttempts >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30); // Bloquear por 30 minutos
        }
    }

    /**
     * Resetea los intentos de login fallidos
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
    }

    // ===================================================================
    // MÉTODOS PARA TOKENS
    // ===================================================================

    /**
     * Establece token de verificación de email
     */
    public void setEmailVerificationToken(String token, int expirationHours) {
        this.emailVerificationToken = token;
        this.emailVerificationExpiresAt = LocalDateTime.now().plusHours(expirationHours);
    }

    /**
     * Establece token de reseteo de contraseña
     */
    public void setPasswordResetToken(String token, int expirationHours) {
        this.passwordResetToken = token;
        this.passwordResetExpiresAt = LocalDateTime.now().plusHours(expirationHours);
    }

    /**
     * Verifica si el token de verificación de email es válido
     */
    public boolean isEmailVerificationTokenValid(String token) {
        return this.emailVerificationToken != null &&
                this.emailVerificationToken.equals(token) &&
                this.emailVerificationExpiresAt != null &&
                this.emailVerificationExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Verifica si el token de reseteo de contraseña es válido
     */
    public boolean isPasswordResetTokenValid(String token) {
        return this.passwordResetToken != null &&
                this.passwordResetToken.equals(token) &&
                this.passwordResetExpiresAt != null &&
                this.passwordResetExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Limpia el token de verificación de email
     */
    public void clearEmailVerificationToken() {
        this.emailVerificationToken = null;
        this.emailVerificationExpiresAt = null;
    }

    /**
     * Limpia el token de reseteo de contraseña
     */
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetExpiresAt = null;
    }

    // ===================================================================
    // MÉTODOS DE UTILIDAD GENERAL
    // ===================================================================

    /**
     * Obtiene el nombre completo del usuario
     */
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (this.firstName != null && !this.firstName.trim().isEmpty()) {
            fullName.append(this.firstName.trim());
        }
        if (this.lastName != null && !this.lastName.trim().isEmpty()) {
            if (!fullName.isEmpty()) fullName.append(" ");
            fullName.append(this.lastName.trim());
        }
        return fullName.toString();
    }

    /**
     * Obtiene las iniciales del usuario
     */
    public String getInitials() {
        String firstInitial = this.firstName != null && !this.firstName.isEmpty()
                ? String.valueOf(this.firstName.charAt(0)).toUpperCase()
                : "";
        String lastInitial = this.lastName != null && !this.lastName.isEmpty()
                ? String.valueOf(this.lastName.charAt(0)).toUpperCase()
                : "";
        return firstInitial + lastInitial;
    }
    /**
     * Obtiene información básica del usuario para logs
     */
    public String getBasicInfo() {
        return String.format("%s (%s) - %s",
                getFullName(),
                email != null ? email : "sin email",
                getRoleName() != null ? getRoleName() : "sin rol");
    }


    /**
     * Verifica si el usuario puede realizar login
     */
    public boolean canLogin() {
        return isActive() && isEmailVerified() && !isAccountLocked();
    }

    /**
     * Obtiene el nombre de la institución
     */
    public String getInstitutionName() {
        return this.institution != null ? this.institution.getName() : null;
    }

    /**
     * Valida que el usuario tenga los datos mínimos requeridos
     */
    public boolean isValid() {
        return firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                password != null && !password.trim().isEmpty() &&
                roleId != null &&
                institutionId != null;
    }

    /**
     * Obtiene el tiempo restante de bloqueo en minutos
     */
    public long getMinutesUntilUnlock() {
        if (!isAccountLocked()) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), this.accountLockedUntil).toMinutes();
    }

    /**
     * Verifica si necesita verificar email
     */
    public boolean needsEmailVerification() {
        return !isEmailVerified() &&
                (emailVerificationToken == null ||
                        emailVerificationExpiresAt == null ||
                        emailVerificationExpiresAt.isBefore(LocalDateTime.now()));
    }

    /**
     * Verifica si puede resetear contraseña
     */
    public boolean canResetPassword() {
        return isActive() && isEmailVerified();
    }
}
