package com.gamified.application.exercise.controller;

import com.gamified.application.exercise.model.dto.request.ExerciseRequestDto;
import com.gamified.application.exercise.model.dto.response.ExerciseResponseDto;
import com.gamified.application.exercise.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador para operaciones del módulo Exercise
 */
@RestController
@RequestMapping("/exercises")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Exercise",
        description = "Provides endpoints for managing adaptive exercises, including exercise generation, attempt submission, and progress tracking."
)
public class ExerciseController {

    private final ExerciseService exerciseService;

    /**
     * Obtiene el siguiente ejercicio disponible para un learning point específico
     * Endpoint clave mencionado en GUIA.md
     * @param studentId ID del estudiante
     * @param learningPointId ID del learning point
     * @param difficulty Dificultad preferida (opcional)
     * @return Siguiente ejercicio disponible
     */
    @GetMapping("/students/{studentId}/learning-point/{learningPointId}/next-exercise")
    @Operation(summary = "Get next exercise for learning point", 
               description = "Retrieves the next available exercise for a specific student and learning point")
    public ResponseEntity<ExerciseResponseDto.NextExerciseDto> getNextExercise(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId,
            @Parameter(description = "Learning point ID") @PathVariable Integer learningPointId,
            @Parameter(description = "Preferred difficulty level") @RequestParam(required = false) String difficulty) {
        
        log.info("GET /api/exercises/students/{}/learning-point/{}/next-exercise - Obteniendo siguiente ejercicio", 
                studentId, learningPointId);
        
        ExerciseResponseDto.NextExerciseDto nextExercise = 
                exerciseService.getNextExercise(studentId, learningPointId, difficulty);
        return ResponseEntity.ok(nextExercise);
    }

    /**
     * Envía respuesta de ejercicio
     * @param request Datos del intento
     * @return Resultado del intento
     */
    @PostMapping("/attempts")
    @Operation(summary = "Submit exercise attempt", 
               description = "Submits a student's answer to an exercise and returns feedback and score")
    public ResponseEntity<ExerciseResponseDto.AttemptResultDto> submitExerciseAttempt(
            @Valid @RequestBody ExerciseRequestDto.SubmitAttemptDto request) {
        
        log.info("POST /api/exercises/attempts - Enviando intento de ejercicio {} por estudiante {}", 
                request.getGeneratedExerciseId(), request.getStudentProfileId());
        
        ExerciseResponseDto.AttemptResultDto result = exerciseService.submitExerciseAttempt(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Obtiene historial de intentos de ejercicios de un estudiante
     * @param studentId ID del estudiante
     * @param limit Número máximo de intentos a retornar (opcional, default 50)
     * @return Lista de intentos históricos
     */
    @GetMapping("/students/{studentId}/exercise-attempts")
    @Operation(summary = "Get student exercise attempts", 
               description = "Retrieves the exercise attempt history for a specific student")
    public ResponseEntity<List<ExerciseResponseDto.AttemptHistoryDto>> getExerciseAttempts(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId,
            @Parameter(description = "Maximum number of attempts to return") @RequestParam(required = false, defaultValue = "50") Integer limit) {
        
        log.info("GET /api/exercises/students/{}/exercise-attempts - Obteniendo historial de intentos", studentId);
        
        List<ExerciseResponseDto.AttemptHistoryDto> attempts = 
                exerciseService.getAttemptHistory(studentId, limit);
        return ResponseEntity.ok(attempts);
    }

    /**
     * Obtiene ejercicios completados por un estudiante
     * @param studentId ID del estudiante
     * @return Lista de ejercicios completados
     */
    @GetMapping("/students/{studentId}/completed-exercises")
    @Operation(summary = "Get completed exercises", 
               description = "Retrieves all exercises that have been successfully completed by a specific student")
    public ResponseEntity<List<ExerciseResponseDto.CompletedExerciseDto>> getCompletedExercises(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId) {
        
        log.info("GET /api/exercises/students/{}/completed-exercises - Obteniendo ejercicios completados", studentId);
        
        List<ExerciseResponseDto.CompletedExerciseDto> completedExercises = 
                exerciseService.getCompletedExercises(studentId);
        return ResponseEntity.ok(completedExercises);
    }

    /**
     * Obtiene estadísticas generales de ejercicios del estudiante
     * @param studentId ID del estudiante
     * @return Estadísticas del estudiante
     */
    @GetMapping("/students/{studentId}/stats")
    @Operation(summary = "Get student exercise statistics", 
               description = "Retrieves comprehensive exercise statistics for a specific student")
    public ResponseEntity<ExerciseResponseDto.StudentExerciseStatsDto> getStudentExerciseStats(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId) {
        
        log.info("GET /api/exercises/students/{}/stats - Obteniendo estadísticas de ejercicios", studentId);
        
        ExerciseResponseDto.StudentExerciseStatsDto stats = 
                exerciseService.getStudentExerciseStats(studentId);
        return ResponseEntity.ok(stats);
    }

    // ===================================================================
    // ADDITIONAL ENDPOINTS FOR FUTURE EXPANSION
    // ===================================================================

    /**
     * Obtiene el siguiente ejercicio con parámetros adicionales (endpoint alternativo)
     * @param request Request con parámetros de filtrado
     * @return Siguiente ejercicio disponible
     */
    @PostMapping("/students/{studentId}/next-exercise")
    @Operation(summary = "Get next exercise with advanced parameters", 
               description = "Retrieves the next exercise with advanced filtering and preference options")
    public ResponseEntity<ExerciseResponseDto.NextExerciseDto> getNextExerciseAdvanced(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId,
            @Valid @RequestBody ExerciseRequestDto.NextExerciseRequestDto request) {
        
        log.info("POST /api/exercises/students/{}/next-exercise - Obteniendo siguiente ejercicio con filtros avanzados", studentId);
        
        // Asegurar que el studentId coincida
        request.setStudentProfileId(studentId);
        
        ExerciseResponseDto.NextExerciseDto nextExercise = 
                exerciseService.getNextExercise(studentId, request.getLearningPointId(), request.getPreferredDifficulty());
        return ResponseEntity.ok(nextExercise);
    }

    /**
     * Obtiene intentos de un ejercicio específico por un estudiante
     * @param studentId ID del estudiante
     * @param exerciseId ID del ejercicio
     * @return Lista de intentos para el ejercicio específico
     */
    @GetMapping("/students/{studentId}/exercises/{exerciseId}/attempts")
    @Operation(summary = "Get attempts for specific exercise", 
               description = "Retrieves all attempts made by a student for a specific exercise")
    public ResponseEntity<List<ExerciseResponseDto.AttemptHistoryDto>> getExerciseSpecificAttempts(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId,
            @Parameter(description = "Exercise ID") @PathVariable Integer exerciseId) {
        
        log.info("GET /api/exercises/students/{}/exercises/{}/attempts - Obteniendo intentos para ejercicio específico", 
                studentId, exerciseId);
        
        // Filtrar del historial general solo los intentos del ejercicio específico
        List<ExerciseResponseDto.AttemptHistoryDto> allAttempts = 
                exerciseService.getAttemptHistory(studentId, 100);
        
        List<ExerciseResponseDto.AttemptHistoryDto> exerciseAttempts = allAttempts.stream()
                .filter(attempt -> attempt.getExerciseId().equals(exerciseId))
                .toList();
        
        return ResponseEntity.ok(exerciseAttempts);
    }
} 