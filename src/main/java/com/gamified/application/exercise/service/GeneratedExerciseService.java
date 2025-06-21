package com.gamified.application.exercise.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamified.application.exercise.model.dto.response.GeneratedExerciseResponseDto;
import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.GeneratedExercise;
import com.gamified.application.exercise.repository.ExerciseRepository;
import com.gamified.application.exercise.repository.GeneratedExerciseRepository;
import com.gamified.application.learning.model.entity.LearningPoint;
import com.gamified.application.learning.repository.LearningRepository;
import com.gamified.application.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio principal para gestión de ejercicios generados por IA
 * Implementa el flujo transaccional: determinar plantilla → generar con IA → persistir → entregar
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeneratedExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final GeneratedExerciseRepository generatedExerciseRepository;
    private final LearningRepository learningRepository;
    private final AzureAiClient azureAiClient;
    private final ExercisePromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    /**
     * Obtiene el siguiente ejercicio para un estudiante, usando el pool de ejercicios pre-generados
     * o generando uno nuevo con IA si es necesario
     * 
     * @param studentId ID del estudiante
     * @param learningPointId ID del learning point
     * @param preferredDifficulty Dificultad preferida (opcional)
     * @return Ejercicio generado listo para presentar al estudiante
     */
    @Transactional
    public GeneratedExerciseResponseDto.GeneratedExerciseDto getNextExercise(
            Integer studentId, Integer learningPointId, String preferredDifficulty) {
        
        log.info("Obteniendo siguiente ejercicio generado para estudiante {} en learning point {}", 
                studentId, learningPointId);

        // 1. Determinar la plantilla de ejercicio apropiada
        Exercise exerciseTemplate = determineExerciseTemplate(studentId, learningPointId, preferredDifficulty);
        
        // 2. Intentar obtener un ejercicio del pool pre-generado
        Optional<GeneratedExercise> poolExerciseOpt = getFromExercisePool(exerciseTemplate.getId());
        
        GeneratedExercise generatedExercise;
        boolean fromPool = poolExerciseOpt.isPresent();
        
        if (fromPool) {
            // 3a. Usar ejercicio del pool (rápido y barato)
            generatedExercise = poolExerciseOpt.get();
            log.info("Ejercicio obtenido del pool pre-generado. ID: {}", generatedExercise.getId());
        } else {
            // 3b. Generar nuevo ejercicio con IA (lento y costoso)
            generatedExercise = generateNewExercise(exerciseTemplate, studentId);
            log.info("Nuevo ejercicio generado con IA. ID: {}", generatedExercise.getId());
        }

        // 4. Mapear a DTO de respuesta
        return mapToGeneratedExerciseDto(generatedExercise, exerciseTemplate, fromPool);
    }

    /**
     * Determina la plantilla de ejercicio apropiada basada en el estudiante y learning point
     */
    private Exercise determineExerciseTemplate(Integer studentId, Integer learningPointId, String preferredDifficulty) {
        // Verificar que el learning point existe
        Optional<LearningPoint> learningPointOpt = learningRepository.findLearningPointById(learningPointId);
        if (learningPointOpt.isEmpty()) {
            throw new ResourceNotFoundException("Learning point no encontrado con ID: " + learningPointId);
        }

        // Buscar ejercicio plantilla disponible para este learning point
        Optional<Exercise> exerciseOpt = exerciseRepository.findNextExerciseForLearningPoint(
                studentId, learningPointId, preferredDifficulty);
        
        if (exerciseOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                "No hay plantillas de ejercicios disponibles para este learning point: " + learningPointId);
        }

        return exerciseOpt.get();
    }

    /**
     * Intenta obtener un ejercicio del pool pre-generado
     */
    private Optional<GeneratedExercise> getFromExercisePool(Integer exerciseTemplateId) {
        List<GeneratedExercise> availableExercises = generatedExerciseRepository
                .findAvailableByTemplate(exerciseTemplateId, 1);
        
        if (!availableExercises.isEmpty()) {
            GeneratedExercise exercise = availableExercises.get(0);
            // Cargar la información de la plantilla
            Optional<Exercise> templateOpt = exerciseRepository.findExerciseById(exercise.getExerciseTemplateId());
            templateOpt.ifPresent(exercise::setExerciseTemplate);
            return Optional.of(exercise);
        }
        
        return Optional.empty();
    }

    /**
     * Genera un nuevo ejercicio usando IA de manera transaccional
     */
    @Transactional
    private GeneratedExercise generateNewExercise(Exercise exerciseTemplate, Integer studentId) {
        try {
            // 1. Construir el prompt dinámico
            String prompt = promptBuilder.buildPromptForExercise(exerciseTemplate, studentId);
            
            // 2. Llamar al servicio de IA
            long startTime = System.currentTimeMillis();
            String aiJsonResponse = azureAiClient.generateExerciseContent(prompt);
            long generationTime = System.currentTimeMillis() - startTime;
            
            // 3. Validar el JSON de respuesta
            validateAiResponse(aiJsonResponse);
            
            // 4. Calcular hash de la respuesta correcta (opcional pero útil)
            String correctAnswerHash = calculateCorrectAnswerHash(aiJsonResponse);
            
            // 5. Crear y persistir la entidad GeneratedExercise
            GeneratedExercise generatedExercise = GeneratedExercise.builder()
                    .exerciseTemplateId(exerciseTemplate.getId())
                    .generatedContentJson(aiJsonResponse)
                    .correctAnswerHash(correctAnswerHash)
                    .generationPrompt(prompt)
                    .aiModelVersion(azureAiClient.getModelVersion())
                    .createdAt(LocalDateTime.now())
                    .exerciseTemplate(exerciseTemplate)
                    .build();

            Long savedId = generatedExerciseRepository.save(generatedExercise);
            generatedExercise.setId(savedId);
            
            log.info("Ejercicio generado y persistido exitosamente. ID: {}, Tiempo: {}ms", 
                    savedId, generationTime);
            
            return generatedExercise;
            
        } catch (Exception e) {
            log.error("Error al generar nuevo ejercicio para plantilla {}", exerciseTemplate.getId(), e);
            throw new RuntimeException("Error al generar ejercicio con IA: " + e.getMessage(), e);
        }
    }

    /**
     * Valida que la respuesta de la IA sea JSON válido y tenga la estructura esperada
     */
    private void validateAiResponse(String aiJsonResponse) {
        try {
            // Validar que sea JSON válido
            var jsonNode = objectMapper.readTree(aiJsonResponse);
            
            // Validar que tenga los campos mínimos requeridos
            if (!jsonNode.has("question")) {
                throw new RuntimeException("Respuesta de IA no contiene campo 'question'");
            }
            if (!jsonNode.has("correct_answer")) {
                throw new RuntimeException("Respuesta de IA no contiene campo 'correct_answer'");
            }
            
        } catch (Exception e) {
            log.error("JSON inválido de IA: {}", aiJsonResponse, e);
            throw new RuntimeException("Respuesta de IA no es JSON válido: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula un hash SHA256 de la respuesta correcta para validaciones rápidas
     */
    private String calculateCorrectAnswerHash(String aiJsonResponse) {
        try {
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
            log.warn("Error al calcular hash de respuesta correcta", e);
            return null;
        }
    }

    /**
     * Mapea GeneratedExercise a DTO de respuesta
     */
    private GeneratedExerciseResponseDto.GeneratedExerciseDto mapToGeneratedExerciseDto(
            GeneratedExercise generatedExercise, Exercise exerciseTemplate, boolean fromPool) {
        
        // Obtener información del learning point
        Optional<LearningPoint> learningPointOpt = learningRepository
                .findLearningPointById(exerciseTemplate.getLearningPointId());
        String learningPointTitle = learningPointOpt.map(LearningPoint::getTitle).orElse("Learning Point");

        return GeneratedExerciseResponseDto.GeneratedExerciseDto.builder()
                .generatedExerciseId(generatedExercise.getId())
                .exerciseTemplateId(generatedExercise.getExerciseTemplateId())
                .exerciseContent(generatedExercise.getGeneratedContentJson())
                .exerciseType(exerciseTemplate.getTitle()) // o buscar el nombre del tipo
                .difficulty(exerciseTemplate.getDifficulty())
                .estimatedTimeMinutes(exerciseTemplate.getEstimatedTimeMinutes())
                .pointsValue(100) // o calcularlo dinámicamente
                .learningPointTitle(learningPointTitle)
                .generatedAt(generatedExercise.getCreatedAt())
                .aiModelVersion(generatedExercise.getAiModelVersion())
                .build();
    }

    /**
     * Obtiene estadísticas del pool de ejercicios
     */
    public GeneratedExerciseResponseDto.GeneratedExerciseStatsDto getPoolStats() {
        var poolStats = generatedExerciseRepository.getPoolStats();
        
        return GeneratedExerciseResponseDto.GeneratedExerciseStatsDto.builder()
                .totalGeneratedExercises(poolStats.getTotalGenerated())
                .totalAttempts(poolStats.getTotalUsed())
                .averageGenerationTime(poolStats.getAverageGenerationTimeMs())
                .cacheHitRate(poolStats.getCacheHitRate())
                .mostUsedAiModel(azureAiClient.getModelVersion())
                .successfulGenerations(poolStats.getTotalGenerated()) // Se puede refinar
                .failedGenerations(0) // Se puede implementar tracking de fallos
                .build();
    }
}
