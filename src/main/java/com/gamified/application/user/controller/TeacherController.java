package com.gamified.application.user.controller;

import com.gamified.application.user.model.dto.response.UserResponseDto;
import com.gamified.application.user.service.TeacherService;
import com.gamified.application.auth.service.auth.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Controlador específico para operaciones relacionadas con profesores
 */
@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Teacher ",
        description = "Provides endpoints for managing teacher profiles, classrooms, and related users."
)
public class TeacherController {

    private final TeacherService teacherService;
    private final TokenService tokenService;

    /**
     * Obtiene todos los usuarios relacionados con el teacher actual
     * Esto incluye todos los estudiantes de sus classrooms y sus guardianes asociados
     * 
     * @param authentication Datos de autenticación del teacher
     * @param request Request HTTP para extraer el token
     * @return TeacherRelatedUsersDto con estudiantes y guardianes organizados
     */
    @GetMapping("/my-related-users")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<UserResponseDto.TeacherRelatedUsersDto> getMyRelatedUsers(
            Authentication authentication, 
            HttpServletRequest request) {
        
        try {
            Long teacherUserId = getUserIdFromToken(request);
            log.info("GET /api/teachers/my-related-users - Teacher {} obteniendo usuarios relacionados", teacherUserId);
            
            UserResponseDto.TeacherRelatedUsersDto relatedUsers = teacherService.getRelatedUsers(teacherUserId);
            return ResponseEntity.ok(relatedUsers);
            
        } catch (Exception e) {
            log.error("Error obteniendo usuarios relacionados del teacher: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener usuarios relacionados: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene los classrooms del teacher actual
     * 
     * @param authentication Datos de autenticación del teacher
     * @param request Request HTTP para extraer el token
     * @return Lista de classrooms del teacher
     */
    @GetMapping("/my-classrooms")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<UserResponseDto.ClassroomDto>> getMyClassrooms(
            Authentication authentication, 
            HttpServletRequest request) {
        
        try {
            Long teacherUserId = getUserIdFromToken(request);
            log.info("GET /api/teachers/my-classrooms - Teacher {} obteniendo sus classrooms", teacherUserId);
            
            List<UserResponseDto.ClassroomDto> classrooms = teacherService.getTeacherClassrooms(teacherUserId);
            return ResponseEntity.ok(classrooms);
            
        } catch (Exception e) {
            log.error("Error obteniendo classrooms del teacher: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener classrooms: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene los estudiantes de un classroom específico del teacher con información completa
     * 
     * @param classroomId ID del classroom
     * @param authentication Datos de autenticación del teacher
     * @param request Request HTTP para extraer el token
     * @return Lista de estudiantes del classroom con información detallada y guardián
     */
    @GetMapping("/classrooms/{classroomId}/students-with-guardian")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<UserResponseDto.StudentWithGuardianDto>> getStudentsByClassroom(
            @PathVariable Long classroomId,
            Authentication authentication, 
            HttpServletRequest request) {
        
        try {
            Long teacherUserId = getUserIdFromToken(request);
            log.info("GET /api/teachers/classrooms/{}/students - Teacher {} obteniendo estudiantes del classroom", 
                    classroomId, teacherUserId);
            
            // Verificar que el classroom pertenece al teacher
            List<UserResponseDto.StudentWithGuardianDto> students = 
                teacherService.getStudentsByClassroom(teacherUserId, classroomId);
            return ResponseEntity.ok(students);
            
        } catch (Exception e) {
            log.error("Error obteniendo estudiantes del classroom {}: {}", classroomId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener estudiantes del classroom: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene resumen estadístico de los usuarios relacionados al teacher
     * 
     * @param authentication Datos de autenticación del teacher
     * @param request Request HTTP para extraer el token  
     * @return Estadísticas de usuarios relacionados
     */
    @GetMapping("/my-stats")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<UserResponseDto.TeacherStatsDto> getMyStats(
            Authentication authentication, 
            HttpServletRequest request) {
        
        try {
            Long teacherUserId = getUserIdFromToken(request);
            log.info("GET /api/teachers/my-stats - Teacher {} obteniendo estadísticas", teacherUserId);
            
            UserResponseDto.TeacherStatsDto stats = teacherService.getTeacherStats(teacherUserId);
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error obteniendo estadísticas del teacher: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener estadísticas: " + e.getMessage());
        }
    }

    /**
     * Extrae el ID de usuario del token JWT en la request
     * @param request Request HTTP
     * @return ID del usuario
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Token de autorización no encontrado");
        }
        
        String token = authHeader.substring(7);
        Long userId = tokenService.extractUserId(token);
        if (userId == null) {
            throw new IllegalStateException("No se pudo extraer el ID de usuario del token");
        }
        
        return userId;
    }
} 