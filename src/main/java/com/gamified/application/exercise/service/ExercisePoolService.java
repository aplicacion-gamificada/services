package com.gamified.application.exercise.service;

import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.GeneratedExercise;
import com.gamified.application.exercise.repository.ExerciseRepository;
import com.gamified.application.exercise.repository.GeneratedExerciseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio para gestionar el pool de ejercicios pre-generados
 * Ejecuta pre-generación asíncrona para reducir costos y latencia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExercisePoolService {

    private final ExerciseRepository exerciseRepository;
    private final GeneratedExerciseRepository generatedExerciseRepository;
    private final AzureAiClient azureAiClient;
    private final ExercisePromptBuilder promptBuilder;

    @Value("${exercise.pool.min-exercises-per-template:5}")
    private Integer minExercisesPerTemplate;

    @Value("${exercise.pool.max-exercises-per-template:20}")
    private Integer maxExercisesPerTemplate;

    @Value("${exercise.pool.batch-size:10}")
    private Integer batchSize;

    @Value("${exercise.pool.enabled:true}")
    private Boolean poolEnabled;

    /**
     * Job programado que se ejecuta para mantener el pool de ejercicios
     * Se ejecuta cada 2 horas durante horas de baja demanda
     */
    @Scheduled(cron = "0 0 */2 * * *") // Cada 2 horas
    public void maintainExercisePool() {
        if (!poolEnabled) {
            log.debug("Pool de ejercicios deshabilitado");
            return;
        }

        if (!azureAiClient.isConfigured()) {
            log.warn("Azure AI no está configurado, saltando mantenimiento del pool");
            return;
        }

        log.info("Iniciando mantenimiento del pool de ejercicios");
        
        try {
            // 1. Identificar plantillas que necesitan más ejercicios
            List<Integer> templatesThatNeedExercises = identifyTemplatesNeedingExercises();
            
            if (templatesThatNeedExercises.isEmpty()) {
                log.info("Pool de ejercicios está completo, no se necesita pre-generación");
                return;
            }

            // 2. Generar ejercicios para cada plantilla que lo necesite
            int totalGenerated = 0;
            for (Integer templateId : templatesThatNeedExercises) {
                int generated = generateExercisesForTemplate(templateId);
                totalGenerated += generated;
                
                // Pausa entre plantillas para no sobrecargar la API
                Thread.sleep(1000);
            }

            log.info("Mantenimiento del pool completado. Total de ejercicios generados: {}", totalGenerated);

        } catch (Exception e) {
            log.error("Error durante el mantenimiento del pool de ejercicios", e);
        }
    }

    /**
     * Job de limpieza que elimina ejercicios antiguos no utilizados
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Domingos a las 3 AM
    public void cleanupOldExercises() {
        if (!poolEnabled) {
            return;
        }

        log.info("Iniciando limpieza de ejercicios antiguos");
        
        try {
            // Eliminar ejercicios generados hace más de 30 días que no han sido usados
            Integer deletedCount = generatedExerciseRepository.deleteOldExercises(30);
            log.info("Limpieza completada. Ejercicios eliminados: {}", deletedCount);
            
        } catch (Exception e) {
            log.error("Error durante la limpieza de ejercicios antiguos", e);
        }
    }

    /**
     * Identifica plantillas de ejercicios que necesitan más ejercicios en el pool
     */
    private List<Integer> identifyTemplatesNeedingExercises() {
        // Obtener las plantillas más demandadas
        List<Integer> mostDemandedTemplates = generatedExerciseRepository
                .findMostDemandedTemplates(batchSize);

        return mostDemandedTemplates.stream()
                .filter(templateId -> {
                    Integer currentCount = generatedExerciseRepository.countByTemplate(templateId);
                    return currentCount < minExercisesPerTemplate;
                })
                .limit(batchSize)
                .toList();
    }

    /**
     * Genera ejercicios para una plantilla específica hasta alcanzar el mínimo
     */
    private int generateExercisesForTemplate(Integer templateId) {
        log.info("Generando ejercicios para plantilla {}", templateId);
        
        Optional<Exercise> templateOpt = exerciseRepository.findExerciseById(templateId);
        if (templateOpt.isEmpty()) {
            log.warn("Plantilla de ejercicio no encontrada: {}", templateId);
            return 0;
        }

        Exercise template = templateOpt.get();
        Integer currentCount = generatedExerciseRepository.countByTemplate(templateId);
        int toGenerate = Math.min(minExercisesPerTemplate - currentCount, 
                                 maxExercisesPerTemplate - currentCount);

        if (toGenerate <= 0) {
            return 0;
        }

        int generatedCount = 0;
        for (int i = 0; i < toGenerate; i++) {
            try {
                generateSingleExerciseForPool(template);
                generatedCount++;
                
                // Pausa entre generaciones para no sobrecargar la API
                Thread.sleep(500);
                
            } catch (Exception e) {
                log.error("Error generando ejercicio {} para plantilla {}", i + 1, templateId, e);
                // Continuar con el siguiente ejercicio
            }
        }

        log.info("Generados {} ejercicios para plantilla {}", generatedCount, templateId);
        return generatedCount;
    }

    /**
     * Genera un solo ejercicio para el pool de manera transaccional
     */
    @Transactional
    private void generateSingleExerciseForPool(Exercise template) {
        // 1. Construir prompt para pool generation (sin contexto específico de estudiante)
        String prompt = promptBuilder.buildPromptForPoolGeneration(template);
        
        // 2. Llamar al servicio de IA
        String aiJsonResponse = azureAiClient.generateExerciseContent(prompt);
        
        // 3. Calcular hash de la respuesta correcta
        String correctAnswerHash = calculateCorrectAnswerHash(aiJsonResponse);
        
        // 4. Crear y persistir la entidad usando los nombres de campo correctos del esquema real
        GeneratedExercise generatedExercise = GeneratedExercise.builder()
                .exerciseTemplateId(template.getId())
                // Nota: En el esquema real no hay student_profile_id en generated_exercise
                // Los ejercicios del pool son genéricos
                .generatedContentJson(aiJsonResponse) // Campo real: generated_content_json
                .correctAnswerHash(correctAnswerHash) // Campo real: correct_answer_hash
                .generationPrompt(prompt) // Campo real: generation_prompt
                .aiModelVersion(azureAiClient.getModelVersion()) // Campo real: ai_model_version
                .createdAt(LocalDateTime.now()) // Campo real: created_at
                .build();

        Long savedId = generatedExerciseRepository.save(generatedExercise);
        log.debug("Ejercicio generado para pool. ID: {}", savedId);
    }

    /**
     * Genera ejercicios de forma asíncrona para una plantilla específica
     */
    @Async
    public CompletableFuture<Integer> generateExercisesAsync(Integer templateId, Integer count) {
        log.info("Iniciando generación asíncrona de {} ejercicios para plantilla {}", count, templateId);
        
        Optional<Exercise> templateOpt = exerciseRepository.findExerciseById(templateId);
        if (templateOpt.isEmpty()) {
            log.warn("Plantilla no encontrada: {}", templateId);
            return CompletableFuture.completedFuture(0);
        }

        Exercise template = templateOpt.get();
        int generated = 0;

        for (int i = 0; i < count; i++) {
            try {
                generateSingleExerciseForPool(template);
                generated++;
            } catch (Exception e) {
                log.error("Error en generación asíncrona del ejercicio {} para plantilla {}", 
                         i + 1, templateId, e);
            }
        }

        log.info("Generación asíncrona completada. {} ejercicios generados para plantilla {}", 
                generated, templateId);
        
        return CompletableFuture.completedFuture(generated);
    }

    /**
     * Fuerza la generación inmediata de ejercicios para plantillas específicas
     * Útil para casos donde se necesita llenar el pool rápidamente
     */
    public void warmUpPool(List<Integer> templateIds) {
        log.info("Calentando pool para {} plantillas", templateIds.size());
        
        for (Integer templateId : templateIds) {
            try {
                generateExercisesForTemplate(templateId);
            } catch (Exception e) {
                log.error("Error calentando pool para plantilla {}", templateId, e);
            }
        }
        
        log.info("Pool warming completado");
    }

    /**
     * Obtiene estadísticas del pool
     */
    public GeneratedExerciseRepository.PoolStats getPoolStatistics() {
        return generatedExerciseRepository.getPoolStats();
    }

    /**
     * Verifica si el pool tiene suficientes ejercicios para una plantilla
     */
    public boolean hasAvailableExercises(Integer templateId) {
        Integer count = generatedExerciseRepository.countByTemplate(templateId);
        return count > 0;
    }

    /**
     * Calcula hash SHA256 de la respuesta correcta
     */
    private String calculateCorrectAnswerHash(String aiJsonResponse) {
        try {
            var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var jsonNode = objectMapper.readTree(aiJsonResponse);
            String correctAnswer = jsonNode.path("correct_answer").asText();
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(correctAnswer.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
            
        } catch (Exception e) {
            log.warn("Error calculando hash de respuesta correcta", e);
            return null;
        }
    }
}
