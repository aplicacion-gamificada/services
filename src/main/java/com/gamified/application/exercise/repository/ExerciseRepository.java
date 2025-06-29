package com.gamified.application.exercise.repository;

import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.ExerciseAttempt;
import com.gamified.application.exercise.model.entity.ExerciseType;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de Exercise
 */
public interface ExerciseRepository {
    
    // ===================================================================
    // EXERCISE TYPES
    // ===================================================================
    
    /**
     * Obtiene todos los tipos de ejercicio activos
     */
    List<ExerciseType> findAllActiveExerciseTypes();
    
    /**
     * Busca un tipo de ejercicio por ID
     */
    Optional<ExerciseType> findExerciseTypeById(Integer exerciseTypeId);
    
    // ===================================================================
    // EXERCISES
    // ===================================================================
    
    /**
     * Genera o busca el siguiente ejercicio para un learning point
     */
    Optional<Exercise> findNextExerciseForLearningPoint(Integer studentProfileId, Integer learningPointId, String difficulty);
    
    /**
     * Busca un ejercicio por ID
     */
    Optional<Exercise> findExerciseById(Integer exerciseId);
    
    /**
     * Crea un nuevo ejercicio (para IA)
     */
    Integer createExercise(Exercise exercise);
    
    /**
     * Obtiene ejercicios por learning point
     */
    List<Exercise> findExercisesByLearningPoint(Integer learningPointId);
    
    /**
     * Cuenta ejercicios completados por estudiante en un learning point
     */
    Integer countCompletedExercisesByStudentAndLearningPoint(Integer studentProfileId, Integer learningPointId);
    
    // ===================================================================
    // EXERCISE ATTEMPTS
    // ===================================================================
    
    /**
     * Crea un nuevo intento de ejercicio
     */
    Integer createExerciseAttempt(ExerciseAttempt attempt);
    
    /**
     * Obtiene intentos de un estudiante para un ejercicio específico
     */
    List<ExerciseAttempt> findAttemptsByStudentAndExercise(Integer studentProfileId, Integer exerciseId);
    
    /**
     * Obtiene el historial de intentos de un estudiante
     */
    List<ExerciseAttempt> findAttemptHistoryByStudent(Integer studentProfileId, Integer limit);
    
    /**
     * Obtiene ejercicios completados por un estudiante
     */
    List<Exercise> findCompletedExercisesByStudent(Integer studentProfileId);
    
    /**
     * Verifica si un ejercicio está completado por el estudiante
     */
    Boolean isExerciseCompletedByStudent(Integer studentProfileId, Integer exerciseId);
    
    /**
     * Obtiene la mejor puntuación de un estudiante en un ejercicio
     */
    Optional<Double> getBestScoreByStudentAndExercise(Integer studentProfileId, Integer exerciseId);
    
    /**
     * Cuenta total de intentos de un estudiante en un ejercicio
     */
    Integer countAttemptsByStudentAndExercise(Integer studentProfileId, Integer exerciseId);
    
    /**
     * Cuenta total de intentos de un estudiante en una plantilla de ejercicio
     */
    Integer countAttemptsByStudentAndTemplate(Integer studentProfileId, Integer exerciseTemplateId);
    
    // ===================================================================
    // ANALYTICS & STATISTICS
    // ===================================================================
    
    /**
     * Obtiene estadísticas generales de ejercicios del estudiante
     */
    Optional<StudentExerciseStats> getStudentExerciseStats(Integer studentProfileId);
    
    /**
     * Obtiene estadísticas por tipo de ejercicio del estudiante
     */
    List<ExerciseTypeStats> getStudentExerciseTypeStats(Integer studentProfileId);
    
    /**
     * Cuenta intentos recientes de un estudiante en un learning point
     */
    Integer countRecentAttemptsByStudentAndLearningPoint(Integer studentId, Integer learningPointId, int days);
    
    // ===================================================================
    // HELPER CLASSES FOR STATISTICS
    // ===================================================================
    
    /**
     * Clase para estadísticas generales del estudiante
     */
    class StudentExerciseStats {
        public Integer totalExercisesAttempted;
        public Integer totalExercisesCompleted;
        public Double averageScore;
        public Integer totalTimeSpentMinutes;
        public String preferredDifficulty;
        public Integer getTotalExercisesCompleted() {
            return totalExercisesCompleted;
        }
        public void setTotalExercisesCompleted(Integer totalExercisesCompleted) {
            this.totalExercisesCompleted = totalExercisesCompleted;
        }
        public Double getAverageScore() {
            return averageScore;
        }
        public String getPreferredDifficulty() {
            return preferredDifficulty;
        }
        public void setPreferredDifficulty(String preferredDifficulty) {
            this.preferredDifficulty = preferredDifficulty;
        }
    }
    
    /**
     * Clase para estadísticas por tipo de ejercicio
     */
    class ExerciseTypeStats {
        public Integer exerciseTypeId;
        public String exerciseTypeName;
        public Integer totalAttempts;
        public Integer totalCompleted;
        public Double averageScore;
        public String strongestDifficulty;
    }
} 