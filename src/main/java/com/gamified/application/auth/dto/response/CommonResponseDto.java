package com.gamified.application.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class CommonResponseDto {
    /**
     * DTO para respuestas de error estandarizadas
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class ErrorResponseDto {

        private String error;
        private String message;
        private String details;
        private Integer status;
        private String path;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime timestamp;

        private List<FieldErrorDto> fieldErrors;
        private String errorCode; // Código interno para manejo de errores
        private String requestId; // Para tracking de errores
        private String userFriendlyMessage; // Mensaje más amigable para el usuario
    }

    /**
     * DTO para errores de campo específicos
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class FieldErrorDto {
        private String field;
        private String message;
        private Object rejectedValue;
        private String errorCode;
    }

    /**
     * DTO para respuestas de éxito simples
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SuccessResponseDto {

        @Builder.Default
        private Boolean success = true;

        private String message;
        private Object data;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Builder.Default
        private LocalDateTime timestamp = LocalDateTime.now();

        private String operation; // Tipo de operación realizada
        private String userMessage; // Mensaje específico para mostrar al usuario

        /**
         * Constructor para respuestas simples
         */
        public SuccessResponseDto(String message) {
            this.success = true;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        /**
         * Constructor para respuestas con datos
         */
        public SuccessResponseDto(String message, Object data) {
            this.success = true;
            this.message = message;
            this.data = data;
            this.timestamp = LocalDateTime.now();
        }

        /**
         * DTO para información de sesión activa
         */
        /*
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @ToString
        public static class SessionInfoResponseDto {

            private String sessionId;
            private Long userId;
            private String userEmail;
            private String userName;
            private String roleName;

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime loginTime;

            @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime lastActivity;

            private String ipAddress;
            private String deviceInfo;
            private String userAgent;

            private Long tokenExpiresIn; // Segundos hasta expiración
            private Boolean rememberMe;

            // Estadísticas de la sesión
            private Integer actionsPerformed;
            private Integer pagesVisited;
            private Integer minutesActive;

            // Estado de la sesión
            private Boolean isActive;
            private String sessionStatus; // "active", "expired", "terminated"
        }
        */
    }
}
