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
        
        String learningPointTitle = learningPointOpt.map(LearningPoint::getTitle).orElse("Matemáticas");
        String learningPointDescription = learningPointOpt.map(LearningPoint::getDescription).orElse("");

        // Obtener historial del estudiante para personalización
        String studentContext = buildStudentContext(studentId, exerciseTemplate.getLearningPointId());

        // Construir el prompt principal
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Genera un ejercicio educativo de matemáticas con las siguientes especificaciones:\n\n");
        
        // Contexto del learning point
        prompt.append("**CONTEXTO EDUCATIVO:**\n");
        prompt.append("- Tema: ").append(learningPointTitle).append("\n");
        if (!learningPointDescription.isEmpty()) {
            prompt.append("- Descripción: ").append(learningPointDescription).append("\n");
        }
        prompt.append("- Dificultad: ").append(exerciseTemplate.getDifficulty()).append("\n");
        prompt.append("- Tiempo estimado: ").append(exerciseTemplate.getEstimatedTimeMinutes()).append(" minutos\n\n");

        // Contexto del estudiante
        if (!studentContext.isEmpty()) {
            prompt.append("**CONTEXTO DEL ESTUDIANTE:**\n");
            prompt.append(studentContext).append("\n\n");
        }

        // Especificaciones del ejercicio
        prompt.append("**ESPECIFICACIONES DEL EJERCICIO:**\n");
        prompt.append("- Título base: ").append(exerciseTemplate.getTitle()).append("\n");
        if (exerciseTemplate.getQuestionText() != null && !exerciseTemplate.getQuestionText().isEmpty()) {
            prompt.append("- Tipo de pregunta: ").append(exerciseTemplate.getQuestionText()).append("\n");
        }
        prompt.append("- Debe ser único y no repetir ejercicios anteriores del estudiante\n");
        prompt.append("- Nivel de dificultad apropiado para el progreso actual del estudiante\n\n");

        // Formato de respuesta requerido
        prompt.append("**FORMATO DE RESPUESTA REQUERIDO (JSON):**\n");
        prompt.append("Responde ÚNICAMENTE con el siguiente JSON válido, sin texto adicional:\n\n");
        prompt.append("{\n");
        prompt.append("  \"question\": \"[Enunciado completo del ejercicio]\",\n");
        prompt.append("  \"question_type\": \"[multiple_choice|numeric|text|true_false]\",\n");
        prompt.append("  \"correct_answer\": \"[Respuesta correcta]\",\n");
        prompt.append("  \"options\": [\"opción1\", \"opción2\", \"opción3\", \"opción4\"], // Solo para multiple_choice\n");
        prompt.append("  \"explanation\": \"[Explicación de la solución paso a paso]\",\n");
        prompt.append("  \"hints\": [\"pista1\", \"pista2\", \"pista3\"],\n");
        prompt.append("  \"difficulty_level\": \"").append(exerciseTemplate.getDifficulty()).append("\",\n");
        prompt.append("  \"estimated_time_minutes\": ").append(exerciseTemplate.getEstimatedTimeMinutes()).append(",\n");
        prompt.append("  \"learning_objectives\": [\"objetivo1\", \"objetivo2\"],\n");
        prompt.append("  \"tags\": [\"tag1\", \"tag2\"]\n");
        prompt.append("}\n\n");

        // Instrucciones adicionales
        prompt.append("**INSTRUCCIONES IMPORTANTES:**\n");
        prompt.append("- La pregunta debe ser clara y sin ambigüedades\n");
        prompt.append("- Las opciones (si aplica) deben ser plausibles pero solo una correcta\n");
        prompt.append("- La explicación debe ser educativa y fácil de entender\n");
        prompt.append("- Las pistas deben guiar al estudiante sin dar la respuesta directa\n");
        prompt.append("- Usa vocabulario apropiado para el nivel educativo\n");
        prompt.append("- El ejercicio debe fomentar el pensamiento crítico\n");

        String finalPrompt = prompt.toString();
        log.debug("Prompt construido. Longitud: {} caracteres", finalPrompt.length());
        
        return finalPrompt;
    }

    /**
     * Construye contexto específico del estudiante para personalizar el ejercicio
     */
    private String buildStudentContext(Integer studentId, Integer learningPointId) {
        StringBuilder context = new StringBuilder();
        
        try {
            // Obtener estadísticas de rendimiento del estudiante
            Optional<ExerciseRepository.StudentExerciseStats> statsOpt = 
                    exerciseRepository.getStudentExerciseStats(studentId);
            
            if (statsOpt.isPresent()) {
                ExerciseRepository.StudentExerciseStats stats = statsOpt.get();
                
                context.append("- Ejercicios completados: ").append(stats.getTotalExercisesCompleted()).append("\n");
                context.append("- Promedio de puntuación: ").append(String.format("%.1f", stats.getAverageScore())).append("\n");
                context.append("- Dificultad preferida: ").append(stats.getPreferredDifficulty()).append("\n");
                
                // Agregar información sobre áreas de fortaleza o debilidad
                if (stats.getAverageScore() < 60) {
                    context.append("- Nota: El estudiante necesita ejercicios con más apoyo y explicaciones detalladas\n");
                } else if (stats.getAverageScore() > 85) {
                    context.append("- Nota: El estudiante está listo para desafíos más complejos\n");
                }
            }

            // Obtener intentos recientes en este learning point
            Integer recentAttempts = exerciseRepository.countRecentAttemptsByStudentAndLearningPoint(
                    studentId, learningPointId, 7); // últimos 7 días
            
            if (recentAttempts > 0) {
                context.append("- Intentos recientes en este tema: ").append(recentAttempts).append("\n");
            }

        } catch (Exception e) {
            log.warn("Error al construir contexto del estudiante {}", studentId, e);
            // Continuar sin contexto específico
        }
        
        return context.toString();
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
        
        String learningPointTitle = learningPointOpt.map(LearningPoint::getTitle).orElse("Matemáticas");

        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Genera un ejercicio educativo de matemáticas para el pool de ejercicios:\n\n");
        prompt.append("**ESPECIFICACIONES:**\n");
        prompt.append("- Tema: ").append(learningPointTitle).append("\n");
        prompt.append("- Dificultad: ").append(exerciseTemplate.getDifficulty()).append("\n");
        prompt.append("- Tipo: ").append(exerciseTemplate.getTitle()).append("\n");
        prompt.append("- Tiempo estimado: ").append(exerciseTemplate.getEstimatedTimeMinutes()).append(" minutos\n\n");

        // Usar el mismo formato de respuesta JSON
        prompt.append("**FORMATO DE RESPUESTA (JSON):**\n");
        prompt.append("{\n");
        prompt.append("  \"question\": \"[Enunciado del ejercicio]\",\n");
        prompt.append("  \"question_type\": \"[multiple_choice|numeric|text|true_false]\",\n");
        prompt.append("  \"correct_answer\": \"[Respuesta correcta]\",\n");
        prompt.append("  \"options\": [\"opción1\", \"opción2\", \"opción3\", \"opción4\"],\n");
        prompt.append("  \"explanation\": \"[Explicación paso a paso]\",\n");
        prompt.append("  \"hints\": [\"pista1\", \"pista2\", \"pista3\"],\n");
        prompt.append("  \"difficulty_level\": \"").append(exerciseTemplate.getDifficulty()).append("\",\n");
        prompt.append("  \"estimated_time_minutes\": ").append(exerciseTemplate.getEstimatedTimeMinutes()).append("\n");
        prompt.append("}\n\n");

        prompt.append("Genera contenido único y educativo. Responde SOLO con JSON válido.");

        return prompt.toString();
    }
}
