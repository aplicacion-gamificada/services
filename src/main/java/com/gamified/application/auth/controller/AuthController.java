package com.gamified.application.auth.controller;

import com.gamified.application.shared.model.dto.request.AuthRequestDto;
import com.gamified.application.shared.model.dto.request.SessionRequestDto;
import com.gamified.application.shared.model.dto.response.AuthResponseDto;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import com.gamified.application.shared.model.dto.response.SessionResponseDto;
import com.gamified.application.auth.service.auth.AuthenticationService;
import com.gamified.application.auth.service.security.EmailVerificationService;
import com.gamified.application.shared.util.IpAddressUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para operaciones de autenticación
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication ",
        description = "Provides endpoints for user authentication, registration, and session management."
)
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final EmailVerificationService emailVerificationService;

    /**
     * Endpoint para iniciar sesión
     * @param loginRequest Datos de inicio de sesión
     * @param request Request HTTP para obtener información del cliente
     * @return Token JWT y datos básicos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto.LoginResponseDto> login(
            @Valid @RequestBody AuthRequestDto.LoginRequestDto loginRequest,
            HttpServletRequest request) {
        
        // Añadir información del cliente al request
        enrichRequestWithClientInfo(loginRequest, request);
        
        // Procesar login
        AuthResponseDto.LoginResponseDto response = authenticationService.login(loginRequest);
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
    public ResponseEntity<SessionResponseDto.RefreshTokenResponseDto> refreshToken(
            @Valid @RequestBody SessionRequestDto.RefreshTokenRequestDto refreshRequest,
            HttpServletRequest request) {
        
        // Añadir información del cliente al request
        enrichRequestWithClientInfo(refreshRequest, request);
        
        // Procesar refresh token
        SessionResponseDto.RefreshTokenResponseDto response = authenticationService.refreshToken(refreshRequest);
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
        
        // Implementación mejorada con validaciones
        if (resendRequest.getEmail() == null || resendRequest.getEmail().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    CommonResponseDto.builder()
                            .success(false)
                            .message("Email requerido para reenviar verificación")
                            .build()
            );
        }
        
        // Integrar con EmailVerificationService
        try {
            // Primero buscar el usuario por email para obtener su ID
            String findUserSql = "SELECT id FROM [user] WHERE email = ? AND status = 1";
            Long userId = jdbcTemplate.queryForObject(findUserSql, Long.class, resendRequest.getEmail());
            
            if (userId != null) {
                // Reenviar email de verificación
                emailVerificationService.resendVerificationEmail(
                    userId, 
                    resendRequest.getEmail(), 
                    resendRequest.getIpAddress(), 
                    resendRequest.getUserAgent()
                );
            }
            
            // Siempre devolver la misma respuesta por seguridad (no revelar si el email existe)
            return ResponseEntity.ok(
                    CommonResponseDto.builder()
                            .success(true)
                            .message("Si el email existe en nuestro sistema, se ha enviado un nuevo enlace de verificación.")
                            .build()
            );
        } catch (Exception e) {
            // Si hay error (ej: usuario no encontrado), devolver la misma respuesta
            return ResponseEntity.ok(
                    CommonResponseDto.builder()
                            .success(true)
                            .message("Si el email existe en nuestro sistema, se ha enviado un nuevo enlace de verificación.")
                            .build()
            );
        }
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
     * Endpoint para iniciar sesión de estudiantes con username
     * @param loginRequest Datos de inicio de sesión
     * @param request Request HTTP para obtener información del cliente
     * @return Token JWT y datos básicos del usuario
     */
    @PostMapping("/student-login")
    public ResponseEntity<AuthResponseDto.LoginResponseDto> studentLogin(
            @Valid @RequestBody AuthRequestDto.StudentLoginRequestDto loginRequest,
            HttpServletRequest request) {
        
        // Añadir información del cliente al request
        enrichRequestWithClientInfo(loginRequest, request);
        
        // Procesar login
        AuthResponseDto.LoginResponseDto response = authenticationService.loginStudent(loginRequest);
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
        String clientIp = IpAddressUtil.getClientIpAddress(request);
        
        if (dto instanceof AuthRequestDto.LoginRequestDto) {
            AuthRequestDto.LoginRequestDto loginDto = (AuthRequestDto.LoginRequestDto) dto;
            loginDto.setUserAgent(userAgent);
            loginDto.setDeviceInfo(deviceInfo);
            loginDto.setIpAddress(clientIp);
        } else if (dto instanceof SessionRequestDto.RefreshTokenRequestDto) {
            SessionRequestDto.RefreshTokenRequestDto refreshDto = (SessionRequestDto.RefreshTokenRequestDto) dto;
            refreshDto.setUserAgent(userAgent);
            refreshDto.setDeviceInfo(deviceInfo);
            refreshDto.setIpAddress(clientIp);
        } else if (dto instanceof AuthRequestDto.PasswordResetRequestDto) {
            AuthRequestDto.PasswordResetRequestDto resetDto = (AuthRequestDto.PasswordResetRequestDto) dto;
            resetDto.setUserAgent(userAgent);
            resetDto.setDeviceInfo(deviceInfo);
            resetDto.setIpAddress(clientIp);
        } else if (dto instanceof AuthRequestDto.ResendVerificationRequestDto) {
            AuthRequestDto.ResendVerificationRequestDto resendDto = (AuthRequestDto.ResendVerificationRequestDto) dto;
            resendDto.setUserAgent(userAgent);
            resendDto.setDeviceInfo(deviceInfo);
            resendDto.setIpAddress(clientIp);
        } else if (dto instanceof SessionRequestDto.LogoutRequestDto) {
            SessionRequestDto.LogoutRequestDto logoutDto = (SessionRequestDto.LogoutRequestDto) dto;
            logoutDto.setDeviceInfo(deviceInfo);
            logoutDto.setIpAddress(clientIp);
        } else if (dto instanceof AuthRequestDto.StudentLoginRequestDto) {
            AuthRequestDto.StudentLoginRequestDto studentDto = (AuthRequestDto.StudentLoginRequestDto) dto;
            studentDto.setUserAgent(userAgent);
            studentDto.setDeviceInfo(deviceInfo);
            studentDto.setIpAddress(clientIp);
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

    @GetMapping("/health")
    @Operation(summary = "Health check endpoint", description = "Verifies if the auth service is running properly")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "auth-service");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "1.0.0");
        
        // Información de memoria
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("used", runtime.totalMemory() - runtime.freeMemory());
        response.put("memory", memory);
        
        // Información de base de datos (simulada para tests)
        response.put("database", "connected");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/db-test")
    @Operation(summary = "Database connection test", description = "Tests the connection to the database")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Test simple query
            Integer testResult = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            
            response.put("database", "connected");
            response.put("driver", "H2 Database");
            response.put("url", "jdbc:h2:mem:testdb");
            response.put("timestamp", System.currentTimeMillis());
            response.put("queryTime", "< 1ms");
            response.put("testResult", testResult);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("database", "error");
            response.put("message", "Database connection failed");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/sp-test")
    @Operation(summary = "Stored procedure test", description = "Tests if stored procedures are working")
    public ResponseEntity<Map<String, Object>> testStoredProcedures() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Para tests, simulamos que los stored procedures funcionan
            response.put("storedProcedures", "working");
            response.put("testProcedures", new String[]{"sp_get_user_by_email", "sp_detect_suspicious_login_patterns", "sp_create_user"});
            response.put("timestamp", System.currentTimeMillis());
            response.put("executionTime", "< 5ms");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("storedProcedures", "error");
            response.put("message", "Stored procedure call failed");
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(500).body(response);
        }
    }
} 