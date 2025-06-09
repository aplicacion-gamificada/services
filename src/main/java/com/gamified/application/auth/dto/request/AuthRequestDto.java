package com.gamified.application.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

public class AuthRequestDto {
    /**
     * DTO para solicitudes de login
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"password"})
    public static class LoginRequestDto {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
        private String password;

        @Builder.Default
        private Boolean rememberMe = false;

        private String deviceInfo;
        private String userAgent;
    }

    /**
     * DTO para solicitudes de registro de usuario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"password", "confirmPassword"})
    public static class RegisterRequestDto {

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El nombre solo puede contener letras y espacios")
        private String firstName;

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
        @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$", message = "El apellido solo puede contener letras y espacios")
        private String lastName;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        private String email;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
                message = "La contraseña debe contener al menos: 1 minúscula, 1 mayúscula, 1 número y 1 carácter especial")
        private String password;

        @NotBlank(message = "La confirmación de contraseña es obligatoria")
        private String confirmPassword;

        @NotNull(message = "El rol es obligatorio")
        @Min(value = 1, message = "Debe seleccionar un rol válido")
        @Max(value = 3, message = "Debe seleccionar un rol válido")
        private Long roleId;

        @NotNull(message = "La institución es obligatoria")
        @Positive(message = "Debe seleccionar una institución válida")
        private Long institutionId;

        private String deviceInfo;

        /**
         * Valida que las contraseñas coincidan
         */
        public boolean isPasswordConfirmed() {
            return password != null && password.equals(confirmPassword);
        }
    }

    /**
     * DTO para solicitudes de nueva contraseña con token
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"newPassword", "confirmNewPassword"})
    public static class PasswordResetConfirmRequestDto {

        @NotBlank(message = "El token de reseteo es obligatorio")
        private String resetToken;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        private String email;

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
                message = "La contraseña debe contener al menos: 1 minúscula, 1 mayúscula, 1 número y 1 carácter especial")
        private String newPassword;

        @NotBlank(message = "La confirmación de la nueva contraseña es obligatoria")
        private String confirmNewPassword;

        /**
         * Valida que las nuevas contraseñas coincidan
         */
        public boolean isNewPasswordConfirmed() {
            return newPassword != null && newPassword.equals(confirmNewPassword);
        }
    }

    /**
     * DTO para solicitudes de reseteo de contraseña
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class PasswordResetRequestDto {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        private String email;

        private String deviceInfo;
        private String userAgent;
    }

    /**
     * DTO para solicitudes de cambio de contraseña (usuario autenticado)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"currentPassword", "newPassword", "confirmNewPassword"})
    public static class PasswordChangeRequestDto {

        @NotBlank(message = "La contraseña actual es obligatoria")
        private String currentPassword;

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
                message = "La contraseña debe contener al menos: 1 minúscula, 1 mayúscula, 1 número y 1 carácter especial")
        private String newPassword;

        @NotBlank(message = "La confirmación de la nueva contraseña es obligatoria")
        private String confirmNewPassword;

        /**
         * Válida que las nuevas contraseñas coincidan
         */
        public boolean isNewPasswordConfirmed() {
            return newPassword != null && newPassword.equals(confirmNewPassword);
        }

        /**
         * Válida que la nueva contraseña sea diferente de la actual
         */
        public boolean isPasswordChanged() {
            return currentPassword != null && newPassword != null && !currentPassword.equals(newPassword);
        }
    }

    /**
     * DTO para solicitudes de verificación de email
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class EmailVerificationRequestDto {

        @NotBlank(message = "El token de verificación es obligatorio")
        private String verificationToken;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        private String email;
    }

    /**
     * DTO para reenvío de verificación de email
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class ResendVerificationRequestDto {

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        @Size(max = 100, message = "El email no puede exceder 100 caracteres")
        private String email;

        private String deviceInfo;
        private String userAgent;
    }

    // Add a PasswordResetExecuteRequestDto
    /**
     * DTO para solicitudes de reseteo de contraseña con token
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"resetToken", "newPassword", "confirmNewPassword"})
    public static class PasswordResetExecuteRequestDto {

        @NotBlank(message = "El token de reseteo es obligatorio")
        private String resetToken;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "Formato de email inválido")
        private String email;

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 128, message = "La contraseña debe tener entre 8 y 128 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).*$",
                message = "La contraseña debe contener al menos: 1 minúscula, 1 mayúscula, 1 número y 1 carácter especial")
        private String newPassword;

        @NotBlank(message = "La confirmación de la nueva contraseña es obligatoria")
        private String confirmNewPassword;

        /**
         * Valida que las nuevas contraseñas coincidan
         */
        public boolean isNewPasswordConfirmed() {
            return newPassword != null && newPassword.equals(confirmNewPassword);
        }
    }

    /**
     * DTO para solicitudes de login de estudiantes con username
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"password"})
    public static class StudentLoginRequestDto {

        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(max = 50, message = "El nombre de usuario no puede exceder 50 caracteres")
        private String username;

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
        private String password;

        @Builder.Default
        private Boolean rememberMe = false;

        private String deviceInfo;
        private String userAgent;
    }
}
