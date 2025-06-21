package com.gamified.application.report.controller;

import com.gamified.application.report.model.entity.StudentReport;
import com.gamified.application.report.service.ReportService;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para módulo de reportes y analytics
 * Fase 4: Módulo de Reportes y Analytics
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    /**
     * Genera reporte semanal para un estudiante
     */
    @GetMapping("/student/{studentProfileId}/weekly")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @authenticationUtils.isOwnProfile(#studentProfileId))")
    public ResponseEntity<CommonResponseDto<StudentReport>> getWeeklyReport(@PathVariable Integer studentProfileId) {
        
        logger.info("GET /api/reports/student/{}/weekly", studentProfileId);
        
        try {
            StudentReport report = reportService.generateWeeklyReport(studentProfileId);
            
            return ResponseEntity.ok(
                new CommonResponseDto<>(true, "Reporte semanal generado exitosamente", report)
            );
            
        } catch (Exception e) {
            logger.error("Error generando reporte semanal para estudiante {}: {}", studentProfileId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                new CommonResponseDto<>(false, "Error al generar reporte semanal: " + e.getMessage(), (StudentReport) null)
            );
        }
    }

    /**
     * Genera reporte mensual para un estudiante
     */
    @GetMapping("/student/{studentProfileId}/monthly")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @authenticationUtils.isOwnProfile(#studentProfileId))")
    public ResponseEntity<CommonResponseDto<StudentReport>> getMonthlyReport(@PathVariable Integer studentProfileId) {
        
        logger.info("GET /api/reports/student/{}/monthly", studentProfileId);
        
        try {
            StudentReport report = reportService.generateMonthlyReport(studentProfileId);
            
            return ResponseEntity.ok(
                new CommonResponseDto<>(true, "Reporte mensual generado exitosamente", report)
            );
            
        } catch (Exception e) {
            logger.error("Error generando reporte mensual para estudiante {}: {}", studentProfileId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                new CommonResponseDto<>(false, "Error al generar reporte mensual: " + e.getMessage(), (StudentReport) null)
            );
        }
    }

    /**
     * Genera reporte personalizado para un estudiante en un periodo específico
     */
    @GetMapping("/student/{studentProfileId}/custom")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @authenticationUtils.isOwnProfile(#studentProfileId))")
    public ResponseEntity<CommonResponseDto<StudentReport>> getCustomReport(
            @PathVariable Integer studentProfileId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        logger.info("GET /api/reports/student/{}/custom?startDate={}&endDate={}", 
                   studentProfileId, startDate, endDate);
        
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            
            StudentReport report = reportService.generateStudentReport(studentProfileId, "CUSTOM", start, end);
            
            return ResponseEntity.ok(
                new CommonResponseDto<>(true, "Reporte personalizado generado exitosamente", report)
            );
            
        } catch (DateTimeParseException e) {
            logger.error("Error de formato de fecha: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                new CommonResponseDto<>(false, "Formato de fecha inválido. Use ISO 8601 (YYYY-MM-DDTHH:mm:ss)", (StudentReport) null)
            );
        } catch (Exception e) {
            logger.error("Error generando reporte personalizado para estudiante {}: {}", studentProfileId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                new CommonResponseDto<>(false, "Error al generar reporte personalizado: " + e.getMessage(), (StudentReport) null)
            );
        }
    }

    /**
     * Obtiene estudiantes que requieren atención en un aula
     */
    @GetMapping("/classroom/{classroomId}/alerts")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<CommonResponseDto<List<StudentReport>>> getStudentsNeedingAttention(@PathVariable Integer classroomId) {
        
        logger.info("GET /api/reports/classroom/{}/alerts", classroomId);
        
        try {
            List<StudentReport> alertReports = reportService.getStudentsNeedingAttention(classroomId);
            
            String message = alertReports.isEmpty() ? 
                "No hay estudiantes que requieran atención especial" : 
                String.format("Encontrados %d estudiantes que requieren atención", alertReports.size());
            
            return ResponseEntity.ok(
                new CommonResponseDto<>(true, message, alertReports)
            );
            
        } catch (Exception e) {
            logger.error("Error obteniendo estudiantes que requieren atención en aula {}: {}", classroomId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                new CommonResponseDto<>(false, "Error al obtener estudiantes con alertas: " + e.getMessage(), (List<StudentReport>) null)
            );
        }
    }

    /**
     * Genera resumen general de un aula
     */
    @GetMapping("/classroom/{classroomId}/summary")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<CommonResponseDto<Map<String, Object>>> getClassroomSummary(@PathVariable Integer classroomId) {
        
        logger.info("GET /api/reports/classroom/{}/summary", classroomId);
        
        try {
            Map<String, Object> summary = reportService.generateClassroomSummary(classroomId);
            
            return ResponseEntity.ok(
                new CommonResponseDto<>(true, "Resumen de aula generado exitosamente", summary)
            );
            
        } catch (Exception e) {
            logger.error("Error generando resumen de aula {}: {}", classroomId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                new CommonResponseDto<>(false, "Error al generar resumen de aula: " + e.getMessage(), (Map<String, Object>) null)
            );
        }
    }

    /**
     * Endpoint para dashboards - métricas rápidas de un estudiante
     */
    @GetMapping("/student/{studentProfileId}/quick-stats")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @authenticationUtils.isOwnProfile(#studentProfileId))")
    public ResponseEntity<CommonResponseDto<Map<String, Object>>> getQuickStats(@PathVariable Integer studentProfileId) {
        
        logger.info("GET /api/reports/student/{}/quick-stats", studentProfileId);
        
        try {
            // Generar reporte de los últimos 7 días para obtener métricas rápidas
            StudentReport weeklyReport = reportService.generateWeeklyReport(studentProfileId);
            
            // Extraer solo las métricas más importantes para dashboards
            Map<String, Object> quickStats = Map.of(
                "successRate", weeklyReport.getSuccessRate(),
                "totalExercises", weeklyReport.getTotalExercisesCompleted(),
                "pointsEarned", weeklyReport.getTotalPointsEarned(),
                "currentStreak", weeklyReport.getCurrentStreak(),
                "achievementsUnlocked", weeklyReport.getAchievementsUnlocked(),
                "alertLevel", weeklyReport.getAlertLevel(),
                "alertReason", weeklyReport.getAlertReason(),
                "overallScore", weeklyReport.getOverallPerformanceScore(),
                "activeDays", weeklyReport.getActiveDays()
            );
            
            return ResponseEntity.ok(
                new CommonResponseDto<>(true, "Estadísticas rápidas obtenidas exitosamente", quickStats)
            );
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas rápidas para estudiante {}: {}", studentProfileId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                new CommonResponseDto<>(false, "Error al obtener estadísticas rápidas: " + e.getMessage(), (Map<String, Object>) null)
            );
        }
    }

    /**
     * Endpoint para obtener tendencias de rendimiento de un estudiante
     */
    @GetMapping("/student/{studentProfileId}/trends")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @authenticationUtils.isOwnProfile(#studentProfileId))")
    public ResponseEntity<CommonResponseDto<Map<String, Object>>> getPerformanceTrends(@PathVariable Integer studentProfileId) {
        
        logger.info("GET /api/reports/student/{}/trends", studentProfileId);
        
        try {
            // Generar reportes de diferentes periodos para comparar tendencias
            StudentReport currentWeek = reportService.generateWeeklyReport(studentProfileId);
            StudentReport currentMonth = reportService.generateMonthlyReport(studentProfileId);
            
            // Calcular tendencias básicas
            Map<String, Object> trends = Map.of(
                "weekly", Map.of(
                    "successRate", currentWeek.getSuccessRate(),
                    "totalExercises", currentWeek.getTotalExercisesCompleted(),
                    "activeDays", currentWeek.getActiveDays(),
                    "alertLevel", currentWeek.getAlertLevel()
                ),
                "monthly", Map.of(
                    "successRate", currentMonth.getSuccessRate(),
                    "totalExercises", currentMonth.getTotalExercisesCompleted(),
                    "activeDays", currentMonth.getActiveDays(),
                    "achievementsUnlocked", currentMonth.getAchievementsUnlocked()
                ),
                "comparison", Map.of(
                    "weeklyVsMonthlySuccessRate", 
                        currentWeek.getSuccessRate() - (currentMonth.getSuccessRate() / 4.0), // Aproximación semanal del mensual
                    "isImproving", 
                        currentWeek.getOverallPerformanceScore() >= currentMonth.getOverallPerformanceScore() / 4.0
                )
            );
            
            return ResponseEntity.ok(
                new CommonResponseDto<>(true, "Tendencias de rendimiento obtenidas exitosamente", trends)
            );
            
        } catch (Exception e) {
            logger.error("Error obteniendo tendencias para estudiante {}: {}", studentProfileId, e.getMessage());
            return ResponseEntity.internalServerError().body(
                new CommonResponseDto<>(false, "Error al obtener tendencias de rendimiento: " + e.getMessage(), (Map<String, Object>) null)
            );
        }
    }
} 