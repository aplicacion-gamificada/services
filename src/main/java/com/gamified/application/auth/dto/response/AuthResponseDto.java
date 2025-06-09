package com.gamified.application.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

public class AuthResponseDto {
    /**
     * DTO de respuesta para estado de autenticación
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class AuthStatusResponseDto {

        private Boolean isAuthenticated;
        private Boolean tokenValid;
        private Boolean emailVerified;
        private Boolean accountActive;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastValidation;

        private Long tokenExpiresIn; // Segundos hasta expiración
        private String authStatus; // "valid", "expired", "invalid", "blocked"

        // Info mínima del usuario si está autenticado
        private String userEmail;
        private String roleName;
    }

    /**
     * DTO de respuesta para verificación de email
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class EmailVerificationResponseDto {

        private Boolean success;
        private String message;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime verifiedAt;

        private Boolean accountActivated;
        private String nextStep;
        private String redirectUrl;

        // Información del usuario verificado
        private String userEmail;
        private String userName;
    }

    /**
     * DTO de respuesta para login exitoso
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"accessToken", "refreshToken"})
    public static class LoginResponseDto {

        private String accessToken;
        private String refreshToken;

        @Builder.Default
        private String tokenType = "Bearer";

        private Long expiresIn; // Segundos hasta expiración

        private UserResponseDto.UserInfoDto userInfo;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime loginTime;

        private String sessionId;
        private Boolean rememberMe;

        private SessionResponseDto.SessionInfoResponseDto sessionInfo;
    }

    /**
     * DTO de respuesta para registro de usuario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class RegisterResponseDto {

        private Long userId;
        private String message;
        private Boolean emailVerificationRequired;
        private String verificationEmailSent;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime registeredAt;

        // Información del siguiente paso
        private String nextStep;
        private String nextStepDescription;

        // URL de verificación (opcional para testing)
        private String verificationUrl;
    }

    /**
     * DTO de respuesta para operaciones de contraseña
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class PasswordOperationResponseDto {

        private Boolean success;
        private String message;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime processedAt;

        // Para reset de contraseña
        private String resetToken; // Solo para testing - normalmente no se devuelve
        private Integer tokenExpirationHours;

        // Para cambio de contraseña
        private Boolean requiresReauth;
        private String nextStep;

        // Información adicional
        private String emailSentTo; // Email parcialmente ocultado
    }

    /**
     * Información básica de la institución
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class InstitutionInfoDto {
        private Long id;
        private String name;
        private String city;
        private String state;
        private String country;
        private String logoUrl;
        private String fullAddress;
    }

    /**
     * Información básica del rol
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class RoleInfoDto {
        private Long id;
        private String name;
        private String code;
        private String description;
    }

    /**
     * Preferencias básicas del usuario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserPreferencesDto {
        @Builder.Default
        private String language = "es";

        @Builder.Default
        private String timezone = "America/Lima";

        @Builder.Default
        private String theme = "light";

        @Builder.Default
        private Boolean emailNotifications = true;

        @Builder.Default
        private Boolean browserNotifications = false;
    }
}
