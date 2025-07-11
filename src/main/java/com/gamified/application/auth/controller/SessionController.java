package com.gamified.application.auth.controller;

import com.gamified.application.shared.model.dto.request.SessionRequestDto;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import com.gamified.application.shared.model.dto.response.SessionResponseDto;
import com.gamified.application.auth.service.auth.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Controlador para operaciones relacionadas con sesiones de usuario
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Tag(
        name = "Session ",
        description = "Provides endpoints for managing user sessions and session management."
)
public class SessionController {

    private final TokenService tokenService;

    /**
     * Obtiene las sesiones activas del usuario actual
     * @param authentication Datos de autenticación del usuario
     * @return Lista de sesiones activas
     */
    @GetMapping
    public ResponseEntity<List<SessionResponseDto.SessionInfoResponseDto>> getActiveSessions(
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        List<SessionResponseDto.SessionInfoResponseDto> sessions = tokenService.getActiveSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Renombra una sesión
     * @param sessionId ID de la sesión
     * @param renameRequest Datos para renombrar la sesión
     * @param authentication Datos de autenticación del usuario
     * @return Resultado de la operación
     */
    @PutMapping("/{sessionId}/rename")
    public ResponseEntity<CommonResponseDto> renameSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionRequestDto.RenameSessionRequestDto renameRequest,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        renameRequest.setSessionId(sessionId);
        renameRequest.setUserId(userId);
        
        CommonResponseDto response = tokenService.renameSession(renameRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Revoca una sesión específica
     * @param sessionId ID de la sesión
     * @param authentication Datos de autenticación del usuario
     * @return Resultado de la operación
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<CommonResponseDto> revokeSession(
            @PathVariable Long sessionId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        
        CommonResponseDto response = tokenService.revokeSession(sessionId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Revoca todas las sesiones del usuario excepto la actual
     * @param request Request HTTP que contiene el token actual
     * @param authentication Datos de autenticación del usuario
     * @return Resultado de la operación
     */
    @DeleteMapping("/all-except-current")
    public ResponseEntity<CommonResponseDto> revokeAllSessionsExceptCurrent(
            HttpServletRequest request,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        String currentToken = extractTokenFromRequest(request);
        
        CommonResponseDto response = tokenService.revokeAllSessionsExceptCurrent(userId, currentToken);
        return ResponseEntity.ok(response);
    }

    /**
     * Revoca todas las sesiones del usuario (incluida la actual)
     * @param authentication Datos de autenticación del usuario
     * @return Resultado de la operación
     */
    @DeleteMapping("/all")
    public ResponseEntity<CommonResponseDto> revokeAllSessions(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        
        CommonResponseDto response = tokenService.revokeAllSessions(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Revoca todas las sesiones de un usuario específico (solo para administradores)
     * @param userId ID del usuario
     * @return Resultado de la operación
     */
    @DeleteMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResponseDto> revokeAllSessionsByAdmin(@PathVariable Long userId) {
        CommonResponseDto response = tokenService.revokeAllSessions(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Extrae el ID de usuario de la autenticación
     * @param authentication Autenticación actual
     * @return ID del usuario
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        try {
            // El principal puede ser el email del usuario o el ID
            // Según la configuración del JWT, el subject suele ser el email
            String principal = authentication.getName();
            
            // Intentar parsear como Long primero (si es userId)
            try {
                return Long.valueOf(principal);
            } catch (NumberFormatException e) {
                // Si no es un número, probablemente es un email
                // Buscar el usuario por email usando UserDetailsServiceImpl
                return findUserIdByEmail(principal);
            }
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo extraer el ID de usuario de la autenticación", e);
        }
    }
    
    /**
     * Busca el ID de usuario por email
     * @param email Email del usuario
     * @return ID del usuario
     */
    private Long findUserIdByEmail(String email) {
        try {
            // Consulta directa para obtener el ID por email
            String sql = "SELECT id FROM [user] WHERE email = ? AND status = 1";
            Long userId = tokenService.findUserIdByEmail(email);
            if (userId != null) {
                return userId;
            }
            throw new IllegalArgumentException("Usuario no encontrado con email: " + email);
        } catch (Exception e) {
            throw new IllegalStateException("Error buscando usuario por email: " + email, e);
        }
    }

    /**
     * Extrae el token JWT de la petición HTTP
     * @param request Petición HTTP
     * @return Token JWT
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
} 