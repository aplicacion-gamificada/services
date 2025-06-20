package com.gamified.application.learning.service;

import com.gamified.application.learning.model.dto.response.LearningResponseDto;
import com.gamified.application.learning.model.entity.*;
import com.gamified.application.learning.repository.LearningRepository;
import com.gamified.application.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio Learning
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LearningServiceImpl implements LearningService {

    private final LearningRepository learningRepository;

    @Override
    public List<LearningResponseDto.StemAreaDto> getAllStemAreas() {
        log.info("Obteniendo todas las áreas STEM activas");
        
        List<StemArea> stemAreas = learningRepository.findAllActiveStemAreas();
        
        return stemAreas.stream()
                .map(this::mapToStemAreaDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningResponseDto.SpecializationDto> getSpecializationsByStemArea(Integer stemAreaId) {
        log.info("Obteniendo especializaciones para área STEM ID: {}", stemAreaId);
        
        // Verificar que el área STEM existe
        Optional<StemArea> stemAreaOpt = learningRepository.findStemAreaById(stemAreaId);
        if (stemAreaOpt.isEmpty()) {
            throw new ResourceNotFoundException("Área STEM no encontrada con ID: " + stemAreaId);
        }
        
        List<Specialization> specializations = learningRepository.findSpecializationsByStemArea(stemAreaId);
        StemArea stemArea = stemAreaOpt.get();
        
        return specializations.stream()
                .map(spec -> mapToSpecializationDto(spec, stemArea))
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningResponseDto.ModuleDto> getModulesBySpecialization(Integer specializationId) {
        log.info("Obteniendo módulos para especialización ID: {}", specializationId);
        
        // Verificar que la especialización existe
        Optional<Specialization> specializationOpt = learningRepository.findSpecializationById(specializationId);
        if (specializationOpt.isEmpty()) {
            throw new ResourceNotFoundException("Especialización no encontrada con ID: " + specializationId);
        }
        
        List<com.gamified.application.learning.model.entity.Module> modules = 
                learningRepository.findModulesBySpecialization(specializationId);
        Specialization specialization = specializationOpt.get();
        
        return modules.stream()
                .map(module -> mapToModuleDto(module, specialization))
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningResponseDto.UnitDto> getUnitsByModule(Integer moduleId) {
        log.info("Obteniendo unidades para módulo ID: {}", moduleId);
        
        // Verificar que el módulo existe
        Optional<com.gamified.application.learning.model.entity.Module> moduleOpt = 
                learningRepository.findModuleById(moduleId);
        if (moduleOpt.isEmpty()) {
            throw new ResourceNotFoundException("Módulo no encontrado con ID: " + moduleId);
        }
        
        List<Unit> units = learningRepository.findUnitsByModule(moduleId);
        com.gamified.application.learning.model.entity.Module module = moduleOpt.get();
        
        return units.stream()
                .map(unit -> mapToUnitDto(unit, module))
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningResponseDto.LearningPointDto> getLearningPointsByUnit(Integer unitId) {
        log.info("Obteniendo learning points para unidad ID: {}", unitId);
        
        // Verificar que la unidad existe
        Optional<Unit> unitOpt = learningRepository.findUnitById(unitId);
        if (unitOpt.isEmpty()) {
            throw new ResourceNotFoundException("Unidad no encontrada con ID: " + unitId);
        }
        
        List<LearningPoint> learningPoints = learningRepository.findLearningPointsByUnit(unitId);
        Unit unit = unitOpt.get();
        
        return learningPoints.stream()
                .map(lp -> mapToLearningPointDto(lp, unit))
                .collect(Collectors.toList());
    }

    @Override
    public List<LearningResponseDto.LessonDto> getLessonsByLearningPoint(Integer learningPointId) {
        log.info("Obteniendo lecciones para learning point ID: {}", learningPointId);
        
        // Verificar que el learning point existe
        Optional<LearningPoint> learningPointOpt = learningRepository.findLearningPointById(learningPointId);
        if (learningPointOpt.isEmpty()) {
            throw new ResourceNotFoundException("Learning point no encontrado con ID: " + learningPointId);
        }
        
        List<Lesson> lessons = learningRepository.findLessonsByLearningPoint(learningPointId);
        LearningPoint learningPoint = learningPointOpt.get();
        
        return lessons.stream()
                .map(lesson -> mapToLessonDto(lesson, learningPoint))
                .collect(Collectors.toList());
    }

    @Override
    public LearningResponseDto.LessonDetailDto getLessonById(Integer lessonId) {
        log.info("Obteniendo lección detallada ID: {}", lessonId);
        
        // Buscar la lección
        Optional<Lesson> lessonOpt = learningRepository.findLessonById(lessonId);
        if (lessonOpt.isEmpty()) {
            throw new ResourceNotFoundException("Lección no encontrada con ID: " + lessonId);
        }
        
        Lesson lesson = lessonOpt.get();
        
        // Buscar el learning point para obtener el título
        Optional<LearningPoint> learningPointOpt = learningRepository.findLearningPointById(lesson.getLearningPointId());
        String learningPointTitle = learningPointOpt.map(LearningPoint::getTitle).orElse("Learning Point");
        
        // Buscar lecciones anterior y siguiente
        Optional<Lesson> previousLessonOpt = learningRepository.findPreviousLesson(
                lesson.getLearningPointId(), lesson.getSequenceOrder());
        Optional<Lesson> nextLessonOpt = learningRepository.findNextLesson(
                lesson.getLearningPointId(), lesson.getSequenceOrder());
        
        return mapToLessonDetailDto(lesson, learningPointTitle, previousLessonOpt, nextLessonOpt);
    }

    // ===================================================================
    // MAPPERS
    // ===================================================================

    private LearningResponseDto.StemAreaDto mapToStemAreaDto(StemArea stemArea) {
        Integer specializationsCount = learningRepository.countSpecializationsByStemArea(stemArea.getId());
        
        return LearningResponseDto.StemAreaDto.builder()
                .id(stemArea.getId())
                .title(stemArea.getTitle())
                .description(stemArea.getDescription())
                .status(stemArea.getStatus())
                .specializationsCount(specializationsCount)
                .build();
    }

    private LearningResponseDto.SpecializationDto mapToSpecializationDto(Specialization specialization, StemArea stemArea) {
        Integer modulesCount = learningRepository.countModulesBySpecialization(specialization.getId());
        
        return LearningResponseDto.SpecializationDto.builder()
                .id(specialization.getId())
                .stemAreaId(specialization.getStemAreaId())
                .stemAreaTitle(stemArea.getTitle())
                .title(specialization.getTitle())
                .description(specialization.getDescription())
                .status(specialization.getStatus())
                .modulesCount(modulesCount)
                .build();
    }

    private LearningResponseDto.ModuleDto mapToModuleDto(
            com.gamified.application.learning.model.entity.Module module, Specialization specialization) {
        Integer unitsCount = learningRepository.countUnitsByModule(module.getId());
        
        return LearningResponseDto.ModuleDto.builder()
                .id(module.getId())
                .specializationId(module.getSpecializationId())
                .specializationTitle(specialization.getTitle())
                .title(module.getTitle())
                .description(module.getDescription())
                .sequence(module.getSequence())
                .status(module.getStatus())
                .unitsCount(unitsCount)
                .build();
    }

    private LearningResponseDto.UnitDto mapToUnitDto(Unit unit, com.gamified.application.learning.model.entity.Module module) {
        Integer learningPointsCount = learningRepository.countLearningPointsByUnit(unit.getId());
        
        return LearningResponseDto.UnitDto.builder()
                .id(unit.getId())
                .moduleId(unit.getModuleId())
                .moduleTitle(module.getTitle())
                .title(unit.getTitle())
                .description(unit.getDescription())
                .sequence(unit.getSequence())
                .status(unit.getStatus())
                .learningPointsCount(learningPointsCount)
                .build();
    }

    private LearningResponseDto.LearningPointDto mapToLearningPointDto(LearningPoint learningPoint, Unit unit) {
        Integer lessonsCount = learningRepository.countLessonsByLearningPoint(learningPoint.getId());
        Integer exercisesCount = learningRepository.countExercisesByLearningPoint(learningPoint.getId());
        
        return LearningResponseDto.LearningPointDto.builder()
                .id(learningPoint.getId())
                .learningPathId(learningPoint.getLearningPathId())
                .unitTitle(unit.getTitle())
                .title(learningPoint.getTitle())
                .description(learningPoint.getDescription())
                .sequenceOrder(learningPoint.getSequenceOrder())
                .estimatedDuration(learningPoint.getEstimatedDuration())
                .difficultyWeight(learningPoint.getDifficultyWeight())
                .masteryThreshold(learningPoint.getMasteryThreshold())
                .isPrerequisite(learningPoint.getIsPrerequisite() != null && learningPoint.getIsPrerequisite() == 1)
                .unlockCriteria(learningPoint.getUnlockCriteria())
                .status(learningPoint.getStatus())
                .lessonsCount(lessonsCount)
                .exercisesCount(exercisesCount)
                .createdAt(learningPoint.getCreatedAt())
                .build();
    }

    private LearningResponseDto.LessonDto mapToLessonDto(Lesson lesson, LearningPoint learningPoint) {
        return LearningResponseDto.LessonDto.builder()
                .id(lesson.getId())
                .learningPointId(lesson.getLearningPointId())
                .learningPointTitle(learningPoint.getTitle())
                .title(lesson.getTitle())
                .contentData(lesson.getContentData())
                .sequenceOrder(lesson.getSequenceOrder())
                .estimatedReadingTime(lesson.getEstimatedReadingTime())
                .isMandatory(lesson.getIsMandatory() != null && lesson.getIsMandatory() == 1)
                .createdAt(lesson.getCreatedAt())
                .build();
    }

    private LearningResponseDto.LessonDetailDto mapToLessonDetailDto(
            Lesson lesson, String learningPointTitle, 
            Optional<Lesson> previousLessonOpt, Optional<Lesson> nextLessonOpt) {
        
        // Mapear lecciones de navegación
        LearningResponseDto.LessonDto previousLesson = null;
        if (previousLessonOpt.isPresent()) {
            Lesson prev = previousLessonOpt.get();
            previousLesson = LearningResponseDto.LessonDto.builder()
                    .id(prev.getId())
                    .learningPointId(prev.getLearningPointId())
                    .learningPointTitle(learningPointTitle)
                    .title(prev.getTitle())
                    .sequenceOrder(prev.getSequenceOrder())
                    .estimatedReadingTime(prev.getEstimatedReadingTime())
                    .isMandatory(prev.getIsMandatory() != null && prev.getIsMandatory() == 1)
                    .createdAt(prev.getCreatedAt())
                    .build();
        }
        
        LearningResponseDto.LessonDto nextLesson = null;
        if (nextLessonOpt.isPresent()) {
            Lesson next = nextLessonOpt.get();
            nextLesson = LearningResponseDto.LessonDto.builder()
                    .id(next.getId())
                    .learningPointId(next.getLearningPointId())
                    .learningPointTitle(learningPointTitle)
                    .title(next.getTitle())
                    .sequenceOrder(next.getSequenceOrder())
                    .estimatedReadingTime(next.getEstimatedReadingTime())
                    .isMandatory(next.getIsMandatory() != null && next.getIsMandatory() == 1)
                    .createdAt(next.getCreatedAt())
                    .build();
        }
        
        return LearningResponseDto.LessonDetailDto.builder()
                .id(lesson.getId())
                .learningPointId(lesson.getLearningPointId())
                .learningPointTitle(learningPointTitle)
                .title(lesson.getTitle())
                .contentData(lesson.getContentData())    
                .sequenceOrder(lesson.getSequenceOrder())
                .estimatedReadingTime(lesson.getEstimatedReadingTime())
                .isMandatory(lesson.getIsMandatory() != null && lesson.getIsMandatory() == 1)
                .createdAt(lesson.getCreatedAt())
                .previousLesson(previousLesson)
                .nextLesson(nextLesson)
                // TODO: Implementar progreso cuando esté disponible el módulo PROGRESS
                .isCompleted(false)
                .completedAt(null)
                .build();
    }
} 