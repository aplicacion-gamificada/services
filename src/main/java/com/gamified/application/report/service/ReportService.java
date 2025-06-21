package com.gamified.application.report.service;

import com.gamified.application.report.model.entity.StudentReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Servicio para generar reportes y analytics
 * Fase 4: Módulo de Reportes y Analytics
 */
@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Genera un reporte completo para un estudiante en un periodo específico
     */
    public StudentReport generateStudentReport(Integer studentProfileId, String reportType, 
                                             LocalDateTime periodStart, LocalDateTime periodEnd) {
        logger.info("Generando reporte {} para estudiante {} desde {} hasta {}", 
                   reportType, studentProfileId, periodStart, periodEnd);

        StudentReport report = new StudentReport(studentProfileId, reportType, periodStart, periodEnd);

        try {
            // Obtener información básica del estudiante
            populateStudentBasicInfo(report);
            
            // Calcular métricas de ejercicios
            populateExerciseMetrics(report);
            
            // Calcular métricas de logros
            populateAchievementMetrics(report);
            
            // Calcular métricas de progreso y streaks
            populateProgressMetrics(report);
            
            // Calcular métricas de learning points
            populateLearningPointMetrics(report);
            
            // Obtener comparativas de clase
            populateClassComparatives(report);
            
            // Calcular puntuaciones y alertas
            report.calculateOverallPerformanceScore();
            report.calculateAlertLevel();
            
            logger.info("Reporte generado exitosamente para estudiante {}: score={}, alert={}", 
                       studentProfileId, report.getOverallPerformanceScore(), report.getAlertLevel());
            
            return report;
            
        } catch (Exception e) {
            logger.error("Error generando reporte para estudiante {}: {}", studentProfileId, e.getMessage());
            throw new RuntimeException("Error al generar reporte", e);
        }
    }

    /**
     * Llena información básica del estudiante
     */
    private void populateStudentBasicInfo(StudentReport report) {
        String sql = """
            SELECT sp.username 
            FROM student_profile sp 
            WHERE sp.id = ?
            """;
        
        try {
            String username = jdbcTemplate.queryForObject(sql, String.class, report.getStudentProfileId());
            report.setStudentUsername(username);
        } catch (Exception e) {
            logger.warn("No se pudo obtener información básica del estudiante {}: {}", 
                       report.getStudentProfileId(), e.getMessage());
            report.setStudentUsername("Usuario Desconocido");
        }
    }

    /**
     * Calcula métricas de ejercicios completados
     */
    private void populateExerciseMetrics(StudentReport report) {
        String sql = """
            SELECT 
                COUNT(*) as total_exercises,
                SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) as correct_exercises,
                AVG(CAST(ea.time_spent AS DECIMAL(8,2))) as avg_time,
                SUM(ea.points_earned) as total_points
            FROM exercise_attempt ea
            WHERE ea.student_profile_id = ?
              AND ea.completed_at BETWEEN ? AND ?
              AND ea.completed_at IS NOT NULL
            """;
        
        try {
            Map<String, Object> metrics = jdbcTemplate.queryForMap(sql, 
                report.getStudentProfileId(), 
                report.getReportPeriodStart(), 
                report.getReportPeriodEnd()
            );
            
            Integer totalExercises = (Integer) metrics.get("total_exercises");
            Integer correctExercises = (Integer) metrics.get("correct_exercises");
            Double avgTime = (Double) metrics.get("avg_time");
            Integer totalPoints = (Integer) metrics.get("total_points");
            
            report.setTotalExercisesCompleted(totalExercises != null ? totalExercises : 0);
            report.setCorrectExercises(correctExercises != null ? correctExercises : 0);
            report.setAverageTimePerExercise(avgTime != null ? avgTime : 0.0);
            report.setTotalPointsEarned(totalPoints != null ? totalPoints : 0);
            
            // Calcular tasa de éxito
            if (totalExercises != null && totalExercises > 0) {
                double successRate = (correctExercises != null ? correctExercises : 0) / (double) totalExercises;
                report.setSuccessRate(Math.round(successRate * 10000.0) / 100.0); // Porcentaje con 2 decimales
            } else {
                report.setSuccessRate(0.0);
            }
            
        } catch (Exception e) {
            logger.warn("No se pudieron obtener métricas de ejercicios para estudiante {}: {}", 
                       report.getStudentProfileId(), e.getMessage());
            setDefaultExerciseMetrics(report);
        }
    }

    /**
     * Calcula métricas de logros
     */
    private void populateAchievementMetrics(StudentReport report) {
        String sql = """
            SELECT 
                COUNT(*) as achievements_count,
                SUM(sa.points_awarded) as achievement_points
            FROM student_achievement sa
            WHERE sa.student_profile_id = ?
              AND sa.earned_at BETWEEN ? AND ?
              AND sa.is_active = 1
            """;
        
        try {
            Map<String, Object> metrics = jdbcTemplate.queryForMap(sql, 
                report.getStudentProfileId(), 
                report.getReportPeriodStart(), 
                report.getReportPeriodEnd()
            );
            
            Integer achievementsCount = (Integer) metrics.get("achievements_count");
            Integer achievementPoints = (Integer) metrics.get("achievement_points");
            
            report.setAchievementsUnlocked(achievementsCount != null ? achievementsCount : 0);
            report.setTotalAchievementPoints(achievementPoints != null ? achievementPoints : 0);
            
        } catch (Exception e) {
            logger.warn("No se pudieron obtener métricas de logros para estudiante {}: {}", 
                       report.getStudentProfileId(), e.getMessage());
            report.setAchievementsUnlocked(0);
            report.setTotalAchievementPoints(0);
        }
    }

    /**
     * Calcula métricas de progreso y streaks
     */
    private void populateProgressMetrics(StudentReport report) {
        try {
            // Días activos en el periodo
            String activeDaysSQL = """
                SELECT COUNT(DISTINCT CAST(ea.completed_at AS DATE)) as active_days
                FROM exercise_attempt ea
                WHERE ea.student_profile_id = ?
                  AND ea.completed_at BETWEEN ? AND ?
                  AND ea.completed_at IS NOT NULL
                """;
            
            Integer activeDays = jdbcTemplate.queryForObject(activeDaysSQL, Integer.class, 
                report.getStudentProfileId(), 
                report.getReportPeriodStart(), 
                report.getReportPeriodEnd()
            );
            report.setActiveDays(activeDays != null ? activeDays : 0);
            
            // Información de streaks de la tabla streak
            String streakSQL = """
                SELECT current_streak_days, longest_streak_days
                FROM streak
                WHERE student_profile_id = ?
                """;
            
            try {
                Map<String, Object> streakData = jdbcTemplate.queryForMap(streakSQL, report.getStudentProfileId());
                Integer currentStreak = (Integer) streakData.get("current_streak_days");
                Integer longestStreak = (Integer) streakData.get("longest_streak_days");
                
                report.setCurrentStreak(currentStreak != null ? currentStreak : 0);
                report.setLongestStreak(longestStreak != null ? longestStreak : 0);
                
            } catch (Exception e) {
                // Si no existe registro de streak, establecer valores por defecto
                report.setCurrentStreak(0);
                report.setLongestStreak(0);
            }
            
        } catch (Exception e) {
            logger.warn("No se pudieron obtener métricas de progreso para estudiante {}: {}", 
                       report.getStudentProfileId(), e.getMessage());
            report.setActiveDays(0);
            report.setCurrentStreak(0);
            report.setLongestStreak(0);
        }
    }

    /**
     * Calcula métricas de learning points
     */
    private void populateLearningPointMetrics(StudentReport report) {
        // Esta es una métrica más compleja que requiere lógica de negocio específica
        // Por ahora, establecemos valores por defecto
        report.setLearningPointsCompleted(0);
        report.setLearningPointsInProgress(0);
        
        // TODO: Implementar cuando se defina la lógica de progreso en learning points
    }

    /**
     * Calcula comparativas con la clase
     */
    private void populateClassComparatives(StudentReport report) {
        try {
            // Obtener la clase del estudiante (si está enrollado)
            String classSQL = """
                SELECT c.id as classroom_id
                FROM enrollment e
                INNER JOIN classroom c ON e.classroom_id = c.id
                WHERE e.student_profile_id = ?
                LIMIT 1
                """;
            
            Integer classroomId = jdbcTemplate.queryForObject(classSQL, Integer.class, report.getStudentProfileId());
            
            if (classroomId != null) {
                // Obtener estadísticas de la clase
                String classStatsSQL = """
                    SELECT 
                        COUNT(DISTINCT e.student_profile_id) as total_classmates,
                        AVG(CASE 
                            WHEN ea_count.total > 0 THEN 
                                (ea_count.correct * 100.0) / ea_count.total 
                            ELSE 0 
                        END) as class_average
                    FROM enrollment e
                    LEFT JOIN (
                        SELECT 
                            ea.student_profile_id,
                            COUNT(*) as total,
                            SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) as correct
                        FROM exercise_attempt ea
                        WHERE ea.completed_at BETWEEN ? AND ?
                          AND ea.completed_at IS NOT NULL
                        GROUP BY ea.student_profile_id
                    ) ea_count ON e.student_profile_id = ea_count.student_profile_id
                    WHERE e.classroom_id = ?
                    """;
                
                Map<String, Object> classStats = jdbcTemplate.queryForMap(classStatsSQL, 
                    report.getReportPeriodStart(), 
                    report.getReportPeriodEnd(), 
                    classroomId
                );
                
                Integer totalClassmates = (Integer) classStats.get("total_classmates");
                Double classAverage = (Double) classStats.get("class_average");
                
                report.setTotalClassmates(totalClassmates != null ? totalClassmates : 0);
                report.setClassAverage(classAverage != null ? Math.round(classAverage * 100.0) / 100.0 : 0.0);
                
                // Calcular ranking (simplificado)
                if (report.getSuccessRate() != null && classAverage != null) {
                    if (report.getSuccessRate() >= classAverage) {
                        report.setClassRanking(1); // Simplificado: arriba o abajo del promedio
                    } else {
                        report.setClassRanking(2);
                    }
                } else {
                    report.setClassRanking(null);
                }
                
            } else {
                // No está en ninguna clase
                report.setTotalClassmates(0);
                report.setClassAverage(null);
                report.setClassRanking(null);
            }
            
        } catch (Exception e) {
            logger.warn("No se pudieron obtener comparativas de clase para estudiante {}: {}", 
                       report.getStudentProfileId(), e.getMessage());
            report.setTotalClassmates(0);
            report.setClassAverage(null);
            report.setClassRanking(null);
        }
    }

    /**
     * Establece métricas por defecto para ejercicios
     */
    private void setDefaultExerciseMetrics(StudentReport report) {
        report.setTotalExercisesCompleted(0);
        report.setCorrectExercises(0);
        report.setSuccessRate(0.0);
        report.setAverageTimePerExercise(0.0);
        report.setTotalPointsEarned(0);
    }

    /**
     * Genera reporte semanal para un estudiante
     */
    public StudentReport generateWeeklyReport(Integer studentProfileId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minus(7, ChronoUnit.DAYS);
        return generateStudentReport(studentProfileId, "WEEKLY", startDate, endDate);
    }

    /**
     * Genera reporte mensual para un estudiante
     */
    public StudentReport generateMonthlyReport(Integer studentProfileId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minus(30, ChronoUnit.DAYS);
        return generateStudentReport(studentProfileId, "MONTHLY", startDate, endDate);
    }

    /**
     * Obtiene estudiantes que requieren atención (con alertas)
     */
    public List<StudentReport> getStudentsNeedingAttention(Integer classroomId) {
        logger.info("Obteniendo estudiantes que requieren atención en aula {}", classroomId);
        
        List<StudentReport> alertReports = new ArrayList<>();
        
        try {
            // Obtener estudiantes del aula
            String studentsSQL = """
                SELECT e.student_profile_id
                FROM enrollment e
                WHERE e.classroom_id = ?
                """;
            
            List<Integer> studentIds = jdbcTemplate.queryForList(studentsSQL, Integer.class, classroomId);
            
            // Generar reportes semanales para cada estudiante
            for (Integer studentId : studentIds) {
                try {
                    StudentReport report = generateWeeklyReport(studentId);
                    
                    // Solo incluir estudiantes con alertas
                    if (!"NONE".equals(report.getAlertLevel())) {
                        alertReports.add(report);
                    }
                    
                } catch (Exception e) {
                    logger.warn("Error generando reporte para estudiante {}: {}", studentId, e.getMessage());
                }
            }
            
            // Ordenar por nivel de alerta (HIGH, MEDIUM, LOW)
            alertReports.sort((a, b) -> {
                int priorityA = getAlertPriority(a.getAlertLevel());
                int priorityB = getAlertPriority(b.getAlertLevel());
                return Integer.compare(priorityB, priorityA); // Orden descendente
            });
            
            logger.info("Encontrados {} estudiantes que requieren atención en aula {}", 
                       alertReports.size(), classroomId);
            
            return alertReports;
            
        } catch (Exception e) {
            logger.error("Error obteniendo estudiantes que requieren atención: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Obtiene la prioridad numérica del nivel de alerta
     */
    private int getAlertPriority(String alertLevel) {
        return switch (alertLevel) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    /**
     * Genera reporte general de una clase
     */
    public Map<String, Object> generateClassroomSummary(Integer classroomId) {
        logger.info("Generando resumen de aula {}", classroomId);
        
        String sql = """
            SELECT 
                COUNT(DISTINCT e.student_profile_id) as total_students,
                COALESCE(AVG(ea_stats.success_rate), 0) as class_average_success_rate,
                COALESCE(SUM(ea_stats.total_exercises), 0) as total_exercises_completed,
                COALESCE(SUM(ea_stats.total_points), 0) as total_points_earned,
                COALESCE(COUNT(DISTINCT ea_stats.student_profile_id), 0) as active_students_last_week
            FROM enrollment e
            LEFT JOIN (
                SELECT 
                    ea.student_profile_id,
                    COUNT(*) as total_exercises,
                    SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) as correct_exercises,
                    (SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) * 100.0) / COUNT(*) as success_rate,
                    SUM(ea.points_earned) as total_points
                FROM exercise_attempt ea
                WHERE ea.completed_at >= DATEADD(DAY, -7, GETDATE())
                  AND ea.completed_at IS NOT NULL
                GROUP BY ea.student_profile_id
            ) ea_stats ON e.student_profile_id = ea_stats.student_profile_id
            WHERE e.classroom_id = ?
            """;
        
        try {
            return jdbcTemplate.queryForMap(sql, classroomId);
        } catch (Exception e) {
            logger.error("Error generando resumen de aula {}: {}", classroomId, e.getMessage());
            throw new RuntimeException("Error al generar resumen de aula", e);
        }
    }
} 