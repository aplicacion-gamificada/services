package com.gamified.application.exercise.service;

import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.repository.ExerciseRepository;
import com.gamified.application.learning.model.entity.LearningPoint;
import com.gamified.application.learning.repository.LearningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

// ===== IMPORTS AGREGADOS =====
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Servicio para construir prompts dinámicos cuando NO hay PromptTemplate disponible
 * 
 * NOTA: Este servicio es un FALLBACK cuando no se encuentra un PromptTemplate apropiado.
 * La lógica principal de construcción de prompts debe usar PromptBuilderService con PromptTemplate.
 * 
 * USO RECOMENDADO:
 * 1. PRIMERO: Intentar encontrar un PromptTemplate apropiado por exerciseSubtype
 * 2. SI NO HAY: Usar este servicio como fallback
 * 3. OBJETIVO: Migrar todos los ejercicios a usar PromptTemplate
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExercisePromptBuilder {

    private final ExerciseRepository exerciseRepository;
    private final LearningRepository learningRepository;

    /**
     * Construye un prompt FALLBACK cuando no hay PromptTemplate disponible
     * 
     * @param exerciseTemplate Plantilla del ejercicio
     * @param studentId ID del estudiante (para personalización)
     * @return Prompt estructurado para enviar a la IA
     */
    public String buildPromptForExercise(Exercise exerciseTemplate, Integer studentId) {
        log.warn("Usando ExercisePromptBuilder como FALLBACK - No se encontró PromptTemplate para ejercicio {}", 
                exerciseTemplate.getId());

        // ACTUALIZADO: Obtener información de la unidad en lugar del learning point
        String unitTitle = getUnitContextForExercise(exerciseTemplate.getUnitId());
        String difficulty = exerciseTemplate.getDifficulty() != null ? exerciseTemplate.getDifficulty() : "level_1";
        
        // ===== USAR SUBTIPO ESPECÍFICO =====
        String exerciseSubtype = exerciseTemplate.getExerciseSubtype() != null ? 
            exerciseTemplate.getExerciseSubtype() : "general";

        // Construir prompt base HARDCODEADO (temporal hasta migrar a PromptTemplate)
        String basePrompt = String.format(
            "Eres un experto profesor de matemáticas que debe crear un ejercicio educativo de alta calidad. " +
            "UNIDAD: %s, DIFICULTAD: %s, SUBTIPO: %s. " +
            
            "PROCESO OBLIGATORIO: " +
            "1. PRIMERO: Razona internamente sobre qué concepto específico evaluar " +
            "2. SEGUNDO: Crea una pregunta clara y pedagógicamente válida " +
            "3. TERCERO: Resuelve la pregunta paso a paso para obtener la respuesta correcta " +
            "4. CUARTO: Genera 3 opciones incorrectas pero plausibles (errores comunes) " +
            "5. QUINTO: Verifica que la respuesta correcta es coherente con la pregunta " +
            "6. SEXTO: Escribe una explicación clara del proceso de solución ",
            unitTitle, difficulty, exerciseSubtype
        );
        
        // ===== APLICAR REGLAS DE GENERACIÓN ESPECÍFICAS =====
        Map<String, Object> generationRules = exerciseTemplate.getGenerationRulesAsMap();
        if (!generationRules.isEmpty()) {
            basePrompt = enhancePromptWithGenerationRules(basePrompt, generationRules);
        }
        
        // ===== APLICAR PARÁMETROS DE DIFICULTAD =====
        Map<String, Object> difficultyParams = exerciseTemplate.getDifficultyParametersAsMap();
        if (!difficultyParams.isEmpty()) {
            basePrompt = enhancePromptWithDifficultyParameters(basePrompt, difficultyParams);
        }
        
        // Agregar instrucciones finales hardcodeadas
        return basePrompt + """
            
            REQUISITOS ESTRICTOS: 
            - TODO en español (pregunta, opciones, explicación) 
            - La pregunta debe ser específica y unívoca 
            - Las opciones deben ser respuestas completas, no letras (A, B, C, D) 
            - Las opciones incorrectas deben representar errores conceptuales reales 
            - La explicación debe enseñar el método correcto paso a paso 
            
            FORMATO JSON EXACTO: 
            {"question": "[pregunta específica en español]", "correct_answer": "[respuesta completa correcta]", 
            "options": ["[respuesta correcta]", "[error común 1]", "[error común 2]", "[error común 3]"], 
            "explanation": "[explicación paso a paso en español]"}
            """;
    }

    /**
     * Construye prompt FALLBACK para generar ejercicios para el pool
     */
    public String buildPromptForPoolGeneration(Exercise exerciseTemplate) {
        log.warn("Usando ExercisePromptBuilder como FALLBACK para pool - ejercicio template {}", 
                exerciseTemplate.getId());

        // ACTUALIZADO: Obtener información de la unidad en lugar del learning point
        String unitTitle = getUnitContextForExercise(exerciseTemplate.getUnitId());
        String difficulty = exerciseTemplate.getDifficulty() != null ? exerciseTemplate.getDifficulty() : "level_1";

        // Prompt HARDCODEADO para pool generation
        String prompt = String.format(
            "Como experto profesor de matemáticas, crea un ejercicio educativo de calidad sobre %s nivel %s. " +
            
            "PROCESO: " +
            "1. Razona qué concepto específico evaluar " +
            "2. Crea pregunta clara en español " +
            "3. Resuelve correctamente " +
            "4. Genera opciones incorrectas plausibles " +
            "5. Verifica coherencia " +
            
            "REQUISITOS: " +
            "- TODO en español " +
            "- Pregunta específica y clara " +
            "- 4 opciones completas (no letras A,B,C,D) " +
            "- Opciones incorrectas = errores comunes reales " +
            "- Explicación paso a paso " +
            "- Varía la formulación de preguntas " +
            
            "FORMATO JSON: " +
            "{\"question\": \"[pregunta en español]\", \"correct_answer\": \"[respuesta completa]\", " +
            "\"options\": [\"[correcta]\", \"[error 1]\", \"[error 2]\", \"[error 3]\"], " +
            "\"explanation\": \"[método paso a paso en español]\"}",
            unitTitle,
            difficulty
        );

        return prompt;
    }

    // ===== MÉTODOS HELPER =====
    
    private String enhancePromptWithGenerationRules(String basePrompt, Map<String, Object> generationRules) {
        StringBuilder enhanced = new StringBuilder(basePrompt);
        
        if (generationRules.containsKey("complexity_level")) {
            enhanced.append("\nNIVEL DE COMPLEJIDAD: ").append(generationRules.get("complexity_level"));
        }
        
        if (generationRules.containsKey("required_concepts")) {
            enhanced.append("\nCONCEPTOS REQUERIDOS: ").append(generationRules.get("required_concepts"));
        }
        
        if (generationRules.containsKey("avoid_concepts")) {
            enhanced.append("\nEVITAR CONCEPTOS: ").append(generationRules.get("avoid_concepts"));
        }
        
        if (generationRules.containsKey("context_type")) {
            enhanced.append("\nTIPO DE CONTEXTO: ").append(generationRules.get("context_type"));
        }
        
        return enhanced.toString();
    }

    private String enhancePromptWithDifficultyParameters(String basePrompt, Map<String, Object> difficultyParams) {
        StringBuilder enhanced = new StringBuilder(basePrompt);
        
        if (difficultyParams.containsKey("max_steps")) {
            enhanced.append("\nMÁXIMO PASOS DE SOLUCIÓN: ").append(difficultyParams.get("max_steps"));
        }
        
        if (difficultyParams.containsKey("number_range")) {
            enhanced.append("\nRANGO NUMÉRICO: ").append(difficultyParams.get("number_range"));
        }
        
        if (difficultyParams.containsKey("allow_decimals")) {
            enhanced.append("\nPERMITIR DECIMALES: ").append(difficultyParams.get("allow_decimals"));
        }
        
        if (difficultyParams.containsKey("require_calculator")) {
            enhanced.append("\nREQUIERE CALCULADORA: ").append(difficultyParams.get("require_calculator"));
        }
        
        return enhanced.toString();
    }

    // ===== MÉTODOS DE UTILIDAD (mantenidos para compatibilidad) =====
    
    /**
     * Construye contexto específico del estudiante para personalizar el ejercicio
     * Método usado potencialmente por otros servicios
     */
    public String buildStudentContext(Integer studentId, Integer learningPointId) {
        // Contexto simplificado - solo información esencial
        try {
            Optional<ExerciseRepository.StudentExerciseStats> statsOpt = 
                    exerciseRepository.getStudentExerciseStats(studentId);
            
            if (statsOpt.isPresent()) {
                ExerciseRepository.StudentExerciseStats stats = statsOpt.get();
                return String.format("Estudiante nivel: %.0f puntos promedio", stats.getAverageScore());
            }
            
            return "Estudiante nuevo";
        } catch (Exception e) {
            log.warn("Error al obtener contexto del estudiante {}: {}", studentId, e.getMessage());
            return "Estudiante estándar";
        }
    }

    /**
     * Limpia texto removiendo caracteres problemáticos
     * Método usado potencialmente por otros servicios
     */
    public String cleanText(String text) {
        if (text == null) return "texto";
        
        return text
            .replaceAll("[áàäâã]", "a")
            .replaceAll("[éèëê]", "e") 
            .replaceAll("[íìïî]", "i")
            .replaceAll("[óòöôõ]", "o")
            .replaceAll("[úùüû]", "u")
            .replaceAll("[ñ]", "n")
            .replaceAll("[ÁÀÄÂÃ]", "A")
            .replaceAll("[ÉÈËÊ]", "E")
            .replaceAll("[ÍÌÏÎ]", "I") 
            .replaceAll("[ÓÒÖÔÕ]", "O")
            .replaceAll("[ÚÙÜÛ]", "U")
            .replaceAll("[Ñ]", "N")
            .replaceAll("[^a-zA-Z0-9 ]", " ")
            .replaceAll("\\s+", " ")
            .trim();
    }

    /**
     * NUEVO: Obtiene contexto de la unidad para un ejercicio
     * Método auxiliar para obtener información de contexto desde la unidad
     * 
     * @param unitId ID de la unidad
     * @return Título de la unidad o valor por defecto
     */
    private String getUnitContextForExercise(Integer unitId) {
        if (unitId == null) {
            log.warn("Exercise no tiene unitId asignado, usando contexto por defecto");
            return "Matemáticas - Unidad general";
        }
        
        try {
            // Usar LearningRepository para obtener información de la unidad
            Optional<com.gamified.application.learning.model.entity.Unit> unitOpt = 
                    learningRepository.findUnitById(unitId);
            
            if (unitOpt.isPresent()) {
                com.gamified.application.learning.model.entity.Unit unit = unitOpt.get();
                return unit.getTitle() != null ? unit.getTitle() : "Unidad " + unitId;
            } else {
                log.warn("Unidad con ID {} no encontrada", unitId);
                return "Matemáticas - Unidad " + unitId;
            }
            
        } catch (Exception e) {
            log.error("Error obteniendo contexto de unidad {}: {}", unitId, e.getMessage());
            return "Matemáticas - Unidad general";
        }
    }
}
