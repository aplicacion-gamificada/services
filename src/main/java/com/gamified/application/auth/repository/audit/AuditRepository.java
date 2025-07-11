package com.gamified.application.auth.repository.audit;

import com.gamified.application.auth.entity.audit.AuditLog;
import com.gamified.application.auth.entity.audit.LoginHistory;
import com.gamified.application.shared.repository.Result;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de auditoría
 */
public interface AuditRepository {
    /**
     * Registra una acción en el log de auditoría
     * @param auditLog Datos de la acción
     * @return Resultado con el log creado
     */
    Result<AuditLog> logAction(AuditLog auditLog);
    
    /**
     * Registra un intento de login
     * @param loginHistory Datos del intento
     * @return Resultado con el registro creado
     */
    Result<LoginHistory> logLoginAttempt(LoginHistory loginHistory);
    
    /**
     * Busca un log de auditoría por ID
     * @param id ID del log
     * @return Log si existe, empty si no
     */
    Optional<AuditLog> findAuditLogById(Long id);
    
    /**
     * Busca un historial de login por ID
     * @param id ID del historial
     * @return Historial si existe, empty si no
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
     * Busca intentos de login fallidos recientes por usuario
     * @param userId ID del usuario
     * @param hours Horas recientes a considerar
     * @return Lista de intentos fallidos
     */
    List<LoginHistory> findRecentFailedLoginAttempts(Long userId, int hours);
    
    /**
     * Cuenta intentos de login fallidos recientes por usuario
     * @param userId ID del usuario
     * @param hours Horas recientes a considerar
     * @return Número de intentos fallidos
     */
    int countRecentFailedLoginAttempts(Long userId, int hours);
} 