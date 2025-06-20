package com.gamified.application.progress.service;

import com.gamified.application.progress.model.dto.request.ProgressRequestDto;
import com.gamified.application.progress.model.dto.response.ProgressResponseDto;
import com.gamified.application.progress.model.entity.LearningPath;
import com.gamified.application.progress.model.entity.LessonProgress;
import com.gamified.application.progress.repository.ProgressRepository;
import com.gamified.application.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio Progress
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;

    @Override
    public ProgressResponseDto.LearningPathDto getLearningPathByStudent(Integer studentProfileId) {
        log.info("Obteniendo learning path para estudiante ID: {}", studentProfileId);
        
        Optional<LearningPath> learningPathOpt = progressRepository.findActiveLearningPathByStudent(studentProfileId);
        
        if (learningPathOpt.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró learning path activo para el estudiante con ID: " + studentProfileId);
        }
        
        LearningPath learningPath = learningPathOpt.get();
        return mapToLearningPathDto(learningPath);
    }

    @Override
    public ProgressResponseDto.LearningPathDto createLearningPath(ProgressRequestDto.CreateLearningPathDto request) {
        log.info("Creando learning path para estudiante ID: {}", request.getStudentProfileId());
        
        // Verificar que no exista un learning path activo
        Optional<LearningPath> existingPath = progressRepository.findActiveLearningPathByStudent(request.getStudentProfileId());
        if (existingPath.isPresent()) {
            log.warn("Ya existe un learning path activo para el estudiante ID: {}", request.getStudentProfileId());
            return mapToLearningPathDto(existingPath.get());
        }
        
        // Crear el learning path
        LearningPath learningPath = LearningPath.builder()
                .studentProfileId(request.getStudentProfileId())
                .adaptiveInterventionId(null)
                .currentLearningPointId(request.getStartingLearningPointId())
                .unitsId(request.getUnitsId())
                .completionPercentage(BigDecimal.ZERO)
                .difficultyAdjustment(request.getDifficultyAdjustment() != null ? 
                                    request.getDifficultyAdjustment() : BigDecimal.ONE)
                .isActive(1)
                .build();
        
        LearningPath createdPath = progressRepository.createLearningPath(learningPath);
        log.info("Learning path creado exitosamente con ID: {}", createdPath.getId());
        
        return mapToLearningPathDto(createdPath);
    }

    @Override
    public ProgressResponseDto.CurrentProgressDto getCurrentProgress(Integer studentProfileId) {
        log.info("Obteniendo progreso actual para estudiante ID: {}", studentProfileId);
        
        ProgressResponseDto.LearningPathDto learningPath = getLearningPathByStudent(studentProfileId);
        
        // Por ahora retorna estructura básica
        return ProgressResponseDto.CurrentProgressDto.builder()
                .studentProfileId(studentProfileId)
                .learningPath(learningPath)
                .totalLearningPoints(0)
                .completedLearningPoints(0)
                .totalLessons(0)
                .completedLessons(0)
                .overallCompletionPercentage(learningPath.getCompletionPercentage())
                .currentLearningPoint(null)
                .currentLessons(new ArrayList<>())
                .build();
    }

    @Override
    public ProgressResponseDto.LessonCompletionDto completeLessonById(
            Integer studentProfileId, Integer lessonId, Integer timeSpentMinutes) {
        log.info("Completando lección {} para estudiante {}", lessonId, studentProfileId);
        
        // Implementación básica
        return ProgressResponseDto.LessonCompletionDto.builder()
                .lessonId(lessonId)
                .lessonTitle("Lección")
                .wasAlreadyCompleted(false)
                .completedAt(LocalDateTime.now())
                .overallProgress(BigDecimal.ZERO)
                .allLessonsCompleted(false)
                .build();
    }

    @Override
    public ProgressResponseDto.NextLearningPointDto getNextLearningPoint(Integer studentProfileId) {
        log.info("Obteniendo siguiente learning point para estudiante ID: {}", studentProfileId);
        
        // Implementación básica
        return ProgressResponseDto.NextLearningPointDto.builder()
                .learningPointId(1)
                .title("Siguiente Learning Point")
                .description("Descripción del siguiente learning point")
                .sequenceOrder(1)
                .estimatedDuration(30)
                .difficultyWeight(BigDecimal.ONE)
                .isUnlocked(true)
                .unitId(1)
                .unitTitle("Unidad")
                .totalLessons(0)
                .lessons(new ArrayList<>())
                .build();
    }

    // ===================================================================
    // MÉTODOS PRIVADOS DE APOYO
    // ===================================================================

    private ProgressResponseDto.LearningPathDto mapToLearningPathDto(LearningPath learningPath) {
        // Obtener información adicional
        Optional<ProgressRepository.LearningPointInfo> currentLearningPointOpt = 
                progressRepository.findLearningPointById(learningPath.getCurrentLearningPointId());
        String currentLearningPointTitle = currentLearningPointOpt.map(ProgressRepository.LearningPointInfo::getTitle).orElse("Learning Point");
        
        Optional<ProgressRepository.UnitInfo> unitInfoOpt = progressRepository.findUnitById(learningPath.getUnitsId());
        String unitTitle = unitInfoOpt.map(ProgressRepository.UnitInfo::getTitle).orElse("Unidad");
        
        return ProgressResponseDto.LearningPathDto.builder()
                .id(learningPath.getId())
                .studentProfileId(learningPath.getStudentProfileId())
                .currentLearningPointId(learningPath.getCurrentLearningPointId())
                .currentLearningPointTitle(currentLearningPointTitle)
                .unitsId(learningPath.getUnitsId())
                .unitTitle(unitTitle)
                .completionPercentage(learningPath.getCompletionPercentage())
                .difficultyAdjustment(learningPath.getDifficultyAdjustment())
                .isActive(learningPath.getIsActive() == 1)
                .createdAt(learningPath.getCreatedAt())
                .updatedAt(learningPath.getUpdatedAt())
                .totalLessons(0)
                .completedLessons(0)
                .remainingLessons(0)
                .build();
    }

    private ProgressResponseDto.LearningPointProgressDto getCurrentLearningPointProgress(
            Integer studentProfileId, Integer learningPointId) {
        
        Optional<ProgressRepository.LearningPointInfo> learningPointOpt = 
                progressRepository.findLearningPointById(learningPointId);
        
        if (learningPointOpt.isEmpty()) {
            throw new ResourceNotFoundException("Learning point no encontrado con ID: " + learningPointId);
        }
        
        ProgressRepository.LearningPointInfo learningPoint = learningPointOpt.get();
        
        Integer totalLessons = progressRepository.countLessonsByLearningPoint(learningPointId);
        Integer completedLessons = progressRepository.countCompletedLessonsByStudentAndLearningPoint(
                studentProfileId, learningPointId);
        
        BigDecimal completionPercentage = totalLessons > 0 ? 
                BigDecimal.valueOf(completedLessons).multiply(BigDecimal.valueOf(100))
                          .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP) : 
                BigDecimal.ZERO;
        
        boolean isCompleted = completedLessons.equals(totalLessons);
        
        return ProgressResponseDto.LearningPointProgressDto.builder()
                .learningPointId(learningPointId)
                .title(learningPoint.getTitle())
                .description(learningPoint.getDescription())
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .completionPercentage(completionPercentage)
                .isCompleted(isCompleted)
                .completedAt(isCompleted ? LocalDateTime.now() : null) // Simplificado
                .build();
    }

    private List<ProgressResponseDto.LessonProgressDto> getCurrentLessonsProgress(
            Integer studentProfileId, Integer learningPointId) {
        
        List<LessonProgress> lessonProgressList = 
                progressRepository.findLessonProgressByStudentAndLearningPoint(studentProfileId, learningPointId);
        
        return lessonProgressList.stream()
                .map(progress -> {
                    Optional<ProgressRepository.LessonInfo> lessonInfoOpt = 
                            progressRepository.findLessonById(progress.getLessonId());
                    
                    return ProgressResponseDto.LessonProgressDto.builder()
                            .lessonId(progress.getLessonId())
                            .title(lessonInfoOpt.map(ProgressRepository.LessonInfo::getTitle).orElse("Lección"))
                            .sequenceOrder(lessonInfoOpt.map(ProgressRepository.LessonInfo::getSequenceOrder).orElse(0))
                            .isCompleted(progress.isCompleted())
                            .completedAt(progress.getCompletedAt())
                            .timeSpentMinutes(progress.getTimeSpentMinutes())
                            .isMandatory(lessonInfoOpt.map(ProgressRepository.LessonInfo::isMandatory).orElse(false))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void updateLearningPathProgress(Integer studentProfileId, Integer learningPointId) {
        // Obtener learning path actual
        Optional<LearningPath> learningPathOpt = progressRepository.findActiveLearningPathByStudent(studentProfileId);
        
        if (learningPathOpt.isPresent()) {
            LearningPath learningPath = learningPathOpt.get();
            
            // Calcular nuevo porcentaje de completitud
            Integer totalLessons = progressRepository.countLessonsByLearningPoint(learningPointId);
            Integer completedLessons = progressRepository.countCompletedLessonsByStudentAndLearningPoint(
                    studentProfileId, learningPointId);
            
            if (totalLessons > 0) {
                BigDecimal newCompletionPercentage = BigDecimal.valueOf(completedLessons)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalLessons), 2, RoundingMode.HALF_UP);
                
                learningPath.setCompletionPercentage(newCompletionPercentage);
                progressRepository.updateLearningPath(learningPath);
            }
        }
    }

    private Optional<ProgressRepository.LessonInfo> getNextLesson(Integer studentProfileId, Integer currentLessonId) {
        // Simplificado: retorna vacío por ahora
        return Optional.empty();
    }

    private Optional<ProgressRepository.LearningPointInfo> getNextLearningPointInfo(Integer studentProfileId) {
        // Obtener learning path actual
        Optional<LearningPath> learningPathOpt = progressRepository.findActiveLearningPathByStudent(studentProfileId);
        
        if (learningPathOpt.isPresent()) {
            LearningPath learningPath = learningPathOpt.get();
            return progressRepository.findNextLearningPoint(
                    learningPath.getCurrentLearningPointId(), learningPath.getUnitsId());
        }
        
        return Optional.empty();
    }

    private LearningPath getCurrentLearningPath(Integer studentProfileId) {
        return progressRepository.findActiveLearningPathByStudent(studentProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path no encontrado para estudiante: " + studentProfileId));
    }
} 