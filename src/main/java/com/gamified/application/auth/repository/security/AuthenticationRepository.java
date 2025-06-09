package com.gamified.application.auth.repository.security;

import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.repository.interfaces.Result;

import java.util.Optional;

/**
 * Repositorio para operaciones específicas de autenticación
 */
public interface AuthenticationRepository {
    /**
     * Autentica un usuario usando SP de base de datos
     * @param email Email del usuario
     * @param passwordHash Hash del password proporcionado
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @return Usuario autenticado o empty si falló
     */
    Optional<User> authenticateUser(String email, String passwordHash, String ipAddress, String userAgent);
    
    /**
     * Registra un intento de login
     * @param userId ID del usuario
     * @param successful Si el login fue exitoso
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @param failureReason Razón del fallo (si aplica)
     * @return Resultado de la operación
     */
    Result<Boolean> recordLoginAttempt(Long userId, boolean successful, String ipAddress, String userAgent, String failureReason);
    
    /**
     * Verifica si un patrón de login es sospechoso
     * @param userId ID del usuario
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @return true si el patrón es sospechoso
     */
    boolean detectSuspiciousLoginPattern(Long userId, String ipAddress, String userAgent);
    
    /**
     * Bloquea temporalmente una cuenta
     * @param userId ID del usuario
     * @param minutesToLock Minutos que estará bloqueada
     * @param reason Razón del bloqueo
     * @return Resultado de la operación
     */
    Result<Boolean> lockUserAccount(Long userId, int minutesToLock, String reason);
    
    /**
     * Desbloquea una cuenta
     * @param userId ID del usuario
     * @return Resultado de la operación
     */
    Result<Boolean> unlockUserAccount(Long userId);
    
    /**
     * Actualiza la fecha de último login
     * @param userId ID del usuario
     * @param ipAddress Dirección IP del cliente
     * @return Resultado de la operación
     */
    Result<Boolean> updateLastLogin(Long userId, String ipAddress);
    
    /**
     * Resetea el contador de intentos fallidos
     * @param userId ID del usuario
     * @return Resultado de la operación
     */
    Result<Boolean> resetFailedAttempts(Long userId);
} 