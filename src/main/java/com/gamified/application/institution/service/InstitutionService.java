package com.gamified.application.institution.service;

import com.gamified.application.institution.model.dto.request.InstitutionRequestDto;
import com.gamified.application.institution.model.dto.request.AdminRequestDto;
import com.gamified.application.institution.model.dto.response.InstitutionResponseDto;
import com.gamified.application.institution.model.dto.response.AdminResponseDto;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;

import java.util.List;

/**
 * Servicio para operaciones relacionadas con instituciones
 */
public interface InstitutionService {

    /**
     * Obtiene todas las instituciones activas
     * @return Lista de instituciones activas
     */
    List<InstitutionResponseDto.InstitutionSummaryDto> getAllActiveInstitutions();

    /**
     * Obtiene una institución por su ID
     * @param institutionId ID de la institución
     * @return DTO con detalles de la institución
     */
    InstitutionResponseDto.InstitutionDetailDto getInstitutionById(Long institutionId);

    /**
     * Busca instituciones por nombre
     * @param query Texto a buscar en el nombre
     * @param limit Límite de resultados
     * @return Lista de instituciones que coinciden con la búsqueda
     */
    List<InstitutionResponseDto.InstitutionSummaryDto> searchInstitutions(String query, int limit);
    
    /**
     * Registra una nueva institución
     * @param requestDto Datos de la institución a registrar
     * @return La institución registrada
     */
    InstitutionResponseDto.InstitutionDetailDto registerInstitution(
            InstitutionRequestDto.InstitutionRegistrationRequestDto requestDto);

    // ==================== MÉTODOS ADMINISTRATIVOS ====================

    /**
     * Obtiene resumen de usuarios de una institución
     * @param institutionId ID de la institución
     * @return Resumen de usuarios por rol
     */
    AdminResponseDto.InstitutionUsersSummaryDto getUsersSummary(Long institutionId);

    /**
     * Obtiene lista de usuarios de una institución con filtros
     * @param institutionId ID de la institución
     * @param role Filtro por rol (opcional)
     * @param unassigned Si mostrar solo estudiantes sin guardián (opcional)
     * @return Lista de usuarios filtrada
     */
    List<AdminResponseDto.UserSummaryDto> getUsers(Long institutionId, String role, Boolean unassigned);

    /**
     * Obtiene estudiantes sin guardián asignado
     * @param institutionId ID de la institución
     * @return Lista de estudiantes sin guardián
     */
    List<AdminResponseDto.StudentForAssignmentDto> getUnassignedStudents(Long institutionId);

    /**
     * Obtiene guardianes disponibles para asignación
     * @param institutionId ID de la institución
     * @return Lista de guardianes disponibles
     */
    List<AdminResponseDto.GuardianForAssignmentDto> getAvailableGuardians(Long institutionId);

    /**
     * Obtiene estudiantes de un guardián específico
     * @param institutionId ID de la institución
     * @param guardianProfileId ID del perfil del guardián
     * @return Lista de estudiantes del guardián
     */
    List<AdminResponseDto.StudentResponseDto> getStudentsByGuardian(Long institutionId, Long guardianProfileId);

    /**
     * Asigna un guardián a un estudiante
     * @param institutionId ID de la institución
     * @param request Datos de la asignación
     * @return Resultado de la operación
     */
    CommonResponseDto assignGuardianToStudent(Long institutionId, AdminRequestDto.AssignGuardianRequestDto request);

    /**
     * Reasigna un guardián (con auditoría)
     * @param institutionId ID de la institución
     * @param request Datos de la reasignación
     * @return Resultado de la operación
     */
    CommonResponseDto reassignGuardian(Long institutionId, AdminRequestDto.ReassignGuardianRequestDto request);

    /**
     * Obtiene estadísticas institucionales
     * @param institutionId ID de la institución
     * @return Estadísticas de la institución
     */
    AdminResponseDto.InstitutionStatisticsDto getStatistics(Long institutionId);
    
    /**
     * Valida que un usuario pertenece a una institución
     * @param userEmail Email del usuario
     * @param institutionId ID de la institución
     * @return true si el usuario pertenece a la institución
     */
    boolean validateUserBelongsToInstitution(String userEmail, Long institutionId);
    
    /**
     * Obtiene el rol de un usuario
     * @param userEmail Email del usuario
     * @return Nombre del rol del usuario
     */
    String getUserRole(String userEmail);
} 