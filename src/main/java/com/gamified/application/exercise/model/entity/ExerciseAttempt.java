package com.gamified.application.exercise.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un intento de ejercicio por parte de un estudiante
 * 
 * Esquema real de la BD:
 * - id (int)
 * - exercise_template_id (int) - FK a la plantilla de ejercicio
 * - student_profile_id (int) - FK al estudiante
 * - attempt_number (int) - Número del intento
 * - is_correct (int) - Si es correcto (0/1)
 * - points_earned (int) - Puntos ganados
 * - time_spent (int) - Tiempo gastado
 * - started_at (datetime2) - Cuándo empezó
 * - completed_at (datetime2) - Cuándo terminó
 * - generated_exercise_id (bigint) - FK al ejercicio específico generado por IA
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAttempt {
    private Integer id;
    private Integer exerciseTemplateId; // Campo real: exercise_template_id (no exercise_id)
    private Integer studentProfileId; // Campo real: student_profile_id
    private Integer attemptNumber; // Campo real: attempt_number
    private Boolean isCorrect; // Campo real: is_correct (int convertido a Boolean)
    private Integer pointsEarned; // Campo real: points_earned
    private Integer timeSpent; // Campo real: time_spent (en segundos)
    private LocalDateTime startedAt; // Campo real: started_at
    private LocalDateTime completedAt; // Campo real: completed_at 
    private Long generatedExerciseId; // Campo real: generated_exercise_id - FK al ejercicio específico generado por IA
    
    // Relaciones virtuales
    private GeneratedExercise generatedExercise;
    private Exercise exerciseTemplate;
    
    // Métodos de compatibilidad para el código existente
    public Integer getExerciseId() {
        return exerciseTemplateId;
    }
    
    public void setExerciseId(Integer exerciseId) {
        this.exerciseTemplateId = exerciseId;
    }
    
    public Integer getTimeSpentSeconds() {
        return timeSpent;
    }
    
    public void setTimeSpentSeconds(Integer timeSpentSeconds) {
        this.timeSpent = timeSpentSeconds;
    }
    
    public LocalDateTime getSubmittedAt() {
        return completedAt;
    }
    
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.completedAt = submittedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return startedAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.startedAt = createdAt;
    }
    
    // Campos que no existen en el esquema real pero el código los usa
    public String getSubmittedAnswer() {
        // En el esquema real no hay submitted_answer
        return null;
    }
    
    public void setSubmittedAnswer(String submittedAnswer) {
        // No-op ya que el campo no existe en el esquema real
    }
    
    public Integer getHintsUsed() {
        // En el esquema real no hay hints_used
        return 0;
    }
    
    public void setHintsUsed(Integer hintsUsed) {
        // No-op ya que el campo no existe en el esquema real
    }
    
    public Double getScore() {
        // En el esquema real no hay score separado, se puede derivar de points_earned
        return pointsEarned != null ? pointsEarned.doubleValue() : null;
    }
    
    public void setScore(Double score) {
        // Mapear score a points_earned
        this.pointsEarned = score != null ? score.intValue() : null;
    }
    
    public String getFeedback() {
        // En el esquema real no hay feedback
        return null;
    }
    
    public void setFeedback(String feedback) {
        // No-op ya que el campo no existe en el esquema real
    }
} 