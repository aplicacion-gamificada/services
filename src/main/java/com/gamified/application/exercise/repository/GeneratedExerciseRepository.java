package com.gamified.application.exercise.repository;

import com.gamified.application.exercise.model.entity.GeneratedExercise;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de base de datos con ejercicios generados por IA
 */
public interface GeneratedExerciseRepository {

    /**
     * Guarda un nuevo ejercicio generado en la base de datos
     * @param generatedExercise Ejercicio a guardar
     * @return ID del ejercicio generado guardado
     */
    Long save(GeneratedExercise generatedExercise);

    /**
     * Busca un ejercicio generado por su ID
     * @param id ID del ejercicio generado
     * @return Optional con el ejercicio si existe
     */
    Optional<GeneratedExercise> findById(Long id);

    /**
     * Busca ejercicios generados disponibles (no utilizados) para una plantilla específica
     * @param exerciseTemplateId ID de la plantilla de ejercicio
     * @param limit Número máximo de ejercicios a retornar
     * @return Lista de ejercicios disponibles del pool
     */
    List<GeneratedExercise> findAvailableByTemplate(Integer exerciseTemplateId, Integer limit);

    /**
     * Marca un ejercicio generado como utilizado (vinculándolo a un intento)
     * @param generatedExerciseId ID del ejercicio generado
     * @param attemptId ID del intento que lo utilizó
     */
    void markAsUsed(Long generatedExerciseId, Integer attemptId);

    /**
     * Obtiene ejercicios generados para una plantilla específica con paginación
     * @param exerciseTemplateId ID de la plantilla
     * @param offset Desplazamiento para paginación
     * @param limit Límite de resultados
     * @return Lista de ejercicios generados
     */
    List<GeneratedExercise> findByTemplateWithPagination(Integer exerciseTemplateId, Integer offset, Integer limit);

    /**
     * Cuenta ejercicios generados por plantilla
     * @param exerciseTemplateId ID de la plantilla
     * @return Número total de ejercicios generados para esa plantilla
     */
    Integer countByTemplate(Integer exerciseTemplateId);

    /**
     * Obtiene estadísticas de uso del pool de ejercicios
     * @return Estadísticas de cache/pool
     */
    PoolStats getPoolStats();

    /**
     * Busca plantillas de ejercicios más demandadas para pre-generación
     * @param limit Número de plantillas a retornar
     * @return Lista de IDs de plantillas ordenadas por demanda
     */
    List<Integer> findMostDemandedTemplates(Integer limit);

    /**
     * Elimina ejercicios generados antiguos para limpieza
     * @param daysOld Días de antigüedad para eliminar
     * @return Número de ejercicios eliminados
     */
    Integer deleteOldExercises(Integer daysOld);

    /**
     * Clase para estadísticas del pool de ejercicios
     */
    class PoolStats {
        private Integer totalGenerated;
        private Integer totalUsed;
        private Integer availableInPool;
        private Double cacheHitRate;
        private Double averageGenerationTimeMs;

        // Constructors, getters, setters
        public PoolStats() {}

        public PoolStats(Integer totalGenerated, Integer totalUsed, Integer availableInPool, 
                        Double cacheHitRate, Double averageGenerationTimeMs) {
            this.totalGenerated = totalGenerated;
            this.totalUsed = totalUsed;
            this.availableInPool = availableInPool;
            this.cacheHitRate = cacheHitRate;
            this.averageGenerationTimeMs = averageGenerationTimeMs;
        }

        // Getters and setters
        public Integer getTotalGenerated() { return totalGenerated; }
        public void setTotalGenerated(Integer totalGenerated) { this.totalGenerated = totalGenerated; }

        public Integer getTotalUsed() { return totalUsed; }
        public void setTotalUsed(Integer totalUsed) { this.totalUsed = totalUsed; }

        public Integer getAvailableInPool() { return availableInPool; }
        public void setAvailableInPool(Integer availableInPool) { this.availableInPool = availableInPool; }

        public Double getCacheHitRate() { return cacheHitRate; }
        public void setCacheHitRate(Double cacheHitRate) { this.cacheHitRate = cacheHitRate; }

        public Double getAverageGenerationTimeMs() { return averageGenerationTimeMs; }
        public void setAverageGenerationTimeMs(Double averageGenerationTimeMs) { this.averageGenerationTimeMs = averageGenerationTimeMs; }
    }
}
