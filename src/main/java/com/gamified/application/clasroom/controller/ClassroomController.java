package com.gamified.application.clasroom.controller;

import com.gamified.application.clasroom.model.dto.request.ClassroomRequestDto;
import com.gamified.application.clasroom.model.dto.response.ClassroomResponseDto;
import com.gamified.application.clasroom.service.ClassroomService;
import com.gamified.application.auth.service.auth.TokenService;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para gestión de aulas (Classroom)
 * Implementa los endpoints necesarios para crear aulas e inscribir estudiantes
 */
@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Classroom Management", description = "Endpoints para gestión de aulas y inscripciones de estudiantes")
public class ClassroomController {

    private final ClassroomService classroomService;
    private final TokenService tokenService;

    // ===================================================================
    // CLASSROOM MANAGEMENT ENDPOINTS
    // ===================================================================

    /**
     * Crea un nuevo aula para el profesor autenticado
     * POST /api/teachers/classrooms
     */
    @PostMapping("/classrooms")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Crear aula", description = "Crea un nuevo aula para el profesor autenticado")
    public ResponseEntity<ClassroomResponseDto.ClassroomDto> createClassroom(
            @Valid @RequestBody ClassroomRequestDto.CreateClassroomRequestDto request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        try {
            Long teacherUserId = getUserIdFromToken(httpRequest);
            log.info("POST /api/teachers/classrooms - Teacher {} creating classroom: {}", teacherUserId, request);
            
            ClassroomResponseDto.ClassroomDto classroom = classroomService.createClassroom(teacherUserId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(classroom);
            
        } catch (Exception e) {
            log.error("Error creating classroom: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear el aula: " + e.getMessage());
        }
    }

    /**
     * Obtiene todas las aulas del profesor autenticado
     * GET /api/teachers/classrooms (ya existe en TeacherController, pero podemos duplicar para completitud)
     */
    @GetMapping("/classrooms")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Obtener aulas del profesor", description = "Obtiene todas las aulas del profesor autenticado")
    public ResponseEntity<List<ClassroomResponseDto.ClassroomDto>> getMyClassrooms(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        try {
            Long teacherUserId = getUserIdFromToken(httpRequest);
            log.info("GET /api/teachers/classrooms - Teacher {} getting classrooms", teacherUserId);
            
            List<ClassroomResponseDto.ClassroomDto> classrooms = classroomService.getTeacherClassrooms(teacherUserId);
            
            return ResponseEntity.ok(classrooms);
            
        } catch (Exception e) {
            log.error("Error getting teacher classrooms: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener las aulas: " + e.getMessage());
        }
    }

    /**
     * Obtiene detalles completos de un aula específica
     * GET /api/teachers/classrooms/{classroomId}
     */
    @GetMapping("/classrooms/{classroomId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Obtener detalles del aula", description = "Obtiene detalles completos de un aula específica")
    public ResponseEntity<ClassroomResponseDto.ClassroomDetailDto> getClassroomDetail(
            @PathVariable Integer classroomId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        try {
            Long teacherUserId = getUserIdFromToken(httpRequest);
            log.info("GET /api/teachers/classrooms/{} - Teacher {} getting classroom detail", classroomId, teacherUserId);
            
            ClassroomResponseDto.ClassroomDetailDto classroomDetail = classroomService.getClassroomDetail(teacherUserId, classroomId);
            
            return ResponseEntity.ok(classroomDetail);
            
        } catch (Exception e) {
            log.error("Error getting classroom detail for classroom {}: {}", classroomId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener detalles del aula: " + e.getMessage());
        }
    }

    // ===================================================================
    // STUDENT ENROLLMENT ENDPOINTS
    // ===================================================================

    /**
     * Inscribe un estudiante en un aula específica
     * POST /api/teachers/classrooms/{classroomId}/enroll
     */
    @PostMapping("/classrooms/{classroomId}/enroll")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Inscribir estudiante", description = "Inscribe un estudiante en un aula específica")
    public ResponseEntity<ClassroomResponseDto.EnrollmentResponseDto> enrollStudent(
            @PathVariable Integer classroomId,
            @Valid @RequestBody ClassroomRequestDto.EnrollStudentRequestDto request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        try {
            Long teacherUserId = getUserIdFromToken(httpRequest);
            log.info("POST /api/teachers/classrooms/{}/enroll - Teacher {} enrolling student {}", 
                    classroomId, teacherUserId, request.getStudentProfileId());
            
            ClassroomResponseDto.EnrollmentResponseDto enrollment = classroomService.enrollStudent(teacherUserId, classroomId, request);
            
            HttpStatus status = enrollment.getSuccess() ? HttpStatus.CREATED : HttpStatus.CONFLICT;
            
            return ResponseEntity.status(status).body(enrollment);
            
        } catch (Exception e) {
            log.error("Error enrolling student {} in classroom {}: {}", request.getStudentProfileId(), classroomId, e.getMessage(), e);
            throw new RuntimeException("Error al inscribir estudiante: " + e.getMessage());
        }
    }

    /**
     * Obtiene todos los estudiantes de un aula específica
     * GET /api/teachers/classrooms/{classroomId}/students
     */
    @GetMapping("/classrooms/{classroomId}/students")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Obtener estudiantes del aula", description = "Obtiene todos los estudiantes inscritos en un aula específica")
    public ResponseEntity<List<ClassroomResponseDto.StudentInClassroomDto>> getClassroomStudents(
            @PathVariable Integer classroomId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        try {
            Long teacherUserId = getUserIdFromToken(httpRequest);
            log.info("GET /api/teachers/classrooms/{}/students - Teacher {} getting students", classroomId, teacherUserId);
            
            List<ClassroomResponseDto.StudentInClassroomDto> students = classroomService.getClassroomStudents(teacherUserId, classroomId);
            
            return ResponseEntity.ok(students);
            
        } catch (Exception e) {
            log.error("Error getting students for classroom {}: {}", classroomId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener estudiantes del aula: " + e.getMessage());
        }
    }

    /**
     * Desinscribe un estudiante de un aula
     * DELETE /api/teachers/classrooms/{classroomId}/students/{studentProfileId}
     */
    @DeleteMapping("/classrooms/{classroomId}/students/{studentProfileId}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Desinscribir estudiante", description = "Desinscribe un estudiante de un aula específica")
    public ResponseEntity<CommonResponseDto> unenrollStudent(
            @PathVariable Integer classroomId,
            @PathVariable Integer studentProfileId,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        try {
            Long teacherUserId = getUserIdFromToken(httpRequest);
            log.info("DELETE /api/teachers/classrooms/{}/students/{} - Teacher {} unenrolling student", 
                    classroomId, studentProfileId, teacherUserId);
            
            boolean result = classroomService.unenrollStudent(teacherUserId, classroomId, studentProfileId);
            
            CommonResponseDto response = CommonResponseDto.builder()
                    .success(result)
                    .message(result ? "Estudiante desinscrito exitosamente" : "No se pudo desinscribir al estudiante")
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error unenrolling student {} from classroom {}: {}", studentProfileId, classroomId, e.getMessage(), e);
            
            CommonResponseDto response = CommonResponseDto.builder()
                    .success(false)
                    .message("Error al desinscribir estudiante: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===================================================================
    // HELPER METHODS
    // ===================================================================

    /**
     * Extrae el User ID del token JWT
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return tokenService.extractUserId(token);
            }
            throw new RuntimeException("Token no encontrado en el header Authorization");
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener ID de usuario del token: " + e.getMessage());
        }
    }
} 