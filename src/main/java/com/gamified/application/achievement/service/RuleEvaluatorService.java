package com.gamified.application.achievement.service;

import com.gamified.application.exercise.repository.ExerciseRepository;
import com.gamified.application.shared.model.dto.engine.AchievementRuleDto;
import com.gamified.application.shared.model.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que evalúa reglas específicas contra el contexto del estudiante
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluatorService {

    private final ExerciseRepository exerciseRepository;

    /**
     * Evalúa una regla completa contra el contexto del estudiante
     */
    public AchievementRuleDto.RuleEvaluationResult evaluateRule(
            AchievementRuleDto.RuleSchema ruleSchema, 
            Integer studentProfileId, 
            DomainEvent.BaseDomainEvent triggerEvent) {
        
        log.debug("Evaluando regla {} para estudiante {}", ruleSchema.getRuleType(), studentProfileId);
        
        try {
            List<AchievementRuleDto.ConditionResult> conditionResults = new ArrayList<>();
            boolean allConditionsPassed = true;
            String failureReason = null;
            double totalCompletion = 0.0;
            
            // Evaluar cada condición en la regla
            for (AchievementRuleDto.RuleCondition condition : ruleSchema.getConditions()) {
                AchievementRuleDto.ConditionResult result = evaluateCondition(
                    condition, studentProfileId, triggerEvent);
                
                conditionResults.add(result);
                
                if (!result.getPassed()) {
                    allConditionsPassed = false;
                    if (failureReason == null) {
                        failureReason = result.getDescription();
                    }
                }
                
                // Calcular progreso (porcentaje de condiciones cumplidas)
                totalCompletion += result.getPassed() ? 1.0 : 0.0;
            }
            
            double completionPercentage = ruleSchema.getConditions().isEmpty() ? 0.0 :
                (totalCompletion / ruleSchema.getConditions().size()) * 100.0;
            
            return AchievementRuleDto.RuleEvaluationResult.builder()
                .passed(allConditionsPassed)
                .ruleName(ruleSchema.getRuleType())
                .ruleType(ruleSchema.getRuleType())
                .conditionResults(conditionResults)
                .contextData(createContextData(studentProfileId, triggerEvent))
                .failureReason(failureReason)
                .completionPercentage(completionPercentage)
                .build();
                
        } catch (Exception e) {
            log.error("Error evaluando regla: {}", e.getMessage(), e);
            return AchievementRuleDto.RuleEvaluationResult.builder()
                .passed(false)
                .failureReason("Error interno: " + e.getMessage())
                .completionPercentage(0.0)
                .build();
        }
    }

    /**
     * Evalúa una condición específica
     */
    private AchievementRuleDto.ConditionResult evaluateCondition(
            AchievementRuleDto.RuleCondition condition, 
            Integer studentProfileId, 
            DomainEvent.BaseDomainEvent triggerEvent) {
        
        try {
            switch (condition.getConditionType()) {
                case "EXERCISE":
                    return evaluateExerciseCondition((AchievementRuleDto.ExerciseCondition) condition, 
                        studentProfileId, triggerEvent);
                case "STREAK":
                    return evaluateStreakCondition((AchievementRuleDto.StreakCondition) condition, 
                        studentProfileId, triggerEvent);
                case "TIME":
                    return evaluateTimeCondition((AchievementRuleDto.TimeCondition) condition, 
                        studentProfileId, triggerEvent);
                case "PERFORMANCE":
                    return evaluatePerformanceCondition((AchievementRuleDto.PerformanceCondition) condition, 
                        studentProfileId, triggerEvent);
                case "COMPOSITE":
                    return evaluateCompositeCondition((AchievementRuleDto.CompositeCondition) condition, 
                        studentProfileId, triggerEvent);
                default:
                    log.warn("Tipo de condición desconocido: {}", condition.getConditionType());
                    return createFailedResult(condition.getConditionType(), 
                        "Tipo de condición no soportado: " + condition.getConditionType());
            }
        } catch (Exception e) {
            log.error("Error evaluando condición {}: {}", condition.getConditionType(), e.getMessage(), e);
            return createFailedResult(condition.getConditionType(), 
                "Error evaluando condición: " + e.getMessage());
        }
    }

    /**
     * Evalúa condición de ejercicios
     */
    private AchievementRuleDto.ConditionResult evaluateExerciseCondition(
            AchievementRuleDto.ExerciseCondition condition, 
            Integer studentProfileId, 
            DomainEvent.BaseDomainEvent triggerEvent) {
        
        try {
            // Obtener estadísticas del estudiante
            ExerciseRepository.StudentExerciseStats stats = exerciseRepository.getStudentExerciseStats(studentProfileId)
                .orElse(new ExerciseRepository.StudentExerciseStats());
            
            Map<String, Object> actualValues = new HashMap<>();
            Map<String, Object> requiredValues = new HashMap<>();
            
            // Evaluar según el criterio requerido
            Integer completedExercises = stats.getTotalExercisesCompleted() != null ? 
                stats.getTotalExercisesCompleted() : 0;
            
            actualValues.put("completedExercises", completedExercises);
            requiredValues.put("requiredCount", condition.getRequiredCount());
            
            boolean passed = completedExercises >= condition.getRequiredCount();
            
            // Filtros adicionales si están especificados
            if (condition.getDifficulty() != null && !condition.getDifficulty().isEmpty()) {
                // Aquí podrías agregar lógica para filtrar por dificultad
                requiredValues.put("difficulty", condition.getDifficulty());
            }
            
            if (condition.getMinimumAccuracy() != null) {
                Double averageScore = stats.getAverageScore() != null ? stats.getAverageScore() : 0.0;
                actualValues.put("averageAccuracy", averageScore);
                requiredValues.put("minimumAccuracy", condition.getMinimumAccuracy());
                
                if (averageScore < condition.getMinimumAccuracy()) {
                    passed = false;
                }
            }
            
            String description = passed ? 
                String.format("Completó %d ejercicios (requeridos: %d)", completedExercises, condition.getRequiredCount()) :
                String.format("Solo %d ejercicios completados, se requieren %d", completedExercises, condition.getRequiredCount());
            
            return AchievementRuleDto.ConditionResult.builder()
                .conditionType("EXERCISE")
                .passed(passed)
                .description(description)
                .actualValues(actualValues)
                .requiredValues(requiredValues)
                .build();
                
        } catch (Exception e) {
            log.error("Error evaluando condición de ejercicio: {}", e.getMessage(), e);
            return createFailedResult("EXERCISE", "Error accediendo datos de ejercicios");
        }
    }

    /**
     * Evalúa condición de racha
     */
    private AchievementRuleDto.ConditionResult evaluateStreakCondition(
            AchievementRuleDto.StreakCondition condition, 
            Integer studentProfileId, 
            DomainEvent.BaseDomainEvent triggerEvent) {
        
        // Implementación básica - expandir según necesidades
        Map<String, Object> actualValues = new HashMap<>();
        Map<String, Object> requiredValues = new HashMap<>();
        
        // Simulación de racha actual (implementar consulta real)
        Integer currentStreak = getCurrentStreak(studentProfileId, condition.getStreakType());
        
        actualValues.put("currentStreak", currentStreak);
        requiredValues.put("requiredStreakLength", condition.getRequiredStreakLength());
        
        boolean passed = currentStreak >= condition.getRequiredStreakLength();
        String description = passed ? 
            String.format("Racha actual: %d días (requerida: %d)", currentStreak, condition.getRequiredStreakLength()) :
            String.format("Racha actual: %d días, se requieren %d", currentStreak, condition.getRequiredStreakLength());
        
        return AchievementRuleDto.ConditionResult.builder()
            .conditionType("STREAK")
            .passed(passed)
            .description(description)
            .actualValues(actualValues)
            .requiredValues(requiredValues)
            .build();
    }

    private AchievementRuleDto.ConditionResult evaluateTimeCondition(
            AchievementRuleDto.TimeCondition condition, 
            Integer studentProfileId, 
            DomainEvent.BaseDomainEvent triggerEvent) {
        
        // Implementación básica para condiciones de tiempo
        return createFailedResult("TIME", "Condiciones de tiempo no implementadas aún");
    }

    private AchievementRuleDto.ConditionResult evaluatePerformanceCondition(
            AchievementRuleDto.PerformanceCondition condition, 
            Integer studentProfileId, 
            DomainEvent.BaseDomainEvent triggerEvent) {
        
        // Implementación básica para condiciones de rendimiento
        return createFailedResult("PERFORMANCE", "Condiciones de rendimiento no implementadas aún");
    }

    private AchievementRuleDto.ConditionResult evaluateCompositeCondition(
            AchievementRuleDto.CompositeCondition condition, 
            Integer studentProfileId, 
            DomainEvent.BaseDomainEvent triggerEvent) {
        
        // Implementación básica para condiciones compuestas
        return createFailedResult("COMPOSITE", "Condiciones compuestas no implementadas aún");
    }

    /**
     * Obtiene la racha actual del estudiante (implementación temporal)
     */
    private Integer getCurrentStreak(Integer studentProfileId, String streakType) {
        // TODO: Implementar consulta real a base de datos
        return 0; // Temporal
    }

    /**
     * Crea datos de contexto para la evaluación
     */
    private Map<String, Object> createContextData(Integer studentProfileId, DomainEvent.BaseDomainEvent triggerEvent) {
        Map<String, Object> context = new HashMap<>();
        context.put("studentProfileId", studentProfileId);
        context.put("evaluatedAt", LocalDateTime.now());
        context.put("triggerEventType", triggerEvent.getEventType());
        return context;
    }

    /**
     * Crea un resultado fallido
     */
    private AchievementRuleDto.ConditionResult createFailedResult(String conditionType, String description) {
        return AchievementRuleDto.ConditionResult.builder()
            .conditionType(conditionType)
            .passed(false)
            .description(description)
            .actualValues(new HashMap<>())
            .requiredValues(new HashMap<>())
            .build();
    }
} 