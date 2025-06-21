package com.gamified.application.notification.model.entity;

import java.time.LocalDateTime;

/**
 * Entidad para notificaciones del sistema
 * Fase 4: Módulo de Notificaciones
 */
public class Notification {
    private Integer id;
    private Integer recipientUserId;
    private String recipientUserType; // STUDENT, TEACHER, ADMIN
    private String notificationType; // ACHIEVEMENT, REMINDER, ALERT, INFO, SYSTEM
    private String title;
    private String message;
    private String priority; // LOW, MEDIUM, HIGH, URGENT
    
    // Metadatos específicos según el tipo
    private Integer relatedEntityId; // ID del logro, ejercicio, etc.
    private String relatedEntityType; // ACHIEVEMENT, EXERCISE, CLASSROOM, etc.
    private String actionUrl; // URL para redirigir al hacer clic
    
    // Estado de la notificación
    private Boolean isRead;
    private Boolean isActive;
    private LocalDateTime readAt;
    private LocalDateTime expiresAt;
    
    // Configuración de entrega
    private Boolean sendEmail;
    private Boolean sendPush;
    private Boolean showInApp;
    private String emailStatus; // PENDING, SENT, FAILED
    private String pushStatus; // PENDING, SENT, FAILED
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime scheduledFor; // Para notificaciones programadas
    private LocalDateTime sentAt;

    // Constructors
    public Notification() {}

    public Notification(Integer recipientUserId, String recipientUserType, String notificationType, 
                       String title, String message, String priority) {
        this.recipientUserId = recipientUserId;
        this.recipientUserType = recipientUserType;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.priority = priority;
        this.isRead = false;
        this.isActive = true;
        this.sendEmail = false;
        this.sendPush = false;
        this.showInApp = true;
        this.createdAt = LocalDateTime.now();
        this.scheduledFor = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Integer recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getRecipientUserType() {
        return recipientUserType;
    }

    public void setRecipientUserType(String recipientUserType) {
        this.recipientUserType = recipientUserType;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getRelatedEntityId() {
        return relatedEntityId;
    }

    public void setRelatedEntityId(Integer relatedEntityId) {
        this.relatedEntityId = relatedEntityId;
    }

    public String getRelatedEntityType() {
        return relatedEntityType;
    }

    public void setRelatedEntityType(String relatedEntityType) {
        this.relatedEntityType = relatedEntityType;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    public Boolean getSendPush() {
        return sendPush;
    }

    public void setSendPush(Boolean sendPush) {
        this.sendPush = sendPush;
    }

    public Boolean getShowInApp() {
        return showInApp;
    }

    public void setShowInApp(Boolean showInApp) {
        this.showInApp = showInApp;
    }

    public String getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(String emailStatus) {
        this.emailStatus = emailStatus;
    }

    public String getPushStatus() {
        return pushStatus;
    }

    public void setPushStatus(String pushStatus) {
        this.pushStatus = pushStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    // Business Methods
    
    /**
     * Marca la notificación como leída
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
    
    /**
     * Marca la notificación como enviada
     */
    public void markAsSent() {
        this.sentAt = LocalDateTime.now();
        if (this.sendEmail) {
            this.emailStatus = "SENT";
        }
        if (this.sendPush) {
            this.pushStatus = "SENT";
        }
    }
    
    /**
     * Verifica si la notificación ha expirado
     */
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Verifica si la notificación está lista para ser enviada
     */
    public boolean isReadyToSend() {
        return isActive && scheduledFor != null && 
               LocalDateTime.now().isAfter(scheduledFor) && 
               !isExpired();
    }
    
    /**
     * Configura la notificación como de alta prioridad con email
     */
    public void setHighPriorityWithEmail(String actionUrl) {
        this.priority = "HIGH";
        this.sendEmail = true;
        this.sendPush = true;
        this.actionUrl = actionUrl;
        this.emailStatus = "PENDING";
        this.pushStatus = "PENDING";
    }
    
    /**
     * Crea una notificación de logro desbloqueado
     */
    public static Notification createAchievementNotification(Integer studentUserId, String achievementName, 
                                                           Integer achievementId, Integer pointsEarned) {
        Notification notification = new Notification(
            studentUserId, 
            "STUDENT", 
            "ACHIEVEMENT",
            "¡Nuevo logro desbloqueado!",
            String.format("¡Felicidades! Has desbloqueado el logro '%s' y ganado %d puntos.", 
                         achievementName, pointsEarned),
            "MEDIUM"
        );
        
        notification.setRelatedEntityId(achievementId);
        notification.setRelatedEntityType("ACHIEVEMENT");
        notification.setActionUrl("/achievements/" + achievementId);
        notification.setSendPush(true);
        
        return notification;
    }
    
    /**
     * Crea una notificación de recordatorio de actividad
     */
    public static Notification createActivityReminderNotification(Integer studentUserId, int daysSinceLastActivity) {
        Notification notification = new Notification(
            studentUserId,
            "STUDENT",
            "REMINDER",
            "¡Te extrañamos!",
            String.format("Han pasado %d días desde tu última actividad. ¡Continúa aprendiendo!", daysSinceLastActivity),
            "LOW"
        );
        
        notification.setActionUrl("/dashboard");
        notification.setSendEmail(true);
        notification.setExpiresAt(LocalDateTime.now().plusDays(7)); // Expira en 7 días
        
        return notification;
    }
    
    /**
     * Crea una notificación de alerta para profesores
     */
    public static Notification createStudentAlertNotification(Integer teacherUserId, String studentName, 
                                                            Integer studentId, String alertReason) {
        Notification notification = new Notification(
            teacherUserId,
            "TEACHER",
            "ALERT",
            "Estudiante requiere atención",
            String.format("El estudiante %s requiere atención: %s", studentName, alertReason),
            "HIGH"
        );
        
        notification.setRelatedEntityId(studentId);
        notification.setRelatedEntityType("STUDENT");
        notification.setActionUrl("/students/" + studentId + "/report");
        notification.setHighPriorityWithEmail("/students/" + studentId + "/report");
        
        return notification;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", recipientUserId=" + recipientUserId +
                ", notificationType='" + notificationType + '\'' +
                ", title='" + title + '\'' +
                ", priority='" + priority + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
} 