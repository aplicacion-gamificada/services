package com.gamified.application.institution.controller;

import com.gamified.application.institution.model.dto.request.InstitutionRequestDto;
import com.gamified.application.institution.model.dto.request.AdminRequestDto;
import com.gamified.application.institution.model.dto.response.InstitutionResponseDto;
import com.gamified.application.institution.model.dto.response.AdminResponseDto;
import com.gamified.application.institution.service.InstitutionService;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para operaciones relacionadas con instituciones
 */
@RestController
@RequestMapping("/institutions")
@RequiredArgsConstructor
@Slf4j
@Tag(
        name = "Institution ",
        description = "Provides endpoints for managing institutions, administrators, and user assignments."
)
public class InstitutionController {

    private final InstitutionService institutionService;

    /**
     * Obtiene todas las instituciones activas
     * @return Lista de instituciones
     */
    @GetMapping
    public ResponseEntity<List<InstitutionResponseDto.InstitutionSummaryDto>> getAllActiveInstitutions() {
        log.info("GET /api/institutions - Obteniendo todas las instituciones activas");
        List<InstitutionResponseDto.InstitutionSummaryDto> institutions = 
                institutionService.getAllActiveInstitutions();
        return ResponseEntity.ok(institutions);
    }

    /**
     * Obtiene una institución por su ID
     * @param institutionId ID de la institución
     * @return Institución encontrada
     */
    @GetMapping("/{institutionId}")
    public ResponseEntity<InstitutionResponseDto.InstitutionDetailDto> getInstitutionById(
            @PathVariable Long institutionId) {
        log.info("GET /api/institutions/{} - Obteniendo institución por ID", institutionId);
        
        InstitutionResponseDto.InstitutionDetailDto institution = 
                institutionService.getInstitutionById(institutionId);
        return ResponseEntity.ok(institution);
    }

    /**
     * Busca instituciones por nombre
     * @param query Texto a buscar
     * @param limit Límite de resultados
     * @return Lista de instituciones que coinciden con la búsqueda
     */
    @GetMapping("/search")
    public ResponseEntity<List<InstitutionResponseDto.InstitutionSummaryDto>> searchInstitutions(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/institutions/search?query={}&limit={} - Buscando instituciones", query, limit);
        
        List<InstitutionResponseDto.InstitutionSummaryDto> institutions = 
                institutionService.searchInstitutions(query, limit);
        return ResponseEntity.ok(institutions);
    }
    
    /**
     * Registra una nueva institución
     * @param requestDto Datos de la institución a registrar
     * @return La institución registrada
     */
    @PostMapping
    public ResponseEntity<CommonResponseDto<InstitutionResponseDto.InstitutionDetailDto>> registerInstitution(
            @Valid @RequestBody InstitutionRequestDto.InstitutionRegistrationRequestDto requestDto) {
        log.info("POST /api/institutions - Registrando institución: {}", requestDto.getName());
        
        try {
            InstitutionResponseDto.InstitutionDetailDto registeredInstitution =
                    institutionService.registerInstitution(requestDto);
            
            CommonResponseDto<InstitutionResponseDto.InstitutionDetailDto> response = CommonResponseDto.<InstitutionResponseDto.InstitutionDetailDto>builder()
                    .success(true)
                    .message("Institución registrada exitosamente")
                    .timestamp(LocalDateTime.now())
                    .data(registeredInstitution)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            // En caso de nombre duplicado u otros errores de validación
            log.warn("Error al registrar institución: {}", e.getMessage());
            
            CommonResponseDto<InstitutionResponseDto.InstitutionDetailDto> response = CommonResponseDto.<InstitutionResponseDto.InstitutionDetailDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // Otros errores no esperados
            log.error("Error inesperado al registrar institución", e);
            
            CommonResponseDto<InstitutionResponseDto.InstitutionDetailDto> response = CommonResponseDto.<InstitutionResponseDto.InstitutionDetailDto>builder()
                    .success(false)
                    .message("Error interno al procesar la solicitud")
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ===================================================================
    // ENDPOINTS DE GESTIÓN ADMINISTRATIVA (Solo ADMIN)
    // ===================================================================

    /**
     * ADMIN: Obtener resumen de usuarios de la institución
     * @param institutionId ID de la institución
     * @return Resumen con estadísticas de usuarios
     */
    @GetMapping("/{institutionId}/users/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponseDto.InstitutionUsersSummaryDto> getInstitutionUsersSummary(
            @PathVariable Long institutionId,
            Authentication authentication) {
        log.info("GET /api/institutions/{}/users/summary - Admin obteniendo resumen", institutionId);
        
        validateAdminBelongsToInstitution(institutionId, authentication);
        AdminResponseDto.InstitutionUsersSummaryDto summary = institutionService.getUsersSummary(institutionId);
        return ResponseEntity.ok(summary);
    }

    /**
     * ADMIN: Obtener todos los usuarios de la institución con filtros
     * @param institutionId ID de la institución
     * @param role Filtro por rol: ADMIN, TEACHER, STUDENT, GUARDIAN (opcional)
     * @param unassigned Solo estudiantes sin guardián (opcional)
     * @return Lista de usuarios
     */
    @GetMapping("/{institutionId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminResponseDto.UserSummaryDto>> getInstitutionUsers(
            @PathVariable Long institutionId,
            @RequestParam(required = false, name = "role") String role,
            @RequestParam(required = false) Boolean unassigned,
            Authentication authentication) {
        log.info("GET /api/institutions/{}/users - Admin obteniendo usuarios (role={}, unassigned={})", 
                institutionId, role, unassigned);
        
        // Validar que el role sea válido si se proporciona
        if (role != null && !role.isEmpty()) {
            if (!role.matches("^(ADMIN|TEACHER|STUDENT|GUARDIAN)$")) {
                throw new IllegalArgumentException("Rol inválido. Debe ser uno de: ADMIN, TEACHER, STUDENT, GUARDIAN");
            }
        }
        
        validateAdminBelongsToInstitution(institutionId, authentication);
        List<AdminResponseDto.UserSummaryDto> users = institutionService.getUsers(institutionId, role, unassigned);
        return ResponseEntity.ok(users);
    }

    /**
     * ADMIN: Obtener estudiantes sin guardián asignado
     * @param institutionId ID de la institución
     * @return Lista de estudiantes sin guardián
     */
    @GetMapping("/{institutionId}/students/unassigned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminResponseDto.StudentForAssignmentDto>> getUnassignedStudents(
            @PathVariable Long institutionId,
            Authentication authentication) {
        log.info("GET /api/institutions/{}/students/unassigned - Admin obteniendo estudiantes sin guardián", institutionId);
        
        validateAdminBelongsToInstitution(institutionId, authentication);
        List<AdminResponseDto.StudentForAssignmentDto> students = institutionService.getUnassignedStudents(institutionId);
        return ResponseEntity.ok(students);
    }

    /**
     * ADMIN: Obtener guardianes disponibles para asignación
     * @param institutionId ID de la institución
     * @return Lista de guardianes disponibles
     */
    @GetMapping("/{institutionId}/guardians/available")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AdminResponseDto.GuardianForAssignmentDto>> getAvailableGuardians(
            @PathVariable Long institutionId,
            Authentication authentication) {
        log.info("GET /api/institutions/{}/guardians/available - Admin obteniendo guardianes disponibles", institutionId);
        
        validateAdminBelongsToInstitution(institutionId, authentication);
        List<AdminResponseDto.GuardianForAssignmentDto> guardians = institutionService.getAvailableGuardians(institutionId);
        return ResponseEntity.ok(guardians);
    }

    /**
     * ADMIN/TEACHER: Obtener estudiantes de un guardián específico
     * @param institutionId ID de la institución
     * @param guardianProfileId ID del perfil del guardián
     * @return Lista de estudiantes del guardián
     */
    @GetMapping("/{institutionId}/guardians/{guardianProfileId}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<AdminResponseDto.StudentResponseDto>> getStudentsByGuardian(
            @PathVariable Long institutionId,
            @PathVariable Long guardianProfileId,
            Authentication authentication) {
        log.info("GET /api/institutions/{}/guardians/{}/students - Obteniendo estudiantes del guardián", 
                institutionId, guardianProfileId);
        
        validateInstitutionalAccess(institutionId, authentication, true); // Permitir TEACHER
        List<AdminResponseDto.StudentResponseDto> students = institutionService.getStudentsByGuardian(institutionId, guardianProfileId);
        return ResponseEntity.ok(students);
    }

    /**
     * ADMIN: Asignar guardián a estudiante
     * @param institutionId ID de la institución
     * @param request Datos de la asignación
     * @return Resultado de la operación
     */
    @PostMapping("/{institutionId}/assign-guardian")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponseDto> assignGuardianToStudent(
            @PathVariable Long institutionId,
            @Valid @RequestBody AdminRequestDto.AssignGuardianRequestDto request,
            Authentication authentication) {
        log.info("POST /api/institutions/{}/assign-guardian - Admin asignando guardián", institutionId);
        
        try {
            validateAdminBelongsToInstitution(institutionId, authentication);
            CommonResponseDto response = institutionService.assignGuardianToStudent(institutionId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al asignar guardián en institución {}: {}", institutionId, e.getMessage());
            
            CommonResponseDto response = CommonResponseDto.builder()
                    .success(false)
                    .message("Error al asignar guardián: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * ADMIN: Reasignar guardián (con auditoría)
     * @param institutionId ID de la institución
     * @param request Datos de la reasignación
     * @return Resultado de la operación
     */
    @PutMapping("/{institutionId}/reassign-guardian")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponseDto> reassignGuardian(
            @PathVariable Long institutionId,
            @Valid @RequestBody AdminRequestDto.ReassignGuardianRequestDto request,
            Authentication authentication) {
        log.info("PUT /api/institutions/{}/reassign-guardian - Admin reasignando guardián", institutionId);
        
        try {
            validateAdminBelongsToInstitution(institutionId, authentication);
            CommonResponseDto response = institutionService.reassignGuardian(institutionId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al reasignar guardián en institución {}: {}", institutionId, e.getMessage());
            
            CommonResponseDto response = CommonResponseDto.builder()
                    .success(false)
                    .message("Error al reasignar guardián: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * ADMIN: Obtener estadísticas institucionales
     * @param institutionId ID de la institución
     * @return Estadísticas de la institución
     */
    @GetMapping("/{institutionId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminResponseDto.InstitutionStatisticsDto> getInstitutionStatistics(
            @PathVariable Long institutionId,
            Authentication authentication) {
        log.info("GET /api/institutions/{}/statistics - Admin obteniendo estadísticas", institutionId);
        
        validateAdminBelongsToInstitution(institutionId, authentication);
        AdminResponseDto.InstitutionStatisticsDto statistics = institutionService.getStatistics(institutionId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Valida que el administrador pertenece a la institución
     * @param institutionId ID de la institución
     * @param authentication Datos de autenticación del usuario
     * @throws IllegalArgumentException si el usuario no pertenece a la institución
     */
    private void validateAdminBelongsToInstitution(Long institutionId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        
        String userEmail = authentication.getName();
        
        // Buscar el usuario por email y verificar que pertenece a la institución
        try {
            boolean belongsToInstitution = institutionService.validateUserBelongsToInstitution(userEmail, institutionId);
            if (!belongsToInstitution) {
                throw new IllegalArgumentException("El administrador no pertenece a esta institución");
            }
        } catch (Exception e) {
            log.warn("Error validando pertenencia institucional para usuario {}: {}", userEmail, e.getMessage());
            throw new IllegalArgumentException("Error validando permisos de acceso");
        }
    }

    /**
     * Valida permisos y pertenencia institucional para operaciones de consulta
     * @param institutionId ID de la institución
     * @param authentication Datos de autenticación del usuario
     * @param allowTeacher Si se permite acceso a profesores (además de admins)
     */
    private void validateInstitutionalAccess(Long institutionId, Authentication authentication, boolean allowTeacher) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        
        String userEmail = authentication.getName();
        
        try {
            // Obtener el rol del usuario
            String userRole = institutionService.getUserRole(userEmail);
            
            // Verificar si el usuario tiene permiso para esta operación
            boolean hasPermission = "ADMIN".equals(userRole) || (allowTeacher && "TEACHER".equals(userRole));
            
            if (!hasPermission) {
                throw new IllegalArgumentException("Permisos insuficientes para esta operación");
            }
            
            // Verificar que pertenece a la institución
            boolean belongsToInstitution = institutionService.validateUserBelongsToInstitution(userEmail, institutionId);
            if (!belongsToInstitution) {
                throw new IllegalArgumentException("El usuario no pertenece a esta institución");
            }
        } catch (Exception e) {
            log.warn("Error validando acceso institucional para usuario {}: {}", userEmail, e.getMessage());
            throw new IllegalArgumentException("Error validando permisos de acceso");
        }
    }
} 