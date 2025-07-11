package com.gamified.application.auth.service.auth;

import com.gamified.application.shared.model.dto.request.AuthRequestDto;
import com.gamified.application.shared.model.dto.response.AuthResponseDto;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import com.gamified.application.shared.model.dto.request.SessionRequestDto;
import com.gamified.application.shared.model.dto.response.SessionResponseDto;

/**
 * Servicio para operaciones de autenticación de usuarios
 */
public interface AuthenticationService {
    
    /**
     * Autentica un usuario con sus credenciales
     * @param authRequest Credenciales del usuario (email y password)
     * @return Respuesta con tokens y datos básicos del usuario
     */
    AuthResponseDto.LoginResponseDto login(AuthRequestDto.LoginRequestDto authRequest);
    
    /**
     * Autentica un usuario estudiante con username y contraseña
     * @param loginRequest Credenciales del estudiante (username y password)
     * @return Respuesta con tokens y datos básicos del usuario
     */
    AuthResponseDto.LoginResponseDto loginStudent(AuthRequestDto.StudentLoginRequestDto loginRequest);
    
    /**
     * Registra un nuevo usuario según su rol
     * @param registerRequest Datos del nuevo usuario
     * @return Respuesta con resultado del registro
     */
    CommonResponseDto register(AuthRequestDto.RegisterRequestDto registerRequest);
    
    /**
     * Genera un nuevo access token usando un refresh token
     * @param refreshRequest Refresh token actual
     * @return Nuevos tokens generados
     */
    SessionResponseDto.RefreshTokenResponseDto refreshToken(SessionRequestDto.RefreshTokenRequestDto refreshRequest);
    
    /**
     * Cierra la sesión del usuario
     * @param logoutRequest Token de sesión a cerrar
     * @return Respuesta con resultado del cierre de sesión
     */
    CommonResponseDto logout(SessionRequestDto.LogoutRequestDto logoutRequest);
    
    /**
     * Verifica un token de verificación de email
     * @param verifyRequest Token de verificación
     * @return Respuesta con resultado de la verificación
     */
    CommonResponseDto verifyEmail(AuthRequestDto.EmailVerificationRequestDto verifyRequest);
    
    /**
     * Solicita un reseteo de password
     * @param resetRequest Email del usuario
     * @return Respuesta con resultado de la solicitud
     */
    CommonResponseDto requestPasswordReset(AuthRequestDto.PasswordResetRequestDto resetRequest);
    
    /**
     * Resetea el password de un usuario usando un token
     * @param resetRequest Token y nuevo password
     * @return Respuesta con resultado del reseteo
     */
    CommonResponseDto executePasswordReset(AuthRequestDto.PasswordResetExecuteRequestDto resetRequest);
} 