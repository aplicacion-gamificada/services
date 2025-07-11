package com.gamified.application.progress.repository;

import com.gamified.application.progress.model.entity.LearningPath;
import com.gamified.application.progress.model.entity.LessonProgress;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface para operaciones del módulo Progress
 */
public interface ProgressRepository {
    
    // ===================================================================
    // LEARNING PATH OPERATIONS
    // ===================================================================
    
    /**
     * Busca el learning path activo de un estudiante
     * @param studentProfileId ID del perfil de estudiante
     * @return Learning path activo del estudiante
     */
    Optional<LearningPath> findActiveLearningPathByStudent(Integer studentProfileId);
    
    /**
     * Crea un nuevo learning path para un estudiante
     * @param learningPath Datos del learning path a crear
     * @return Learning path creado con ID asignado
     */
    LearningPath createLearningPath(LearningPath learningPath);
    
    /**
     * Actualiza un learning path existente
     * @param learningPath Learning path a actualizar
     * @return Learning path actualizado
     */
    LearningPath updateLearningPath(LearningPath learningPath);
    
    /**
     * Busca un learning point por ID
     * @param learningPointId ID del learning point
     * @return Learning point encontrado con título
     */
    Optional<LearningPointInfo> findLearningPointById(Integer learningPointId);
    
    /**
     * Busca una unidad por ID
     * @param unitId ID de la unidad
     * @return Unidad encontrada con título
     */
    Optional<UnitInfo> findUnitById(Integer unitId);
    
    // ===================================================================
    // LESSON PROGRESS OPERATIONS
    // ===================================================================
    
    /**
     * Obtiene el progreso de lecciones para un estudiante en un learning point
     * @param studentProfileId ID del perfil de estudiante
     * @param learningPointId ID del learning point
     * @return Lista de progreso de lecciones
     */
    List<LessonProgress> findLessonProgressByStudentAndLearningPoint(
            Integer studentProfileId, Integer learningPointId);
    
    /**
     * Marca una lección como completada
     * @param lessonProgress Progreso de la lección a marcar
     * @return Progreso actualizado
     */
    LessonProgress markLessonAsCompleted(LessonProgress lessonProgress);
    
    /**
     * Verifica si una lección está completada por un estudiante
     * @param studentProfileId ID del perfil de estudiante
     * @param lessonId ID de la lección
     * @return true si está completada, false en caso contrario
     */
    boolean isLessonCompleted(Integer studentProfileId, Integer lessonId);
    
    /**
     * Busca información de una lección por ID
     * @param lessonId ID de la lección
     * @return Información de la lección
     */
    Optional<LessonInfo> findLessonById(Integer lessonId);
    
    // ===================================================================
    // NAVIGATION OPERATIONS
    // ===================================================================
    
    /**
     * Busca el siguiente learning point en secuencia
     * @param currentLearningPointId ID del learning point actual
     * @param unitId ID de la unidad
     * @return Siguiente learning point disponible
     */
    Optional<LearningPointInfo> findNextLearningPoint(Integer currentLearningPointId, Integer unitId);
    
    /**
     * Cuenta el total de lecciones en un learning point
     * @param learningPointId ID del learning point
     * @return Total de lecciones
     */
    Integer countLessonsByLearningPoint(Integer learningPointId);
    
    /**
     * Cuenta lecciones completadas por un estudiante en un learning point
     * @param studentProfileId ID del perfil de estudiante
     * @param learningPointId ID del learning point
     * @return Total de lecciones completadas
     */
    Integer countCompletedLessonsByStudentAndLearningPoint(
            Integer studentProfileId, Integer learningPointId);
    
    /**
     * Obtiene todas las lecciones de un learning point
     * @param learningPointId ID del learning point
     * @return Lista de información de lecciones
     */
    List<LessonInfo> findLessonsByLearningPoint(Integer learningPointId);
    
    // ===================================================================
    // STATISTICS OPERATIONS
    // ===================================================================
    
    /**
     * Cuenta el total de learning points en una unidad
     * @param unitId ID de la unidad
     * @return Total de learning points
     */
    Integer countLearningPointsByUnit(Integer unitId);
    
    /**
     * Cuenta learning points completados por un estudiante en una unidad
     * @param studentProfileId ID del perfil de estudiante
     * @param unitId ID de la unidad
     * @return Total de learning points completados
     */
    Integer countCompletedLearningPointsByStudentAndUnit(
            Integer studentProfileId, Integer unitId);
    
    // ===================================================================
    // INNER CLASSES FOR DATA TRANSFER
    // ===================================================================
    
    /**
     * Información básica de un learning point
     */
    class LearningPointInfo {
        private Integer id;
        private String title;
        private String description;
        private Integer sequenceOrder;
        private Integer estimatedDuration;
        private java.math.BigDecimal difficultyWeight;
        private String unlockCriteria;
        private Integer learningPathId;
        
        // Constructor, getters y setters
        public LearningPointInfo() {}
        
        public LearningPointInfo(Integer id, String title, String description, 
                               Integer sequenceOrder, Integer estimatedDuration,
                               java.math.BigDecimal difficultyWeight, String unlockCriteria, Integer learningPathId) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.sequenceOrder = sequenceOrder;
            this.estimatedDuration = estimatedDuration;
            this.difficultyWeight = difficultyWeight;
            this.unlockCriteria = unlockCriteria;
            this.learningPathId = learningPathId;
        }
        
        // Getters y setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getSequenceOrder() { return sequenceOrder; }
        public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
        public Integer getEstimatedDuration() { return estimatedDuration; }
        public void setEstimatedDuration(Integer estimatedDuration) { this.estimatedDuration = estimatedDuration; }
        public java.math.BigDecimal getDifficultyWeight() { return difficultyWeight; }
        public void setDifficultyWeight(java.math.BigDecimal difficultyWeight) { this.difficultyWeight = difficultyWeight; }
        public String getUnlockCriteria() { return unlockCriteria; }
        public void setUnlockCriteria(String unlockCriteria) { this.unlockCriteria = unlockCriteria; }
        public Integer getLearningPathId() { return learningPathId; }
        public void setLearningPathId(Integer learningPathId) { this.learningPathId = learningPathId; }
    }
    
    /**
     * Información básica de una lección
     */
    class LessonInfo {
        private Integer id;
        private String title;
        private Integer sequenceOrder;
        private Integer learningPointId;
        private boolean isMandatory;
        
        // Constructor, getters y setters
        public LessonInfo() {}
        
        public LessonInfo(Integer id, String title, Integer sequenceOrder, 
                         Integer learningPointId, boolean isMandatory) {
            this.id = id;
            this.title = title;
            this.sequenceOrder = sequenceOrder;
            this.learningPointId = learningPointId;
            this.isMandatory = isMandatory;
        }
        
        // Getters y setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public Integer getSequenceOrder() { return sequenceOrder; }
        public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }
        public Integer getLearningPointId() { return learningPointId; }
        public void setLearningPointId(Integer learningPointId) { this.learningPointId = learningPointId; }
        public boolean isMandatory() { return isMandatory; }
        public void setMandatory(boolean mandatory) { isMandatory = mandatory; }
    }
    
    /**
     * Información básica de una unidad
     */
    class UnitInfo {
        private Integer id;
        private String title;
        
        // Constructor, getters y setters
        public UnitInfo() {}
        
        public UnitInfo(Integer id, String title) {
            this.id = id;
            this.title = title;
        }
        
        // Getters y setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }
} 