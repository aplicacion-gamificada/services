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

        // Construir prompt ESPECÍFICO y CLARO para generar ejercicios completos
        String prompt = String.format(
            "Crea un ejercicio de %s dificultad %s. Genera 4 opciones completas y realistas. " +
            "EJEMPLO para fracciones: {\"question\": \"Cual es el resultado de 1/2 + 1/4?\", " +
            "\"correct_answer\": \"3/4\", \"options\": [\"3/4\", \"2/6\", \"1/3\", \"2/4\"], " +
            "\"explanation\": \"Para sumar fracciones: 1/2 + 1/4 = 2/4 + 1/4 = 3/4\"}. " +
            "Usa comillas dobles, opciones realistas, no solo letras A B C D.",
            cleanText(learningPointTitle), 
            difficulty
        );

        log.debug("Prompt construido. Longitud: {} caracteres", prompt.length());
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

        // Prompt ULTRA SIMPLE para pool
        String prompt = String.format(
            "Crea ejercicio de %s nivel %s. " +
            "Responde JSON: {\"question\": \"texto\", \"correct_answer\": \"respuesta\", " +
            "\"options\": [\"A\", \"B\", \"C\", \"D\"], \"explanation\": \"explicacion\"}. " +
            "Solo ASCII, sin acentos, una linea.",
            cleanText(learningPointTitle),
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
