package com.gamified.application.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
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
    }
}
