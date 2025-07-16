package com.gamified.application.learning.service;

import com.gamified.application.learning.model.entity.LearningPointExercise;
import com.gamified.application.learning.repository.LearningPointExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio para gestionar ejercicios asignados a learning points
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningPointExerciseServiceImpl implements LearningPointExerciseService {

    private final LearningPointExerciseRepository learningPointExerciseRepository;

    @Override
    @Transactional
    public Integer assignExerciseToLearningPoint(Integer learningPointId, Integer exerciseTemplateId, Integer sequenceOrder) {
        log.info("Asignando ejercicio template {} a learning point {} con orden {}", 
                exerciseTemplateId, learningPointId, sequenceOrder);
        
        LearningPointExercise assignment = LearningPointExercise.builder()
                .learningPointId(learningPointId)
                .exerciseTemplateId(exerciseTemplateId)
                .sequenceOrder(sequenceOrder)
                .isCompleted(0)
                .isActive(1)
                .assignedAt(LocalDateTime.now())
                .build();
        
        Integer assignmentId = learningPointExerciseRepository.assignExerciseToLearningPoint(assignment);
        
        if (assignmentId != null) {
            log.info("Ejercicio asignado exitosamente con ID: {}", assignmentId);
        } else {
            log.error("Error al asignar ejercicio template {} a learning point {}", exerciseTemplateId, learningPointId);
        }
        
        return assignmentId;
    }

    @Override
    @Transactional
    public Integer assignExercise(LearningPointExercise assignment) {
        log.info("Asignando ejercicio completo a learning point {} con template {} y orden {}", 
                assignment.getLearningPointId(), assignment.getExerciseTemplateId(), assignment.getSequenceOrder());
        
        Integer assignmentId = learningPointExerciseRepository.assignExerciseToLearningPoint(assignment);
        
        if (assignmentId != null) {
            log.info("Ejercicio asignado exitosamente con ID: {}", assignmentId);
        } else {
            log.error("Error al asignar ejercicio completo a learning point {}", assignment.getLearningPointId());
        }
        
        return assignmentId;
    }

    @Override
    @Transactional
    public boolean assignGeneratedExerciseToAssignment(Integer assignmentId, Long generatedExerciseId) {
        log.info("Asignando ejercicio generado {} a asignación {}", generatedExerciseId, assignmentId);
        
        boolean success = learningPointExerciseRepository.assignGeneratedExercise(assignmentId, generatedExerciseId);
        
        if (success) {
            log.info("Ejercicio generado asignado exitosamente");
        } else {
            log.error("Error al asignar ejercicio generado {} a asignación {}", generatedExerciseId, assignmentId);
        }
        
        return success;
    }

    @Override
    public Optional<LearningPointExercise> getNextPendingExercise(Integer learningPointId) {
        log.debug("Buscando siguiente ejercicio pendiente para learning point {}", learningPointId);
        
        return learningPointExerciseRepository.findNextPendingExercise(learningPointId);
    }

    @Override
    @Transactional
    public boolean markExerciseAsCompleted(Integer learningPointId, Integer exerciseTemplateId) {
        log.info("Marcando ejercicio template {} como completado en learning point {}", 
                exerciseTemplateId, learningPointId);
        
        boolean success = learningPointExerciseRepository.markAsCompleted(learningPointId, exerciseTemplateId);
        
        if (success) {
            log.info("Ejercicio marcado como completado exitosamente");
        } else {
            log.error("Error al marcar ejercicio como completado");
        }
        
        return success;
    }

    @Override
    public List<LearningPointExercise> getExercisesForLearningPoint(Integer learningPointId) {
        log.debug("Obteniendo ejercicios para learning point {}", learningPointId);
        
        return learningPointExerciseRepository.findByLearningPointId(learningPointId);
    }

    @Override
    public ProgressDto getLearningPointProgress(Integer learningPointId) {
        log.debug("Calculando progreso para learning point {}", learningPointId);
        
        Integer totalExercises = learningPointExerciseRepository.countTotalExercisesByLearningPoint(learningPointId);
        Integer completedExercises = learningPointExerciseRepository.countCompletedExercisesByLearningPoint(learningPointId);
        
        Double completionPercentage = totalExercises > 0 
            ? (completedExercises.doubleValue() / totalExercises.doubleValue()) * 100 
            : 0.0;
        
        Optional<LearningPointExercise> nextPending = getNextPendingExercise(learningPointId);
        
        return new ProgressDto(totalExercises, completedExercises, completionPercentage, nextPending);
    }

    @Override
    @Transactional
    public boolean reorderExercises(Integer learningPointId, List<Integer> exerciseTemplateIds) {
        log.info("Reordenando {} ejercicios para learning point {}", exerciseTemplateIds.size(), learningPointId);
        
        boolean success = learningPointExerciseRepository.reorderExercises(learningPointId, exerciseTemplateIds);
        
        if (success) {
            log.info("Ejercicios reordenados exitosamente");
        } else {
            log.error("Error al reordenar ejercicios para learning point {}", learningPointId);
        }
        
        return success;
    }

    /**
     * Asigna automáticamente ejercicios de una unidad a un learning point
     * Método de utilidad para inicializar ejercicios cuando se crea un learning point
     * 
     * @param learningPointId ID del learning point
     * @param unitId ID de la unidad
     * @return número de ejercicios asignados
     */
    @Transactional
    public Integer assignExercisesFromUnit(Integer learningPointId, Integer unitId) {
        log.info("Asignando ejercicios de la unidad {} al learning point {}", unitId, learningPointId);
        
        // TODO: Implementar cuando se tenga acceso al ExerciseRepository
        // Obtener ejercicios de la unidad y asignarlos secuencialmente
        
        log.warn("assignExercisesFromUnit no implementado completamente - se necesita integración con ExerciseRepository");
        return 0;
    }
} 