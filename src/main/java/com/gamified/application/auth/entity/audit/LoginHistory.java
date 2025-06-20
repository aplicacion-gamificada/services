package com.gamified.application.auth.entity.audit;


import com.gamified.application.user.model.entity.User;
import lombok.*;
import java.time.LocalDateTime;

/**
 * POJO que registra el historial de logins de los usuarios
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = {"id"})
public class LoginHistory {

    private Long id;
    private Long userId;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private Boolean success;
    private String failureReason;
    private String browser;
    private String operatingSystem;
    private String device;
    private Integer sessionDurationMinutes;
    private LocalDateTime logoutTime;

    // Objeto relacionado (se carga por separado si es necesario)
    private User user;

    /**
     * Constructor para mapeo desde stored procedures (datos básicos)
     */
    public LoginHistory(Long id, Long userId, LocalDateTime loginTime, String ipAddress,
                        String userAgent, Boolean success, String failureReason) {
        this.id = id;
        this.userId = userId;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.success = success;
        this.failureReason = failureReason;
    }

    /**
     * Constructor completo para mapeo desde stored procedures
     */
    public LoginHistory(Long id, Long userId, LocalDateTime loginTime, String ipAddress,
                        String userAgent, Boolean success, String failureReason, String browser,
                        String operatingSystem, String device, Integer sessionDurationMinutes,
                        LocalDateTime logoutTime) {
        this.id = id;
        this.userId = userId;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.success = success;
        this.failureReason = failureReason;
        this.browser = browser;
        this.operatingSystem = operatingSystem;
        this.device = device;
        this.sessionDurationMinutes = sessionDurationMinutes;
        this.logoutTime = logoutTime;
    }

    /**
     * Constructor para login exitoso
     */
    public LoginHistory(Long userId, String ipAddress, String userAgent, String browser,
                        String operatingSystem, String device) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.browser = browser;
        this.operatingSystem = operatingSystem;
        this.device = device;
        this.success = true;
        this.loginTime = LocalDateTime.now();
    }

    /**
     * Constructor para login fallido
     */
    public LoginHistory(Long userId, String ipAddress, String userAgent, String failureReason) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.failureReason = failureReason;
        this.success = false;
        this.loginTime = LocalDateTime.now();
    }

    /**
     * Verifica si el login fue exitoso
     */
    public boolean isSuccessful() {
        return Boolean.TRUE.equals(this.success);
    }

    /**
     * Registra el logout y calcula duración de sesión
     */
    public void recordLogout() {
        this.logoutTime = LocalDateTime.now();
        if (this.loginTime != null) {
            long minutes = java.time.Duration.between(this.loginTime, this.logoutTime).toMinutes();
            this.sessionDurationMinutes = (int) minutes;
        }
    }

    /**
     * Verifica si la sesión sigue activa
     */
    public boolean isSessionActive() {
        return this.logoutTime == null && Boolean.TRUE.equals(this.success);
    }

    /**
     * Obtiene información resumida del login
     */
    public String getLoginSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Login ").append(Boolean.TRUE.equals(success) ? "exitoso" : "fallido");
        summary.append(" desde ").append(ipAddress != null ? ipAddress : "IP desconocida");
        if (browser != null) {
            summary.append(" usando ").append(browser);
        }
        if (operatingSystem != null) {
            summary.append(" en ").append(operatingSystem);
        }
        return summary.toString();
    }

    /**
     * Obtiene la duración de la sesión como texto legible
     */
    public String getSessionDurationText() {
        if (sessionDurationMinutes == null || sessionDurationMinutes <= 0) {
            return "N/A";
        }

        if (sessionDurationMinutes < 60) {
            return sessionDurationMinutes + " minutos";
        } else {
            int hours = sessionDurationMinutes / 60;
            int minutes = sessionDurationMinutes % 60;
            return hours + " horas" + (minutes > 0 ? " y " + minutes + " minutos" : "");
        }
    }
}
