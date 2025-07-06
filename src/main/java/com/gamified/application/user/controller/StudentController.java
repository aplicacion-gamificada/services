package com.gamified.application.user.controller;

import com.gamified.application.user.service.StudentService;
import com.gamified.application.user.model.dto.response.StudentResponseDto;
import com.gamified.application.shared.util.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * Controlador para operaciones específicas de estudiantes
 */
@RestController
@RequestMapping("/user/student")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Student",
        description = "Provides endpoints for student-specific operations including assigned specializations and progress tracking."
)
@SecurityRequirement(name = "bearerAuth")
public class StudentController {

    private final StudentService studentService;
    private final AuthenticationUtils authenticationUtils;

    /**
     * Obtiene la especialización asignada al estudiante autenticado
     * @param authentication Información de autenticación del usuario
     * @return Especialización asignada del estudiante
     */
    @GetMapping("/assigned-specialization")
    @Operation(summary = "Get assigned specialization", description = "Retrieves the specialization assigned to the authenticated student through their classroom")
    public ResponseEntity<StudentResponseDto.AssignedSpecializationDto> getAssignedSpecialization(
            Authentication authentication) {
        log.info("GET /api/user/student/assigned-specialization - Obteniendo especialización asignada");
        
        Integer studentProfileId = authenticationUtils.getStudentProfileIdFromAuthentication(authentication);
        
        StudentResponseDto.AssignedSpecializationDto assignedSpecialization = 
                studentService.getAssignedSpecialization(studentProfileId);
        
        return ResponseEntity.ok(assignedSpecialization);
    }

    /**
     * Obtiene el progreso del estudiante en una especialización específica
     * @param specializationId ID de la especialización
     * @param authentication Información de autenticación del usuario
     * @return Progreso del estudiante en la especialización
     */
    @GetMapping("/specialization/{specializationId}/progress")
    @Operation(summary = "Get specialization progress", description = "Retrieves the progress of the authenticated student in a specific specialization")
    public ResponseEntity<StudentResponseDto.SpecializationProgressDto> getSpecializationProgress(
            @Parameter(description = "Specialization ID") @PathVariable Integer specializationId,
            Authentication authentication) {
        log.info("GET /api/user/student/specialization/{}/progress - Obteniendo progreso de especialización", specializationId);
        
        Integer studentProfileId = authenticationUtils.getStudentProfileIdFromAuthentication(authentication);
        
        StudentResponseDto.SpecializationProgressDto progress = 
                studentService.getSpecializationProgress(studentProfileId, specializationId);
        
        return ResponseEntity.ok(progress);
    }

    /**
     * Obtiene el progreso del estudiante en un classroom específico
     * @param classroomId ID del classroom
     * @param authentication Información de autenticación del usuario
     * @return Progreso del estudiante en el classroom
     */
    @GetMapping("/classroom/{classroomId}/progress")
    @Operation(summary = "Get classroom progress", description = "Retrieves the progress of the authenticated student in a specific classroom")
    public ResponseEntity<StudentResponseDto.ClassroomProgressDto> getClassroomProgress(
            @Parameter(description = "Classroom ID") @PathVariable Integer classroomId,
            Authentication authentication) {
        log.info("GET /api/user/student/classroom/{}/progress - Obteniendo progreso de classroom", classroomId);
        
        Integer studentProfileId = authenticationUtils.getStudentProfileIdFromAuthentication(authentication);
        
        StudentResponseDto.ClassroomProgressDto progress = 
                studentService.getClassroomProgress(studentProfileId, classroomId);
        
        return ResponseEntity.ok(progress);
    }

    /**
     * Obtiene el progreso del estudiante en el classroom de una especialización específica
     * Este endpoint combina ambos conceptos para futuras expansiones donde un estudiante
     * pueda tener múltiples classrooms
     * @param classroomId ID del classroom
     * @param specializationId ID de la especialización
     * @param authentication Información de autenticación del usuario
     * @return Progreso del estudiante en el classroom/especialización
     */
    @GetMapping("/classroom/{classroomId}/specialization/{specializationId}/progress")
    @Operation(summary = "Get classroom specialization progress", description = "Retrieves the progress of the authenticated student in a specific classroom and specialization combination")
    public ResponseEntity<StudentResponseDto.ClassroomProgressDto> getClassroomSpecializationProgress(
            @Parameter(description = "Classroom ID") @PathVariable Integer classroomId,
            @Parameter(description = "Specialization ID") @PathVariable Integer specializationId,
            Authentication authentication) {
        log.info("GET /api/user/student/classroom/{}/specialization/{}/progress - Obteniendo progreso de classroom/especialización", 
                classroomId, specializationId);
        
        Integer studentProfileId = authenticationUtils.getStudentProfileIdFromAuthentication(authentication);
        
        // Por ahora, usamos el método existente de classroom progress
        // En el futuro, se podría crear un método específico que valide ambos IDs
        StudentResponseDto.ClassroomProgressDto progress = 
                studentService.getClassroomProgress(studentProfileId, classroomId);
        
        return ResponseEntity.ok(progress);
    }

    /*
     * TODO: Implementar dashboard endpoint una vez que se creen los DTOs necesarios
     * 
     * @GetMapping("/dashboard")
     * @Operation(summary = "Get student dashboard", description = "Retrieves a comprehensive dashboard view for the authenticated student including progress, achievements, and next actions")
     * public ResponseEntity<StudentResponseDto.DashboardDto> getStudentDashboard(
     *         Authentication authentication) {
     *     log.info("GET /api/user/student/dashboard - Obteniendo dashboard del estudiante");
     *     
     *     Integer studentProfileId = authenticationUtils.getStudentProfileIdFromAuthentication(authentication);
     *     
     *     StudentResponseDto.DashboardDto dashboard = 
     *             studentService.getStudentDashboard(studentProfileId);
     *     
     *     return ResponseEntity.ok(dashboard);
     * }
     */
} 