package com.gamified.application.notification.service;

import com.gamified.application.notification.model.entity.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Servicio para gestión de notificaciones
 * Fase 4: Módulo de Notificaciones
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Envía una notificación al sistema
     */
    public void sendNotification(Notification notification) {
        logger.info("Enviando notificación tipo {} a usuario {}", 
                   notification.getNotificationType(), notification.getRecipientUserId());
        
        try {
            // Simular guardado en base de datos (sin tabla real aún)
            logger.info("Guardando notificación: {}", notification.getTitle());
            
            // Marcar como enviada
            notification.markAsSent();
            
            // Procesar según el tipo de entrega
            if (notification.getShowInApp()) {
                processInAppNotification(notification);
            }
            
            if (notification.getSendEmail()) {
                processEmailNotification(notification);
            }
            
            if (notification.getSendPush()) {
                processPushNotification(notification);
            }
            
            logger.info("Notificación enviada exitosamente: {}", notification.getId());
            
        } catch (Exception e) {
            logger.error("Error enviando notificación: {}", e.getMessage());
            throw new RuntimeException("Error al enviar notificación", e);
        }
    }

    /**
     * Procesa notificación en la aplicación
     */
    private void processInAppNotification(Notification notification) {
        logger.info("Procesando notificación in-app para usuario {}: {}", 
                   notification.getRecipientUserId(), notification.getTitle());
        // TODO: Implementar almacenamiento en BD cuando esté disponible
    }

    /**
     * Procesa notificación por email
     */
    @Async
    private void processEmailNotification(Notification notification) {
        logger.info("Procesando notificación por email para usuario {}: {}", 
                   notification.getRecipientUserId(), notification.getTitle());
        
        try {
            // Simular envío de email
            Thread.sleep(100); // Simular latencia de envío
            notification.setEmailStatus("SENT");
            logger.info("Email enviado exitosamente a usuario {}", notification.getRecipientUserId());
            
        } catch (Exception e) {
            logger.error("Error enviando email a usuario {}: {}", notification.getRecipientUserId(), e.getMessage());
            notification.setEmailStatus("FAILED");
        }
    }

    /**
     * Procesa notificación push
     */
    @Async
    private void processPushNotification(Notification notification) {
        logger.info("Procesando notificación push para usuario {}: {}", 
                   notification.getRecipientUserId(), notification.getTitle());
        
        try {
            // Simular envío de push
            Thread.sleep(50); // Simular latencia de envío
            notification.setPushStatus("SENT");
            logger.info("Push enviado exitosamente a usuario {}", notification.getRecipientUserId());
            
        } catch (Exception e) {
            logger.error("Error enviando push a usuario {}: {}", notification.getRecipientUserId(), e.getMessage());
            notification.setPushStatus("FAILED");
        }
    }

    /**
     * Envía notificación de logro desbloqueado
     */
    public void sendAchievementUnlockedNotification(Integer studentUserId, String achievementName, 
                                                   Integer achievementId, Integer pointsEarned) {
        logger.info("Enviando notificación de logro desbloqueado a estudiante {}: {}", 
                   studentUserId, achievementName);
        
        Notification notification = Notification.createAchievementNotification(
            studentUserId, achievementName, achievementId, pointsEarned
        );
        
        sendNotification(notification);
    }

    /**
     * Envía recordatorio de actividad a estudiantes inactivos
     */
    public void sendActivityReminder(Integer studentUserId, int daysSinceLastActivity) {
        logger.info("Enviando recordatorio de actividad a estudiante {} (inactivo por {} días)", 
                   studentUserId, daysSinceLastActivity);
        
        Notification notification = Notification.createActivityReminderNotification(
            studentUserId, daysSinceLastActivity
        );
        
        sendNotification(notification);
    }

    /**
     * Envía alerta de estudiante a profesor
     */
    public void sendStudentAlertToTeacher(Integer teacherUserId, String studentName, 
                                        Integer studentId, String alertReason) {
        logger.info("Enviando alerta de estudiante {} a profesor {}: {}", 
                   studentName, teacherUserId, alertReason);
        
        Notification notification = Notification.createStudentAlertNotification(
            teacherUserId, studentName, studentId, alertReason
        );
        
        sendNotification(notification);
    }

    /**
     * Envía recordatorios masivos a estudiantes inactivos
     */
    public void sendMassActivityReminders() {
        logger.info("Enviando recordatorios masivos a estudiantes inactivos");
        
        try {
            // Buscar estudiantes inactivos (sin actividad en más de 3 días)
            String sql = """
                SELECT DISTINCT 
                    sp.id as student_id,
                    sp.user_id,
                    DATEDIFF(DAY, MAX(COALESCE(ea.completed_at, sp.created_at)), GETDATE()) as days_inactive
                FROM student_profile sp
                LEFT JOIN exercise_attempt ea ON sp.id = ea.student_profile_id
                WHERE sp.is_active = 1
                GROUP BY sp.id, sp.user_id, sp.created_at
                HAVING DATEDIFF(DAY, MAX(COALESCE(ea.completed_at, sp.created_at)), GETDATE()) >= 3
                ORDER BY days_inactive DESC
                """;
            
            List<InactiveStudent> inactiveStudents = jdbcTemplate.query(sql, new InactiveStudentRowMapper());
            
            logger.info("Encontrados {} estudiantes inactivos para recordatorios", inactiveStudents.size());
            
            for (InactiveStudent student : inactiveStudents) {
                try {
                    sendActivityReminder(student.userId, student.daysInactive);
                } catch (Exception e) {
                    logger.warn("Error enviando recordatorio a estudiante {}: {}", 
                               student.userId, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error enviando recordatorios masivos: {}", e.getMessage());
        }
    }

    /**
     * Envía alertas de estudiantes con problemas a sus profesores
     */
    public void sendStudentAlertsToTeachers() {
        logger.info("Enviando alertas de estudiantes con problemas a profesores");
        
        try {
            // Buscar estudiantes con alertas y sus profesores
            String sql = """
                SELECT DISTINCT
                    tp.user_id as teacher_user_id,
                    sp.user_id as student_user_id,
                    sp.id as student_profile_id,
                    sp.username as student_name,
                    'Bajo rendimiento detectado' as alert_reason
                FROM enrollment e
                INNER JOIN classroom c ON e.classroom_id = c.id
                INNER JOIN teacher_profile tp ON c.teacher_profile_id = tp.id
                INNER JOIN student_profile sp ON e.student_profile_id = sp.id
                LEFT JOIN exercise_attempt ea ON sp.id = ea.student_profile_id 
                    AND ea.completed_at >= DATEADD(DAY, -7, GETDATE())
                WHERE sp.is_active = 1 AND tp.is_active = 1
                GROUP BY tp.user_id, sp.user_id, sp.id, sp.username
                HAVING 
                    COUNT(ea.id) > 0 AND
                    (CAST(SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) AS FLOAT) / COUNT(ea.id)) < 0.6
                """;
            
            List<StudentAlert> alerts = jdbcTemplate.query(sql, new StudentAlertRowMapper());
            
            logger.info("Encontradas {} alertas de estudiantes para profesores", alerts.size());
            
            for (StudentAlert alert : alerts) {
                try {
                    sendStudentAlertToTeacher(
                        alert.teacherUserId, 
                        alert.studentName, 
                        alert.studentUserId, 
                        alert.alertReason
                    );
                } catch (Exception e) {
                    logger.warn("Error enviando alerta de estudiante {} a profesor {}: {}", 
                               alert.studentName, alert.teacherUserId, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error enviando alertas de estudiantes: {}", e.getMessage());
        }
    }

    /**
     * Envía notificación de bienvenida a nuevo usuario
     */
    public void sendWelcomeNotification(Integer userId, String userType, String username) {
        logger.info("Enviando notificación de bienvenida a {} {}", userType, username);
        
        String title = "¡Bienvenido a la plataforma!";
        String message = String.format("Hola %s, ¡bienvenido! Estamos emocionados de tenerte aquí.", username);
        
        if ("STUDENT".equals(userType)) {
            message += " ¡Comienza tu aventura de aprendizaje y desbloquea increíbles logros!";
        } else if ("TEACHER".equals(userType)) {
            message += " ¡Esperamos que disfrutes guiando a tus estudiantes hacia el éxito!";
        }
        
        Notification notification = new Notification(userId, userType, "INFO", title, message, "MEDIUM");
        notification.setActionUrl("/dashboard");
        notification.setSendEmail(true);
        notification.setExpiresAt(LocalDateTime.now().plusDays(30));
        
        sendNotification(notification);
    }

    /**
     * Procesa eventos de logros desbloqueados automáticamente
     */
    @EventListener
    public void handleAchievementUnlockedEvent(Object event) {
        // TODO: Implementar cuando se definan los eventos específicos
        //logger.info("Procesando evento de logro desbloqueado: {}", event);
    }

    // Clases auxiliares para mapeo de datos
    
    private static class InactiveStudent {
        int studentId;
        int userId;
        int daysInactive;
    }
    
    private static class StudentAlert {
        int teacherUserId;
        int studentUserId;
        int studentProfileId;
        String studentName;
        String alertReason;
    }
    
    // Row Mappers
    
    private static class InactiveStudentRowMapper implements RowMapper<InactiveStudent> {
        @Override
        public InactiveStudent mapRow(ResultSet rs, int rowNum) throws SQLException {
            InactiveStudent student = new InactiveStudent();
            student.studentId = rs.getInt("student_id");
            student.userId = rs.getInt("user_id");
            student.daysInactive = rs.getInt("days_inactive");
            return student;
        }
    }
    
    private static class StudentAlertRowMapper implements RowMapper<StudentAlert> {
        @Override
        public StudentAlert mapRow(ResultSet rs, int rowNum) throws SQLException {
            StudentAlert alert = new StudentAlert();
            alert.teacherUserId = rs.getInt("teacher_user_id");
            alert.studentUserId = rs.getInt("student_user_id");
            alert.studentProfileId = rs.getInt("student_profile_id");
            alert.studentName = rs.getString("student_name");
            alert.alertReason = rs.getString("alert_reason");
            return alert;
        }
    }

    /**
     * Procesa notificaciones programadas
     */
    public void processScheduledNotifications() {
        logger.info("Procesando notificaciones programadas");
        
        // Enviar recordatorios de actividad (ejecutar diariamente)
        sendMassActivityReminders();
        
        // Enviar alertas de estudiantes (ejecutar cada 6 horas)
        sendStudentAlertsToTeachers();
        
        logger.info("Procesamiento de notificaciones programadas completado");
    }

    /**
     * Obtiene estadísticas básicas de notificaciones
     */
    public void logNotificationStats() {
        logger.info("=== ESTADÍSTICAS DE NOTIFICACIONES ===");
        logger.info("Sistema de notificaciones activo y funcionando");
        logger.info("Tipos soportados: ACHIEVEMENT, REMINDER, ALERT, INFO, SYSTEM");
        logger.info("Canales: In-App, Email, Push");
        logger.info("======================================");
    }
} 