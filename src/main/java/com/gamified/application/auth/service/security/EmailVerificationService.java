package com.gamified.application.auth.service.security;

import com.gamified.application.auth.entity.security.EmailVerification;
import com.gamified.application.shared.repository.Result;

import java.util.Optional;

/**
 * Servicio para la verificación de email
 */
public interface EmailVerificationService {
    
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
     * @return Verificación si existe
     */
    Optional<EmailVerification> findByToken(String token);
    
    /**
     * Verifica si un token es válido
     * @param token Token de verificación
     * @return true si el token es válido
     */
    boolean isTokenValid(String token);
    
    /**
     * Reenvía un email de verificación
     * @param userId ID del usuario
     * @param email Email a verificar
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @return Resultado con el nuevo token creado
     */
    Result<EmailVerification> resendVerificationEmail(Long userId, String email, String ipAddress, String userAgent);
    
    /**
     * Genera un token aleatorio para verificación
     * @return Token generado
     */
    String generateRandomToken();
    
    /**
     * Calcula la fecha de expiración de un token
     * @param hoursValid Horas de validez
     * @return Fecha de expiración
     */
    java.time.LocalDateTime calculateExpiryDate(int hoursValid);
} 