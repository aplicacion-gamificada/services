package com.gamified.application.exercise.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamified.application.exercise.model.dto.response.GeneratedExerciseResponseDto;
import com.gamified.application.exercise.model.dto.response.ExerciseResponseDto;
import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.ExerciseType;
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
import java.util.Map;
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
     * Obtiene el siguiente ejercicio para un estudiante, generando dinámicamente con IA
     * según el flujo descrito en la guía de implementación
     * 
     * @param studentId ID del estudiante
     * @param learningPointId ID del learning point
     * @param preferredDifficulty Dificultad preferida (opcional)
     * @return Ejercicio generado listo para presentar al estudiante
     */
    @Transactional
    public ExerciseResponseDto.NextExerciseDto getNextExercise(
            Integer studentId, Integer learningPointId, String preferredDifficulty) {
        
        log.info("Generando ejercicio dinámico con IA para estudiante {} en learning point {}", 
                studentId, learningPointId);

        // 1. Determinar la plantilla de ejercicio apropiada
        Exercise exerciseTemplate = determineExerciseTemplate(studentId, learningPointId, preferredDifficulty);
        
        // 2. Generar nuevo ejercicio dinámicamente con IA (siguiendo la guía)
        GeneratedExercise generatedExercise = generateNewExercise(exerciseTemplate, studentId);
        log.info("Nuevo ejercicio generado dinámicamente con IA. ID: {}", generatedExercise.getId());

        // 3. Mapear a DTO de respuesta con formato completo según la guía
        return mapToNextExerciseDto(generatedExercise, exerciseTemplate);
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
            
            // 5. Crear y persistir la entidad GeneratedExercise usando los nombres de campo correctos
            GeneratedExercise generatedExercise = GeneratedExercise.builder()
                    .exerciseTemplateId(exerciseTemplate.getId())
                    // Nota: En el esquema real no hay student_profile_id en generated_exercise
                    // Los ejercicios generados son genéricos y se asignan en exercise_attempt
                    .generatedContentJson(aiJsonResponse) // Campo real: generated_content_json
                    .correctAnswerHash(correctAnswerHash) // Campo real: correct_answer_hash
                    .generationPrompt(prompt) // Campo real: generation_prompt
                    .aiModelVersion(azureAiClient.getModelVersion()) // Campo real: ai_model_version
                    .createdAt(LocalDateTime.now()) // Campo real: created_at
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
     * Mapea GeneratedExercise a DTO de respuesta con formato completo según la guía
     */
    private ExerciseResponseDto.NextExerciseDto mapToNextExerciseDto(
            GeneratedExercise generatedExercise, Exercise exerciseTemplate) {
        
        try {
            // 1. Obtener información del learning point
            Optional<LearningPoint> learningPointOpt = learningRepository
                    .findLearningPointById(exerciseTemplate.getLearningPointId());
            LearningPoint learningPoint = learningPointOpt.orElseThrow(() -> 
                    new ResourceNotFoundException("Learning point no encontrado"));

            // 2. Parsear el contenido JSON generado por la IA
            Map<String, Object> aiContent = objectMapper.readValue(
                    generatedExercise.getGeneratedContentJson(), 
                    new TypeReference<Map<String, Object>>() {});

            // 3. Obtener información del tipo de ejercicio
            Optional<ExerciseType> exerciseTypeOpt = exerciseRepository
                    .findExerciseTypeById(exerciseTemplate.getExerciseTypeId());
            String exerciseTypeName = exerciseTypeOpt
                    .map(ExerciseType::getName)
                    .orElse("multiple_choice");

            // 4. Determinar render_variant según el tipo de ejercicio
            String renderVariant = determineRenderVariant(exerciseTypeName);

            // 5. Construir configuración del ejercicio según GUIA.md
            ExerciseResponseDto.ExerciseConfigDto config = buildExerciseConfig(
                    exerciseTypeName, exerciseTemplate, aiContent);

            // 6. Construir contenido del ejercicio
            ExerciseResponseDto.ExerciseContentDto content = buildExerciseContent(aiContent);

            // 7. Verificar intentos previos del estudiante en esta plantilla
            // TODO: Implementar lógica para obtener studentId y consultar intentos previos
            Boolean hasAttempts = false;
            Integer previousAttempts = 0;

            // 8. Construir respuesta completa según estructura de GUIA.md
            return ExerciseResponseDto.NextExerciseDto.builder()
                    .generatedExerciseId(generatedExercise.getId())
                    .exerciseTemplateId(exerciseTemplate.getId())
                    .exerciseType(exerciseTypeName)
                    .renderVariant(renderVariant)
                    .difficultyLevel(exerciseTemplate.getDifficulty())
                    .config(config)
                    .content(content)
                    .learningPointId(learningPoint.getId())
                    .learningPointTitle(learningPoint.getTitle())
                    .hasAttempts(hasAttempts)
                    .previousAttempts(previousAttempts)
                    .build();

        } catch (Exception e) {
            log.error("Error construyendo respuesta de ejercicio: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando contenido del ejercicio: " + e.getMessage());
        }
    }

    /**
     * Determina el render_variant según el tipo de ejercicio
     */
    private String determineRenderVariant(String exerciseType) {
        return switch (exerciseType.toLowerCase()) {
            case "multiple_choice", "múltiple_opción" -> "radio-buttons";
            case "drag_and_drop", "arrastrar_soltar" -> "drag-to-sort";
            case "numeric_input", "entrada_numérica" -> "number-input";
            case "text_input", "entrada_texto" -> "text-area";
            case "true_false", "verdadero_falso" -> "toggle-buttons";
            case "ordering", "ordenamiento" -> "sortable-list";
            case "matching", "emparejamiento" -> "connect-pairs";
            case "fill_blanks", "llenar_espacios" -> "input-blanks";
            default -> "default-renderer";
        };
    }

    /**
     * Construye la configuración del ejercicio según el tipo y GUIA.md
     */
    private ExerciseResponseDto.ExerciseConfigDto buildExerciseConfig(
            String exerciseType, Exercise exerciseTemplate, Map<String, Object> aiContent) {
        
        // Configuración base
        ExerciseResponseDto.ExerciseConfigDto.ExerciseConfigDtoBuilder configBuilder = 
                ExerciseResponseDto.ExerciseConfigDto.builder()
                .showTimer(true)
                .allowPartialScore(true)
                .showHints(true)
                .allowRetry(false);

        // Configuración específica por tipo
        switch (exerciseType.toLowerCase()) {
            case "drag_and_drop", "arrastrar_soltar" -> {
                configBuilder
                    .maxTime(90)
                    .itemCount(getItemCount(aiContent))
                    .shuffleItems(true);
            }
            case "multiple_choice", "múltiple_opción" -> {
                configBuilder
                    .maxTime(60)
                    .shuffleItems(true);
            }
            case "numeric_input", "entrada_numérica" -> {
                configBuilder
                    .maxTime(120)
                    .allowPartialScore(false);
            }
            default -> {
                configBuilder
                    .maxTime(exerciseTemplate.getEstimatedTimeMinutes() * 60);
            }
        }

        return configBuilder.build();
    }

    /**
     * Construye el contenido del ejercicio desde el JSON de la IA
     */
    private ExerciseResponseDto.ExerciseContentDto buildExerciseContent(Map<String, Object> aiContent) {
        return ExerciseResponseDto.ExerciseContentDto.builder()
                .title((String) aiContent.get("question"))
                .instructions((String) aiContent.getOrDefault("instructions", "Selecciona la respuesta correcta"))
                .options(aiContent.get("options"))
                .correctAnswer(aiContent.get("correct_answer"))
                .explanation((String) aiContent.get("explanation"))
                .hints((List<String>) aiContent.get("hints"))
                .imageUrl((String) aiContent.get("image_url"))
                .build();
    }

    /**
     * Obtiene el número de elementos del contenido de IA
     */
    private Integer getItemCount(Map<String, Object> aiContent) {
        Object options = aiContent.get("options");
        if (options instanceof List) {
            return ((List<?>) options).size();
        }
        return 4; // Default
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
