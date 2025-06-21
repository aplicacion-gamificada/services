package com.gamified.application.report.model.entity;

import java.time.LocalDateTime;

/**
 * Entidad para reportes de estudiantes
 * Fase 4: Módulo de Reportes y Analytics
 */
public class StudentReport {
    private Integer id;
    private Integer studentProfileId;
    private String studentUsername;
    private String reportType; // DAILY, WEEKLY, MONTHLY, CUSTOM
    private LocalDateTime reportPeriodStart;
    private LocalDateTime reportPeriodEnd;
    
    // Métricas de ejercicios
    private Integer totalExercisesCompleted;
    private Integer correctExercises;
    private Double successRate;
    private Double averageTimePerExercise;
    private Integer totalPointsEarned;
    
    // Métricas de logros
    private Integer achievementsUnlocked;
    private Integer totalAchievementPoints;
    
    // Métricas de progreso
    private Integer activeDays;
    private Integer currentStreak;
    private Integer longestStreak;
    
    // Métricas de learning points
    private Integer learningPointsCompleted;
    private Integer learningPointsInProgress;
    
    // Alertas y rendimiento
    private String alertLevel; // NONE, LOW, MEDIUM, HIGH
    private String alertReason;
    private Double overallPerformanceScore;
    
    // Comparativas
    private Double classAverage;
    private Integer classRanking;
    private Integer totalClassmates;
    
    // Metadata
    private LocalDateTime generatedAt;
    private LocalDateTime lastUpdated;
    private Boolean isActive;

    // Constructors
    public StudentReport() {}

    public StudentReport(Integer studentProfileId, String reportType, 
                        LocalDateTime reportPeriodStart, LocalDateTime reportPeriodEnd) {
        this.studentProfileId = studentProfileId;
        this.reportType = reportType;
        this.reportPeriodStart = reportPeriodStart;
        this.reportPeriodEnd = reportPeriodEnd;
        this.generatedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.isActive = true;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudentProfileId() {
        return studentProfileId;
    }

    public void setStudentProfileId(Integer studentProfileId) {
        this.studentProfileId = studentProfileId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDateTime getReportPeriodStart() {
        return reportPeriodStart;
    }

    public void setReportPeriodStart(LocalDateTime reportPeriodStart) {
        this.reportPeriodStart = reportPeriodStart;
    }

    public LocalDateTime getReportPeriodEnd() {
        return reportPeriodEnd;
    }

    public void setReportPeriodEnd(LocalDateTime reportPeriodEnd) {
        this.reportPeriodEnd = reportPeriodEnd;
    }

    public Integer getTotalExercisesCompleted() {
        return totalExercisesCompleted;
    }

    public void setTotalExercisesCompleted(Integer totalExercisesCompleted) {
        this.totalExercisesCompleted = totalExercisesCompleted;
    }

    public Integer getCorrectExercises() {
        return correctExercises;
    }

    public void setCorrectExercises(Integer correctExercises) {
        this.correctExercises = correctExercises;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public Double getAverageTimePerExercise() {
        return averageTimePerExercise;
    }

    public void setAverageTimePerExercise(Double averageTimePerExercise) {
        this.averageTimePerExercise = averageTimePerExercise;
    }

    public Integer getTotalPointsEarned() {
        return totalPointsEarned;
    }

    public void setTotalPointsEarned(Integer totalPointsEarned) {
        this.totalPointsEarned = totalPointsEarned;
    }

    public Integer getAchievementsUnlocked() {
        return achievementsUnlocked;
    }

    public void setAchievementsUnlocked(Integer achievementsUnlocked) {
        this.achievementsUnlocked = achievementsUnlocked;
    }

    public Integer getTotalAchievementPoints() {
        return totalAchievementPoints;
    }

    public void setTotalAchievementPoints(Integer totalAchievementPoints) {
        this.totalAchievementPoints = totalAchievementPoints;
    }

    public Integer getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(Integer activeDays) {
        this.activeDays = activeDays;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }

    public Integer getLearningPointsCompleted() {
        return learningPointsCompleted;
    }

    public void setLearningPointsCompleted(Integer learningPointsCompleted) {
        this.learningPointsCompleted = learningPointsCompleted;
    }

    public Integer getLearningPointsInProgress() {
        return learningPointsInProgress;
    }

    public void setLearningPointsInProgress(Integer learningPointsInProgress) {
        this.learningPointsInProgress = learningPointsInProgress;
    }

    public String getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(String alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getAlertReason() {
        return alertReason;
    }

    public void setAlertReason(String alertReason) {
        this.alertReason = alertReason;
    }

    public Double getOverallPerformanceScore() {
        return overallPerformanceScore;
    }

    public void setOverallPerformanceScore(Double overallPerformanceScore) {
        this.overallPerformanceScore = overallPerformanceScore;
    }

    public Double getClassAverage() {
        return classAverage;
    }

    public void setClassAverage(Double classAverage) {
        this.classAverage = classAverage;
    }

    public Integer getClassRanking() {
        return classRanking;
    }

    public void setClassRanking(Integer classRanking) {
        this.classRanking = classRanking;
    }

    public Integer getTotalClassmates() {
        return totalClassmates;
    }

    public void setTotalClassmates(Integer totalClassmates) {
        this.totalClassmates = totalClassmates;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Business Methods
    
    /**
     * Calcula la puntuación de rendimiento general basada en múltiples métricas
     */
    public void calculateOverallPerformanceScore() {
        if (totalExercisesCompleted == null || totalExercisesCompleted == 0) {
            this.overallPerformanceScore = 0.0;
            return;
        }
        
        double score = 0.0;
        
        // Factor 1: Tasa de éxito (40% del peso)
        if (successRate != null) {
            score += successRate * 0.4;
        }
        
        // Factor 2: Consistencia/Streak (30% del peso)
        if (currentStreak != null && currentStreak > 0) {
            double streakScore = Math.min(currentStreak / 7.0, 1.0); // Máximo 7 días
            score += streakScore * 0.3;
        }
        
        // Factor 3: Volumen de actividad (20% del peso)
        if (totalExercisesCompleted != null) {
            double activityScore = Math.min(totalExercisesCompleted / 20.0, 1.0); // Máximo 20 ejercicios
            score += activityScore * 0.2;
        }
        
        // Factor 4: Logros (10% del peso)
        if (achievementsUnlocked != null && achievementsUnlocked > 0) {
            double achievementScore = Math.min(achievementsUnlocked / 5.0, 1.0); // Máximo 5 logros
            score += achievementScore * 0.1;
        }
        
        this.overallPerformanceScore = Math.round(score * 100.0) / 100.0; // Redondear a 2 decimales
    }
    
    /**
     * Determina el nivel de alerta basado en las métricas
     */
    public void calculateAlertLevel() {
        if (successRate == null || totalExercisesCompleted == null) {
            this.alertLevel = "NONE";
            return;
        }
        
        // Criterios de alerta
        boolean lowSuccess = successRate < 0.6; // Menos del 60% de aciertos
        boolean lowActivity = totalExercisesCompleted < 5; // Menos de 5 ejercicios en el periodo
        boolean noStreak = currentStreak == null || currentStreak == 0; // Sin racha activa
        
        if (lowSuccess && lowActivity) {
            this.alertLevel = "HIGH";
            this.alertReason = "Baja tasa de éxito y poca actividad";
        } else if (lowSuccess) {
            this.alertLevel = "MEDIUM";
            this.alertReason = "Tasa de éxito por debajo del promedio";
        } else if (lowActivity && noStreak) {
            this.alertLevel = "LOW";
            this.alertReason = "Actividad baja sin constancia";
        } else {
            this.alertLevel = "NONE";
            this.alertReason = null;
        }
    }

    @Override
    public String toString() {
        return "StudentReport{" +
                "id=" + id +
                ", studentProfileId=" + studentProfileId +
                ", studentUsername='" + studentUsername + '\'' +
                ", reportType='" + reportType + '\'' +
                ", successRate=" + successRate +
                ", overallPerformanceScore=" + overallPerformanceScore +
                ", alertLevel='" + alertLevel + '\'' +
                '}';
    }
} 