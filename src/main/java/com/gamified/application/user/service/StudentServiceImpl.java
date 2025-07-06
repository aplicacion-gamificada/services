package com.gamified.application.user.service;

import com.gamified.application.clasroom.repository.ClassroomRepository;
import com.gamified.application.clasroom.model.entity.Classroom;
import com.gamified.application.clasroom.model.dto.response.ClassroomResponseDto;
import com.gamified.application.learning.repository.LearningRepository;
import com.gamified.application.learning.model.entity.Specialization;
import com.gamified.application.learning.model.entity.StemArea;
import com.gamified.application.learning.model.entity.Module;
import com.gamified.application.progress.repository.ProgressRepository;
import com.gamified.application.progress.model.entity.LearningPath;
import com.gamified.application.user.model.dto.response.StudentResponseDto;
import com.gamified.application.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para operaciones específicas de estudiantes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {
    
    private final ClassroomRepository classroomRepository;
    private final LearningRepository learningRepository;
    private final ProgressRepository progressRepository;
    
    @Override
    public StudentResponseDto.AssignedSpecializationDto getAssignedSpecialization(Integer studentProfileId) {
        log.info("Obteniendo especialización asignada para estudiante ID: {}", studentProfileId);
        
        // Buscar las aulas del estudiante
        List<ClassroomResponseDto.ClassroomDto> classrooms = classroomRepository.findClassroomsByStudent(studentProfileId);
        
        if (classrooms.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron aulas para el estudiante con ID: " + studentProfileId);
        }
        
        // Tomar la primera aula (asumiendo que el estudiante tiene una especialización principal)
        ClassroomResponseDto.ClassroomDto primaryClassroom = classrooms.get(0);
        
        // Obtener los detalles completos del classroom para acceder al specializationId
        Optional<Classroom> classroomOpt = classroomRepository.findClassroomById(primaryClassroom.getClassroomId());
        
        if (classroomOpt.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró el classroom con ID: " + primaryClassroom.getClassroomId());
        }
        
        Classroom classroom = classroomOpt.get();
        
        if (classroom.getSpecializationId() == null) {
            throw new ResourceNotFoundException("El classroom no tiene una especialización asignada");
        }
        
        // Obtener la especialización
        Optional<Specialization> specializationOpt = learningRepository.findSpecializationById(classroom.getSpecializationId());
        
        if (specializationOpt.isEmpty()) {
            throw new ResourceNotFoundException("Especialización no encontrada con ID: " + classroom.getSpecializationId());
        }
        
        Specialization specialization = specializationOpt.get();
        
        // Obtener el área STEM
        Optional<StemArea> stemAreaOpt = learningRepository.findStemAreaById(specialization.getStemAreaId());
        
        if (stemAreaOpt.isEmpty()) {
            throw new ResourceNotFoundException("Área STEM no encontrada con ID: " + specialization.getStemAreaId());
        }
        
        StemArea stemArea = stemAreaOpt.get();
        
        // Obtener estadísticas de progreso
        Integer totalModules = learningRepository.countModulesBySpecialization(specialization.getId());
        Integer completedModules = getCompletedModulesCount(studentProfileId, specialization.getId());
        Double progressPercentage = totalModules > 0 ? (completedModules * 100.0) / totalModules : 0.0;
        
        return StudentResponseDto.AssignedSpecializationDto.builder()
                .specializationId(specialization.getId())
                .specializationTitle(specialization.getTitle())
                .specializationDescription(specialization.getDescription())
                .stemAreaId(stemArea.getId())
                .stemAreaTitle(stemArea.getTitle())
                .classroomId(classroom.getId())
                .classroomName(classroom.getName())
                .totalModules(totalModules)
                .completedModules(completedModules)
                .progressPercentage(progressPercentage)
                .enrolledAt(getEnrollmentDate(studentProfileId, classroom.getId()))
                .build();
    }
    
    @Override
    public StudentResponseDto.SpecializationProgressDto getSpecializationProgress(Integer studentProfileId, Integer specializationId) {
        log.info("Obteniendo progreso de especialización ID: {} para estudiante ID: {}", specializationId, studentProfileId);
        
        // Verificar que la especialización existe
        Optional<Specialization> specializationOpt = learningRepository.findSpecializationById(specializationId);
        
        if (specializationOpt.isEmpty()) {
            throw new ResourceNotFoundException("Especialización no encontrada con ID: " + specializationId);
        }
        
        Specialization specialization = specializationOpt.get();
        
        // Obtener módulos de la especialización
        List<Module> modules = learningRepository.findModulesBySpecialization(specializationId);
        
        // Calcular estadísticas generales
        Integer totalModules = modules.size();
        Integer completedModules = getCompletedModulesCount(studentProfileId, specializationId);
        Integer totalUnits = modules.stream().mapToInt(module -> learningRepository.countUnitsByModule(module.getId())).sum();
        Integer completedUnits = getCompletedUnitsCount(studentProfileId, specializationId);
        Integer totalLessons = getTotalLessonsCount(specializationId);
        Integer completedLessons = getCompletedLessonsCount(studentProfileId, specializationId);
        
        Double overallProgress = totalLessons > 0 ? (completedLessons * 100.0) / totalLessons : 0.0;
        
        // Mapear progreso de módulos
        List<StudentResponseDto.ModuleProgressDto> moduleProgress = modules.stream()
                .map(module -> mapToModuleProgressDto(module, studentProfileId))
                .collect(Collectors.toList());
        
        // Obtener siguiente elemento de aprendizaje
        StudentResponseDto.NextLearningItemDto nextLearningItem = getNextLearningItem(studentProfileId, specializationId);
        
        return StudentResponseDto.SpecializationProgressDto.builder()
                .specializationId(specialization.getId())
                .specializationTitle(specialization.getTitle())
                .totalModules(totalModules)
                .completedModules(completedModules)
                .totalUnits(totalUnits)
                .completedUnits(completedUnits)
                .totalLessons(totalLessons)
                .completedLessons(completedLessons)
                .overallProgress(overallProgress)
                .modules(moduleProgress)
                .nextLearningItem(nextLearningItem)
                .lastActivity(getLastActivity(studentProfileId, specializationId))
                .build();
    }
    
    @Override
    public StudentResponseDto.ClassroomProgressDto getClassroomProgress(Integer studentProfileId, Integer classroomId) {
        log.info("Obteniendo progreso de classroom ID: {} para estudiante ID: {}", classroomId, studentProfileId);
        
        // Obtener el classroom
        Optional<Classroom> classroomOpt = classroomRepository.findClassroomById(classroomId);
        
        if (classroomOpt.isEmpty()) {
            throw new ResourceNotFoundException("Classroom no encontrado con ID: " + classroomId);
        }
        
        Classroom classroom = classroomOpt.get();
        
        if (classroom.getSpecializationId() == null) {
            throw new ResourceNotFoundException("El classroom no tiene una especialización asignada");
        }
        
        // Verificar que el estudiante está inscrito en el classroom
        if (!classroomRepository.isStudentEnrolled(classroomId, studentProfileId)) {
            throw new ResourceNotFoundException("El estudiante no está inscrito en el classroom con ID: " + classroomId);
        }
        
        // Obtener la especialización
        Optional<Specialization> specializationOpt = learningRepository.findSpecializationById(classroom.getSpecializationId());
        
        if (specializationOpt.isEmpty()) {
            throw new ResourceNotFoundException("Especialización no encontrada con ID: " + classroom.getSpecializationId());
        }
        
        Specialization specialization = specializationOpt.get();
        
        // Obtener módulos y calcular progreso
        List<Module> modules = learningRepository.findModulesBySpecialization(specialization.getId());
        Integer totalModules = modules.size();
        Integer completedModules = getCompletedModulesCount(studentProfileId, specialization.getId());
        Double overallProgress = totalModules > 0 ? (completedModules * 100.0) / totalModules : 0.0;
        
        // Mapear progreso de módulos
        List<StudentResponseDto.ModuleProgressDto> moduleProgress = modules.stream()
                .map(module -> mapToModuleProgressDto(module, studentProfileId))
                .collect(Collectors.toList());
        
        // Obtener siguiente elemento de aprendizaje
        StudentResponseDto.NextLearningItemDto nextLearningItem = getNextLearningItem(studentProfileId, specialization.getId());
        
        return StudentResponseDto.ClassroomProgressDto.builder()
                .classroomId(classroom.getId())
                .classroomName(classroom.getName())
                .specializationId(specialization.getId())
                .specializationTitle(specialization.getTitle())
                .overallProgress(overallProgress)
                .totalModules(totalModules)
                .completedModules(completedModules)
                .modules(moduleProgress)
                .nextLearningItem(nextLearningItem)
                .lastActivity(getLastActivity(studentProfileId, specialization.getId()))
                .build();
    }
    
    // ===================================================================
    // MÉTODOS AUXILIARES
    // ===================================================================
    
    private Integer getCompletedModulesCount(Integer studentProfileId, Integer specializationId) {
        // TODO: Implementar lógica real con ProgressRepository
        // Por ahora retornamos 0 como placeholder
        return 0;
    }
    
    private Integer getCompletedUnitsCount(Integer studentProfileId, Integer specializationId) {
        // TODO: Implementar lógica real con ProgressRepository
        return 0;
    }
    
    private Integer getTotalLessonsCount(Integer specializationId) {
        // TODO: Implementar lógica real con LearningRepository
        return 0;
    }
    
    private Integer getCompletedLessonsCount(Integer studentProfileId, Integer specializationId) {
        // TODO: Implementar lógica real con ProgressRepository
        return 0;
    }
    
    private StudentResponseDto.ModuleProgressDto mapToModuleProgressDto(Module module, Integer studentProfileId) {
        Integer totalUnits = learningRepository.countUnitsByModule(module.getId());
        Integer completedUnits = getCompletedUnitsForModule(studentProfileId, module.getId());
        Double progressPercentage = totalUnits > 0 ? (completedUnits * 100.0) / totalUnits : 0.0;
        Boolean isCompleted = completedUnits.equals(totalUnits) && totalUnits > 0;
        
        return StudentResponseDto.ModuleProgressDto.builder()
                .moduleId(module.getId())
                .moduleTitle(module.getTitle())
                .moduleDescription(module.getDescription())
                .sequence(module.getSequence())
                .totalUnits(totalUnits)
                .completedUnits(completedUnits)
                .progressPercentage(progressPercentage)
                .isCompleted(isCompleted)
                .isUnlocked(isModuleUnlocked(studentProfileId, module))
                .completedAt(getModuleCompletionDate(studentProfileId, module.getId()))
                .build();
    }
    
    private Integer getCompletedUnitsForModule(Integer studentProfileId, Integer moduleId) {
        // TODO: Implementar lógica real con ProgressRepository
        return 0;
    }
    
    private Boolean isModuleUnlocked(Integer studentProfileId, Module module) {
        // TODO: Implementar lógica real basada en secuencia y progreso
        // Por ahora, desbloqueamos el primer módulo y los subsiguientes si el anterior está completo
        return module.getSequence() == 1;
    }
    
    private LocalDateTime getModuleCompletionDate(Integer studentProfileId, Integer moduleId) {
        // TODO: Implementar lógica real con ProgressRepository
        return null;
    }
    
    private StudentResponseDto.NextLearningItemDto getNextLearningItem(Integer studentProfileId, Integer specializationId) {
        // TODO: Implementar lógica real para determinar el siguiente elemento de aprendizaje
        return StudentResponseDto.NextLearningItemDto.builder()
                .itemType("MODULE")
                .itemId(1)
                .itemTitle("Próximo módulo")
                .itemDescription("Descripción del próximo módulo")
                .navigationPath("/learning/modules/1")
                .isRecommended(true)
                .recommendationReason("Siguiente en la secuencia de aprendizaje")
                .build();
    }
    
    private LocalDateTime getLastActivity(Integer studentProfileId, Integer specializationId) {
        // TODO: Implementar lógica real con ProgressRepository
        return null;
    }
    
    private LocalDateTime getEnrollmentDate(Integer studentProfileId, Integer classroomId) {
        // TODO: Implementar lógica real para obtener la fecha de inscripción
        return LocalDateTime.now();
    }
} 