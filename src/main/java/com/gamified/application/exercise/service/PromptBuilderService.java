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
 * Servicio para construir prompts dinámicos reemplazando placeholders
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromptBuilderService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * Construye un prompt dinámico reemplazando placeholders con datos reales
     * @param promptTemplate Plantilla de prompt con placeholders
     * @param exercise Ejercicio base (plantilla)
     * @param learningPoint Punto de aprendizaje
     * @param studentId ID del estudiante (para personalización)
     * @param difficulty Dificultad deseada
     * @return Prompt construido listo para enviar a la IA
     */
    public String buildPrompt(PromptTemplate promptTemplate, Exercise exercise, 
                             LearningPoint learningPoint, Integer studentId, String difficulty) {
        
        log.debug("Construyendo prompt para ejercicio {} con plantilla {}", 
                exercise.getId(), promptTemplate.getName());
        
        // Crear mapa de valores para reemplazar placeholders
        Map<String, String> placeholderValues = createPlaceholderValues(
                exercise, learningPoint, studentId, difficulty);
        
        // Reemplazar placeholders en el template
        String builtPrompt = replacePlaceholders(promptTemplate.getTemplateText(), placeholderValues);
        
        log.debug("Prompt construido: {}", builtPrompt);
        return builtPrompt;
    }

    /**
     * Crea un mapa con los valores para reemplazar cada placeholder
     */
    private Map<String, String> createPlaceholderValues(Exercise exercise, LearningPoint learningPoint, 
                                                       Integer studentId, String difficulty) {
        Map<String, String> values = new HashMap<>();
        
        // Valores básicos del ejercicio
        values.put("dificultad", difficulty != null ? difficulty : "medium");
        values.put("difficulty", difficulty != null ? difficulty : "medium");
        values.put("nivel", mapDifficultyToSpanish(difficulty));
        
        // Valores del learning point
        if (learningPoint != null) {
            values.put("tema", learningPoint.getTitle());
            values.put("competencia", learningPoint.getDescription());
            values.put("learning_point", learningPoint.getTitle());
            values.put("descripcion", learningPoint.getDescription());
        }
        
        // Valores del ejercicio
        if (exercise != null) {
            values.put("titulo", exercise.getTitle() != null ? exercise.getTitle() : "");
            values.put("tipo", getExerciseTypeName(exercise));
            values.put("tiempo_estimado", String.valueOf(exercise.getEstimatedTimeMinutes()));
        }
        
        // Valores del estudiante (para futuras personalizaciones)
        values.put("student_id", String.valueOf(studentId));
        
        // Valores adicionales comunes
        values.put("idioma", "español");
        values.put("formato", "JSON");
        
        log.debug("Placeholders creados: {}", values.keySet());
        return values;
    }

    /**
     * Reemplaza los placeholders en el texto del template
     */
    private String replacePlaceholders(String templateText, Map<String, String> placeholderValues) {
        if (templateText == null) {
            return "";
        }
        
        String result = templateText;
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(templateText);
        
        while (matcher.find()) {
            String placeholder = matcher.group(1); // El contenido dentro de {{}}
            String value = placeholderValues.get(placeholder);
            
            if (value != null) {
                // Reemplazar {{placeholder}} con el valor
                result = result.replace("{{" + placeholder + "}}", value);
                log.debug("Reemplazado {{{}}} con: {}", placeholder, value);
            } else {
                log.warn("Placeholder no encontrado: {}", placeholder);
                // Dejar el placeholder sin reemplazar para debugging
            }
        }
        
        return result;
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