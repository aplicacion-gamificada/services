package com.gamified.application.progress.controller;

import com.gamified.application.progress.model.dto.request.ProgressRequestDto;
import com.gamified.application.progress.model.dto.response.ProgressResponseDto;
import com.gamified.application.progress.service.ProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import jakarta.validation.Valid;

/**
 * Controlador para operaciones del módulo Progress
 */
@RestController
@RequestMapping("/progress")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Progress",
        description = "Provides endpoints for managing student progress, including learning paths, lesson completion, and progress tracking."
)
public class ProgressController {

    private final ProgressService progressService;

    /**
     * Obtiene el learning path activo de un estudiante
     * @param studentId ID del estudiante
     * @return Learning path del estudiante
     */
    @GetMapping("/students/{studentId}/learning-path")
    @Operation(summary = "Get student learning path", description = "Retrieves the active learning path for a specific student")
    public ResponseEntity<ProgressResponseDto.LearningPathDto> getLearningPathByStudent(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId) {
        log.info("GET /api/progress/students/{}/learning-path - Obteniendo learning path", studentId);
        
        ProgressResponseDto.LearningPathDto learningPath = progressService.getLearningPathByStudent(studentId);
        return ResponseEntity.ok(learningPath);
    }

    /**
     * Crea un nuevo learning path para un estudiante
     * @param studentId ID del estudiante
     * @param request Datos para crear el learning path
     * @return Learning path creado
     */
    @PostMapping("/students/{studentId}/learning-path")
    @Operation(summary = "Create student learning path", description = "Creates a new learning path for a specific student")
    public ResponseEntity<ProgressResponseDto.LearningPathDto> createLearningPath(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId,
            @Valid @RequestBody ProgressRequestDto.CreateLearningPathDto request) {
        log.info("POST /api/progress/students/{}/learning-path - Creando learning path", studentId);
        
        // Asegurar que el studentId del path coincida con el de la URL
        request.setStudentProfileId(studentId);
        
        ProgressResponseDto.LearningPathDto learningPath = progressService.createLearningPath(request);
        return ResponseEntity.ok(learningPath);
    }

    /**
     * Obtiene el progreso actual completo de un estudiante
     * @param studentId ID del estudiante
     * @return Progreso actual del estudiante
     */
    @GetMapping("/students/{studentId}/current-progress")
    @Operation(summary = "Get current student progress", description = "Retrieves the complete current progress for a specific student")
    public ResponseEntity<ProgressResponseDto.CurrentProgressDto> getCurrentProgress(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId) {
        log.info("GET /api/progress/students/{}/current-progress - Obteniendo progreso actual", studentId);
        
        ProgressResponseDto.CurrentProgressDto currentProgress = progressService.getCurrentProgress(studentId);
        return ResponseEntity.ok(currentProgress);
    }

    /**
     * Marca una lección como completada
     * @param studentId ID del estudiante
     * @param lessonId ID de la lección
     * @param timeSpentMinutes Tiempo empleado en minutos (opcional)
     * @return Resultado de completar la lección
     */
    @PostMapping("/students/{studentId}/lessons/{lessonId}/complete")
    @Operation(summary = "Complete lesson", description = "Marks a specific lesson as completed for a student")
    public ResponseEntity<ProgressResponseDto.LessonCompletionDto> completeLesson(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId,
            @Parameter(description = "Lesson ID") @PathVariable Integer lessonId,
            @Parameter(description = "Time spent in minutes") @RequestParam(required = false) Integer timeSpentMinutes) {
        log.info("POST /api/progress/students/{}/lessons/{}/complete - Completando lección", studentId, lessonId);
        
        ProgressResponseDto.LessonCompletionDto completion = 
                progressService.completeLessonById(studentId, lessonId, timeSpentMinutes);
        return ResponseEntity.ok(completion);
    }

    /**
     * Obtiene el siguiente learning point en el learning path del estudiante
     * @param studentId ID del estudiante
     * @return Siguiente learning point disponible
     */
    @GetMapping("/students/{studentId}/learning-path/next-learning-point")
    @Operation(summary = "Get next learning point", description = "Retrieves the next available learning point in the student's learning path")
    public ResponseEntity<ProgressResponseDto.NextLearningPointDto> getNextLearningPoint(
            @Parameter(description = "Student profile ID") @PathVariable Integer studentId) {
        log.info("GET /api/progress/students/{}/learning-path/next-learning-point - Obteniendo siguiente learning point", studentId);
        
        ProgressResponseDto.NextLearningPointDto nextLearningPoint = 
                progressService.getNextLearningPoint(studentId);
        return ResponseEntity.ok(nextLearningPoint);
    }
} 