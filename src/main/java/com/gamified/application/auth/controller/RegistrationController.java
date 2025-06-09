package com.gamified.application.auth.controller;

import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.AuthResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.security.RefreshToken;
import com.gamified.application.auth.service.auth.TokenService;
import com.gamified.application.auth.service.user.UserRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para operaciones de registro de usuarios
 */
@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRegistrationService userRegistrationService;
    private final TokenService tokenService;

    /**
     * Endpoint para verificar disponibilidad de email
     * @param email Email a verificar
     * @return true si está disponible
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> isEmailAvailable(@RequestParam String email) {
        boolean isAvailable = userRegistrationService.isEmailAvailable(email);
        return ResponseEntity.ok(isAvailable);
    }

    /**
     * Endpoint para verificar disponibilidad de nombre de usuario
     * @param username Nombre de usuario a verificar
     * @return true si está disponible
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> isUsernameAvailable(@RequestParam String username) {
        boolean isAvailable = userRegistrationService.isUsernameAvailable(username);
        return ResponseEntity.ok(isAvailable);
    }

    /**
     * Endpoint para registrar un nuevo estudiante
     * @param studentRequest Datos del estudiante
     * @param request Request HTTP para obtener información del cliente
     * @return Perfil del estudiante creado
     */
    @PostMapping("/students")
    public ResponseEntity<UserResponseDto.StudentResponseDto> registerStudent(
            @Valid @RequestBody UserRequestDto.StudentRegistrationRequestDto studentRequest,
            HttpServletRequest request) {
        
        // Procesar registro de estudiante
        UserResponseDto.StudentResponseDto response = userRegistrationService.registerStudent(studentRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registrar un nuevo profesor
     * @param teacherRequest Datos del profesor
     * @param request Request HTTP para obtener información del cliente
     * @return Perfil del profesor creado
     */
    @PostMapping("/teachers")
    public ResponseEntity<UserResponseDto.TeacherResponseDto> registerTeacher(
            @Valid @RequestBody UserRequestDto.TeacherRegistrationRequestDto teacherRequest,
            HttpServletRequest request) {
        
        // Procesar registro de profesor
        UserResponseDto.TeacherResponseDto response = userRegistrationService.registerTeacher(teacherRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registrar un nuevo tutor
     * @param guardianRequest Datos del tutor
     * @param request Request HTTP para obtener información del cliente
     * @return Perfil del tutor creado con token de autenticación
     */
    @PostMapping("/guardians")
    public ResponseEntity<AuthResponseDto.LoginResponseDto> registerGuardian(
            @Valid @RequestBody UserRequestDto.GuardianRegistrationRequestDto guardianRequest,
            HttpServletRequest request) {
        
        // Procesar registro de tutor
        UserResponseDto.GuardianResponseDto guardianResponse = userRegistrationService.registerGuardian(guardianRequest);
        
        // Generar token para el usuario recién registrado
        User user = userRegistrationService.findUserById(guardianResponse.getId());
        
        // Generar claims adicionales
        Map<String, Object> claims = new HashMap<>();
        claims.put("guardianProfileId", guardianResponse.getGuardianProfileId());
        
        // Generar token de acceso
        String accessToken = tokenService.generateAccessToken(user, claims);
        
        // Generar refresh token
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String deviceInfo = extractDeviceInfo(userAgent);
        
        RefreshToken refreshToken = tokenService.generateRefreshToken(
                user.getId(), 
                ipAddress, 
                userAgent, 
                deviceInfo, 
                "Sesión inicial " + deviceInfo
        );
        
        // Crear respuesta con tokens
        AuthResponseDto.LoginResponseDto response = AuthResponseDto.LoginResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(3600L) // 1 hora
                .userInfo(
                        UserResponseDto.UserInfoDto.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .fullName(user.getFirstName() + " " + user.getLastName())
                                .role(AuthResponseDto.RoleInfoDto.builder()
                                        .name(user.getRole().getName())
                                        .code(user.getRoleName())
                                        .build())
                                .emailVerified(user.isEmailVerified())
                                .accountActive(user.isActive())
                                .build()
                )
                .loginTime(LocalDateTime.now())
                .sessionId(refreshToken.getId().toString())
                .rememberMe(false)
                .build();
        
        return ResponseEntity.ok(response);
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