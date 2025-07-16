package com.gamified.application.exercise.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamified.application.exercise.model.dto.request.ExerciseRequestDto;
import com.gamified.application.exercise.model.dto.response.ExerciseResponseDto;
import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.ExerciseAttempt;
import com.gamified.application.exercise.model.entity.ExerciseType;
import com.gamified.application.exercise.model.entity.GeneratedExercise;
import com.gamified.application.exercise.model.entity.PromptTemplate;
import com.gamified.application.exercise.repository.ExerciseRepository;
import com.gamified.application.exercise.repository.GeneratedExerciseRepository;
import com.gamified.application.exercise.repository.PromptTemplateRepository;
import com.gamified.application.learning.repository.LearningRepository;
import com.gamified.application.learning.model.entity.LearningPoint;
import com.gamified.application.exercise.service.AzureAiClient;
import com.gamified.application.shared.exception.ResourceNotFoundException;
import com.gamified.application.shared.model.event.DomainEvent;
import com.gamified.application.learning.service.LearningPointExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio Exercise
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final LearningRepository learningRepository; // Para obtener información del learning point
    private final GeneratedExerciseRepository generatedExerciseRepository; // Para el pool de ejercicios
    private final PromptTemplateRepository promptTemplateRepository; // Para las plantillas de prompts
    private final PromptBuilderService promptBuilderService; // Para construir prompts dinámicos CON PromptTemplate
    private final ExercisePromptBuilder exercisePromptBuilder; // Para fallback SIN PromptTemplate
    private final AzureAiClient azureAiClient; // Para llamar a la IA (se implementará en paso 3)
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final LearningPointExerciseService learningPointExerciseService; // NUEVO: Para manejar tabla intermedia

    @Override
    public ExerciseResponseDto.NextExerciseDto getNextExercise(Integer studentId, Integer learningPointId, String difficulty) {
        log.info("Obteniendo siguiente ejercicio para estudiante {} en learning point {} con dificultad {}", 
                studentId, learningPointId, difficulty);
        
        // 1. Verificar que el learning point existe
        Optional<LearningPoint> learningPointOpt = learningRepository.findLearningPointById(learningPointId);
        if (learningPointOpt.isEmpty()) {
            throw new ResourceNotFoundException("Learning point no encontrado con ID: " + learningPointId);
        }
        LearningPoint learningPoint = learningPointOpt.get();
        
        // 2. ACTUALIZADO: Buscar ejercicio template usando la nueva estructura learning_point_exercise
        Optional<Exercise> exerciseTemplateOpt = findNextExerciseTemplateForLearningPoint(
                studentId, learningPointId, difficulty);
        
        if (exerciseTemplateOpt.isEmpty()) {
            // NUEVO: Cuando no hay ejercicios pre-asignados, generar dinámicamente
            log.info("No hay ejercicios pre-asignados para learning point {}, generando dinámicamente", learningPointId);
            return generateDynamicExercisesForLearningPoint(studentId, learningPointId, difficulty);
        }
        
        Exercise exerciseTemplate = exerciseTemplateOpt.get();
        
        // 3. Intentar obtener ejercicio del pool (ejercicio ya generado)
        List<GeneratedExercise> availableExercises = generatedExerciseRepository
                .findAvailableByTemplate(exerciseTemplate.getId(), 1);
        
        GeneratedExercise generatedExercise;
        
        if (!availableExercises.isEmpty()) {
            // 3a. Usar ejercicio del pool
            generatedExercise = availableExercises.get(0);
            log.info("Usando ejercicio del pool: {}", generatedExercise.getId());
        } else {
            // 3b. Generar nuevo ejercicio con IA
            log.info("No hay ejercicios en el pool, generando nuevo ejercicio para plantilla {}", 
                    exerciseTemplate.getId());
            generatedExercise = generateNewExercise(exerciseTemplate, learningPoint, studentId, difficulty);
        }
        
        // 4. Verificar intentos previos del estudiante en esta plantilla
        Integer previousAttempts = exerciseRepository.countAttemptsByStudentAndTemplate(
                studentId, exerciseTemplate.getId());
        Boolean hasAttempts = previousAttempts > 0;
        
        // 5. Construir respuesta para el frontend
        return buildNextExerciseResponse(generatedExercise, exerciseTemplate, learningPoint, 
                hasAttempts, previousAttempts);
    }

    /**
     * Genera un nuevo ejercicio usando IA cuando no hay ejercicios disponibles en el pool
     */
    private GeneratedExercise generateNewExercise(Exercise exerciseTemplate, LearningPoint learningPoint, 
                                                 Integer studentId, String difficulty) {
        try {
            // 1. INTENTAR OBTENER PROMPTTEMPLATE APROPIADO
            String builtPrompt;
            PromptTemplate promptTemplate = null;
            
            if (exerciseTemplate.getPromptTemplateId() != null) {
                // 1a. Usar PromptTemplate específico del ejercicio
                Optional<PromptTemplate> promptTemplateOpt = promptTemplateRepository
                        .findById(exerciseTemplate.getPromptTemplateId());
                
                if (promptTemplateOpt.isPresent()) {
                    promptTemplate = promptTemplateOpt.get();
                    // ACTUALIZADO: Obtener Unit en lugar de LearningPoint para el prompt
                    Optional<com.gamified.application.learning.model.entity.Unit> unitOpt = 
                            learningRepository.findUnitById(exerciseTemplate.getUnitId());
                    com.gamified.application.learning.model.entity.Unit unit = unitOpt.orElse(null);
                    
                    builtPrompt = promptBuilderService.buildPrompt(
                            promptTemplate, exerciseTemplate, unit, studentId, difficulty);
                    log.info("Usando PromptTemplate específico: {}", promptTemplate.getName());
                } else {
                    log.warn("PromptTemplate con ID {} no encontrado, usando fallback", exerciseTemplate.getPromptTemplateId());
                    builtPrompt = buildFallbackPrompt(exerciseTemplate, studentId);
                }
            } else {
                // 1b. Buscar PromptTemplate por exerciseSubtype
                // TODO: Implementar búsqueda por exerciseSubtype cuando se agregue el método al repositorio
                log.warn("No hay promptTemplateId configurado para ejercicio {}, usando fallback", exerciseTemplate.getId());
                builtPrompt = buildFallbackPrompt(exerciseTemplate, studentId);
            }
            
            // 2. Validar prompt construido antes de enviar a IA
            log.debug("Prompt construido length: {}", builtPrompt != null ? builtPrompt.length() : "null");
            log.debug("PromptTemplate presente: {}", promptTemplate != null ? promptTemplate.getName() : "null");
            
            if (promptTemplate != null && !validateBuiltPrompt(builtPrompt)) {
                log.warn("Prompt construido no es válido, usando fallback");
                log.debug("Prompt que falló validación: {}", builtPrompt);
                builtPrompt = buildFallbackPrompt(exerciseTemplate, studentId);
            }
            
            // 3. Llamar a Azure AI
            String aiResponseJson = azureAiClient.generateExerciseContent(builtPrompt);
            
            // 4. Validar respuesta de IA si hay PromptTemplate
            if (promptTemplate != null && !promptBuilderService.validateAiResponse(aiResponseJson, promptTemplate)) {
                log.warn("Respuesta de IA no cumple validationRules, pero continuando...");
            }
            
            // 5. Guardar ejercicio generado en el pool usando los nombres de campo correctos
            GeneratedExercise generatedExercise = GeneratedExercise.builder()
                    .exerciseTemplateId(exerciseTemplate.getId())
                    // Nota: En el esquema real no hay student_profile_id en generated_exercise
                    .generatedContentJson(aiResponseJson) // Campo real: generated_content_json
                    .correctAnswerHash(generateAnswerHash(aiResponseJson)) // Campo real: correct_answer_hash
                    .generationPrompt(builtPrompt) // Campo real: generation_prompt
                    .aiModelVersion("Phi-4-mini-instruct-v1") // Campo real: ai_model_version
                    .createdAt(LocalDateTime.now()) // Campo real: created_at
                    .build();
            
            Long generatedId = generatedExerciseRepository.save(generatedExercise);
            generatedExercise.setId(generatedId);
            
            log.info("Nuevo ejercicio generado con ID: {}", generatedId);
            return generatedExercise;
            
        } catch (Exception e) {
            log.error("Error generando nuevo ejercicio: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo generar el ejercicio: " + e.getMessage());
        }
    }

    /**
     * Construye un prompt fallback cuando no hay PromptTemplate disponible
     */
    private String buildFallbackPrompt(Exercise exerciseTemplate, Integer studentId) {
        return exercisePromptBuilder.buildPromptForExercise(exerciseTemplate, studentId);
    }

    /**
     * Valida que un prompt construido sea válido para enviar a IA
     * (Diferente a validateTemplate que busca placeholders)
     */
    private boolean validateBuiltPrompt(String builtPrompt) {
        if (builtPrompt == null || builtPrompt.trim().isEmpty()) {
            log.debug("Prompt está vacío o es null");
            return false;
        }
        
        if (builtPrompt.length() < 50) {
            log.debug("Prompt demasiado corto: {} caracteres", builtPrompt.length());
            return false;
        }
        
        // Verificar que no tenga placeholders sin reemplazar
        if (builtPrompt.contains("{{") && builtPrompt.contains("}}")) {
            log.debug("Prompt contiene placeholders sin reemplazar");
            return false;
        }
        
        // Verificar que tenga palabras clave mínimas para un ejercicio matemático
        String lowerPrompt = builtPrompt.toLowerCase();
        if (!lowerPrompt.contains("ejercicio") && !lowerPrompt.contains("pregunta") && 
            !lowerPrompt.contains("problema") && !lowerPrompt.contains("question")) {
            log.debug("Prompt no parece ser de un ejercicio educativo");
            return false;
        }
        
        log.debug("Prompt construido es válido: {} caracteres", builtPrompt.length());
        return true;
    }

    /**
     * Simula la respuesta de la IA (temporal hasta implementar Azure AI)
     */
    private String simulateAiResponse(Exercise exerciseTemplate, String difficulty) {
        // Simulación básica de respuesta JSON de la IA
        return """
            {
                "title": "Ejercicio de %s",
                "instructions": "Resuelve el siguiente problema de nivel %s",
                "options": ["Opción A", "Opción B", "Opción C", "Opción D"],
                "correct_answer": "Opción A",
                "explanation": "La respuesta correcta es A porque...",
                "hints": ["Pista 1: Revisa los conceptos básicos", "Pista 2: Piensa en los ejemplos vistos"]
            }
            """.formatted(exerciseTemplate.getTitle(), difficulty);
    }

    /**
     * Genera un hash de la respuesta correcta para validaciones rápidas
     */
    private String generateAnswerHash(String aiResponseJson) {
        try {
            // Extraer la respuesta correcta del JSON y generar hash
            // Por simplicidad, usamos un hash básico
            return String.valueOf(aiResponseJson.hashCode());
        } catch (Exception e) {
            return "hash_error";
        }
    }

    /**
     * Extrae la respuesta correcta del JSON generado por IA
     */
    private String extractCorrectAnswerFromAiJson(String aiContentJson) {
        try {
            Map<String, Object> aiContent = objectMapper.readValue(
                    aiContentJson, new TypeReference<Map<String, Object>>() {});
            
            Object correctAnswer = aiContent.get("correct_answer");
            return correctAnswer != null ? correctAnswer.toString() : "No disponible";
        } catch (Exception e) {
            log.error("Error extrayendo respuesta correcta del JSON: {}", e.getMessage());
            return "Error procesando respuesta";
        }
    }

    /**
     * Construye la respuesta final para el frontend con la estructura requerida según GUIA.md
     */
    private ExerciseResponseDto.NextExerciseDto buildNextExerciseResponse(
            GeneratedExercise generatedExercise, Exercise exerciseTemplate, 
            LearningPoint learningPoint, Boolean hasAttempts, Integer previousAttempts) {
        
        try {
            // 1. Parsear el contenido JSON generado por la IA
            Map<String, Object> aiContent = objectMapper.readValue(
                    generatedExercise.getGeneratedContentJson(), 
                    new TypeReference<Map<String, Object>>() {});
            
            // 2. Obtener información del tipo de ejercicio
            Optional<ExerciseType> exerciseTypeOpt = exerciseRepository
                    .findExerciseTypeById(exerciseTemplate.getExerciseTypeId());
            String exerciseTypeName = exerciseTypeOpt
                    .map(ExerciseType::getName)
                    .orElse("multiple_choice");
            
            // 3. Determinar render_variant según el tipo de ejercicio
            String renderVariant = determineRenderVariant(exerciseTypeName);
            
            // 4. Construir configuración del ejercicio según GUIA.md
            ExerciseResponseDto.ExerciseConfigDto config = buildExerciseConfig(
                    exerciseTypeName, exerciseTemplate, aiContent);
            
            // 5. Construir contenido del ejercicio
            ExerciseResponseDto.ExerciseContentDto content = buildExerciseContent(aiContent);
            
            // 6. Construir respuesta completa según estructura de GUIA.md
            return ExerciseResponseDto.NextExerciseDto.builder()
                    .generatedExerciseId(generatedExercise.getId())
                    .exerciseTemplateId(exerciseTemplate.getId())
                    .exerciseType(exerciseTypeName)
                    .renderVariant(renderVariant)
                    .difficultyLevel(generatedExercise.getDifficultyLevel())
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
     * Construye la configuración del ejercicio según el tipo y GUIA.md
     */
    private ExerciseResponseDto.ExerciseConfigDto buildExerciseConfig(
            String exerciseTypeName, Exercise exerciseTemplate, Map<String, Object> aiContent) {
        
        ExerciseResponseDto.ExerciseConfigDto.ExerciseConfigDtoBuilder configBuilder = 
                ExerciseResponseDto.ExerciseConfigDto.builder();
        
        // 1. PRIMERO: Usar configuración específica del ejercicio si existe
        Map<String, Object> exerciseConfig = exerciseTemplate.getExerciseConfigAsMap();
        if (!exerciseConfig.isEmpty()) {
            // Aplicar configuración específica del ejercicio
            configBuilder
                .showTimer((Boolean) exerciseConfig.getOrDefault("showTimer", true))
                .maxTime((Integer) exerciseConfig.getOrDefault("maxTime", 300))
                .allowPartialScore((Boolean) exerciseConfig.getOrDefault("allowPartialScore", true))
                .showHints((Boolean) exerciseConfig.getOrDefault("showHints", true))
                .allowRetry((Boolean) exerciseConfig.getOrDefault("allowRetry", false))
                .itemCount((Integer) exerciseConfig.getOrDefault("itemCount", null))
                .shuffleItems((Boolean) exerciseConfig.getOrDefault("shuffleItems", false));
            
            return configBuilder.build();
        }
        
        // 2. SEGUNDO: Usar configuración por defecto del tipo de ejercicio
        if (exerciseTemplate.getExerciseType() != null) {
            Map<String, Object> defaultConfig = exerciseTemplate.getExerciseType().getDefaultConfigAsMap();
            if (!defaultConfig.isEmpty()) {
                // Aplicar configuración por defecto del tipo
                configBuilder
                    .showTimer((Boolean) defaultConfig.getOrDefault("showTimer", true))
                    .maxTime((Integer) defaultConfig.getOrDefault("maxTime", 300))
                    .allowPartialScore((Boolean) defaultConfig.getOrDefault("allowPartialScore", true))
                    .showHints((Boolean) defaultConfig.getOrDefault("showHints", true))
                    .allowRetry((Boolean) defaultConfig.getOrDefault("allowRetry", false))
                    .itemCount((Integer) defaultConfig.getOrDefault("itemCount", null))
                    .shuffleItems((Boolean) defaultConfig.getOrDefault("shuffleItems", false));
                
                return configBuilder.build();
    }


}
        
        // 3. TERCERO: Configuración fallback por tipo (código existente como respaldo)
        configBuilder
                .showTimer(true)
                .allowPartialScore(true)
                .showHints(true)
                .allowRetry(false);

        // Configuración específica por tipo como antes
        switch (exerciseTypeName.toLowerCase()) {
            case "drag_and_drop" -> {
                configBuilder
                    .maxTime(420) // 7 minutos
                    .allowPartialScore(true)
                    .shuffleItems(true)
                    .itemCount(getItemCount(aiContent));
            }
            case "multiple_choice" -> {
                configBuilder
                    .maxTime(300) // 5 minutos
                    .allowPartialScore(false)
                    .shuffleItems(true);
            }
            case "numeric_input" -> {
                configBuilder
                    .maxTime(480) // 8 minutos
                    .allowPartialScore(false)
                    .shuffleItems(false);
            }
            default -> {
                configBuilder
                    .maxTime(exerciseTemplate.getEstimatedTimeMinutes() * 60)
                    .allowPartialScore(false)
                    .shuffleItems(false);
            }
        }

        return configBuilder.build();
    }

    /**
     * Construye el contenido del ejercicio desde el JSON de la IA
     */
    private ExerciseResponseDto.ExerciseContentDto buildExerciseContent(Map<String, Object> aiContent) {
        return ExerciseResponseDto.ExerciseContentDto.builder()
                .title((String) aiContent.get("title"))
                .instructions((String) aiContent.get("instructions"))
                .options(aiContent.get("options")) // Puede ser List, Map, etc.
                .correctAnswer(aiContent.get("correct_answer"))
                .explanation((String) aiContent.get("explanation"))
                .hints(parseHints(aiContent.get("hints")))
                .imageUrl((String) aiContent.get("image_url"))
                .build();
    }

    /**
     * Obtiene el número de elementos para ejercicios que lo requieren
     */
    private Integer getItemCount(Map<String, Object> aiContent) {
        Object items = aiContent.get("items");
        if (items instanceof List) {
            return ((List<?>) items).size();
        }
        Object options = aiContent.get("options");
        if (options instanceof List) {
            return ((List<?>) options).size();
        }
        return 4; // Valor por defecto
    }

    /**
     * Parsea las pistas del JSON de la IA
     */
    @SuppressWarnings("unchecked")
    private List<String> parseHints(Object hintsObj) {
        if (hintsObj instanceof List) {
            return (List<String>) hintsObj;
        }
        return List.of("Revisa los conceptos fundamentales", "Piensa paso a paso");
    }

    /**
     * Determina la variante de renderizado según el tipo de ejercicio
     */
    private String determineRenderVariant(String exerciseType) {
        return switch (exerciseType) {
            case "multiple_choice" -> "radio-buttons";
            case "drag_and_drop" -> "drag-to-sort";
            case "numeric_input" -> "number-input";
            case "text_input" -> "text-area";
            case "true_false" -> "toggle-buttons";
            case "ordering" -> "sortable-list";
            case "matching" -> "connect-pairs";
            case "fill_blanks" -> "input-blanks";
            default -> "default-renderer";
        };
    }

    @Override
    public ExerciseResponseDto.AttemptResultDto submitExerciseAttempt(ExerciseRequestDto.SubmitAttemptDto request) {
        log.info("Procesando intento de ejercicio generado {} por estudiante {}", 
                request.getExerciseId(), request.getStudentProfileId());
        
        // En el nuevo sistema, el exerciseId debería ser el ID del GeneratedExercise
        // Buscar el ejercicio generado
        Optional<GeneratedExercise> generatedExerciseOpt = generatedExerciseRepository.findById(request.getExerciseId().longValue());
        if (generatedExerciseOpt.isEmpty()) {
            throw new ResourceNotFoundException("Ejercicio generado no encontrado con ID: " + request.getExerciseId());
        }
        
        GeneratedExercise generatedExercise = generatedExerciseOpt.get();
        
        // Obtener la plantilla de ejercicio
        Optional<Exercise> exerciseTemplateOpt = exerciseRepository.findExerciseById(generatedExercise.getExerciseTemplateId());
        if (exerciseTemplateOpt.isEmpty()) {
            throw new ResourceNotFoundException("Plantilla de ejercicio no encontrada con ID: " + generatedExercise.getExerciseTemplateId());
        }
        Exercise exerciseTemplate = exerciseTemplateOpt.get();
        
        // Extraer respuesta correcta del JSON generado por IA
        String correctAnswer = extractCorrectAnswerFromAiJson(generatedExercise.getGeneratedContentJson());
        
        // Calcular el número de intento
        Integer attemptNumber = exerciseRepository.countAttemptsByStudentAndTemplate(
                request.getStudentProfileId(), exerciseTemplate.getId()) + 1;
        
        // Evaluar la respuesta
        Boolean isCorrect = evaluateAnswer(correctAnswer, request.getSubmittedAnswer());
        Double score = calculateScore(isCorrect, request.getTimeSpentSeconds(), 
                request.getHintsUsed(), exerciseTemplate.getEstimatedTimeMinutes());
        
        // Generar feedback
        String feedback = generateFeedback(isCorrect, correctAnswer, request.getSubmittedAnswer());
        
        // Crear el intento usando los nombres de campo correctos del esquema real
        ExerciseAttempt attempt = ExerciseAttempt.builder()
                .exerciseTemplateId(exerciseTemplate.getId()) // Campo real: exercise_template_id
                .generatedExerciseId(generatedExercise.getId()) // Campo real: generated_exercise_id
                .studentProfileId(request.getStudentProfileId()) // Campo real: student_profile_id
                .attemptNumber(attemptNumber) // Campo real: attempt_number
                .isCorrect(isCorrect) // Campo real: is_correct (convertido a int en BD)
                .pointsEarned(score.intValue()) // Campo real: points_earned
                .timeSpent(request.getTimeSpentSeconds()) // Campo real: time_spent
                .startedAt(LocalDateTime.now()) // Campo real: started_at
                .completedAt(LocalDateTime.now()) // Campo real: completed_at
                .build();
        
        Integer attemptId = exerciseRepository.createExerciseAttempt(attempt);
        attempt.setId(attemptId);
        
        // Marcar ejercicio generado como usado
        generatedExerciseRepository.markAsUsed(generatedExercise.getId(), attemptId);
        
        // Emitir evento de ejercicio completado para el Rule Engine
        publishExerciseCompletedEvent(exerciseTemplate, attempt);
        
        // Buscar siguiente ejercicio sugerido
        ExerciseResponseDto.NextExerciseDto nextExercise = null;
        try {
            nextExercise = getNextExercise(request.getStudentProfileId(), 
                    exerciseTemplate.getLearningPointId(), exerciseTemplate.getDifficulty());
        } catch (Exception e) {
            log.warn("No se pudo obtener siguiente ejercicio: {}", e.getMessage());
        }
        
        return mapToAttemptResultDto(attempt, correctAnswer, nextExercise);
    }

    @Override
    public List<ExerciseResponseDto.AttemptHistoryDto> getAttemptHistory(Integer studentId, Integer limit) {
        log.info("Obteniendo historial de intentos para estudiante {}", studentId);
        
        List<ExerciseAttempt> attempts = exerciseRepository.findAttemptHistoryByStudent(studentId, limit);
        
        return attempts.stream()
                .map(this::mapToAttemptHistoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseResponseDto.CompletedExerciseDto> getCompletedExercises(Integer studentId) {
        log.info("Obteniendo ejercicios completados para estudiante {}", studentId);
        
        List<Exercise> completedExercises = exerciseRepository.findCompletedExercisesByStudent(studentId);
        
        return completedExercises.stream()
                .map(exercise -> mapToCompletedExerciseDto(exercise, studentId))
                .collect(Collectors.toList());
    }

    @Override
    public ExerciseResponseDto.StudentExerciseStatsDto getStudentExerciseStats(Integer studentId) {
        log.info("Obteniendo estadísticas de ejercicios para estudiante {}", studentId);
        
        Optional<ExerciseRepository.StudentExerciseStats> statsOpt = 
                exerciseRepository.getStudentExerciseStats(studentId);
        
        if (statsOpt.isEmpty()) {
            // Retornar estadísticas vacías
            return ExerciseResponseDto.StudentExerciseStatsDto.builder()
                    .totalExercisesAttempted(0)
                    .totalExercisesCompleted(0)
                    .averageScore(0.0)
                    .totalTimeSpentMinutes(0)
                    .preferredDifficulty("medium")
                    .exerciseTypeStats(new ArrayList<>())
                    .build();
        }
        
        ExerciseRepository.StudentExerciseStats stats = statsOpt.get();
        List<ExerciseRepository.ExerciseTypeStats> typeStats = 
                exerciseRepository.getStudentExerciseTypeStats(studentId);
        
        return mapToStudentExerciseStatsDto(stats, typeStats);
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    /**
     * Evalúa si una respuesta es correcta
     */
    private Boolean evaluateAnswer(String correctAnswer, String submittedAnswer) {
        if (correctAnswer == null || submittedAnswer == null) {
            return false;
        }
        
        // Normalizar respuestas (remover espacios, convertir a minúsculas)
        String normalizedCorrect = correctAnswer.trim().toLowerCase();
        String normalizedSubmitted = submittedAnswer.trim().toLowerCase();
        
        return normalizedCorrect.equals(normalizedSubmitted);
    }

    /**
     * Calcula la puntuación basada en corrección, tiempo y pistas usadas
     */
    private Double calculateScore(Boolean isCorrect, Integer timeSpent, Integer hintsUsed, Integer estimatedTime) {
        if (!isCorrect) {
            return 0.0;
        }
        
        double baseScore = 100.0;
        
        // Penalizar por tiempo excedido
        if (timeSpent != null && estimatedTime != null && estimatedTime > 0) {
            double timeRatio = (double) timeSpent / (estimatedTime * 60); // Convert minutes to seconds
            if (timeRatio > 1.0) {
                baseScore *= Math.max(0.5, 1.0 / timeRatio); // Minimum 50% if over time
            }
        }
        
        // Penalizar por uso de pistas
        if (hintsUsed != null && hintsUsed > 0) {
            baseScore *= Math.max(0.3, 1.0 - (hintsUsed * 0.15)); // 15% per hint, minimum 30%
        }
        
        return Math.round(baseScore * 100.0) / 100.0; // Round to 2 decimal places
    }

    /**
     * Genera feedback personalizado
     */
    private String generateFeedback(Boolean isCorrect, String correctAnswer, String submittedAnswer) {
        try {
            if (isCorrect) {
                return objectMapper.writeValueAsString(java.util.Map.of(
                    "type", "success",
                    "message", "¡Correcto! Excelente trabajo.",
                    "correctAnswer", correctAnswer
                ));
            } else {
                return objectMapper.writeValueAsString(java.util.Map.of(
                    "type", "error",
                    "message", "Respuesta incorrecta. La respuesta correcta era: " + correctAnswer,
                    "correctAnswer", correctAnswer,
                    "submittedAnswer", submittedAnswer
                ));
            }
        } catch (Exception e) {
            log.error("Error generando feedback JSON: {}", e.getMessage());
            return isCorrect ? "Correcto" : "Incorrecto";
        }
    }

    /**
     * Publica evento de ejercicio completado para el Rule Engine
     */
    private void publishExerciseCompletedEvent(Exercise exercise, ExerciseAttempt attempt) {
        try {
            // Obtener tipo de ejercicio
            String exerciseType = exerciseRepository.findExerciseTypeById(exercise.getExerciseTypeId())
                .map(ExerciseType::getName)
                .orElse("Unknown");

            DomainEvent.ExerciseCompletedEvent event = DomainEvent.ExerciseCompletedEvent.builder()
                .exerciseId(exercise.getId())
                .studentProfileId(attempt.getStudentProfileId())
                .learningPointId(exercise.getLearningPointId())
                .difficulty(exercise.getDifficulty())
                .isCorrect(attempt.getIsCorrect())
                .score(attempt.getScore())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .hintsUsed(attempt.getHintsUsed())
                .attemptNumber(attempt.getAttemptNumber())
                .exerciseType(exerciseType)
                .build();
                
            // Configurar campos base manualmente
            event.setEventId(java.util.UUID.randomUUID().toString());
            event.setOccurredAt(java.time.LocalDateTime.now());
            event.setEventType("EXERCISE_COMPLETED");
            event.setUserId(attempt.getStudentProfileId());

            eventPublisher.publishEvent(event);
            log.debug("Evento ExerciseCompleted publicado para estudiante {} y ejercicio {}", 
                attempt.getStudentProfileId(), exercise.getId());

        } catch (Exception e) {
            log.error("Error publicando evento de ejercicio completado: {}", e.getMessage(), e);
        }
    }

    // ===================================================================
    // MAPPERS
    // ===================================================================

    /**
     * Mapea un Exercise template a NextExerciseDto (para compatibilidad hacia atrás)
     * Este método se eliminará cuando se complete la migración al nuevo flujo
     */
    private ExerciseResponseDto.NextExerciseDto mapToNextExerciseDto(
            Exercise exercise, LearningPoint learningPoint, String exerciseTypeName, 
            Boolean hasAttempts, Integer previousAttempts) {
        
        // Como Exercise ahora es solo una plantilla, usar valores predeterminados
        List<String> defaultHints = List.of("Revisa los conceptos básicos", "Piensa paso a paso");
        
        // Crear configuración básica para el ejercicio template
        ExerciseResponseDto.ExerciseConfigDto config = ExerciseResponseDto.ExerciseConfigDto.builder()
                .showTimer(true)
                .maxTime(exercise.getEstimatedTime() != null ? exercise.getEstimatedTime() : 300)
                .allowPartialScore(true)
                .itemCount(4) // Valor predeterminado
                .shuffleItems(true)
                .showHints(true)
                .allowRetry(true)
                .build();
        
        // Crear contenido del ejercicio usando la información de la plantilla
        ExerciseResponseDto.ExerciseContentDto content = ExerciseResponseDto.ExerciseContentDto.builder()
                .title(exercise.getTitle())
                .instructions(exercise.getInstructions() != null ? exercise.getInstructions() : exercise.getDescription())
                .options(List.of()) // Las opciones se generarán dinámicamente por IA
                .correctAnswer("Se generará dinámicamente") // La respuesta se generará por IA
                .explanation("El ejercicio se generará usando IA basado en la plantilla: " + exercise.getTitle())
                .hints(defaultHints)
                .build();
        
        return ExerciseResponseDto.NextExerciseDto.builder()
                .generatedExerciseId(exercise.getId().longValue()) // Nota: Para exercises legacy
                .exerciseTemplateId(exercise.getId()) // Para exercises legacy, el template es el mismo
                .exerciseType(exerciseTypeName.toLowerCase().replace(" ", "_"))
                .renderVariant("default")
                .difficultyLevel(exercise.getDifficulty())
                .config(config)
                .content(content)
                .learningPointId(exercise.getLearningPointId())
                .learningPointTitle(learningPoint.getTitle())
                .hasAttempts(hasAttempts)
                .previousAttempts(previousAttempts)
                .build();
    }

    private ExerciseResponseDto.AttemptResultDto mapToAttemptResultDto(
            ExerciseAttempt attempt, String correctAnswer, ExerciseResponseDto.NextExerciseDto nextExercise) {
        
        return ExerciseResponseDto.AttemptResultDto.builder()
                .attemptId(attempt.getId())
                .exerciseId(attempt.getExerciseId())
                .isCorrect(attempt.getIsCorrect())
                .score(attempt.getScore())
                .feedback(attempt.getFeedback())
                .correctAnswer(correctAnswer)
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .attemptNumber(attempt.getAttemptNumber())
                .submittedAt(attempt.getSubmittedAt())
                .nextExercise(nextExercise)
                .build();
    }

    private ExerciseResponseDto.AttemptHistoryDto mapToAttemptHistoryDto(ExerciseAttempt attempt) {
        // Obtener información adicional del ejercicio
        Optional<Exercise> exerciseOpt = exerciseRepository.findExerciseById(attempt.getExerciseId());
        String exerciseTitle = exerciseOpt.map(Exercise::getTitle).orElse("Ejercicio desconocido");
        String difficulty = exerciseOpt.map(Exercise::getDifficulty).orElse("medium");
        Integer learningPointId = exerciseOpt.map(Exercise::getLearningPointId).orElse(null);
        
        // Obtener información del tipo de ejercicio
        String exerciseTypeName = "Tipo desconocido";
        if (exerciseOpt.isPresent()) {
            Optional<ExerciseType> typeOpt = exerciseRepository.findExerciseTypeById(
                    exerciseOpt.get().getExerciseTypeId());
            exerciseTypeName = typeOpt.map(ExerciseType::getName).orElse("Tipo desconocido");
        }
        
        // Obtener título del learning point
        String learningPointTitle = "Learning Point";
        if (learningPointId != null) {
            Optional<LearningPoint> lpOpt = learningRepository.findLearningPointById(learningPointId);
            learningPointTitle = lpOpt.map(LearningPoint::getTitle).orElse("Learning Point");
        }
        
        return ExerciseResponseDto.AttemptHistoryDto.builder()
                .attemptId(attempt.getId())
                .exerciseId(attempt.getExerciseId())
                .exerciseTitle(exerciseTitle)
                .exerciseTypeName(exerciseTypeName)
                .difficulty(difficulty)
                .isCorrect(attempt.getIsCorrect())
                .score(attempt.getScore())
                .timeSpentSeconds(attempt.getTimeSpentSeconds())
                .attemptNumber(attempt.getAttemptNumber())
                .submittedAt(attempt.getSubmittedAt())
                .learningPointId(learningPointId)
                .learningPointTitle(learningPointTitle)
                .build();
    }

    private ExerciseResponseDto.CompletedExerciseDto mapToCompletedExerciseDto(Exercise exercise, Integer studentId) {
        // Obtener información del tipo de ejercicio
        Optional<ExerciseType> typeOpt = exerciseRepository.findExerciseTypeById(exercise.getExerciseTypeId());
        String exerciseTypeName = typeOpt.map(ExerciseType::getName).orElse("Tipo desconocido");
        
        // Obtener título del learning point
        Optional<LearningPoint> lpOpt = learningRepository.findLearningPointById(exercise.getLearningPointId());
        String learningPointTitle = lpOpt.map(LearningPoint::getTitle).orElse("Learning Point");
        
        // Obtener estadísticas del estudiante para este ejercicio
        Integer totalAttempts = exerciseRepository.countAttemptsByStudentAndExercise(studentId, exercise.getId());
        Optional<Double> bestScoreOpt = exerciseRepository.getBestScoreByStudentAndExercise(studentId, exercise.getId());
        Double bestScore = bestScoreOpt.orElse(0.0);
        
        // Obtener intentos para calcular promedio
        List<ExerciseAttempt> attempts = exerciseRepository.findAttemptsByStudentAndExercise(studentId, exercise.getId());
        Double averageScore = attempts.stream()
                .filter(attempt -> attempt.getScore() != null)
                .mapToDouble(ExerciseAttempt::getScore)
                .average()
                .orElse(0.0);
        
        Integer averageTimeSeconds = (int) attempts.stream()
                .filter(attempt -> attempt.getTimeSpentSeconds() != null)
                .mapToInt(ExerciseAttempt::getTimeSpentSeconds)
                .average()
                .orElse(0.0);
        
        // Obtener fecha de completado (primer intento correcto)
        LocalDateTime completedAt = attempts.stream()
                .filter(attempt -> attempt.getIsCorrect() != null && attempt.getIsCorrect())
                .map(ExerciseAttempt::getSubmittedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
        
        return ExerciseResponseDto.CompletedExerciseDto.builder()
                .exerciseId(exercise.getId())
                .exerciseTitle(exercise.getTitle())
                .exerciseTypeName(exerciseTypeName)
                .difficulty(exercise.getDifficulty())
                .totalAttempts(totalAttempts)
                .bestScore(bestScore)
                .isCompleted(true)
                .completedAt(completedAt)
                .learningPointId(exercise.getLearningPointId())
                .learningPointTitle(learningPointTitle)
                .averageTimeSeconds(averageTimeSeconds)
                .averageScore(averageScore)
                .build();
    }

    private ExerciseResponseDto.StudentExerciseStatsDto mapToStudentExerciseStatsDto(
            ExerciseRepository.StudentExerciseStats stats, 
            List<ExerciseRepository.ExerciseTypeStats> typeStats) {
        
        List<ExerciseResponseDto.ExerciseTypeStatsDto> typeStatsDtos = typeStats.stream()
                .map(this::mapToExerciseTypeStatsDto)
                .collect(Collectors.toList());
        
        return ExerciseResponseDto.StudentExerciseStatsDto.builder()
                .totalExercisesAttempted(stats.totalExercisesAttempted)
                .totalExercisesCompleted(stats.totalExercisesCompleted)
                .averageScore(stats.averageScore)
                .totalTimeSpentMinutes(stats.totalTimeSpentMinutes)
                .preferredDifficulty(stats.preferredDifficulty)
                .exerciseTypeStats(typeStatsDtos)
                .build();
    }

    private ExerciseResponseDto.ExerciseTypeStatsDto mapToExerciseTypeStatsDto(
            ExerciseRepository.ExerciseTypeStats stats) {
        
        return ExerciseResponseDto.ExerciseTypeStatsDto.builder()
                .exerciseTypeId(stats.exerciseTypeId)
                .exerciseTypeName(stats.exerciseTypeName)
                .totalAttempts(stats.totalAttempts)
                .totalCompleted(stats.totalCompleted)
                .averageScore(stats.averageScore)
                .strongestDifficulty(stats.strongestDifficulty)
                .build();
    }

    /**
     * Parsea un array JSON a Lista de Strings
     */
    private List<String> parseJsonArray(String jsonArray) {
        if (jsonArray == null || jsonArray.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Error parseando JSON array '{}': {}", jsonArray, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * NUEVO: Busca el siguiente ejercicio template para un learning point usando la tabla intermedia
     * Reemplaza el método deprecated findNextExerciseForLearningPoint
     */
    private Optional<Exercise> findNextExerciseTemplateForLearningPoint(Integer studentId, Integer learningPointId, String difficulty) {
        try {
            // 1. Buscar el siguiente ejercicio pendiente en learning_point_exercise
            Optional<com.gamified.application.learning.model.entity.LearningPointExercise> nextPendingOpt = 
                    learningPointExerciseService.getNextPendingExercise(learningPointId);
            
            if (nextPendingOpt.isEmpty()) {
                log.warn("No hay ejercicios pendientes para learning point {}", learningPointId);
                return Optional.empty();
            }
            
            // 2. Obtener la plantilla de ejercicio correspondiente
            Integer exerciseTemplateId = nextPendingOpt.get().getExerciseTemplateId();
            Optional<Exercise> exerciseTemplateOpt = exerciseRepository.findExerciseById(exerciseTemplateId);
            
            if (exerciseTemplateOpt.isEmpty()) {
                log.error("Exercise template {} no encontrado para learning point {}", exerciseTemplateId, learningPointId);
                return Optional.empty();
            }
            
            return exerciseTemplateOpt;
            
        } catch (Exception e) {
            log.error("Error buscando exercise template para learning point {}: {}", learningPointId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * NUEVO: Genera dinámicamente ejercicios para un learning point cuando no hay pre-asignados
     * Basado en el learning path del estudiante, su nivel actual, competencias y unit relacionada
     */
    private ExerciseResponseDto.NextExerciseDto generateDynamicExercisesForLearningPoint(
            Integer studentId, Integer learningPointId, String difficulty) {
        
        log.info("Iniciando generación dinámica de ejercicios para estudiante {} en learning point {}", 
                studentId, learningPointId);
        
        try {
            // 1. Obtener el learning path del estudiante para este learning point
            Optional<com.gamified.application.learning.model.entity.LearningPath> learningPathOpt = 
                    learningRepository.findLearningPathByStudentAndLearningPoint(studentId, learningPointId);
            
            if (learningPathOpt.isEmpty()) {
                throw new ResourceNotFoundException("No se encontró learning path para estudiante " + studentId + " y learning point " + learningPointId);
            }
            
            com.gamified.application.learning.model.entity.LearningPath learningPath = learningPathOpt.get();
            
            // 2. Obtener la unit relacionada con este learning path
            Optional<com.gamified.application.learning.model.entity.Unit> unitOpt = 
                    learningRepository.findUnitById(learningPath.getUnitsId());
            
            if (unitOpt.isEmpty()) {
                throw new ResourceNotFoundException("Unit no encontrada para learning path " + learningPath.getId());
            }
            
            com.gamified.application.learning.model.entity.Unit unit = unitOpt.get();
            
            // 3. Buscar exercise templates disponibles para esta unit
            List<Exercise> availableTemplates = exerciseRepository.findExercisesByUnitId(unit.getId());
            
            if (availableTemplates.isEmpty()) {
                throw new ResourceNotFoundException("No hay plantillas de ejercicio disponibles para la unit " + unit.getId());
            }
            
            // 4. Filtrar por dificultad si se especifica
            List<Exercise> filteredTemplates = availableTemplates.stream()
                    .filter(exercise -> difficulty == null || difficulty.equals(exercise.getDifficulty()))
                    .collect(Collectors.toList());
            
            if (filteredTemplates.isEmpty()) {
                // Usar todas las plantillas si el filtro por dificultad no devuelve resultados
                filteredTemplates = availableTemplates;
                log.warn("No se encontraron ejercicios con dificultad {}, usando todos los disponibles", difficulty);
            }
            
            // 5. Generar 1 ejercicio y asignarlo al learning point
            int exercisesToGenerate = 1; // CORREGIDO: Solo generar 1 ejercicio por petición
            List<com.gamified.application.learning.model.entity.LearningPointExercise> assignedExercises = new ArrayList<>();
            
            for (int i = 0; i < exercisesToGenerate; i++) {
                Exercise template = filteredTemplates.get(i % filteredTemplates.size());
                
                // Generar ejercicio usando la IA
                GeneratedExercise generatedExercise = generateNewExercise(template, 
                        learningRepository.findLearningPointById(learningPointId).get(), studentId, difficulty);
                
                // Asignar ejercicio al learning point
                com.gamified.application.learning.model.entity.LearningPointExercise assignment = 
                        com.gamified.application.learning.model.entity.LearningPointExercise.builder()
                        .learningPointId(learningPointId)
                        .generatedExerciseId(generatedExercise.getId())
                        .exerciseTemplateId(template.getId())
                        .sequenceOrder(i + 1)
                        .isCompleted(0)
                        .assignedAt(LocalDateTime.now())
                        .isActive(1)
                        .build();
                
                Integer assignmentId = learningPointExerciseService.assignExercise(assignment);
                assignment.setId(assignmentId);
                assignedExercises.add(assignment);
                
                log.info("Ejercicio {} asignado dinámicamente a learning point {} con sequence_order {}", 
                        generatedExercise.getId(), learningPointId, i + 1);
            }
            
            // 6. Retornar el primer ejercicio generado
            com.gamified.application.learning.model.entity.LearningPointExercise firstAssignment = assignedExercises.get(0);
            Optional<GeneratedExercise> firstGeneratedExerciseOpt = 
                    generatedExerciseRepository.findById(firstAssignment.getGeneratedExerciseId());
            
            if (firstGeneratedExerciseOpt.isEmpty()) {
                throw new RuntimeException("Error recuperando el primer ejercicio generado");
            }
            
            GeneratedExercise firstGeneratedExercise = firstGeneratedExerciseOpt.get();
            Optional<Exercise> firstTemplateOpt = exerciseRepository.findExerciseById(firstAssignment.getExerciseTemplateId());
            Exercise firstTemplate = firstTemplateOpt.get();
            
            Optional<LearningPoint> learningPointOpt = learningRepository.findLearningPointById(learningPointId);
            LearningPoint learningPoint = learningPointOpt.get();
            
            log.info("Generación dinámica completada. Retornando primer ejercicio con ID {}", firstGeneratedExercise.getId());
            
            return buildNextExerciseResponse(firstGeneratedExercise, firstTemplate, learningPoint, false, 0);
            
        } catch (Exception e) {
            log.error("Error durante la generación dinámica de ejercicios para learning point {}: {}", 
                    learningPointId, e.getMessage(), e);
            throw new RuntimeException("No se pudieron generar ejercicios dinámicamente: " + e.getMessage());
        }
    }
}