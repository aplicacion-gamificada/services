package com.gamified.application.shared.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gamified.application.user.model.dto.response.UserResponseDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SystemResponseDto {
    /**
     * DTO de respuesta para información de roles disponibles
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class RoleListResponseDto {

        private List<RoleDto> roles;
        private Integer totalRoles;
    }

    /**
     * DTO de respuesta para información de instituciones disponibles
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class InstitutionListResponseDto {

        private List<InstitutionDto> institutions;
        private UserResponseDto.PaginationDto pagination;
        private Integer totalActiveInstitutions;
    }

    /**
     * DTO de respuesta para verificación de salud del sistema
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class HealthCheckResponseDto {

        // Estado general
        private String overallStatus; // "UP", "DOWN", "DEGRADED", "MAINTENANCE"
        private String statusMessage;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime checkTimestamp;

        // Componentes del sistema
        private Map<String, ComponentHealthDto> components;

        // Métricas del sistema
        private SystemHealthMetricsDto metrics;

        // Información adicional
        private String systemVersion;
        private String environment;
        private String uptime;
        private List<String> activeProfiles;
    }

    /**
     * DTO de respuesta para configuración completa del sistema
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SystemConfigResponseDto {

        // Configuraciones principales
        private ApplicationSettingsDto applicationSettings;
        private SecuritySettingsDto securitySettings;
        private LearningSettingsDto learningSettings;
        private NotificationSettingsDto notificationSettings;

        // Configuraciones de integración
        private IntegrationSettingsDto integrationSettings;

        // Feature flags
        private FeatureFlagsDto featureFlags;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastUpdated;

        private String configVersion;
        private String environment; // "development", "staging", "production"
    }

    /**
     * Configuraciones generales de la aplicación
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class ApplicationSettingsDto {

        // Información básica
        private String applicationName;
        private String applicationVersion;
        private String institutionName;
        private String supportEmail;
        private String supportPhone;

        // Configuraciones de sesión
        private Integer sessionTimeoutMinutes;
        private Integer maxConcurrentSessions;
        private Boolean rememberMeEnabled;
        private Integer rememberMeDurationDays;

        // Configuraciones de contenido
        private String defaultLanguage;
        private String defaultTimezone;
        private List<String> supportedLanguages;
        private List<String> supportedTimezones;

        // Configuraciones de archivos
        private Long maxFileUploadSizeMB;
        private List<String> allowedFileTypes;
        private String contentDeliveryUrl;
    }

    /**
     * Estado de salud de componentes individuales
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class ComponentHealthDto {

        private String name;
        private String status; // "UP", "DOWN", "UNKNOWN", "MAINTENANCE"
        private String description;
        private Long responseTimeMs;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastChecked;

        private String errorMessage;
        private Integer errorCount;

        // Detalles específicos del componente
        private Map<String, Object> details;

        // Información de conectividad
        private String endpoint;
        private String version;
        private Boolean isExternal; // true si es servicio externo
    }

    /**
     * DTO de institución para listados
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class InstitutionDto {
        private Long id;
        private String name;
        private String address;
        private String city;
        private String state;
        private String country;
        private String phone;
        private String email;
        private String website;
        private String logoUrl;
        private Boolean active;
        private Integer userCount; // Cantidad de usuarios en esta institución

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        // Información adicional
        private String fullAddress;
        private String basicInfo;
    }

    /**
     * Configuraciones de integraciones externas
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class IntegrationSettingsDto {

        // Azure Services
        private Boolean azureAiEnabled;
        private String azureAiEndpoint;
        private Boolean azureBlobStorageEnabled;

        // Analytics
        private Boolean analyticsEnabled;
        private String analyticsProvider;
        private Boolean trackUserBehavior;

        // APIs externas
        private Boolean weatherApiEnabled;
        private Boolean translationApiEnabled;
        private Boolean contentModerationEnabled;
    }

    /**
     * Configuraciones específicas del sistema de aprendizaje
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class LearningSettingsDto {

        // Gamificación
        private Boolean gamificationEnabled;
        private Integer pointsPerCorrectAnswer;
        private Integer pointsPerCompletedLesson;
        private Integer experiencePerLearningPoint;

        // IA Adaptativa
        private Boolean aiRecommendationsEnabled;
        private Integer minAttemptsForIntervention;
        private Double strugglingThresholdPercentage;
        private Boolean personalizedContentEnabled;

        // Progreso y evaluación
        private Double masteryThresholdPercentage;
        private Integer maxAttemptsPerExercise;
        private Boolean showHintsEnabled;
        private Boolean showExplanationsEnabled;

        // Configuraciones de tiempo
        private Integer suggestedStudyTimeMinutes;
        private Integer maxSessionTimeMinutes;
        private Integer breakReminderMinutes;
    }

    /**
     * Configuraciones de notificaciones
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class NotificationSettingsDto {

        // Canales habilitados
        private Boolean emailNotificationsEnabled;
        private Boolean inAppNotificationsEnabled;
        private Boolean browserNotificationsEnabled;

        // Tipos de notificaciones
        private Boolean welcomeEmailEnabled;
        private Boolean progressReportsEnabled;
        private Boolean achievementNotificationsEnabled;
        private Boolean reminderNotificationsEnabled;
        private Boolean securityAlertsEnabled;

        // Configuraciones de envío
        private String fromEmailAddress;
        private String fromDisplayName;
        private String emailTemplate;
        private Integer emailRetryAttempts;
    }

    /**
     * DTO de rol para listados
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class RoleDto {
        private Long id;
        private String name;
        private String code;
        private String description;
        private Integer userCount; // Cantidad de usuarios con este rol
        private Boolean isActive;
    }

    /**
     * Configuraciones de seguridad
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SecuritySettingsDto {

        // Autenticación
        private Boolean emailVerificationRequired;
        private Boolean strongPasswordPolicy;
        private Integer passwordMinLength;
        private Integer passwordExpirationDays;

        // Control de acceso
        private Integer maxLoginAttempts;
        private Integer lockoutDurationMinutes;
        private Boolean ipWhitelistEnabled;
        private List<String> allowedIpRanges;

        // Tokens
        private Integer accessTokenExpirationMinutes;
        private Integer refreshTokenExpirationDays;
        private Boolean tokenRotationEnabled;

        // Sesiones
        private Boolean sessionTrackingEnabled;
        private Boolean logSecurityEvents;
        private Boolean enforceHttps;
    }

    /**
     * Métricas detalladas de salud del sistema
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class SystemHealthMetricsDto {

        // Métricas de rendimiento
        private Double cpuUsagePercent;
        private Double memoryUsagePercent;
        private Double diskUsagePercent;
        private Double networkLatencyMs;

        // Métricas de base de datos
        private Integer dbActiveConnections;
        private Integer dbMaxConnections;
        private Double dbAvgQueryTimeMs;
        private Integer dbSlowQueries;

        // Métricas de aplicación
        private Integer activeUserSessions;
        private Integer totalRequestsLastHour;
        private Integer errorCountLastHour;
        private Double avgResponseTimeMs;

        // Métricas de cache
        private Double cacheHitRatio;
        private Long cacheSize;
        private Integer cacheEvictions;

        // Métricas de seguridad
        private Integer failedLoginsLastHour;
        private Integer blockedIpsCount;
        private Integer securityWarningsLastHour;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime metricsGeneratedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class FeatureFlagsDto {

        // Características de autenticación
        private Boolean socialLoginEnabled;
        private Boolean rememberMeEnabled;
        private Boolean passwordResetEnabled;
        private Boolean emailVerificationEnabled;

        // Características del e-learning
        private Boolean gamificationEnabled;
        private Boolean aiRecommendationsEnabled;
        private Boolean adaptiveLearningEnabled;
        private Boolean chatbotEnabled;

        // Características administrativas
        private Boolean userRegistrationEnabled;
        private Boolean maintenanceModeEnabled;
        private Boolean debugModeEnabled;
        private Boolean analyticsEnabled;
    }
}
