package com.gamified.application.achievement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamified.application.achievement.model.Achievement;
import com.gamified.application.achievement.repository.IAchievementRepository;
import com.gamified.application.notification.service.NotificationService;
import com.gamified.application.shared.model.dto.engine.AchievementRuleDto;
import com.gamified.application.shared.model.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Motor de evaluación de reglas de logros
 * Procesa eventos de dominio y evalúa si se deben desbloquear logros
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementEngineService {

    private final IAchievementRepository achievementRepository;
    private final ObjectMapper objectMapper;
    private final RuleEvaluatorService ruleEvaluatorService;
    private final NotificationService notificationService;

    /**
     * Procesa evento de ejercicio completado y evalúa logros aplicables
     */
    @EventListener
    public void handleExerciseCompleted(DomainEvent.ExerciseCompletedEvent event) {
        log.info("Procesando ejercicio completado para estudiante {}", event.getStudentProfileId());
        
        try {
            // Obtener todos los logros activos del sistema
            List<Map<String, Object>> achievements = achievementRepository.getAchievements();
            
            // Evaluar cada logro contra el evento
            for (Map<String, Object> achievementData : achievements) {
                evaluateAchievementForEvent(achievementData, event);
            }
            
        } catch (Exception e) {
            log.error("Error procesando ejercicio completado: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa evento de racha actualizada
     */
    @EventListener
    public void handleStreakUpdated(DomainEvent.StreakUpdatedEvent event) {
        log.info("Procesando racha actualizada para estudiante {}", event.getStudentProfileId());
        
        try {
            List<Map<String, Object>> achievements = achievementRepository.getAchievements();
            
            for (Map<String, Object> achievementData : achievements) {
                evaluateAchievementForEvent(achievementData, event);
            }
            
        } catch (Exception e) {
            log.error("Error procesando racha actualizada: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa evento de learning point completado
     */
    @EventListener
    public void handleLearningPointCompleted(DomainEvent.LearningPointCompletedEvent event) {
        log.info("Procesando learning point completado para estudiante {}", event.getStudentProfileId());
        
        try {
            List<Map<String, Object>> achievements = achievementRepository.getAchievements();
            
            for (Map<String, Object> achievementData : achievements) {
                evaluateAchievementForEvent(achievementData, event);
            }
            
        } catch (Exception e) {
            log.error("Error procesando learning point completado: {}", e.getMessage(), e);
        }
    }

    /**
     * Evalúa un logro específico contra un evento
     */
    private void evaluateAchievementForEvent(Map<String, Object> achievementData, DomainEvent.BaseDomainEvent event) {
        try {
            Integer achievementId = (Integer) achievementData.get("id");
            String triggerRule = (String) achievementData.get("trigger_rule");
            Integer studentProfileId = extractStudentProfileId(event);
            
            if (triggerRule == null || triggerRule.trim().isEmpty()) {
                log.debug("Logro {} no tiene reglas definidas", achievementId);
                return;
            }

            // Verificar si el estudiante ya tiene este logro
            if (studentAlreadyHasAchievement(studentProfileId, achievementId)) {
                log.debug("Estudiante {} ya tiene el logro {}", studentProfileId, achievementId);
                return;
            }

            // Parsear y evaluar la regla
            AchievementRuleDto.RuleSchema ruleSchema = parseRuleFromJson(triggerRule);
            if (ruleSchema == null) {
                log.warn("No se pudo parsear la regla del logro {}: {}", achievementId, triggerRule);
                return;
            }

            // Evaluar la regla contra el contexto del estudiante
            AchievementRuleDto.RuleEvaluationResult result = ruleEvaluatorService.evaluateRule(
                ruleSchema, studentProfileId, event);

            if (result.getPassed()) {
                // Desbloquear el logro
                unlockAchievement(studentProfileId, achievementId, achievementData, result);
            } else {
                log.debug("Regla del logro {} no cumplida para estudiante {}: {}", 
                    achievementId, studentProfileId, result.getFailureReason());
            }

        } catch (Exception e) {
            log.error("Error evaluando logro: {}", e.getMessage(), e);
        }
    }

    /**
     * Desbloquea un logro para un estudiante
     */
    private void unlockAchievement(Integer studentProfileId, Integer achievementId, 
                                 Map<String, Object> achievementData, 
                                 AchievementRuleDto.RuleEvaluationResult evaluationResult) {
        try {
            Integer pointsValue = (Integer) achievementData.get("points_value");
            String achievementName = (String) achievementData.get("achievement_name");
            
            log.info("Desbloqueando logro {} '{}' para estudiante {}", 
                achievementId, achievementName, studentProfileId);

            // Llamar al repositorio para desbloquear
            Map<String, Object> result = achievementRepository.unlockAchievement(
                studentProfileId, achievementId, pointsValue);

            String resultStatus = (String) result.get("result");
            if ("SUCCESS".equals(resultStatus)) {
                log.info("Logro {} desbloqueado exitosamente para estudiante {}", 
                    achievementId, studentProfileId);
                
                // Emitir evento de logro desbloqueado (para posibles notificaciones)
                emitAchievementUnlockedEvent(studentProfileId, achievementData, evaluationResult);
            } else {
                log.warn("Error desbloqueando logro {}: {}", achievementId, result.get("message"));
            }

        } catch (Exception e) {
            log.error("Error desbloqueando logro {}: {}", achievementId, e.getMessage(), e);
        }
    }

    /**
     * Emite un evento de logro desbloqueado y envía notificación
     */
    private void emitAchievementUnlockedEvent(Integer studentProfileId, Map<String, Object> achievementData,
                                            AchievementRuleDto.RuleEvaluationResult evaluationResult) {
        try {
            Integer achievementId = (Integer) achievementData.get("id");
            String achievementName = (String) achievementData.get("achievement_name");
            Integer pointsValue = (Integer) achievementData.get("points_value");
            
            log.info("Enviando notificación de logro desbloqueado: {} para estudiante {}", 
                    achievementName, studentProfileId);
            
            // Obtener el user_id del student_profile_id
            Integer userId = getUserIdFromStudentProfile(studentProfileId);
            
            if (userId != null) {
                // Enviar notificación automáticamente
                notificationService.sendAchievementUnlockedNotification(
                    userId, achievementName, achievementId, pointsValue
                );
                
                log.info("Notificación de logro enviada exitosamente a usuario {}", userId);
            } else {
                log.warn("No se pudo obtener user_id para student_profile_id {}", studentProfileId);
            }
            
        } catch (Exception e) {
            log.error("Error enviando notificación de logro desbloqueado: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Obtiene el user_id a partir del student_profile_id
     */
    private Integer getUserIdFromStudentProfile(Integer studentProfileId) {
        try {
            // Esta consulta debería existir en el repositorio, pero por simplicidad la implementamos aquí
            return achievementRepository.getUserIdFromStudentProfile(studentProfileId);
        } catch (Exception e) {
            log.error("Error obteniendo user_id para student_profile_id {}: {}", studentProfileId, e.getMessage());
            return null;
        }
    }

    /**
     * Extrae el ID del perfil de estudiante del evento
     */
    private Integer extractStudentProfileId(DomainEvent.BaseDomainEvent event) {
        if (event instanceof DomainEvent.ExerciseCompletedEvent) {
            return ((DomainEvent.ExerciseCompletedEvent) event).getStudentProfileId();
        } else if (event instanceof DomainEvent.StreakUpdatedEvent) {
            return ((DomainEvent.StreakUpdatedEvent) event).getStudentProfileId();
        } else if (event instanceof DomainEvent.LearningPointCompletedEvent) {
            return ((DomainEvent.LearningPointCompletedEvent) event).getStudentProfileId();
        }
        return event.getUserId();
    }

    /**
     * Verifica si el estudiante ya tiene el logro
     */
    private boolean studentAlreadyHasAchievement(Integer studentProfileId, Integer achievementId) {
        try {
            // TODO: Implementar consulta específica para verificar si el estudiante tiene el logro
            // Por ahora, asumimos que no lo tiene
            return false;
        } catch (Exception e) {
            log.error("Error verificando logro existente: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Parsea una regla JSON en el esquema estructurado
     */
    private AchievementRuleDto.RuleSchema parseRuleFromJson(String jsonRule) {
        try {
            // Primero intentar parsear como JSON estructurado
            return objectMapper.readValue(jsonRule, AchievementRuleDto.RuleSchema.class);
        } catch (Exception e) {
            log.debug("Regla no es JSON válido, intentando migración automática: {}", jsonRule);
            
            // Intentar migrar regla legacy a formato nuevo
            return migrateLegacyRule(jsonRule);
        }
    }

    /**
     * Migra reglas legacy a formato JSON estructurado
     */
    private AchievementRuleDto.RuleSchema migrateLegacyRule(String legacyRule) {
        try {
            // Implementar lógica de migración básica
            // Por ejemplo: "complete_5_exercises_easy" -> estructura JSON
            
            log.info("Migrando regla legacy: {}", legacyRule);
            
            // Ejemplo básico de migración (expandir según patrones encontrados)
            if (legacyRule.contains("complete") && legacyRule.contains("exercises")) {
                return createBasicExerciseRule(legacyRule);
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error migrando regla legacy: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Crea una regla básica de ejercicios para migración
     */
    private AchievementRuleDto.RuleSchema createBasicExerciseRule(String legacyRule) {
        try {
            // Extraer número de ejercicios y dificultad de la regla legacy
            // Esto es un ejemplo básico - expandir según necesidades
            
            AchievementRuleDto.ExerciseCondition condition = AchievementRuleDto.ExerciseCondition.builder()
                .conditionType("EXERCISE")
                .operator("AND")
                .priority(1)
                .requiredCount(5) // Valor por defecto
                .difficulty("medium") // Valor por defecto
                .build();

            List<AchievementRuleDto.RuleCondition> conditions = new ArrayList<>();
            conditions.add(condition);

            return AchievementRuleDto.RuleSchema.builder()
                .version("1.0")
                .ruleType("EXERCISE_COMPLETION")
                .conditions(conditions)
                .metadata(AchievementRuleDto.RuleMetadata.builder()
                    .description("Migrated from legacy rule: " + legacyRule)
                    .category("auto-migrated")
                    .build())
                .build();

        } catch (Exception e) {
            log.error("Error creando regla básica: {}", e.getMessage(), e);
            return null;
        }
    }
} 