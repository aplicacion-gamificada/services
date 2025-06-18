package com.gamified.application.auth.service.audit;

import com.gamified.application.auth.dto.request.AuditRequestDto;
import com.gamified.application.auth.dto.response.AuditResponseDto;
import com.gamified.application.auth.entity.audit.AuditLog;
import com.gamified.application.auth.entity.audit.LoginHistory;
import com.gamified.application.auth.repository.interfaces.Result;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la auditoría de seguridad
 */
public interface SecurityAuditService {
    
    /**
     * Registra una acción en el log de auditoría
     * @param userId ID del usuario
     * @param actionId ID de la acción
     * @param entityType Tipo de entidad
     * @param entityId ID de la entidad (opcional)
     * @param actionDetails Detalles de la acción
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @param isSensitive Si la acción es sensible
     * @return Resultado con el log creado
     */
    Result<AuditLog> logAction(Long userId, Byte actionId, String entityType, 
                              Long entityId, String actionDetails, String ipAddress, 
                              String userAgent, boolean isSensitive);
    
    /**
     * Registra un intento de login
     * @param userId ID del usuario
     * @param success Si el login fue exitoso
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @param failureReason Razón del fallo (si aplica)
     * @return Resultado con el registro creado
     */
    Result<LoginHistory> logLoginAttempt(Long userId, boolean success, String ipAddress, 
                                        String userAgent, String failureReason);
    
    /**
     * Busca un log de auditoría por ID
     * @param id ID del log
     * @return Log si existe
     */
    Optional<AuditLog> findAuditLogById(Long id);
    
    /**
     * Busca un historial de login por ID
     * @param id ID del historial
     * @return Historial si existe
     */
    Optional<LoginHistory> findLoginHistoryById(Long id);
    
    /**
     * Busca logs de auditoría por usuario
     * @param userId ID del usuario
     * @param limit Límite de registros
     * @return Lista de logs
     */
    List<AuditLog> findAuditLogsByUser(Long userId, int limit);
    
    /**
     * Busca historial de login por usuario
     * @param userId ID del usuario
     * @param limit Límite de registros
     * @return Lista de historiales
     */
    List<LoginHistory> findLoginHistoryByUser(Long userId, int limit);
    
    /**
     * Busca logs de auditoría por tipo de acción
     * @param actionId ID de la acción
     * @param limit Límite de registros
     * @return Lista de logs
     */
    List<AuditLog> findAuditLogsByAction(Byte actionId, int limit);
    
    /**
     * Busca logs de auditoría por rango de fecha
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @param limit Límite de registros
     * @return Lista de logs
     */
    List<AuditLog> findAuditLogsByDateRange(Timestamp startDate, Timestamp endDate, int limit);
    
    /**
     * Busca historial de login por rango de fecha
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @param limit Límite de registros
     * @return Lista de historiales
     */
    List<LoginHistory> findLoginHistoryByDateRange(Timestamp startDate, Timestamp endDate, int limit);
    
    /**
     * Detecta patrones de login sospechosos
     * @param userId ID del usuario
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @return true si el patrón es sospechoso
     */
    boolean detectSuspiciousLoginPattern(Long userId, String ipAddress, String userAgent);
    
    /**
     * Cuenta intentos de login fallidos recientes
     * @param userId ID del usuario
     * @param hours Horas recientes a considerar
     * @return Número de intentos fallidos
     */
    int countRecentFailedLoginAttempts(Long userId, int hours);
    
    /**
     * Genera un reporte de seguridad
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Reporte de seguridad (formato depende de la implementación)
     */
    Object generateSecurityReport(Timestamp startDate, Timestamp endDate);
    
    /**
     * Obtiene el historial de inicio de sesión de un usuario
     * @param userId ID del usuario
     * @param limit Límite de resultados
     * @param offset Offset de paginación
     * @return Lista de registros de inicio de sesión
     */
    List<AuditResponseDto.LoginHistoryResponseDto> getUserLoginHistory(Long userId, int limit, int offset);
    
    /**
     * Obtiene el historial de actividad de un usuario
     * @param userId ID del usuario
     * @param limit Límite de resultados
     * @param offset Offset de paginación
     * @return Lista de registros de actividad
     */
    List<AuditResponseDto.UserActivityResponseDto> getUserActivity(Long userId, int limit, int offset);
    
    /**
     * Busca registros de auditoría según criterios
     * @param searchRequest Criterios de búsqueda
     * @return Resultados de la búsqueda
     */
    AuditResponseDto.AuditLogSearchResponseDto searchAuditLogs(AuditRequestDto.AuditLogSearchRequestDto searchRequest);
    
    /**
     * Obtiene resumen de actividad sospechosa
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Resumen de actividad sospechosa
     */
    AuditResponseDto.SuspiciousActivitySummaryDto getSuspiciousActivity(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Registra un inicio de sesión exitoso
     * @param userId ID del usuario
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @return Resultado con el registro creado
     */
    Result<AuditLog> recordSuccessfulLogin(Long userId, String ipAddress, String userAgent);

    /**
     * Registra un intento de login fallido
     * @param userId ID del usuario
     * @param ipAddress Dirección IP del cliente
     * @param failureReason Razón del fallo
     * @return Resultado con el registro creado
     */
    Result<AuditLog> recordFailedLogin(Long userId, String ipAddress, String failureReason);
} 