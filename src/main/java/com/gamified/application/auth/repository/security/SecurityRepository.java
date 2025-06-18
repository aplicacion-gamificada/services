package com.gamified.application.auth.repository.security;

import com.gamified.application.auth.entity.security.EmailVerification;
import com.gamified.application.auth.entity.security.PasswordHistory;
import com.gamified.application.auth.entity.security.RefreshToken;
import com.gamified.application.auth.repository.interfaces.Result;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones de seguridad
 */
public interface SecurityRepository {
    /**
     * Crea un token de verificación de email
     * @param userId ID del usuario
     * @param email Email a verificar
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @return Resultado con el token creado
     */
    Result<EmailVerification> createEmailVerification(Long userId, String email, String ipAddress, String userAgent);
    
    /**
     * Verifica un email usando el token
     * @param token Token de verificación
     * @return Resultado con el email verificado
     */
    Result<EmailVerification> verifyEmail(String token);
    
    /**
     * Busca una verificación de email por token
     * @param token Token de verificación
     * @return Verificación si existe, empty si no
     */
    Optional<EmailVerification> findEmailVerificationByToken(String token);
    
    /**
     * Crea un refresh token
     * @param userId ID del usuario
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @param deviceInfo Información del dispositivo
     * @param sessionName Nombre de la sesión
     * @return Resultado con el token creado
     */
    Result<RefreshToken> createRefreshToken(Long userId, String ipAddress, String userAgent, String deviceInfo, String sessionName);
    
    /**
     * Busca un refresh token por su valor
     * @param tokenValue Valor del token
     * @return Token si existe, empty si no
     */
    Optional<RefreshToken> findRefreshTokenByValue(String tokenValue);
    
    /**
     * Revoca un token específico
     * @param tokenValue Valor del token
     * @param reason Razón de la revocación
     * @return Resultado con el token revocado
     */
    Result<RefreshToken> revokeToken(String tokenValue, String reason);
    
    /**
     * Revoca todos los tokens de un usuario
     * @param userId ID del usuario
     * @param reason Razón de la revocación
     * @return Número de tokens revocados
     */
    int revokeAllUserTokens(Long userId, String reason);
    
    /**
     * Registra un cambio de password
     * @param userId ID del usuario
     * @param passwordHash Hash del nuevo password
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @param changedByAdmin Si fue cambiado por un administrador
     * @return Resultado de la operación
     */
    Result<PasswordHistory> recordPasswordChange(Long userId, String passwordHash, String ipAddress, String userAgent, boolean changedByAdmin);
    
    /**
     * Obtiene el historial de passwords de un usuario
     * @param userId ID del usuario
     * @param limit Límite de registros
     * @return Lista de cambios de password
     */
    List<PasswordHistory> getPasswordHistory(Long userId, int limit);
    
    /**
     * Verifica si un password ya fue usado
     * @param userId ID del usuario
     * @param passwordHash Hash del password a verificar
     * @return true si el password ya fue usado
     */
    boolean isPasswordPreviouslyUsed(Long userId, String passwordHash);
    
    /**
     * Actualiza el último uso de un refresh token
     * @param tokenId ID del token
     */
    void updateRefreshTokenLastUsed(Long tokenId);
} 