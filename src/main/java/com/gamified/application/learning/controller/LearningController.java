package com.gamified.application.learning.controller;

import com.gamified.application.learning.model.dto.response.LearningResponseDto;
import com.gamified.application.learning.service.LearningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Controlador para operaciones del módulo Learning
 */
@RestController
@RequestMapping("/learning")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Learning ",
        description = "Provides endpoints for managing learning content, including STEM areas, specializations, modules, units, and learning points."
)
public class LearningController {

    private final LearningService learningService;

    /**
     * Obtiene todas las áreas STEM disponibles
     * @return Lista de áreas STEM
     */
    @GetMapping("/stem-areas")
    public ResponseEntity<List<LearningResponseDto.StemAreaDto>> getAllStemAreas() {
        log.info("GET /api/learning/stem-areas - Obteniendo todas las áreas STEM");
        
        List<LearningResponseDto.StemAreaDto> stemAreas = learningService.getAllStemAreas();
        return ResponseEntity.ok(stemAreas);
    }

    /**
     * Obtiene todas las especializaciones de un área STEM
     * @param stemAreaId ID del área STEM
     * @return Lista de especializaciones
     */
    @GetMapping("/stem-areas/{stemAreaId}/specializations")
    public ResponseEntity<List<LearningResponseDto.SpecializationDto>> getSpecializationsByStemArea(
            @PathVariable Integer stemAreaId) {
        log.info("GET /api/learning/stem-areas/{}/specializations - Obteniendo especializaciones", stemAreaId);
        
        List<LearningResponseDto.SpecializationDto> specializations = 
                learningService.getSpecializationsByStemArea(stemAreaId);
        return ResponseEntity.ok(specializations);
    }

    /**
     * Obtiene todos los módulos de una especialización
     * @param specializationId ID de la especialización
     * @return Lista de módulos
     */
    @GetMapping("/specializations/{specializationId}/modules")
    public ResponseEntity<List<LearningResponseDto.ModuleDto>> getModulesBySpecialization(
            @PathVariable Integer specializationId) {
        log.info("GET /api/learning/specializations/{}/modules - Obteniendo módulos", specializationId);
        
        List<LearningResponseDto.ModuleDto> modules = 
                learningService.getModulesBySpecialization(specializationId);
        return ResponseEntity.ok(modules);
    }

    /**
     * Obtiene todas las unidades de un módulo
     * @param moduleId ID del módulo
     * @return Lista de unidades
     */
    @GetMapping("/modules/{moduleId}/units")
    public ResponseEntity<List<LearningResponseDto.UnitDto>> getUnitsByModule(
            @PathVariable Integer moduleId) {
        log.info("GET /api/learning/modules/{}/units - Obteniendo unidades", moduleId);
        
        List<LearningResponseDto.UnitDto> units = learningService.getUnitsByModule(moduleId);
        return ResponseEntity.ok(units);
    }

    /**
     * Obtiene todos los learning points de una unidad
     * @param unitId ID de la unidad
     * @return Lista de learning points
     */
    @GetMapping("/units/{unitId}/learning-points")
    public ResponseEntity<List<LearningResponseDto.LearningPointDto>> getLearningPointsByUnit(
            @PathVariable Integer unitId) {
        log.info("GET /api/learning/units/{}/learning-points - Obteniendo learning points", unitId);
        
        List<LearningResponseDto.LearningPointDto> learningPoints = 
                learningService.getLearningPointsByUnit(unitId);
        return ResponseEntity.ok(learningPoints);
    }

    /**
     * Obtiene todas las lecciones de un learning point
     * @param learningPointId ID del learning point
     * @return Lista de lecciones
     */
    @GetMapping("/learning-points/{learningPointId}/lessons")
    public ResponseEntity<List<LearningResponseDto.LessonDto>> getLessonsByLearningPoint(
            @PathVariable Integer learningPointId) {
        log.info("GET /api/learning/learning-points/{}/lessons - Obteniendo lecciones", learningPointId);
        
        List<LearningResponseDto.LessonDto> lessons = 
                learningService.getLessonsByLearningPoint(learningPointId);
        return ResponseEntity.ok(lessons);
    }

    /**
     * Obtiene una lección específica con navegación
     * @param lessonId ID de la lección
     * @return Lección detallada con navegación
     */
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LearningResponseDto.LessonDetailDto> getLessonById(
            @PathVariable Integer lessonId) {
        log.info("GET /api/learning/lessons/{} - Obteniendo lección detallada", lessonId);
        
        LearningResponseDto.LessonDetailDto lesson = learningService.getLessonById(lessonId);
        return ResponseEntity.ok(lesson);
    }
} 