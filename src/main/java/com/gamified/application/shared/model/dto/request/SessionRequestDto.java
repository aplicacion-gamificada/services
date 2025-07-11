package com.gamified.application.shared.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

public class SessionRequestDto {
    /**
     * DTO para solicitudes de refresh token
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"refreshToken"})
    public static class RefreshTokenRequestDto {

        @NotBlank(message = "El refresh token es obligatorio")
        private String refreshToken;

        private String deviceInfo;
        private String userAgent;
        private String ipAddress;
    }

    /**
     * DTO para solicitudes de logout
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class LogoutRequestDto {

        private String refreshToken; // Opcional - para logout de sesión específica

        @Builder.Default
        private Boolean logoutAllDevices = false; // true = cerrar sesión en todos los dispositivos

        private String deviceInfo;
        private String ipAddress;

        public Boolean isLogoutAllDevices() {
            return this.logoutAllDevices;
        }
    }

    /**
     * DTO para renombrar una sesión
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class RenameSessionRequestDto {

        @NotNull(message = "El ID de la sesión es obligatorio")
        private Long sessionId;

        @NotBlank(message = "El nuevo nombre de la sesión es obligatorio")
        private String newName;

        private Long userId; // Se establecerá internamente desde la autenticación
    }
}
