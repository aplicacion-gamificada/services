package com.gamified.application.auth.repository.core;

import com.gamified.application.user.model.entity.User;
import com.gamified.application.shared.repository.BaseRepository;
import com.gamified.application.shared.repository.Result;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones en la tabla 'user'
 */
public interface UserRepository extends BaseRepository<User, Long> {
    /**
     * Busca un usuario por su email
     * @param email Email del usuario
     * @return Usuario si existe, empty si no
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Busca un usuario para autenticación (incluye password y datos de login)
     * @param email Email del usuario
     * @return Usuario con datos completos para autenticación
     */
    Optional<User> findForAuthentication(String email);
    
    /**
     * Actualiza el estado de login de un usuario (intentos fallidos, bloqueo, etc)
     * @param userId ID del usuario
     * @param successful Si el login fue exitoso
     * @param ipAddress Dirección IP del intento de login
     * @return Resultado de la operación
     */
    Result<Boolean> updateLoginStatus(Long userId, boolean successful, String ipAddress);
    
    /**
     * Marca el email como verificado
     * @param userId ID del usuario
     * @param verificationToken Token de verificación
     * @return Resultado de la operación
     */
    Result<Boolean> verifyEmail(Long userId, String verificationToken);
    
    /**
     * Genera un token para reseteo de password
     * @param userId ID del usuario
     * @param expirationHours Horas de validez del token
     * @return Token generado o empty si falló
     */
    Optional<String> generatePasswordResetToken(Long userId, int expirationHours);
    
    /**
     * Resetea el password de un usuario
     * @param userId ID del usuario
     * @param resetToken Token de reseteo
     * @param newPasswordHash Nuevo hash de password
     * @return Resultado de la operación
     */
    Result<Boolean> resetPassword(Long userId, String resetToken, String newPasswordHash);
    
    /**
     * Busca usuarios por nombre o email (para búsqueda)
     * @param searchTerm Término de búsqueda
     * @param limit Límite de resultados
     * @return Lista de usuarios que coinciden
     */
    List<User> searchUsers(String searchTerm, int limit);
    
    /**
     * Actualiza la contraseña de un usuario
     * @param userId ID del usuario
     * @param newPasswordHash Nuevo hash de la contraseña
     * @return Resultado de la operación
     */
    Result<Boolean> updatePassword(Long userId, String newPasswordHash);
} 