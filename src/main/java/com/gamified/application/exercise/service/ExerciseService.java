package com.gamified.application.exercise.service;

import com.gamified.application.exercise.model.dto.request.ExerciseRequestDto;
import com.gamified.application.exercise.model.dto.response.ExerciseResponseDto;

import java.util.List;

/**
 * Servicio para operaciones de Exercise
 */
public interface ExerciseService {

    /**
     * Obtiene el siguiente ejercicio disponible para un learning point
     * @param studentId ID del estudiante
     * @param learningPointId ID del learning point
     * @param difficulty Dificultad preferida (opcional)
     * @return Siguiente ejercicio disponible
     */
    ExerciseResponseDto.NextExerciseDto getNextExercise(Integer studentId, Integer learningPointId, String difficulty);

    /**
     * Envía un intento de respuesta a un ejercicio
     * @param request Datos del intento
     * @return Resultado del intento
     */
    ExerciseResponseDto.AttemptResultDto submitExerciseAttempt(ExerciseRequestDto.SubmitAttemptDto request);

    /**
     * Obtiene el historial de intentos de un estudiante
     * @param studentId ID del estudiante
     * @param limit Número máximo de intentos a retornar
     * @return Lista de intentos históricos
     */
    List<ExerciseResponseDto.AttemptHistoryDto> getAttemptHistory(Integer studentId, Integer limit);

    /**
     * Obtiene ejercicios completados por un estudiante
     * @param studentId ID del estudiante
     * @return Lista de ejercicios completados
     */
    List<ExerciseResponseDto.CompletedExerciseDto> getCompletedExercises(Integer studentId);

    /**
     * Obtiene estadísticas generales de ejercicios del estudiante
     * @param studentId ID del estudiante
     * @return Estadísticas del estudiante
     */
    ExerciseResponseDto.StudentExerciseStatsDto getStudentExerciseStats(Integer studentId);
} 