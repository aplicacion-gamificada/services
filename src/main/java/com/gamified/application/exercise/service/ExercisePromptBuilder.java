package com.gamified.application.exercise.service;

import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.repository.ExerciseRepository;
import com.gamified.application.learning.model.entity.LearningPoint;
import com.gamified.application.learning.repository.LearningRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio para construir prompts dinámicos para la generación de ejercicios con IA
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExercisePromptBuilder {

    private final ExerciseRepository exerciseRepository;
    private final LearningRepository learningRepository;

    /**
     * Construye un prompt estructurado para generar un ejercicio específico
     * 
     * @param exerciseTemplate Plantilla del ejercicio
     * @param studentId ID del estudiante (para personalización)
     * @return Prompt estructurado para enviar a la IA
     */
    public String buildPromptForExercise(Exercise exerciseTemplate, Integer studentId) {
        log.debug("Construyendo prompt para ejercicio template {} y estudiante {}", 
                exerciseTemplate.getId(), studentId);

        // Obtener información del learning point
        Optional<LearningPoint> learningPointOpt = learningRepository
                .findLearningPointById(exerciseTemplate.getLearningPointId());
        
        String learningPointTitle = learningPointOpt.map(LearningPoint::getTitle).orElse("Matematicas");
        String difficulty = exerciseTemplate.getDifficulty() != null ? exerciseTemplate.getDifficulty() : "level_1";

        // Construir prompt INTELIGENTE y ESPECÍFICO para modelo de razonamiento
        String prompt = String.format(
            "Eres un experto profesor de matemáticas que debe crear un ejercicio educativo de alta calidad. " +
            "TEMA: %s, DIFICULTAD: %s. " +
            
            "PROCESO OBLIGATORIO: " +
            "1. PRIMERO: Razona internamente sobre qué concepto específico evaluar " +
            "2. SEGUNDO: Crea una pregunta clara y pedagógicamente válida " +
            "3. TERCERO: Resuelve la pregunta paso a paso para obtener la respuesta correcta " +
            "4. CUARTO: Genera 3 opciones incorrectas pero plausibles (errores comunes) " +
            "5. QUINTO: Verifica que la respuesta correcta es coherente con la pregunta " +
            "6. SEXTO: Escribe una explicación clara del proceso de solución " +
            
            "REQUISITOS ESTRICTOS: " +
            "- TODO en español (pregunta, opciones, explicación) " +
            "- La pregunta debe ser específica y unívoca " +
            "- Las opciones deben ser respuestas completas, no letras (A, B, C, D) " +
            "- Las opciones incorrectas deben representar errores conceptuales reales " +
            "- La explicación debe enseñar el método correcto paso a paso " +
            "- Usa variedad en la formulación (no siempre 'Cuál es el resultado de...') " +
            
            "FORMATO DE SALIDA (JSON válido): " +
            "{\"question\": \"[pregunta específica en español]\", " +
            "\"correct_answer\": \"[respuesta correcta completa]\", " +
            "\"options\": [\"[opción correcta]\", \"[error común 1]\", \"[error común 2]\", \"[error común 3]\"], " +
            "\"explanation\": \"[explicación paso a paso del método correcto en español]\"} " +
            
            "EJEMPLO PARA FRACCIONES: " +
            "{\"question\": \"María comió 2/5 de una pizza y Juan comió 1/3 de la misma pizza. ¿Qué fracción de la pizza comieron entre los dos?\", " +
            "\"correct_answer\": \"11/15\", " +
            "\"options\": [\"11/15\", \"3/8\", \"2/3\", \"1/2\"], " +
            "\"explanation\": \"Para sumar fracciones con diferentes denominadores: 2/5 + 1/3. Primero encontramos el mínimo común múltiplo de 5 y 3, que es 15. Convertimos: 2/5 = 6/15 y 1/3 = 5/15. Sumamos: 6/15 + 5/15 = 11/15\"}", 
            learningPointTitle, 
            difficulty
        );

        log.debug("Prompt inteligente construido. Longitud: {} caracteres", prompt.length());
        return prompt;
    }

    /**
     * Construye un prompt para pre-generar ejercicios para el pool
     * (versión simplificada sin contexto específico del estudiante)
     */
    public String buildPromptForPoolGeneration(Exercise exerciseTemplate) {
        log.debug("Construyendo prompt para pool generation de template {}", exerciseTemplate.getId());

        // Obtener información del learning point
        Optional<LearningPoint> learningPointOpt = learningRepository
                .findLearningPointById(exerciseTemplate.getLearningPointId());
        
        String learningPointTitle = learningPointOpt.map(LearningPoint::getTitle).orElse("Matematicas");
        String difficulty = exerciseTemplate.getDifficulty() != null ? exerciseTemplate.getDifficulty() : "level_1";

        // Prompt INTELIGENTE para pool - versión más directa pero manteniendo calidad
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
            learningPointTitle,
            difficulty
        );

        return prompt;
    }

    /**
     * Limpia texto removiendo caracteres problemáticos
     */
    private String cleanText(String text) {
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
     * Construye contexto específico del estudiante para personalizar el ejercicio
     */
    private String buildStudentContext(Integer studentId, Integer learningPointId) {
        // Contexto simplificado - solo información esencial
        try {
            Optional<ExerciseRepository.StudentExerciseStats> statsOpt = 
                    exerciseRepository.getStudentExerciseStats(studentId);
            
            if (statsOpt.isPresent()) {
                ExerciseRepository.StudentExerciseStats stats = statsOpt.get();
                return String.format("Estudiante nivel: %.0f puntos promedio", stats.getAverageScore());
            }
        } catch (Exception e) {
            log.warn("Error al construir contexto del estudiante {}", studentId, e);
        }
        
        return "";
    }
}
