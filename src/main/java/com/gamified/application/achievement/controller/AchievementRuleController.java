package com.gamified.application.achievement.controller;

//import com.gamified.application.achievement.service.AchievementEngineService;
import com.gamified.application.achievement.service.RuleMigrationService;
import com.gamified.application.shared.model.dto.ApiResponse;
import com.gamified.application.shared.model.dto.engine.AchievementRuleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para gestionar reglas de achievements y Rule Engine
 */
@RestController
@RequestMapping("/api/achievements/rules")
@RequiredArgsConstructor
@Slf4j
public class AchievementRuleController {

    private final RuleMigrationService ruleMigrationService;

    /**
     * Genera reporte de estado de migración de reglas
     */
    @GetMapping("/migration/report")
    public ResponseEntity<ApiResponse> getMigrationReport() {
        try {
            log.info("Generando reporte de migración de reglas");
            ruleMigrationService.generateMigrationReport();
            
            return ResponseEntity.ok(new ApiResponse(
                true,
                "Reporte generado correctamente (ver logs)",
                LocalDateTime.now(),
                "Reporte completo disponible en logs del servidor"
            ));
                
        } catch (Exception e) {
            log.error("Error generando reporte de migración: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(
                    false,
                    "Error generando reporte: " + e.getMessage(),
                    LocalDateTime.now()
                ));
        }
    }

    /**
     * Migra una regla legacy específica (para testing)
     */
    @PostMapping("/migration/test")
    public ResponseEntity<ApiResponse> testRuleMigration(@RequestBody Map<String, String> request) {
        try {
            String legacyRule = request.get("legacyRule");
            String achievementName = request.get("achievementName");
            
            if (legacyRule == null || legacyRule.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(
                        false,
                        "legacyRule es requerido",
                        LocalDateTime.now()
                    ));
            }
            
            if (achievementName == null) {
                achievementName = "Test Achievement";
            }
            
            log.info("Migrando regla de prueba: {}", legacyRule);
            
            AchievementRuleDto.RuleSchema migratedRule = ruleMigrationService
                .migrateLegacyRuleText(legacyRule, achievementName);
            
            if (migratedRule == null) {
                return ResponseEntity.ok(new ApiResponse(
                    false,
                    "No se pudo migrar la regla: patrón no reconocido",
                    LocalDateTime.now()
                ));
            }
            
            return ResponseEntity.ok(new ApiResponse(
                true,
                "Regla migrada exitosamente",
                LocalDateTime.now(),
                migratedRule
            ));
                
        } catch (Exception e) {
            log.error("Error en migración de prueba: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(
                    false,
                    "Error migrando regla: " + e.getMessage(),
                    LocalDateTime.now()
                ));
        }
    }

    /**
     * Ejecuta migración masiva de todas las reglas legacy
     */
    @PostMapping("/migration/execute")
    public ResponseEntity<ApiResponse> executeMassiveMigration() {
        try {
            log.info("Iniciando migración masiva de reglas");
            
            // Esta operación puede tomar tiempo, mejor ejecutarla de forma asíncrona en producción
            ruleMigrationService.migrateAllLegacyRules();
            
            Map<String, Object> result = new HashMap<>();
            result.put("status", "completed");
            result.put("message", "Migración ejecutada. Ver logs para detalles.");
            
            return ResponseEntity.ok(new ApiResponse(
                true,
                "Migración masiva ejecutada",
                LocalDateTime.now(),
                result
            ));
                
        } catch (Exception e) {
            log.error("Error en migración masiva: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(
                    false,
                    "Error en migración masiva: " + e.getMessage(),
                    LocalDateTime.now()
                ));
        }
    }

    /**
     * Endpoint de health check para el Rule Engine
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse> getRuleEngineHealth() {
        try {
            Map<String, String> health = new HashMap<>();
            health.put("status", "healthy");
            health.put("ruleEngine", "active");
            health.put("eventSystem", "active");
            health.put("version", "1.0");
            
            return ResponseEntity.ok(new ApiResponse(
                true,
                "Rule Engine funcionando correctamente",
                LocalDateTime.now(),
                health
            ));
                
        } catch (Exception e) {
            log.error("Error verificando salud del Rule Engine: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(
                    false,
                    "Error verificando salud: " + e.getMessage(),
                    LocalDateTime.now()
                ));
        }
    }

    /**
     * Obtiene ejemplos de reglas JSON para documentación
     */
    @GetMapping("/examples")
    public ResponseEntity<ApiResponse> getRuleExamples() {
        try {
            Map<String, Object> examples = createRuleExamples();
            
            return ResponseEntity.ok(new ApiResponse(
                true,
                "Ejemplos de reglas JSON",
                LocalDateTime.now(),
                examples
            ));
                
        } catch (Exception e) {
            log.error("Error generando ejemplos: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(new ApiResponse(
                    false,
                    "Error generando ejemplos: " + e.getMessage(),
                    LocalDateTime.now()
                ));
        }
    }

    /**
     * Crea ejemplos de reglas para documentación
     */
    private Map<String, Object> createRuleExamples() {
        Map<String, Object> examples = new HashMap<>();
        
        // Ejemplo 1: Completar ejercicios
        AchievementRuleDto.ExerciseCondition exerciseCondition = AchievementRuleDto.ExerciseCondition.builder()
            .conditionType("EXERCISE")
            .operator("AND")
            .priority(1)
            .requiredCount(10)
            .difficulty("easy")
            .minimumAccuracy(80.0)
            .build();
            
        AchievementRuleDto.RuleSchema exerciseRule = AchievementRuleDto.RuleSchema.builder()
            .version("1.0")
            .ruleType("EXERCISE_COMPLETION")
            .conditions(java.util.Arrays.asList(exerciseCondition))
            .metadata(AchievementRuleDto.RuleMetadata.builder()
                .description("Completar 10 ejercicios fáciles con 80% de precisión")
                .category("exercise")
                .difficulty(2)
                .build())
            .build();
            
        examples.put("exercise_completion", exerciseRule);
        
        // Ejemplo 2: Racha diaria
        AchievementRuleDto.StreakCondition streakCondition = AchievementRuleDto.StreakCondition.builder()
            .conditionType("STREAK")
            .operator("AND")
            .priority(1)
            .requiredStreakLength(7)
            .streakType("daily")
            .minimumActivityPerDay(3)
            .build();
            
        AchievementRuleDto.RuleSchema streakRule = AchievementRuleDto.RuleSchema.builder()
            .version("1.0")
            .ruleType("STREAK_ACHIEVEMENT")
            .conditions(java.util.Arrays.asList(streakCondition))
            .metadata(AchievementRuleDto.RuleMetadata.builder()
                .description("Mantener racha de 7 días con mínimo 3 ejercicios por día")
                .category("consistency")
                .difficulty(4)
                .build())
            .build();
            
        examples.put("daily_streak", streakRule);
        
        // Ejemplo 3: Puntuación perfecta
        AchievementRuleDto.PerformanceCondition perfCondition = AchievementRuleDto.PerformanceCondition.builder()
            .conditionType("PERFORMANCE")
            .operator("AND")
            .priority(1)
            .minimumScore(100.0)
            .minimumAttempts(5)
            .performanceType("score")
            .build();
            
        AchievementRuleDto.RuleSchema perfRule = AchievementRuleDto.RuleSchema.builder()
            .version("1.0")
            .ruleType("PERFECT_SCORE")
            .conditions(java.util.Arrays.asList(perfCondition))
            .metadata(AchievementRuleDto.RuleMetadata.builder()
                .description("Obtener puntuación perfecta en 5 ejercicios")
                .category("performance")
                .difficulty(8)
                .build())
            .build();
            
        examples.put("perfect_score", perfRule);
        
        return examples;
    }
} 