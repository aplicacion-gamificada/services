package com.gamified.application.achievement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamified.application.achievement.repository.IAchievementRepository;
import com.gamified.application.shared.model.dto.engine.AchievementRuleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio para migrar reglas legacy a formato JSON estructurado
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleMigrationService {

    private final IAchievementRepository achievementRepository;
    private final ObjectMapper objectMapper;

    // Patrones regex para detectar diferentes tipos de reglas legacy
    private static final Pattern EXERCISE_COUNT_PATTERN = Pattern.compile(
        "complete[_\\s]*(\\d+)[_\\s]*exercises?(?:[_\\s]*(easy|medium|hard))?", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern STREAK_PATTERN = Pattern.compile(
        "(\\d+)[_\\s]*day[s]?[_\\s]*streak", 
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern PERFECT_SCORE_PATTERN = Pattern.compile(
        "perfect[_\\s]*score[_\\s]*(\\d+)?", 
        Pattern.CASE_INSENSITIVE
    );

    /**
     * Migra todas las reglas legacy del sistema
     */
    public void migrateAllLegacyRules() {
        log.info("Iniciando migración masiva de reglas legacy...");
        
        try {
            List<Map<String, Object>> achievements = achievementRepository.getAchievements();
            int migratedCount = 0;
            int errorCount = 0;
            
            for (Map<String, Object> achievement : achievements) {
                try {
                    if (migrateSingleAchievementRule(achievement)) {
                        migratedCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    log.error("Error migrando logro {}: {}", achievement.get("id"), e.getMessage());
                }
            }
            
            log.info("Migración completa. {} reglas migradas, {} errores", migratedCount, errorCount);
            
        } catch (Exception e) {
            log.error("Error en migración masiva: {}", e.getMessage(), e);
        }
    }

    /**
     * Migra la regla de un logro específico
     */
    public boolean migrateSingleAchievementRule(Map<String, Object> achievementData) {
        try {
            Integer achievementId = (Integer) achievementData.get("id");
            String triggerRule = (String) achievementData.get("trigger_rule");
            String achievementName = (String) achievementData.get("achievement_name");
            
            if (triggerRule == null || triggerRule.trim().isEmpty()) {
                log.debug("Logro {} no tiene regla para migrar", achievementId);
                return false;
            }

            // Verificar si ya es JSON válido
            if (isValidJsonRule(triggerRule)) {
                log.debug("Logro {} ya tiene regla en formato JSON", achievementId);
                return false;
            }

            // Migrar la regla legacy
            AchievementRuleDto.RuleSchema migratedRule = migrateLegacyRuleText(triggerRule, achievementName);
            if (migratedRule == null) {
                log.warn("No se pudo migrar la regla del logro {}: {}", achievementId, triggerRule);
                return false;
            }

            // Convertir a JSON y validar
            String migratedJson = objectMapper.writeValueAsString(migratedRule);
            log.info("Regla migrada para logro {} '{}': {}", achievementId, achievementName, migratedJson);

            // TODO: Actualizar la regla en base de datos
            // updateAchievementRule(achievementId, migratedJson);
            
            return true;

        } catch (Exception e) {
            log.error("Error migrando regla: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Migra texto de regla legacy a esquema estructurado
     */
    public AchievementRuleDto.RuleSchema migrateLegacyRuleText(String legacyRule, String achievementName) {
        try {
            String normalizedRule = legacyRule.toLowerCase().trim();
            
            // Intentar diferentes patrones de migración
            AchievementRuleDto.RuleSchema rule = null;
            
            // 1. Patrón de completar ejercicios
            rule = tryMigrateExerciseCompletionRule(normalizedRule);
            if (rule != null) {
                addMigrationMetadata(rule, legacyRule, "exercise_completion", achievementName);
                return rule;
            }
            
            // 2. Patrón de racha diaria
            rule = tryMigrateStreakRule(normalizedRule);
            if (rule != null) {
                addMigrationMetadata(rule, legacyRule, "streak", achievementName);
                return rule;
            }
            
            // 3. Patrón de puntuación perfecta
            rule = tryMigratePerfectScoreRule(normalizedRule);
            if (rule != null) {
                addMigrationMetadata(rule, legacyRule, "perfect_score", achievementName);
                return rule;
            }
            
            // 4. Fallback: regla genérica
            return createGenericRule(legacyRule, achievementName);
            
        } catch (Exception e) {
            log.error("Error procesando regla legacy '{}': {}", legacyRule, e.getMessage());
            return null;
        }
    }

    /**
     * Intenta migrar regla de completar ejercicios
     */
    private AchievementRuleDto.RuleSchema tryMigrateExerciseCompletionRule(String rule) {
        Matcher matcher = EXERCISE_COUNT_PATTERN.matcher(rule);
        if (matcher.find()) {
            try {
                int exerciseCount = Integer.parseInt(matcher.group(1));
                String difficulty = matcher.group(2); // Puede ser null
                
                AchievementRuleDto.ExerciseCondition condition = AchievementRuleDto.ExerciseCondition.builder()
                    .conditionType("EXERCISE")
                    .operator("AND")
                    .priority(1)
                    .requiredCount(exerciseCount)
                    .difficulty(difficulty != null ? difficulty : "any")
                    .build();

                List<AchievementRuleDto.RuleCondition> conditions = new ArrayList<>();
                conditions.add(condition);

                return AchievementRuleDto.RuleSchema.builder()
                    .version("1.0")
                    .ruleType("EXERCISE_COMPLETION")
                    .conditions(conditions)
                    .build();
                    
            } catch (NumberFormatException e) {
                log.warn("Error parseando número de ejercicios: {}", matcher.group(1));
            }
        }
        return null;
    }

    /**
     * Intenta migrar regla de racha
     */
    private AchievementRuleDto.RuleSchema tryMigrateStreakRule(String rule) {
        Matcher matcher = STREAK_PATTERN.matcher(rule);
        if (matcher.find()) {
            try {
                int streakDays = Integer.parseInt(matcher.group(1));
                
                AchievementRuleDto.StreakCondition condition = AchievementRuleDto.StreakCondition.builder()
                    .conditionType("STREAK")
                    .operator("AND")
                    .priority(1)
                    .requiredStreakLength(streakDays)
                    .streakType("daily")
                    .build();

                List<AchievementRuleDto.RuleCondition> conditions = new ArrayList<>();
                conditions.add(condition);

                return AchievementRuleDto.RuleSchema.builder()
                    .version("1.0")
                    .ruleType("STREAK_ACHIEVEMENT")
                    .conditions(conditions)
                    .build();
                    
            } catch (NumberFormatException e) {
                log.warn("Error parseando días de racha: {}", matcher.group(1));
            }
        }
        return null;
    }

    /**
     * Intenta migrar regla de puntuación perfecta
     */
    private AchievementRuleDto.RuleSchema tryMigratePerfectScoreRule(String rule) {
        Matcher matcher = PERFECT_SCORE_PATTERN.matcher(rule);
        if (matcher.find()) {
            Integer exerciseCount = null;
            if (matcher.group(1) != null) {
                try {
                    exerciseCount = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    exerciseCount = 1; // Default
                }
            } else {
                exerciseCount = 1; // Default
            }
            
            AchievementRuleDto.PerformanceCondition condition = AchievementRuleDto.PerformanceCondition.builder()
                .conditionType("PERFORMANCE")
                .operator("AND")
                .priority(1)
                .minimumScore(100.0)
                .minimumAttempts(exerciseCount)
                .performanceType("score")
                .build();

            List<AchievementRuleDto.RuleCondition> conditions = new ArrayList<>();
            conditions.add(condition);

            return AchievementRuleDto.RuleSchema.builder()
                .version("1.0")
                .ruleType("PERFECT_SCORE")
                .conditions(conditions)
                .build();
        }
        return null;
    }

    /**
     * Crea una regla genérica para casos no reconocidos
     */
    private AchievementRuleDto.RuleSchema createGenericRule(String legacyRule, String achievementName) {
        // Crear una regla básica que siempre falla (requiere migración manual)
        AchievementRuleDto.ExerciseCondition condition = AchievementRuleDto.ExerciseCondition.builder()
            .conditionType("EXERCISE")
            .operator("AND")
            .priority(1)
            .requiredCount(999999) // Imposible de cumplir
            .build();

        List<AchievementRuleDto.RuleCondition> conditions = new ArrayList<>();
        conditions.add(condition);

        AchievementRuleDto.RuleSchema rule = AchievementRuleDto.RuleSchema.builder()
            .version("1.0")
            .ruleType("REQUIRES_MANUAL_MIGRATION")
            .conditions(conditions)
            .build();

        addMigrationMetadata(rule, legacyRule, "generic_fallback", achievementName);
        return rule;
    }

    /**
     * Añade metadatos de migración
     */
    private void addMigrationMetadata(AchievementRuleDto.RuleSchema rule, String originalRule, 
                                    String migrationType, String achievementName) {
        if (rule.getMetadata() == null) {
            rule.setMetadata(AchievementRuleDto.RuleMetadata.builder().build());
        }
        
        rule.getMetadata().setDescription(
            String.format("Auto-migrated from legacy rule for '%s'", achievementName));
        rule.getMetadata().setCategory("auto-migrated");
        rule.getMetadata().setCreatedBy("RuleMigrationService");
        
        if (rule.getMetadata().getCustomProperties() == null) {
            rule.getMetadata().setCustomProperties(new java.util.HashMap<>());
        }
        rule.getMetadata().getCustomProperties().put("originalRule", originalRule);
        rule.getMetadata().getCustomProperties().put("migrationType", migrationType);
        rule.getMetadata().getCustomProperties().put("migrationDate", java.time.LocalDateTime.now().toString());
    }

    /**
     * Verifica si una regla ya es JSON válido
     */
    private boolean isValidJsonRule(String rule) {
        try {
            objectMapper.readValue(rule, AchievementRuleDto.RuleSchema.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Genera reporte de migración
     */
    public void generateMigrationReport() {
        log.info("Generando reporte de migración de reglas...");
        
        try {
            List<Map<String, Object>> achievements = achievementRepository.getAchievements();
            int totalRules = 0;
            int jsonRules = 0;
            int legacyRules = 0;
            int emptyRules = 0;
            
            for (Map<String, Object> achievement : achievements) {
                String triggerRule = (String) achievement.get("trigger_rule");
                totalRules++;
                
                if (triggerRule == null || triggerRule.trim().isEmpty()) {
                    emptyRules++;
                } else if (isValidJsonRule(triggerRule)) {
                    jsonRules++;
                } else {
                    legacyRules++;
                    log.info("Regla legacy en logro {}: {}", achievement.get("id"), triggerRule);
                }
            }
            
            log.info("=== REPORTE DE MIGRACIÓN ===");
            log.info("Total de logros: {}", totalRules);
            log.info("Reglas en formato JSON: {}", jsonRules);
            log.info("Reglas legacy pendientes: {}", legacyRules);
            log.info("Reglas vacías: {}", emptyRules);
            log.info("============================");
            
        } catch (Exception e) {
            log.error("Error generando reporte: {}", e.getMessage(), e);
        }
    }
} 