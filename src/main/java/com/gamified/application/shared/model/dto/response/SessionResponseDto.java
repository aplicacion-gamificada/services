package com.gamified.application.shared.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SessionResponseDto {
    /**
     * DTO de respuesta para logout
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class LogoutResponseDto {

        private Boolean success;
        private String message;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime logoutTime;

        private Integer sessionDurationMinutes;
        private Boolean allDevicesLoggedOut;

        // Estadísticas de la sesión
        private SessionStatsDto sessionStats;
    }

    /**
     * DTO de respuesta para refresh token
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString(exclude = {"accessToken"})
    public static class RefreshTokenResponseDto {

        private String accessToken;

        @Builder.Default
        private String tokenType = "Bearer";

        private Long expiresIn; // Segundos hasta expiración

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime issuedAt;
    }

    /**
     * DTO para listado de sesiones
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SessionListResponseDto {

        private List<SessionInfoResponseDto> activeSessions;
        private Integer totalActiveSessions;
        private SessionInfoResponseDto currentSession;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastUpdated;
    }

    /**
     * DTO de respuesta para estadísticas de sesiones del usuario
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SessionStatsResponseDto {

        // Estadísticas del usuario actual
        private UserSessionStatsDto currentUserStats;

        // Estadísticas comparativas (opcional para profesores/admin)
        private ComparativeStatsDto comparativeStats;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime generatedAt;

        private String reportPeriod; // "daily", "weekly", "monthly", "all_time"
    }

    /**
     * Estadísticas básicas de la sesión
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SessionStatsDto {
        private Integer actionsPerformed;
        private Integer minutesActive;
        private LocalDateTime sessionStartTime;
        private LocalDateTime sessionEndTime;
        private Integer pagesVisited;
        private String mostUsedFeature;
    }

    /**
     * Información de sesión de login
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SessionInfoResponseDto {
        private String deviceInfo;
        private String ipAddress;
        private String userAgent;
        private String browser;
        private String operatingSystem;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime sessionStartTime;
    }

    /**
     * Estadísticas de sesiones del usuario individual
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class UserSessionStatsDto {

        // Contadores básicos
        private Integer totalSessions;
        private Integer totalLoginDays;
        private Integer currentStreak; // Días consecutivos
        private Integer longestStreak;

        // Tiempo de uso
        private Integer totalMinutesActive;
        private Double averageSessionMinutes;
        private Integer longestSessionMinutes;
        private Integer shortestSessionMinutes;

        // Patrones de uso
        private String mostActiveTimeOfDay; // "morning", "afternoon", "evening", "night"
        private String mostActiveDayOfWeek; // "monday", "tuesday", etc.
        private List<String> mostUsedFeatures;

        // Fechas importantes
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime firstLoginDate;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginDate;

        // Dispositivos y ubicaciones
        private Map<String, Integer> deviceUsage; // "mobile": 45, "desktop": 55
        private Map<String, Integer> browserUsage; // "chrome": 80, "firefox": 20
        private List<String> loginLocations; // Top 3 ubicaciones por IP

        // Métricas de productividad (específico para e-learning)
        private Integer completedActivities;
        private Integer averageActivitiesPerSession;
        private Double learningVelocity; // actividades por hora
    }

    /**
     * Estadísticas comparativas para contexto
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class ComparativeStatsDto {

        // Comparación con promedios de la clase/institución
        private Double classAverageSessionMinutes;
        private Double institutionAverageSessionMinutes;
        private String performanceRanking; // "above_average", "average", "below_average"

        // Percentiles
        private Integer sessionDurationPercentile; // Dónde está el usuario (0-100)
        private Integer activityCountPercentile;
        private Integer streakPercentile;

        // Trending
        private String trenDirection; // "improving", "stable", "declining"
        private Double improvementRate; // % de mejora comparado con período anterior
    }
}
