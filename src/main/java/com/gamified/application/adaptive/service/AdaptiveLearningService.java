package com.gamified.application.adaptive.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Servicio de aprendizaje adaptativo
 * Ajusta la dificultad del contenido según el rendimiento del estudiante
 * Fase 4: Módulo de Contenido Adaptativo
 */
@Service
public class AdaptiveLearningService {

    private static final Logger logger = LoggerFactory.getLogger(AdaptiveLearningService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Recomienda el siguiente ejercicio basado en el rendimiento del estudiante
     */
    public Map<String, Object> getRecommendedExercise(Integer studentProfileId, Integer learningPointId) {
        logger.info("Obteniendo ejercicio recomendado para estudiante {} en learning point {}", 
                   studentProfileId, learningPointId);
        
        try {
            // Analizar rendimiento del estudiante
            StudentPerformanceAnalysis analysis = analyzeStudentPerformance(studentProfileId, learningPointId);
            
            // Determinar dificultad recomendada
            DifficultyLevel recommendedDifficulty = determineRecommendedDifficulty(analysis);
            
            // Buscar ejercicio apropiado
            Map<String, Object> recommendedExercise = findExerciseByDifficulty(
                learningPointId, recommendedDifficulty, studentProfileId);
            
            // Agregar información de recomendación
            recommendedExercise.put("recommendation_reason", analysis.getRecommendationReason());
            recommendedExercise.put("difficulty_adjustment", analysis.getDifficultyAdjustment());
            recommendedExercise.put("confidence_level", analysis.getConfidenceLevel());
            
            logger.info("Ejercicio recomendado para estudiante {}: {} (dificultad: {})", 
                       studentProfileId, recommendedExercise.get("title"), recommendedDifficulty);
            
            return recommendedExercise;
            
        } catch (Exception e) {
            logger.error("Error obteniendo ejercicio recomendado: {}", e.getMessage());
            return getDefaultExercise(learningPointId);
        }
    }

    /**
     * Analiza el rendimiento del estudiante
     */
    private StudentPerformanceAnalysis analyzeStudentPerformance(Integer studentProfileId, Integer learningPointId) {
        StudentPerformanceAnalysis analysis = new StudentPerformanceAnalysis();
        
        try {
            // Obtener métricas de los últimos 10 ejercicios
            String sql = """
                SELECT TOP 10
                    ea.is_correct,
                    ea.time_spent,
                    ea.difficulty_level,
                    ea.points_earned,
                    ea.completed_at,
                    e.estimated_duration
                FROM exercise_attempt ea
                INNER JOIN exercise e ON ea.exercise_id = e.id
                WHERE ea.student_profile_id = ? 
                  AND e.learning_point_id = ?
                  AND ea.completed_at IS NOT NULL
                ORDER BY ea.completed_at DESC
                """;
            
            List<Map<String, Object>> recentAttempts = jdbcTemplate.queryForList(sql, studentProfileId, learningPointId);
            
            if (recentAttempts.isEmpty()) {
                analysis.setNewStudent(true);
                analysis.setRecommendationReason("Estudiante nuevo - comenzando con dificultad básica");
                return analysis;
            }
            
            // Calcular métricas
            int totalAttempts = recentAttempts.size();
            int correctAttempts = 0;
            double totalTimeRatio = 0.0;
            String currentDifficulty = "EASY";
            
            for (Map<String, Object> attempt : recentAttempts) {
                Boolean isCorrect = (Boolean) attempt.get("is_correct");
                Integer timeSpent = (Integer) attempt.get("time_spent");
                Integer estimatedDuration = (Integer) attempt.get("estimated_duration");
                
                if (Boolean.TRUE.equals(isCorrect)) {
                    correctAttempts++;
                }
                
                if (timeSpent != null && estimatedDuration != null && estimatedDuration > 0) {
                    totalTimeRatio += (double) timeSpent / estimatedDuration;
                }
                
                String difficulty = (String) attempt.get("difficulty_level");
                if (difficulty != null) {
                    currentDifficulty = difficulty;
                }
            }
            
            double successRate = (double) correctAttempts / totalAttempts;
            double averageTimeRatio = totalTimeRatio / totalAttempts;
            
            analysis.setSuccessRate(successRate);
            analysis.setAverageTimeRatio(averageTimeRatio);
            analysis.setCurrentDifficulty(DifficultyLevel.valueOf(currentDifficulty));
            analysis.setTotalAttempts(totalAttempts);
            
            // Calcular nivel de confianza
            calculateConfidenceLevel(analysis);
            
            // Determinar ajuste de dificultad
            determineDifficultyAdjustment(analysis);
            
            return analysis;
            
        } catch (Exception e) {
            logger.error("Error analizando rendimiento del estudiante: {}", e.getMessage());
            analysis.setNewStudent(true);
            return analysis;
        }
    }

    /**
     * Calcula el nivel de confianza del análisis
     */
    private void calculateConfidenceLevel(StudentPerformanceAnalysis analysis) {
        if (analysis.getTotalAttempts() >= 8) {
            analysis.setConfidenceLevel("HIGH");
        } else if (analysis.getTotalAttempts() >= 5) {
            analysis.setConfidenceLevel("MEDIUM");
        } else {
            analysis.setConfidenceLevel("LOW");
        }
    }

    /**
     * Determina el ajuste de dificultad necesario
     */
    private void determineDifficultyAdjustment(StudentPerformanceAnalysis analysis) {
        double successRate = analysis.getSuccessRate();
        double timeRatio = analysis.getAverageTimeRatio();
        
        if (successRate >= 0.8 && timeRatio <= 1.2) {
            analysis.setDifficultyAdjustment("INCREASE");
            analysis.setRecommendationReason("Excelente rendimiento - aumentando dificultad");
        } else if (successRate <= 0.4 || timeRatio >= 2.0) {
            analysis.setDifficultyAdjustment("DECREASE");
            analysis.setRecommendationReason("Dificultades detectadas - reduciendo dificultad");
        } else if (successRate >= 0.6 && timeRatio <= 1.5) {
            analysis.setDifficultyAdjustment("MAINTAIN");
            analysis.setRecommendationReason("Rendimiento estable - manteniendo nivel actual");
        } else {
            analysis.setDifficultyAdjustment("SLIGHT_INCREASE");
            analysis.setRecommendationReason("Progreso gradual - ajuste ligero de dificultad");
        }
    }

    /**
     * Determina la dificultad recomendada
     */
    private DifficultyLevel determineRecommendedDifficulty(StudentPerformanceAnalysis analysis) {
        if (analysis.isNewStudent()) {
            return DifficultyLevel.EASY;
        }
        
        DifficultyLevel current = analysis.getCurrentDifficulty();
        String adjustment = analysis.getDifficultyAdjustment();
        
        return switch (adjustment) {
            case "INCREASE" -> increaseDifficulty(current);
            case "DECREASE" -> decreaseDifficulty(current);
            case "SLIGHT_INCREASE" -> slightIncreaseDifficulty(current);
            default -> current; // MAINTAIN
        };
    }

    /**
     * Aumenta la dificultad
     */
    private DifficultyLevel increaseDifficulty(DifficultyLevel current) {
        return switch (current) {
            case EASY -> DifficultyLevel.MEDIUM;
            case MEDIUM -> DifficultyLevel.HARD;
            case HARD -> DifficultyLevel.EXPERT;
            case EXPERT -> DifficultyLevel.EXPERT;
        };
    }

    /**
     * Disminuye la dificultad
     */
    private DifficultyLevel decreaseDifficulty(DifficultyLevel current) {
        return switch (current) {
            case EXPERT -> DifficultyLevel.HARD;
            case HARD -> DifficultyLevel.MEDIUM;
            case MEDIUM -> DifficultyLevel.EASY;
            case EASY -> DifficultyLevel.EASY;
        };
    }

    /**
     * Aumenta ligeramente la dificultad
     */
    private DifficultyLevel slightIncreaseDifficulty(DifficultyLevel current) {
        return current == DifficultyLevel.EASY ? DifficultyLevel.MEDIUM : current;
    }

    /**
     * Encuentra un ejercicio por dificultad
     */
    private Map<String, Object> findExerciseByDifficulty(Integer learningPointId, DifficultyLevel difficulty, 
                                                        Integer studentProfileId) {
        try {
            String sql = """
                SELECT TOP 1 e.id, e.title, e.description, e.difficulty_level, 
                       e.estimated_duration, e.points_value
                FROM exercise e
                LEFT JOIN exercise_attempt ea ON e.id = ea.exercise_id 
                    AND ea.student_profile_id = ? 
                    AND ea.completed_at >= DATEADD(DAY, -3, GETDATE())
                WHERE e.learning_point_id = ?
                  AND e.difficulty_level = ?
                  AND e.is_active = 1
                  AND ea.id IS NULL
                ORDER BY NEWID()
                """;
            
            List<Map<String, Object>> exercises = jdbcTemplate.queryForList(sql, 
                studentProfileId, learningPointId, difficulty.name());
            
            if (!exercises.isEmpty()) {
                return exercises.get(0);
            }
            
            return getDefaultExercise(learningPointId);
            
        } catch (Exception e) {
            logger.error("Error buscando ejercicio por dificultad: {}", e.getMessage());
            return getDefaultExercise(learningPointId);
        }
    }

    /**
     * Retorna un ejercicio por defecto
     */
    private Map<String, Object> getDefaultExercise(Integer learningPointId) {
        Map<String, Object> defaultExercise = new HashMap<>();
        defaultExercise.put("id", 0);
        defaultExercise.put("title", "Ejercicio de práctica");
        defaultExercise.put("description", "Ejercicio generado automáticamente");
        defaultExercise.put("difficulty_level", "EASY");
        defaultExercise.put("estimated_duration", 300);
        defaultExercise.put("points_value", 10);
        defaultExercise.put("recommendation_reason", "Ejercicio por defecto");
        return defaultExercise;
    }

    /**
     * Obtiene un plan de estudio personalizado
     */
    public Map<String, Object> getPersonalizedStudyPlan(Integer studentProfileId) {
        logger.info("Generando plan de estudio personalizado para estudiante {}", studentProfileId);
        
        try {
            Map<String, Object> studyPlan = new HashMap<>();
            
            List<Map<String, Object>> weakAreas = identifyWeakAreas(studentProfileId);
            List<Map<String, Object>> strongAreas = identifyStrongAreas(studentProfileId);
            List<String> recommendations = generateStudyRecommendations(weakAreas, strongAreas);
            
            studyPlan.put("student_profile_id", studentProfileId);
            studyPlan.put("weak_areas", weakAreas);
            studyPlan.put("strong_areas", strongAreas);
            studyPlan.put("recommendations", recommendations);
            studyPlan.put("generated_at", LocalDateTime.now());
            
            return studyPlan;
            
        } catch (Exception e) {
            logger.error("Error generando plan de estudio personalizado: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }

    private List<Map<String, Object>> identifyWeakAreas(Integer studentProfileId) {
        try {
            String sql = """
                SELECT 
                    lp.id as learning_point_id,
                    lp.title as learning_point_name,
                    COUNT(ea.id) as total_attempts,
                    SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) as correct_attempts,
                    (SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) * 100.0) / COUNT(ea.id) as success_rate
                FROM learning_point lp
                INNER JOIN exercise e ON lp.id = e.learning_point_id
                INNER JOIN exercise_attempt ea ON e.id = ea.exercise_id
                WHERE ea.student_profile_id = ?
                  AND ea.completed_at >= DATEADD(DAY, -30, GETDATE())
                GROUP BY lp.id, lp.title
                HAVING COUNT(ea.id) >= 3 AND (SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) * 100.0) / COUNT(ea.id) < 60
                ORDER BY success_rate ASC
                """;
            
            return jdbcTemplate.queryForList(sql, studentProfileId);
            
        } catch (Exception e) {
            logger.error("Error identificando áreas débiles: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> identifyStrongAreas(Integer studentProfileId) {
        try {
            String sql = """
                SELECT 
                    lp.id as learning_point_id,
                    lp.title as learning_point_name,
                    COUNT(ea.id) as total_attempts,
                    SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) as correct_attempts,
                    (SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) * 100.0) / COUNT(ea.id) as success_rate
                FROM learning_point lp
                INNER JOIN exercise e ON lp.id = e.learning_point_id
                INNER JOIN exercise_attempt ea ON e.id = ea.exercise_id
                WHERE ea.student_profile_id = ?
                  AND ea.completed_at >= DATEADD(DAY, -30, GETDATE())
                GROUP BY lp.id, lp.title
                HAVING COUNT(ea.id) >= 3 AND (SUM(CASE WHEN ea.is_correct = 1 THEN 1 ELSE 0 END) * 100.0) / COUNT(ea.id) >= 80
                ORDER BY success_rate DESC
                """;
            
            return jdbcTemplate.queryForList(sql, studentProfileId);
            
        } catch (Exception e) {
            logger.error("Error identificando áreas fuertes: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<String> generateStudyRecommendations(List<Map<String, Object>> weakAreas, 
                                                     List<Map<String, Object>> strongAreas) {
        List<String> recommendations = new ArrayList<>();
        
        if (!weakAreas.isEmpty()) {
            recommendations.add("Enfócate en reforzar: " + 
                weakAreas.stream()
                    .map(area -> area.get("learning_point_name").toString())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
        }
        
        if (!strongAreas.isEmpty()) {
            recommendations.add("Continúa destacando en: " + 
                strongAreas.stream()
                    .map(area -> area.get("learning_point_name").toString())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse(""));
        }
        
        if (weakAreas.isEmpty() && strongAreas.isEmpty()) {
            recommendations.add("Continúa practicando regularmente para mantener tu progreso");
        }
        
        recommendations.add("Dedica al menos 15 minutos diarios a la práctica");
        
        return recommendations;
    }

    // Enums y clases auxiliares
    
    public enum DifficultyLevel {
        EASY, MEDIUM, HARD, EXPERT
    }
    
    private static class StudentPerformanceAnalysis {
        private double successRate;
        private double averageTimeRatio;
        private DifficultyLevel currentDifficulty = DifficultyLevel.EASY;
        private int totalAttempts;
        private boolean isNewStudent = false;
        private String confidenceLevel = "LOW";
        private String difficultyAdjustment = "MAINTAIN";
        private String recommendationReason = "";
        
        // Getters and setters
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getAverageTimeRatio() { return averageTimeRatio; }
        public void setAverageTimeRatio(double averageTimeRatio) { this.averageTimeRatio = averageTimeRatio; }
        
        public DifficultyLevel getCurrentDifficulty() { return currentDifficulty; }
        public void setCurrentDifficulty(DifficultyLevel currentDifficulty) { this.currentDifficulty = currentDifficulty; }
        
        public int getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }
        
        public boolean isNewStudent() { return isNewStudent; }
        public void setNewStudent(boolean newStudent) { isNewStudent = newStudent; }
        
        public String getConfidenceLevel() { return confidenceLevel; }
        public void setConfidenceLevel(String confidenceLevel) { this.confidenceLevel = confidenceLevel; }
        
        public String getDifficultyAdjustment() { return difficultyAdjustment; }
        public void setDifficultyAdjustment(String difficultyAdjustment) { this.difficultyAdjustment = difficultyAdjustment; }
        
        public String getRecommendationReason() { return recommendationReason; }
        public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }
    }
} 