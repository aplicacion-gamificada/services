package com.gamified.application.institution.service;

import com.gamified.application.institution.model.dto.request.InstitutionRequestDto;
import com.gamified.application.institution.model.dto.request.AdminRequestDto;
import com.gamified.application.institution.model.dto.response.InstitutionResponseDto;
import com.gamified.application.institution.model.dto.response.AdminResponseDto;
import com.gamified.application.institution.model.entity.Institution;
import com.gamified.application.shared.exception.ResourceNotFoundException;
import com.gamified.application.shared.exception.ResourceConflictException;
import com.gamified.application.institution.repository.InstitutionRepository;
import com.gamified.application.shared.repository.Result;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import com.gamified.application.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de instituciones
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final UserProfileService userProfileService;

    @Override
    public List<InstitutionResponseDto.InstitutionSummaryDto> getAllActiveInstitutions() {
        log.info("Obteniendo todas las instituciones activas");
        List<Institution> institutions = institutionRepository.findAllActive();
        
        // Mapear a DTO de respuesta
        return institutions.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public InstitutionResponseDto.InstitutionDetailDto getInstitutionById(Long institutionId) {
        log.info("Buscando institución con ID: {}", institutionId);
        
        return institutionRepository.findById(institutionId)
                .map(this::mapToDetailDto)
                .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
    }

    @Override
    public List<InstitutionResponseDto.InstitutionSummaryDto> searchInstitutions(String query, int limit) {
        log.info("Buscando instituciones con query: '{}', limit: {}", query, limit);
        
        List<Institution> institutions = institutionRepository.searchByName(query, limit);
        
        // Mapear a DTO de respuesta
        return institutions.stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public InstitutionResponseDto.InstitutionDetailDto registerInstitution(
            InstitutionRequestDto.InstitutionRegistrationRequestDto requestDto) {
        log.info("Registrando nueva institución con nombre: {}", requestDto.getName());
        
        // Verificar si ya existe una institución con el mismo nombre
        institutionRepository.findByName(requestDto.getName()).ifPresent(institution -> {
            throw new IllegalArgumentException("Ya existe una institución con el nombre: " + requestDto.getName());
        });
        
        // Crear entidad Institution a partir del DTO
        Institution institution = Institution.builder()
                .name(requestDto.getName())
                .address(requestDto.getAddress())
                .city(requestDto.getCity())
                .state(requestDto.getState())
                .country(requestDto.getCountry())
                .postalCode(requestDto.getPostalCode())
                .phone(requestDto.getPhone())
                .email(requestDto.getEmail())
                .website(requestDto.getWebsite())
                .logoUrl(requestDto.getLogoUrl())
                .status(true) // Por defecto activa
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Guardar la institución
        Result<Institution> result = institutionRepository.save(institution);
        
        if (!result.isSuccess()) {
            log.error("Error al registrar institución: {}", result.getErrorMessage());
            throw new RuntimeException("Error al registrar la institución: " + result.getErrorMessage());
        }
        
        Institution savedInstitution = result.getData();
        log.info("Institución registrada exitosamente con ID: {}", savedInstitution.getId());
        
        // Mapear a DTO de respuesta
        return mapToDetailDto(savedInstitution);
    }
    
    // Métodos privados para mapeo de entidad a DTO
    
    private InstitutionResponseDto.InstitutionSummaryDto mapToSummaryDto(Institution institution) {
        return InstitutionResponseDto.InstitutionSummaryDto.builder()
                .id(institution.getId())
                .name(institution.getName())
                .city(institution.getCity())
                .province(institution.getState())
                .build();
    }
    
    private InstitutionResponseDto.InstitutionDetailDto mapToDetailDto(Institution institution) {
        return InstitutionResponseDto.InstitutionDetailDto.builder()
                .id(institution.getId())
                .name(institution.getName())
                .address(institution.getAddress())
                .city(institution.getCity())
                .province(institution.getState())
                .country(institution.getCountry())
                .postalCode(institution.getPostalCode())
                .phone(institution.getPhone())
                .email(institution.getEmail())
                .website(institution.getWebsite())
                .createdAt(institution.getCreatedAt())
                .updatedAt(institution.getUpdatedAt())
                .active(institution.isActive())
                .build();
    }

    // ==================== IMPLEMENTACIÓN MÉTODOS ADMINISTRATIVOS ====================

    @Override
    public AdminResponseDto.InstitutionUsersSummaryDto getUsersSummary(Long institutionId) {
        log.info("Obteniendo resumen de usuarios para institución: {}", institutionId);
        
        // Validar que la institución existe
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
        
        // Obtener conteos por rol usando UserProfileService
        java.util.Map<String, Integer> countsByRole = userProfileService.getUserCountsByRoleForInstitution(institutionId);
        
        // Obtener estudiantes sin guardián
        List<com.gamified.application.user.model.dto.response.UserResponseDto.StudentResponseDto> unassignedStudents = 
                userProfileService.getUnassignedStudentsByInstitution(institutionId);
        
        int totalStudents = countsByRole.getOrDefault("STUDENT", 0);
        int studentsWithoutGuardian = unassignedStudents.size();
        int studentsWithGuardian = totalStudents - studentsWithoutGuardian;
        
        return AdminResponseDto.InstitutionUsersSummaryDto.builder()
                .institutionId(institutionId)
                .institutionName(institution.getName())
                .totalStudents(totalStudents)
                .studentsWithGuardian(studentsWithGuardian)
                .studentsWithoutGuardian(studentsWithoutGuardian)
                .totalGuardians(countsByRole.getOrDefault("GUARDIAN", 0))
                .totalTeachers(countsByRole.getOrDefault("TEACHER", 0))
                .totalAdmins(countsByRole.getOrDefault("ADMIN", 0))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Override
    public List<AdminResponseDto.UserSummaryDto> getUsers(Long institutionId, String role, Boolean unassigned) {
        log.info("Obteniendo usuarios para institución: {}, role: {}, unassigned: {}", institutionId, role, unassigned);
        
        // Validar que la institución existe
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
        
        // Obtener usuarios usando UserProfileService
        List<com.gamified.application.user.model.dto.response.UserResponseDto.BasicUserResponseDto> basicUsers = 
                userProfileService.getUsersByInstitution(institutionId, role, unassigned);
        
        // Mapear a AdminResponseDto.UserSummaryDto
        return basicUsers.stream()
                .map(user -> AdminResponseDto.UserSummaryDto.builder()
                        .userId(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .roleName(user.getRoleName())
                        .status(user.getStatus())
                        .emailVerified(true) // Temporalmente true, podríamos ajustar esto más tarde
                        .createdAt(user.getCreatedAt())
                        .lastLoginAt(user.getLastLoginAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminResponseDto.StudentForAssignmentDto> getUnassignedStudents(Long institutionId) {
        log.info("Obteniendo estudiantes sin guardián para institución: {}", institutionId);
        
        // Validar que la institución existe
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
        
        // Obtener estudiantes sin guardián usando UserProfileService
        List<com.gamified.application.user.model.dto.response.UserResponseDto.StudentResponseDto> unassignedStudents = 
                userProfileService.getUnassignedStudentsByInstitution(institutionId);
        
        // Mapear a AdminResponseDto.StudentForAssignmentDto
        return unassignedStudents.stream()
                .map(student -> AdminResponseDto.StudentForAssignmentDto.builder()
                        .userId(student.getId())
                        .studentProfileId(student.getStudentProfileId())
                        .firstName(student.getFirstName())
                        .lastName(student.getLastName())
                        .fullName(student.getFullName())
                        .email(student.getEmail())
                        .username(student.getUsername())
                        .birthDate(student.getBirth_date() != null ? student.getBirth_date().toLocalDate() : null)
                        .pointsAmount(student.getPointsAmount())
                        .currentGuardian(null) // Sin guardián por definición
                        .enrollmentDate(student.getCreatedAt()) // Usamos createdAt como fecha de inscripción
                        .needsGuardian(true) // Por definición, necesita guardián
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminResponseDto.GuardianForAssignmentDto> getAvailableGuardians(Long institutionId) {
        log.info("Obteniendo guardianes disponibles para institución: {}", institutionId);
        
        // Validar que la institución existe
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
        
        // Obtener guardianes disponibles usando UserProfileService
        List<com.gamified.application.user.model.dto.response.UserResponseDto.GuardianResponseDto> availableGuardians = 
                userProfileService.getAvailableGuardiansByInstitution(institutionId);
        
        // Mapear a AdminResponseDto.GuardianForAssignmentDto
        return availableGuardians.stream()
                .map(guardian -> {
                    // Mapear estudiantes básicos
                    List<AdminResponseDto.StudentBasicInfoDto> currentStudents = 
                            guardian.getStudents().stream()
                                    .map(student -> AdminResponseDto.StudentBasicInfoDto.builder()
                                            .studentProfileId(student.getStudentProfileId())
                                            .fullName(student.getFullName())
                                            .username(student.getUsername())
                                            .pointsAmount(student.getPointsAmount())
                                            .build())
                                    .collect(Collectors.toList());
                    
                    return AdminResponseDto.GuardianForAssignmentDto.builder()
                            .userId(guardian.getId())
                            .guardianProfileId(guardian.getGuardianProfileId())
                            .firstName(guardian.getFirstName())
                            .lastName(guardian.getLastName())
                            .fullName(guardian.getFullName())
                            .email(guardian.getEmail())
                            .phone(guardian.getPhone())
                            .currentStudentsCount(guardian.getStudentsCount())
                            .maxStudentsRecommended(5) // Recomendación: máximo 5 estudiantes por guardián
                            .currentStudents(currentStudents)
                            .availableForNewAssignments(guardian.getStudentsCount() < 5) // Disponible si tiene menos de 5
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminResponseDto.StudentResponseDto> getStudentsByGuardian(Long institutionId, Long guardianProfileId) {
        log.info("Obteniendo estudiantes del guardián profile {} en institución: {}", guardianProfileId, institutionId);
        
        // Validar que la institución existe
        institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
        
        // Obtener estudiantes del guardián usando UserProfileService con guardianProfileId
        List<com.gamified.application.user.model.dto.response.UserResponseDto.StudentResponseDto> studentsByGuardian = 
                userProfileService.getStudentsByGuardianProfile(guardianProfileId);
        
        // Mapear a AdminResponseDto.StudentResponseDto
        return studentsByGuardian.stream()
                .map(student -> AdminResponseDto.StudentResponseDto.builder()
                        .userId(student.getId())
                        .studentProfileId(student.getStudentProfileId())
                        .firstName(student.getFirstName())
                        .lastName(student.getLastName())
                        .fullName(student.getFullName())
                        .email(student.getEmail())
                        .username(student.getUsername())
                        .birthDate(student.getBirth_date() != null ? student.getBirth_date().toLocalDate() : null)
                        .pointsAmount(student.getPointsAmount())
                        .roleName(student.getRoleName())
                        .status(student.getStatus())
                        .emailVerified(student.getEmailVerified())
                        .createdAt(student.getCreatedAt())
                        .guardian(AdminResponseDto.GuardianBasicInfoDto.builder()
                                .guardianProfileId(student.getGuardianProfileId())
                                .fullName(student.getGuardianName())
                                .email(student.getGuardianEmail())
                                .phone(null) // No tenemos el teléfono en este contexto
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommonResponseDto assignGuardianToStudent(Long institutionId, AdminRequestDto.AssignGuardianRequestDto request) {
        log.info("Asignando guardián {} a estudiante {} en institución: {}", 
                request.getGuardianProfileId(), request.getStudentProfileId(), institutionId);
        
        try {
            // Validar que la institución existe
            Institution institution = institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
            
            // Realizar la asignación usando UserProfileService
            boolean assignmentSuccessful = userProfileService.assignGuardianToStudent(
                    request.getStudentProfileId(), 
                    request.getGuardianProfileId()
            );
            
            if (assignmentSuccessful) {
                log.info("Guardián {} asignado exitosamente a estudiante {} en institución {}", 
                        request.getGuardianProfileId(), request.getStudentProfileId(), institutionId);
                
                return CommonResponseDto.builder()
                        .success(true)
                        .message("Guardián asignado exitosamente al estudiante")
                        .timestamp(LocalDateTime.now())
                        .build();
            } else {
                log.warn("No se pudo asignar guardián {} a estudiante {} - posiblemente ya tiene guardián asignado", 
                        request.getGuardianProfileId(), request.getStudentProfileId());
                
                return CommonResponseDto.builder()
                        .success(false)
                        .message("No se pudo asignar el guardián. El estudiante podría ya tener un guardián asignado.")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
                    
        } catch (Exception e) {
            log.error("Error al asignar guardián: {}", e.getMessage());
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Error al asignar guardián: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    @Transactional
    public CommonResponseDto reassignGuardian(Long institutionId, AdminRequestDto.ReassignGuardianRequestDto request) {
        log.info("Reasignando guardián de {} a {} para estudiante {} en institución: {}", 
                request.getPreviousGuardianProfileId(), request.getNewGuardianProfileId(), 
                request.getStudentProfileId(), institutionId);
        
        try {
            // Validar que la institución existe
            Institution institution = institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
            
            // Realizar la reasignación usando UserProfileService
            log.info("Ejecutando reasignación: studentProfileId={}, previousGuardianId={}, newGuardianId={}", 
                    request.getStudentProfileId(), request.getPreviousGuardianProfileId(), request.getNewGuardianProfileId());
            
            boolean reassignmentSuccessful = userProfileService.reassignGuardianToStudent(
                    request.getStudentProfileId(), 
                    request.getPreviousGuardianProfileId(),
                    request.getNewGuardianProfileId()
            );
            
            if (reassignmentSuccessful) {
                log.info("✅ Reasignación exitosa");
                return CommonResponseDto.builder()
                        .success(true)
                        .message("Guardián reasignado exitosamente. Motivo: " + request.getReason())
                        .timestamp(LocalDateTime.now())
                        .build();
            } else {
                log.warn("⚠️ Reasignación falló: posiblemente el estudiante no tenía el guardián anterior asignado");
                return CommonResponseDto.builder()
                        .success(false)
                        .message("No se pudo reasignar el guardián. Verifique que el estudiante tenga el guardián anterior asignado.")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
                    
        } catch (Exception e) {
            log.error("❌ Error al reasignar guardián: {}", e.getMessage());
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Error al reasignar guardián: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    @Override
    public AdminResponseDto.InstitutionStatisticsDto getStatistics(Long institutionId) {
        log.info("Obteniendo estadísticas para institución: {}", institutionId);
        
        // Validar que la institución existe
        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Institución no encontrada con ID: " + institutionId));
        
        // Reutilizar la lógica de getUsersSummary para obtener conteos básicos
        java.util.Map<String, Integer> countsByRole = userProfileService.getUserCountsByRoleForInstitution(institutionId);
        
        // Obtener estudiantes sin guardián
        List<com.gamified.application.user.model.dto.response.UserResponseDto.StudentResponseDto> unassignedStudents = 
                userProfileService.getUnassignedStudentsByInstitution(institutionId);
        
        int totalStudents = countsByRole.getOrDefault("STUDENT", 0);
        int totalTeachers = countsByRole.getOrDefault("TEACHER", 0);
        int totalGuardians = countsByRole.getOrDefault("GUARDIAN", 0);
        int totalAdmins = countsByRole.getOrDefault("ADMIN", 0);
        int studentsWithoutGuardian = unassignedStudents.size();
        int studentsWithGuardian = totalStudents - studentsWithoutGuardian;
        
        int totalUsers = totalStudents + totalTeachers + totalGuardians + totalAdmins;
        int activeUsers = totalUsers; // Asumimos que todos los usuarios contados están activos
        int inactiveUsers = 0; // Por ahora 0, podríamos agregar una consulta específica más tarde
        
        double guardianAssignmentPercentage = totalStudents > 0 ? 
                ((double) studentsWithGuardian / totalStudents) * 100.0 : 0.0;
        
        return AdminResponseDto.InstitutionStatisticsDto.builder()
                .institutionId(institutionId)
                .institutionName(institution.getName())
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .totalStudents(totalStudents)
                .totalTeachers(totalTeachers)
                .totalGuardians(totalGuardians)
                .totalAdmins(totalAdmins)
                .studentsWithGuardian(studentsWithGuardian)
                .studentsWithoutGuardian(studentsWithoutGuardian)
                .guardianAssignmentPercentage(guardianAssignmentPercentage)
                .usersLoggedInLast30Days(getUsersLoggedInLast30Days(institutionId))
                .newUsersThisMonth(getNewUsersThisMonth(institutionId))
                .lastActivity(getLastActivity(institutionId))
                .generatedAt(LocalDateTime.now())
                .generatedBy(getCurrentUser())
                .build();
    }

    /**
     * Obtiene el número de usuarios que han iniciado sesión en los últimos 30 días
     * @param institutionId ID de la institución
     * @return Número de usuarios activos en los últimos 30 días
     */
    private int getUsersLoggedInLast30Days(Long institutionId) {
        try {
            String sql = """
                SELECT COUNT(DISTINCT u.id)
                FROM [user] u
                WHERE u.institution_id = ? 
                AND u.status = 1
                AND u.last_login_at >= DATEADD(day, -30, GETDATE())
                """;
            
            Integer count = userProfileService.executeCountQuery(sql, institutionId);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("Error obteniendo usuarios activos últimos 30 días para institución {}: {}", institutionId, e.getMessage());
            return 0;
        }
    }

    /**
     * Obtiene el número de usuarios nuevos este mes
     * @param institutionId ID de la institución
     * @return Número de usuarios creados este mes
     */
    private int getNewUsersThisMonth(Long institutionId) {
        try {
            String sql = """
                SELECT COUNT(*)
                FROM [user] u
                WHERE u.institution_id = ? 
                AND u.status = 1
                AND YEAR(u.created_at) = YEAR(GETDATE())
                AND MONTH(u.created_at) = MONTH(GETDATE())
                """;
            
            Integer count = userProfileService.executeCountQuery(sql, institutionId);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.warn("Error obteniendo usuarios nuevos este mes para institución {}: {}", institutionId, e.getMessage());
            return 0;
        }
    }

    /**
     * Obtiene la fecha de última actividad en la institución
     * @param institutionId ID de la institución
     * @return Fecha de última actividad
     */
    private LocalDateTime getLastActivity(Long institutionId) {
        try {
            String sql = """
                SELECT TOP 1 u.last_login_at
                FROM [user] u
                WHERE u.institution_id = ? 
                AND u.status = 1
                AND u.last_login_at IS NOT NULL
                ORDER BY u.last_login_at DESC
                """;
            
            java.sql.Timestamp lastLogin = userProfileService.executeTimestampQuery(sql, institutionId);
            return lastLogin != null ? lastLogin.toLocalDateTime() : LocalDateTime.now().minusDays(30);
        } catch (Exception e) {
            log.warn("Error obteniendo última actividad para institución {}: {}", institutionId, e.getMessage());
            return LocalDateTime.now().minusDays(30);
        }
    }

    /**
     * Obtiene el usuario actual del contexto de seguridad
     * @return Identificador del usuario actual o "SYSTEM" si no hay contexto de seguridad
     */
    private String getCurrentUser() {
        try {
            // Obtener el usuario actual del contexto de seguridad
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
            
            return "SYSTEM";
        } catch (Exception e) {
            log.warn("Error obteniendo usuario actual: {}", e.getMessage());
            return "SYSTEM";
        }
    }

    @Override
    public boolean validateUserBelongsToInstitution(String userEmail, Long institutionId) {
        try {
            String sql = """
                SELECT COUNT(*)
                FROM [user] u
                WHERE u.email = ? 
                AND u.institution_id = ? 
                AND u.status = 1
                """;
            
            Integer count = userProfileService.executeCountQuery(sql, userEmail, institutionId);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("Error validando pertenencia institucional para usuario {}: {}", userEmail, e.getMessage());
            return false;
        }
    }

    @Override
    public String getUserRole(String userEmail) {
        try {
            String sql = """
                SELECT r.name
                FROM [user] u
                JOIN role r ON u.role_id = r.id
                WHERE u.email = ? 
                AND u.status = 1
                """;
            
            return userProfileService.executeStringQuery(sql, userEmail);
        } catch (Exception e) {
            log.warn("Error obteniendo rol del usuario {}: {}", userEmail, e.getMessage());
            return null;
        }
    }
} 