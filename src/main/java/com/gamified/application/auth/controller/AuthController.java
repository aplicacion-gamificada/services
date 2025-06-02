package com.gamified.application.auth.controller;

import com.gamified.application.auth.dto.request.AuthRequestDto;
import com.gamified.application.auth.dto.request.SessionRequestDto;
import com.gamified.application.auth.dto.response.AuthResponseDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.SessionResponseDto;
import com.gamified.application.auth.service.auth.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para operaciones de autenticación
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * Endpoint para iniciar sesión
     * @param loginRequest Datos de inicio de sesión
     * @param request Request HTTP para obtener información del cliente
     * @return Token JWT y datos básicos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody AuthRequestDto.LoginRequestDto loginRequest,
            HttpServletRequest request) {
        
        // Añadir información del cliente al request
        enrichRequestWithClientInfo(loginRequest, request);
        
        // Procesar login
        AuthResponseDto response = authenticationService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registrar un nuevo usuario
     * @param registerRequest Datos de registro
     * @param request Request HTTP para obtener información del cliente
     * @return Resultado del registro
     */
    @PostMapping("/register")
    public ResponseEntity<CommonResponseDto> register(
            @Valid @RequestBody AuthRequestDto.RegisterRequestDto registerRequest,
            HttpServletRequest request) {
        
        // Añadir información del cliente al request
        registerRequest.setDeviceInfo(request.getHeader("User-Agent"));
        
        // Validar que las contraseñas coincidan
        if (!registerRequest.isPasswordConfirmed()) {
            return ResponseEntity.badRequest().body(
                    CommonResponseDto.builder()
                            .success(false)
                            .message("Las contraseñas no coinciden")
                            .build()
            );
        }
        
        // Procesar registro
        CommonResponseDto response = authenticationService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para renovar el token de acceso
     * @param refreshRequest Refresh token
     * @param request Request HTTP para obtener información del cliente
     * @return Nuevos tokens
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<SessionResponseDto> refreshToken(
            @Valid @RequestBody SessionRequestDto.RefreshTokenRequestDto refreshRequest,
            HttpServletRequest request) {
        
        // Añadir información del cliente al request
        enrichRequestWithClientInfo(refreshRequest, request);
        
        // Procesar refresh token
        SessionResponseDto response = authenticationService.refreshToken(refreshRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para cerrar sesión
     * @param logoutRequest Token a revocar
     * @return Resultado del cierre de sesión
     */
    @PostMapping("/logout")
    public ResponseEntity<CommonResponseDto> logout(
            @Valid @RequestBody SessionRequestDto.LogoutRequestDto logoutRequest,
            HttpServletRequest request) {
        
        // Añadir información del cliente al request
        logoutRequest.setDeviceInfo(request.getHeader("User-Agent"));
        
        // Procesar logout
        CommonResponseDto response = authenticationService.logout(logoutRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar el email
     * @param verifyRequest Token de verificación
     * @return Resultado de la verificación
     */
    @PostMapping("/verify-email")
    public ResponseEntity<CommonResponseDto> verifyEmail(
            @Valid @RequestBody AuthRequestDto.EmailVerificationRequestDto verifyRequest) {
        
        CommonResponseDto response = authenticationService.verifyEmail(verifyRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para reenviar el email de verificación
     * @param resendRequest Email a verificar
     * @param request Request HTTP para obtener información del cliente
     * @return Resultado del reenvío
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<CommonResponseDto> resendVerification(
            @Valid @RequestBody AuthRequestDto.ResendVerificationRequestDto resendRequest,
            HttpServletRequest request) {
        
        // Añadir información del cliente al request
        enrichRequestWithClientInfo(resendRequest, request);
        
        // Esta implementación dependerá de cómo se maneje en el servicio
        // Por ahora, devolveremos una respuesta fija
        return ResponseEntity.ok(
                CommonResponseDto.builder()
                        .success(true)
                        .message("Se ha enviado un nuevo email de verificación. Por favor, revise su bandeja de entrada.")
                        .build()
        );
    }

    /**
     * Endpoint para solicitar reset de contraseña
     * @param resetRequest Email del usuario
     * @param request Request HTTP para obtener información del cliente
     * @return Resultado de la solicitud
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<CommonResponseDto> requestPasswordReset(
            @Valid @RequestBody AuthRequestDto.PasswordResetRequestDto resetRequest,
            HttpServletRequest request) {
        
        // Añadir información del cliente al request
        enrichRequestWithClientInfo(resetRequest, request);
        
        // Procesar solicitud de reset
        CommonResponseDto response = authenticationService.requestPasswordReset(resetRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para ejecutar el reset de contraseña
     * @param resetRequest Token y nueva contraseña
     * @return Resultado del reset
     */
    @PostMapping("/reset-password")
    public ResponseEntity<CommonResponseDto> executePasswordReset(
            @Valid @RequestBody AuthRequestDto.PasswordResetExecuteRequestDto resetRequest) {
        
        // Validar que las contraseñas coincidan
        if (!resetRequest.isNewPasswordConfirmed()) {
            return ResponseEntity.badRequest().body(
                    CommonResponseDto.builder()
                            .success(false)
                            .message("Las contraseñas no coinciden")
                            .build()
            );
        }
        
        // Procesar reset de password
        CommonResponseDto response = authenticationService.executePasswordReset(resetRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Añade información del cliente a la solicitud
     * @param dto DTO con campos para información del cliente
     * @param request Request HTTP
     */
    private void enrichRequestWithClientInfo(Object dto, HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = extractDeviceInfo(userAgent);
        
        if (dto instanceof AuthRequestDto.LoginRequestDto) {
            ((AuthRequestDto.LoginRequestDto) dto).setUserAgent(userAgent);
            ((AuthRequestDto.LoginRequestDto) dto).setDeviceInfo(deviceInfo);
        } else if (dto instanceof SessionRequestDto.RefreshTokenRequestDto) {
            ((SessionRequestDto.RefreshTokenRequestDto) dto).setUserAgent(userAgent);
            ((SessionRequestDto.RefreshTokenRequestDto) dto).setDeviceInfo(deviceInfo);
        } else if (dto instanceof AuthRequestDto.PasswordResetRequestDto) {
            ((AuthRequestDto.PasswordResetRequestDto) dto).setUserAgent(userAgent);
            ((AuthRequestDto.PasswordResetRequestDto) dto).setDeviceInfo(deviceInfo);
        } else if (dto instanceof AuthRequestDto.ResendVerificationRequestDto) {
            ((AuthRequestDto.ResendVerificationRequestDto) dto).setUserAgent(userAgent);
            ((AuthRequestDto.ResendVerificationRequestDto) dto).setDeviceInfo(deviceInfo);
        }
    }
    
    /**
     * Extrae información básica del dispositivo desde el User-Agent
     * @param userAgent Cadena User-Agent
     * @return Información del dispositivo
     */
    private String extractDeviceInfo(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        
        // Extraer información básica para identificar el dispositivo
        String deviceInfo = "Unknown";
        
        if (userAgent.contains("Windows")) {
            deviceInfo = "Windows";
        } else if (userAgent.contains("Mac")) {
            deviceInfo = "Mac";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            deviceInfo = "iOS";
        } else if (userAgent.contains("Android")) {
            deviceInfo = "Android";
        } else if (userAgent.contains("Linux")) {
            deviceInfo = "Linux";
        }
        
        return deviceInfo;
    }
} 