package com.gamified.application.exercise.service;

import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.PromptTemplate;
import com.gamified.application.learning.model.entity.LearningPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servicio para construir prompts dinámicos usando PromptTemplate con separación correcta de responsabilidades
 * 
 * RESPONSABILIDADES:
 * - templateText: Plantilla base con placeholders {{variable}}
 * - generationParameters: Parámetros específicos para la IA (temperatura, tokens, etc.)
 * - validationRules: Reglas para validar la respuesta de la IA
 * - sampleOutput: Ejemplo de salida esperada (para validación y entrenamiento)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromptBuilderService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Construye un prompt completo combinando todos los elementos de PromptTemplate con datos del ejercicio
     * 
     * FLUJO:
     * 1. Tomar templateText como base
     * 2. Reemplazar placeholders con datos reales
     * 3. Aplicar generationParameters del PromptTemplate
     * 4. Aplicar generationRules del Exercise (override específico)
     * 5. Aplicar difficultyParameters del Exercise
     * 6. Agregar validationRules como instrucciones finales
     * 
     * @param promptTemplate Plantilla de prompt con todos sus campos
     * @param exercise Ejercicio base (plantilla)
     * @param unitInfo Información de la unidad (en lugar de learningPoint)
     * @param studentId ID del estudiante (para personalización)
     * @param difficulty Dificultad deseada
     * @return Prompt completo listo para enviar a la IA
     */
    public String buildPrompt(PromptTemplate promptTemplate, Exercise exercise, 
                             com.gamified.application.learning.model.entity.Unit unitInfo, Integer studentId, String difficulty) {
        
        log.debug("Construyendo prompt para ejercicio {} con plantilla {} (subtipo: {})", 
                exercise.getId(), promptTemplate.getName(), promptTemplate.getExerciseSubtype());
        
        log.debug("Template text original: {}", promptTemplate.getTemplateText());
        
        // PASO 1: Plantilla base con placeholders reemplazados
        String basePrompt = buildBasePromptFromTemplate(promptTemplate, exercise, unitInfo, studentId, difficulty);
        
        // PASO 2: Aplicar generationParameters del PromptTemplate
        String enhancedPrompt = applyGenerationParameters(basePrompt, promptTemplate);
        
        // PASO 3: Aplicar generationRules específicas del Exercise (override)
        enhancedPrompt = applyExerciseGenerationRules(enhancedPrompt, exercise);
        
        // PASO 4: Aplicar difficultyParameters del Exercise
        enhancedPrompt = applyDifficultyParameters(enhancedPrompt, exercise);
        
        // PASO 5: Agregar validationRules como instrucciones finales
        String finalPrompt = applyValidationRules(enhancedPrompt, promptTemplate);
        
        log.debug("Prompt final construido. Length: {} caracteres", finalPrompt.length());
        return finalPrompt;
    }

    /**
     * PASO 1: Construye el prompt base reemplazando placeholders en templateText
     */
    private String buildBasePromptFromTemplate(PromptTemplate promptTemplate, Exercise exercise, 
                                             com.gamified.application.learning.model.entity.Unit unitInfo, Integer studentId, String difficulty) {
        
        // Crear mapa de valores para reemplazar placeholders
        Map<String, String> placeholderValues = createPlaceholderValues(
                exercise, unitInfo, studentId, difficulty, promptTemplate);
        
        // Reemplazar placeholders en el templateText
        String basePrompt = replacePlaceholders(promptTemplate.getTemplateText(), placeholderValues);
        
        log.debug("Prompt base construido desde template: {}", promptTemplate.getName());
        return basePrompt;
    }

    /**
     * PASO 2: Aplica generationParameters del PromptTemplate
     * Estos son parámetros globales para este tipo de prompt
     */
    private String applyGenerationParameters(String basePrompt, PromptTemplate promptTemplate) {
        Map<String, Object> genParams = promptTemplate.getGenerationParametersAsMap();
        
        if (genParams.isEmpty()) {
            return basePrompt;
        }
        
        StringBuilder enhanced = new StringBuilder(basePrompt);
        
        // Agregar instrucciones específicas del template
        if (genParams.containsKey("ai_instructions")) {
            enhanced.append("\n\nINSTRUCCIONES ESPECÍFICAS DEL TEMPLATE: ")
                    .append(genParams.get("ai_instructions"));
        }
        
        if (genParams.containsKey("thinking_style")) {
            enhanced.append("\nESTILO DE RAZONAMIENTO: ")
                    .append(genParams.get("thinking_style"));
        }
        
        if (genParams.containsKey("output_format")) {
            enhanced.append("\nFORMATO DE SALIDA REQUERIDO: ")
                    .append(genParams.get("output_format"));
        }
        
        if (genParams.containsKey("context_requirements")) {
            enhanced.append("\nREQUISITOS DE CONTEXTO: ")
                    .append(genParams.get("context_requirements"));
        }
        
        log.debug("GenerationParameters aplicados desde PromptTemplate");
        return enhanced.toString();
    }

    /**
     * PASO 3: Aplica generationRules específicas del Exercise (override específico)
     * Estas reglas tienen prioridad sobre las del template
     */
    private String applyExerciseGenerationRules(String currentPrompt, Exercise exercise) {
        Map<String, Object> generationRules = exercise.getGenerationRulesAsMap();
        
        if (generationRules.isEmpty()) {
            return currentPrompt;
        }
        
        StringBuilder enhanced = new StringBuilder(currentPrompt);
        enhanced.append("\n\nREGLAS ESPECÍFICAS PARA ESTE EJERCICIO:");
        
        if (generationRules.containsKey("complexity_level")) {
            enhanced.append("\n- NIVEL DE COMPLEJIDAD: ").append(generationRules.get("complexity_level"));
        }
        
        if (generationRules.containsKey("required_concepts")) {
            enhanced.append("\n- CONCEPTOS REQUERIDOS: ").append(generationRules.get("required_concepts"));
        }
        
        if (generationRules.containsKey("avoid_concepts")) {
            enhanced.append("\n- EVITAR CONCEPTOS: ").append(generationRules.get("avoid_concepts"));
        }
        
        if (generationRules.containsKey("context_type")) {
            enhanced.append("\n- TIPO DE CONTEXTO: ").append(generationRules.get("context_type"));
        }
        
        if (generationRules.containsKey("special_instructions")) {
            enhanced.append("\n- INSTRUCCIONES ESPECIALES: ").append(generationRules.get("special_instructions"));
        }
        
        log.debug("GenerationRules específicas del Exercise aplicadas");
        return enhanced.toString();
    }

    /**
     * PASO 4: Aplica difficultyParameters del Exercise
     */
    private String applyDifficultyParameters(String currentPrompt, Exercise exercise) {
        Map<String, Object> difficultyParams = exercise.getDifficultyParametersAsMap();
        
        if (difficultyParams.isEmpty()) {
            return currentPrompt;
        }
        
        StringBuilder enhanced = new StringBuilder(currentPrompt);
        enhanced.append("\n\nPARÁMETROS DE DIFICULTAD:");
        
        if (difficultyParams.containsKey("max_steps")) {
            enhanced.append("\n- MÁXIMO PASOS DE SOLUCIÓN: ").append(difficultyParams.get("max_steps"));
        }
        
        if (difficultyParams.containsKey("number_range")) {
            enhanced.append("\n- RANGO NUMÉRICO: ").append(difficultyParams.get("number_range"));
        }
        
        if (difficultyParams.containsKey("allow_decimals")) {
            enhanced.append("\n- PERMITIR DECIMALES: ").append(difficultyParams.get("allow_decimals"));
        }
        
        if (difficultyParams.containsKey("require_calculator")) {
            enhanced.append("\n- REQUIERE CALCULADORA: ").append(difficultyParams.get("require_calculator"));
        }
        
        if (difficultyParams.containsKey("operation_complexity")) {
            enhanced.append("\n- COMPLEJIDAD DE OPERACIONES: ").append(difficultyParams.get("operation_complexity"));
        }
        
        log.debug("DifficultyParameters del Exercise aplicados");
        return enhanced.toString();
    }

    /**
     * PASO 5: Aplica validationRules como instrucciones finales
     * Estas reglas aseguran que la IA genere respuestas en el formato correcto
     */
    private String applyValidationRules(String currentPrompt, PromptTemplate promptTemplate) {
        Map<String, Object> validationRules = promptTemplate.getValidationRulesAsMap();
        
        if (validationRules.isEmpty()) {
            // Reglas de validación por defecto si no hay específicas
            return currentPrompt + "\n\nFORMATO DE SALIDA OBLIGATORIO: JSON válido con campos requeridos.";
        }
        
        StringBuilder enhanced = new StringBuilder(currentPrompt);
        enhanced.append("\n\nREGLAS DE VALIDACIÓN OBLIGATORIAS:");
        
        if (validationRules.containsKey("required_fields")) {
            enhanced.append("\n- CAMPOS OBLIGATORIOS: ").append(validationRules.get("required_fields"));
        }
        
        if (validationRules.containsKey("format_requirements")) {
            enhanced.append("\n- REQUISITOS DE FORMATO: ").append(validationRules.get("format_requirements"));
        }
        
        if (validationRules.containsKey("language_requirements")) {
            enhanced.append("\n- REQUISITOS DE IDIOMA: ").append(validationRules.get("language_requirements"));
        }
        
        if (validationRules.containsKey("content_validation")) {
            enhanced.append("\n- VALIDACIÓN DE CONTENIDO: ").append(validationRules.get("content_validation"));
        }
        
        if (validationRules.containsKey("output_schema")) {
            enhanced.append("\n- ESQUEMA DE SALIDA: ").append(validationRules.get("output_schema"));
        }
        
        log.debug("ValidationRules del PromptTemplate aplicadas");
        return enhanced.toString();
    }

    /**
     * Crea un mapa completo con los valores para reemplazar cada placeholder
     */
    private Map<String, String> createPlaceholderValues(Exercise exercise, 
    com.gamified.application.learning.model.entity.Unit unitInfo, Integer studentId, 
    String difficulty, PromptTemplate promptTemplate) {
        
        log.debug("🔧 Creando valores de placeholders para ejercicio {} con template {}", 
                exercise != null ? exercise.getId() : "null", 
                promptTemplate != null ? promptTemplate.getName() : "null");
        
        Map<String, String> values = new HashMap<>();
        
        // Valores básicos del ejercicio
        values.put("dificultad", difficulty != null ? difficulty : "medium");
        values.put("difficulty", difficulty != null ? difficulty : "medium");
        values.put("nivel", mapDifficultyToSpanish(difficulty));
        
        // ACTUALIZADO: Valores de la unidad en lugar del learning point
        if (unitInfo != null) {
            values.put("tema", unitInfo.getTitle());
            values.put("competencia", unitInfo.getDescription());
            values.put("unidad", unitInfo.getTitle());
            values.put("descripcion", unitInfo.getDescription());
            
            // Mantener compatibilidad con plantillas existentes que usan learning_point
            values.put("learning_point", unitInfo.getTitle());
        }
        
        // Valores del ejercicio
        if (exercise != null) {
            values.put("titulo", exercise.getTitle() != null ? exercise.getTitle() : "");
            values.put("subtipo", exercise.getExerciseSubtype() != null ? exercise.getExerciseSubtype() : "general");
            values.put("tipo", getExerciseTypeName(exercise));
            values.put("tiempo_estimado", String.valueOf(exercise.getEstimatedTimeMinutes()));
        }
        
        // Valores del template
        if (promptTemplate != null) {
            values.put("exercise_subtype", promptTemplate.getExerciseSubtype() != null ? promptTemplate.getExerciseSubtype() : "general");
            values.put("template_name", promptTemplate.getName() != null ? promptTemplate.getName() : "default");
        }
        
        // Valores del estudiante (para futuras personalizaciones)
        values.put("student_id", String.valueOf(studentId));
        
        // Valores adicionales comunes
        values.put("idioma", "español");
        values.put("formato", "JSON");
    
        // ⭐ NUEVO: Agregar criterio_clasificacion basado en el subtipo del ejercicio
        String criterioClasificacion = determinarCriterioClasificacion(exercise, promptTemplate);
        values.put("criterio_clasificacion", criterioClasificacion);
        
        log.debug("🎯 Criterio clasificación determinado: {}", criterioClasificacion);
        log.debug("📋 Placeholders creados: {}", values.keySet());
        log.debug("📝 Todos los valores: {}", values);
        return values;
    }

    /**
     * Determina el criterio de clasificación basado en el ejercicio y template
     */
    private String determinarCriterioClasificacion(Exercise exercise, PromptTemplate promptTemplate) {
        // Lógica para determinar el criterio según el contexto
        if (exercise != null && exercise.getExerciseSubtype() != null) {
            switch (exercise.getExerciseSubtype().toLowerCase()) {
                case "interactive_chart":
                case "chart_classification":
                    return "tamaño"; // Por defecto para gráficos interactivos
                case "color_sorting":
                    return "color";
                case "condition_analysis":
                    return "estado";
                default:
                    return "tamaño";
            }
        }
        
        // Fallback basado en el template
        if (promptTemplate != null && "interactive_chart_main".equals(promptTemplate.getName())) {
            return "tamaño"; // Valor por defecto para este template específico
        }
        
        return "categoría"; // Fallback general
    }

    /**
     * Reemplaza los placeholders en el texto del template
     */
    private String replacePlaceholders(String templateText, Map<String, String> placeholderValues) {
        if (templateText == null) {
            return "";
        }
        
        log.debug("Reemplazando placeholders en template. Placeholders disponibles: {}", placeholderValues.keySet());
        
        String result = templateText;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateText);
        
        while (matcher.find()) {
            String placeholder = matcher.group(1); // El contenido dentro de {{}}
            String value = placeholderValues.get(placeholder);
            
            if (value != null) {
                // Reemplazar {{placeholder}} con el valor
                result = result.replace("{{" + placeholder + "}}", value);
                log.debug("✅ Reemplazado {{{}}} con: {}", placeholder, value);
            } else {
                log.warn("❌ Placeholder no encontrado: {}. Disponibles: {}", placeholder, placeholderValues.keySet());
                // Dejar el placeholder sin reemplazar para debugging
            }
        }
        
        log.debug("Template después de reemplazos: {}", result);
        return result;
    }

    /**
     * Valida que una respuesta de IA cumpla con las validationRules del template
     */
    public boolean validateAiResponse(String aiResponse, PromptTemplate promptTemplate) {
        Map<String, Object> validationRules = promptTemplate.getValidationRulesAsMap();
        
        if (validationRules.isEmpty()) {
            // Validación básica por defecto
            return aiResponse != null && !aiResponse.trim().isEmpty();
        }
        
        try {
            // Validar JSON si es requerido
            if (validationRules.containsKey("format_requirements") && 
                validationRules.get("format_requirements").toString().contains("JSON")) {
                
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.readTree(aiResponse);
            }
            
            // Validar campos requeridos
            if (validationRules.containsKey("required_fields")) {
                String requiredFields = validationRules.get("required_fields").toString();
                for (String field : requiredFields.split(",")) {
                    if (!aiResponse.contains(field.trim())) {
                        log.warn("Campo requerido no encontrado: {}", field.trim());
                        return false;
                    }
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Error validando respuesta de IA: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el sampleOutput como referencia para entrenamiento
     */
    public String getSampleOutput(PromptTemplate promptTemplate) {
        return promptTemplate.getSampleOutput() != null ? 
               promptTemplate.getSampleOutput() : "{}";
    }

    /**
     * Mapea dificultad en inglés a español
     */
    private String mapDifficultyToSpanish(String difficulty) {
        if (difficulty == null) return "intermedio";
        
        return switch (difficulty.toLowerCase()) {
            case "easy" -> "fácil";
            case "medium" -> "intermedio";
            case "hard" -> "difícil";
            default -> difficulty;
        };
    }

    /**
     * Obtiene el nombre del tipo de ejercicio (placeholder para futuras mejoras)
     */
    private String getExerciseTypeName(Exercise exercise) {
        // Por ahora retornamos un valor genérico
        // En el futuro podríamos hacer una consulta para obtener el nombre real del tipo
        return "ejercicio_interactivo";
    }

    /**
     * Valida que un template tenga los placeholders mínimos requeridos
     * @param templateText Texto del template a validar
     * @return true si el template es válido
     */
    public boolean validateTemplate(String templateText) {
        if (templateText == null || templateText.trim().isEmpty()) {
            return false;
        }
        
        // Verificar que tenga al menos un placeholder
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateText);
        return matcher.find();
    }

    /**
     * Extrae todos los placeholders de un template
     * @param templateText Texto del template
     * @return Lista de placeholders encontrados
     */
    public java.util.List<String> extractPlaceholders(String templateText) {
        java.util.List<String> placeholders = new java.util.ArrayList<>();
        
        if (templateText != null) {
            Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateText);
            while (matcher.find()) {
                placeholders.add(matcher.group(1));
            }
        }
        
        return placeholders;
    }
} 