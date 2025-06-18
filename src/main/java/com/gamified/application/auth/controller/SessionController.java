package com.gamified.application.auth.controller;

import com.gamified.application.auth.dto.request.SessionRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.SessionResponseDto;
import com.gamified.application.auth.service.auth.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para operaciones relacionadas con sesiones de usuario
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
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
        // Esto dependerá de cómo se almacene el ID de usuario en el objeto Authentication
        // Por ahora, asumiremos que es un Long que se puede obtener del principal
        return Long.valueOf(authentication.getName());
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