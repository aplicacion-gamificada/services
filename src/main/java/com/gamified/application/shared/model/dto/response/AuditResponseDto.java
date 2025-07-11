package com.gamified.application.shared.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para respuestas relacionadas con auditoría
 */
public class AuditResponseDto {

    /**
     * DTO para respuestas de historial de login
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginHistoryResponseDto {
        private Long id;
        private Long userId;
        private LocalDateTime timestamp;
        private boolean success;
        private String ipAddress;
        private String userAgent;
        private String failureReason;
        private String location;
    }

    /**
     * DTO para respuestas de actividad de usuario
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityResponseDto {
        private Long id;
        private Long userId;
        private LocalDateTime timestamp;
        private Byte actionId;
        private String actionName;
        private String entityType;
        private Long entityId;
        private String actionDetails;
        private String ipAddress;
        private boolean isSensitive;
    }

    /**
     * DTO para resultados de búsqueda de logs de auditoría
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLogSearchResponseDto {
        private List<UserActivityResponseDto> auditLogs;
        private int totalResults;
        private int limit;
        private int offset;
    }

    /**
     * DTO para resumen de actividad sospechosa
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SuspiciousActivitySummaryDto {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private int totalSuspiciousLogins;
        private int totalFailedLogins;
        private int totalSensitiveActions;
        private List<UserSuspiciousActivityDto> suspiciousUsers;
        private Map<String, Integer> suspiciousIpAddresses;
    }

    /**
     * DTO para actividad sospechosa por usuario
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSuspiciousActivityDto {
        private Long userId;
        private String username;
        private int failedLoginAttempts;
        private int suspiciousLogins;
        private int sensitiveActions;
        private List<String> uniqueIpAddresses;
    }
} 