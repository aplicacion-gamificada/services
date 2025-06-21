package com.gamified.application.exercise.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamified.application.exercise.model.dto.request.ExerciseRequestDto;
import com.gamified.application.exercise.model.dto.response.ExerciseResponseDto;
import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.ExerciseAttempt;
import com.gamified.application.exercise.model.entity.ExerciseType;
import com.gamified.application.exercise.repository.ExerciseRepository;
import com.gamified.application.learning.repository.LearningRepository;
import com.gamified.application.learning.model.entity.LearningPoint;
import com.gamified.application.shared.exception.ResourceNotFoundException;
import com.gamified.application.shared.model.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ExerciseResponseDto.NextExerciseDto getNextExercise(Integer studentId, Integer learningPointId, String difficulty) {
        log.info("Obteniendo siguiente ejercicio para estudiante {} en learning point {}", studentId, learningPointId);
        
        // Verificar que el learning point existe
        Optional<LearningPoint> learningPointOpt = learningRepository.findLearningPointById(learningPointId);
        if (learningPointOpt.isEmpty()) {
            throw new ResourceNotFoundException("Learning point no encontrado con ID: " + learningPointId);
        }
        
        LearningPoint learningPoint = learningPointOpt.get();
        
        // Buscar ejercicio disponible
        Optional<Exercise> exerciseOpt = exerciseRepository.findNextExerciseForLearningPoint(
                studentId, learningPointId, difficulty);
        
        if (exerciseOpt.isEmpty()) {
            // Si no hay ejercicios disponibles, podríamos generar uno con IA aquí
            // Por ahora, lanzamos excepción
            throw new ResourceNotFoundException("No hay ejercicios disponibles para este learning point");
        }
        
        Exercise exercise = exerciseOpt.get();
        
        // Obtener información adicional
        Optional<ExerciseType> exerciseTypeOpt = exerciseRepository.findExerciseTypeById(exercise.getExerciseTypeId());
        String exerciseTypeName = exerciseTypeOpt.map(ExerciseType::getName).orElse("Tipo desconocido");
        
        // Verificar si el estudiante tiene intentos previos
        Integer previousAttempts = exerciseRepository.countAttemptsByStudentAndExercise(studentId, exercise.getId());
        Boolean hasAttempts = previousAttempts > 0;
        
        return mapToNextExerciseDto(exercise, learningPoint, exerciseTypeName, hasAttempts, previousAttempts);
    }

    @Override
    public ExerciseResponseDto.AttemptResultDto submitExerciseAttempt(ExerciseRequestDto.SubmitAttemptDto request) {
        log.info("Procesando intento de ejercicio {} por estudiante {}", 
                request.getExerciseId(), request.getStudentProfileId());
        
        // Verificar que el ejercicio existe
        Optional<Exercise> exerciseOpt = exerciseRepository.findExerciseById(request.getExerciseId());
        if (exerciseOpt.isEmpty()) {
            throw new ResourceNotFoundException("Ejercicio no encontrado con ID: " + request.getExerciseId());
        }
        
        Exercise exercise = exerciseOpt.get();
        
        // Calcular el número de intento
        Integer attemptNumber = exerciseRepository.countAttemptsByStudentAndExercise(
                request.getStudentProfileId(), request.getExerciseId()) + 1;
        
        // Evaluar la respuesta
        Boolean isCorrect = evaluateAnswer(exercise.getCorrectAnswer(), request.getSubmittedAnswer());
        Double score = calculateScore(isCorrect, request.getTimeSpentSeconds(), 
                request.getHintsUsed(), exercise.getEstimatedTimeMinutes());
        
        // Generar feedback
        String feedback = generateFeedback(isCorrect, exercise.getCorrectAnswer(), request.getSubmittedAnswer());
        
        // Crear el intento
        ExerciseAttempt attempt = ExerciseAttempt.builder()
                .exerciseId(request.getExerciseId())
                .studentProfileId(request.getStudentProfileId())
                .submittedAnswer(request.getSubmittedAnswer())
                .isCorrect(isCorrect)
                .timeSpentSeconds(request.getTimeSpentSeconds())
                .hintsUsed(request.getHintsUsed())
                .score(score)
                .feedback(feedback)
                .attemptNumber(attemptNumber)
                .submittedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        
        Integer attemptId = exerciseRepository.createExerciseAttempt(attempt);
        attempt.setId(attemptId);
        
        // Emitir evento de ejercicio completado para el Rule Engine
        publishExerciseCompletedEvent(exercise, attempt);
        
        // Buscar siguiente ejercicio sugerido
        ExerciseResponseDto.NextExerciseDto nextExercise = null;
        try {
            nextExercise = getNextExercise(request.getStudentProfileId(), 
                    exercise.getLearningPointId(), exercise.getDifficulty());
        } catch (Exception e) {
            log.warn("No se pudo obtener siguiente ejercicio: {}", e.getMessage());
        }
        
        return mapToAttemptResultDto(attempt, exercise.getCorrectAnswer(), nextExercise);
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

    private ExerciseResponseDto.NextExerciseDto mapToNextExerciseDto(
            Exercise exercise, LearningPoint learningPoint, String exerciseTypeName, 
            Boolean hasAttempts, Integer previousAttempts) {
        
        List<String> possibleAnswers = parseJsonArray(exercise.getPossibleAnswers());
        List<String> hints = parseJsonArray(exercise.getHints());
        
        return ExerciseResponseDto.NextExerciseDto.builder()
                .exerciseId(exercise.getId())
                .learningPointId(exercise.getLearningPointId())
                .learningPointTitle(learningPoint.getTitle())
                .exerciseTypeId(exercise.getExerciseTypeId().toString())
                .exerciseTypeName(exerciseTypeName)
                .title(exercise.getTitle())
                .questionText(exercise.getQuestionText())
                .possibleAnswers(possibleAnswers)
                .difficulty(exercise.getDifficulty())
                .hints(hints)
                .estimatedTimeMinutes(exercise.getEstimatedTimeMinutes())
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
}