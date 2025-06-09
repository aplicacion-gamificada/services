package com.gamified.application.auth.service.security;

import com.gamified.application.auth.entity.security.PasswordHistory;
import com.gamified.application.auth.repository.interfaces.Result;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de contraseñas
 */
public interface PasswordService {
    
    /**
     * Codifica una contraseña en texto plano
     * @param rawPassword Contraseña en texto plano
     * @return Hash de la contraseña
     */
    String encodePassword(String rawPassword);
    
    /**
     * Verifica si una contraseña coincide con un hash
     * @param rawPassword Contraseña en texto plano
     * @param encodedPassword Hash de la contraseña
     * @return true si coinciden
     */
    boolean matches(String rawPassword, String encodedPassword);
    
    /**
     * Genera un token para reseteo de contraseña
     * @param userId ID del usuario
     * @param expirationHours Horas de validez
     * @return Token generado o empty si falló
     */
    Optional<String> generateResetToken(Long userId, int expirationHours);
    
    /**
     * Resetea una contraseña usando un token
     * @param userId ID del usuario
     * @param resetToken Token de reseteo
     * @param newPassword Nueva contraseña en texto plano
     * @return Resultado de la operación
     */
    Result<Boolean> resetPassword(Long userId, String resetToken, String newPassword);
    
    /**
     * Cambia la contraseña de un usuario
     * @param userId ID del usuario
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @return Resultado de la operación
     */
    Result<Boolean> changePassword(Long userId, String currentPassword, String newPassword, String ipAddress, String userAgent);
    
    /**
     * Registra un cambio de contraseña en el historial
     * @param userId ID del usuario
     * @param passwordHash Hash de la nueva contraseña
     * @param ipAddress Dirección IP del cliente
     * @param userAgent User-Agent del cliente
     * @param changedByAdmin Si fue cambiado por un administrador
     * @return Resultado de la operación
     */
    Result<PasswordHistory> recordPasswordChange(Long userId, String passwordHash, String ipAddress, String userAgent, boolean changedByAdmin);
    
    /**
     * Obtiene el historial de contraseñas de un usuario
     * @param userId ID del usuario
     * @param limit Límite de registros
     * @return Lista de cambios de contraseña
     */
    List<PasswordHistory> getPasswordHistory(Long userId, int limit);
    
    /**
     * Verifica si una contraseña ya fue usada por el usuario
     * @param userId ID del usuario
     * @param rawPassword Contraseña en texto plano
     * @return true si la contraseña ya fue usada
     */
    boolean isPasswordPreviouslyUsed(Long userId, String rawPassword);
    
    /**
     * Valida la fortaleza de una contraseña
     * @param password Contraseña a validar
     * @return true si la contraseña es fuerte
     */
    boolean isStrongPassword(String password);
    
    /**
     * Genera una contraseña aleatoria fuerte
     * @param length Longitud de la contraseña
     * @return Contraseña generada
     */
    String generateRandomPassword(int length);
} 