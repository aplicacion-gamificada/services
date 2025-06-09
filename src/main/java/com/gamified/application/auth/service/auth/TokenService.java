package com.gamified.application.auth.service.auth;

import com.gamified.application.auth.dto.request.SessionRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.SessionResponseDto;
import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.security.RefreshToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la generación y validación de tokens
 */
public interface TokenService {
    
    /**
     * Genera un token JWT de acceso
     * @param user Usuario para el que se genera el token
     * @param additionalClaims Claims adicionales para el token
     * @return Token JWT generado
     */
    String generateAccessToken(User user, Map<String, Object> additionalClaims);
    
    /**
     * Genera un token de refresco
     * @param userId ID del usuario
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @param deviceInfo Información del dispositivo
     * @param sessionName Nombre de la sesión
     * @return Token de refresco generado
     */
    RefreshToken generateRefreshToken(Long userId, String ipAddress, String userAgent, String deviceInfo, String sessionName);
    
    /**
     * Valida un token JWT
     * @param token Token a validar
     * @return true si el token es válido
     */
    boolean validateToken(String token);
    
    /**
     * Extrae el ID de usuario de un token JWT
     * @param token Token JWT
     * @return ID del usuario
     */
    Long extractUserId(String token);
    
    /**
     * Extrae el nombre de usuario de un token JWT
     * @param token Token JWT
     * @return Nombre de usuario
     */
    String extractUsername(String token);
    
    /**
     * Extrae los claims de un token JWT
     * @param token Token JWT
     * @return Claims del token
     */
    Map<String, Object> extractClaims(String token);
    
    /**
     * Obtiene las sesiones activas de un usuario
     * @param userId ID del usuario
     * @return Lista de sesiones activas
     */
    List<SessionResponseDto.SessionInfoResponseDto> getActiveSessions(Long userId);

    /**
     * Renombra una sesión
     * @param renameRequest Datos para renombrar la sesión
     * @return Resultado de la operación
     */
    CommonResponseDto renameSession(SessionRequestDto.RenameSessionRequestDto renameRequest);

    /**
     * Revoca un token de refresco
     * @param token Token de refresco
     * @param reason Razón de la revocación
     * @return true si se revocó correctamente
     */
    boolean revokeRefreshToken(String token, String reason);

    /**
     * Revoca una sesión específica
     * @param sessionId ID de la sesión
     * @param userId ID del usuario propietario de la sesión
     * @return Resultado de la operación
     */
    CommonResponseDto revokeSession(Long sessionId, Long userId);

    /**
     * Revoca todos los tokens de un usuario
     * @param userId ID del usuario
     * @return Número de tokens revocados
     */
    CommonResponseDto revokeAllSessions(Long userId);

    /**
     * Revoca todas las sesiones de un usuario excepto la actual
     * @param userId ID del usuario
     * @param currentToken Token actual (para no revocar esta sesión)
     * @return Resultado de la operación
     */
    CommonResponseDto revokeAllSessionsExceptCurrent(Long userId, String currentToken);
    
    /**
     * Busca un token de refresco por su valor
     * @param token Valor del token
     * @return Token si existe
     */
    Optional<RefreshToken> findRefreshToken(String token);
}