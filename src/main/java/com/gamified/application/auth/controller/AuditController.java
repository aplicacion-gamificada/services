package com.gamified.application.auth.controller;

import com.gamified.application.shared.model.dto.request.AuditRequestDto;
import com.gamified.application.shared.model.dto.response.AuditResponseDto;
import com.gamified.application.auth.service.audit.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para operaciones de auditoría de seguridad
 */
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final SecurityAuditService securityAuditService;

    /**
     * Obtiene el historial de inicio de sesión del usuario actual
     * @param authentication Datos de autenticación del usuario
     * @param limit Límite de resultados
     * @param offset Offset de paginación
     * @return Lista de registros de inicio de sesión
     */
    @GetMapping("/login-history")
    public ResponseEntity<List<AuditResponseDto.LoginHistoryResponseDto>> getUserLoginHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        Long userId = getUserIdFromAuthentication(authentication);
        List<AuditResponseDto.LoginHistoryResponseDto> history = 
                securityAuditService.getUserLoginHistory(userId, limit, offset);
        
        return ResponseEntity.ok(history);
    }

    /**
     * Obtiene el historial de actividad del usuario actual
     * @param authentication Datos de autenticación del usuario
     * @param limit Límite de resultados
     * @param offset Offset de paginación
     * @return Lista de registros de actividad
     */
    @GetMapping("/activity")
    public ResponseEntity<List<AuditResponseDto.UserActivityResponseDto>> getUserActivity(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        Long userId = getUserIdFromAuthentication(authentication);
        List<AuditResponseDto.UserActivityResponseDto> activity = 
                securityAuditService.getUserActivity(userId, limit, offset);
        
        return ResponseEntity.ok(activity);
    }

    /**
     * Obtiene el historial de inicio de sesión de un usuario específico (solo para administradores)
     * @param userId ID del usuario
     * @param limit Límite de resultados
     * @param offset Offset de paginación
     * @return Lista de registros de inicio de sesión
     */
    @GetMapping("/users/{userId}/login-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditResponseDto.LoginHistoryResponseDto>> getUserLoginHistoryByAdmin(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        List<AuditResponseDto.LoginHistoryResponseDto> history = 
                securityAuditService.getUserLoginHistory(userId, limit, offset);
        
        return ResponseEntity.ok(history);
    }

    /**
     * Obtiene el historial de actividad de un usuario específico (solo para administradores)
     * @param userId ID del usuario
     * @param limit Límite de resultados
     * @param offset Offset de paginación
     * @return Lista de registros de actividad
     */
    @GetMapping("/users/{userId}/activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditResponseDto.UserActivityResponseDto>> getUserActivityByAdmin(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        List<AuditResponseDto.UserActivityResponseDto> activity = 
                securityAuditService.getUserActivity(userId, limit, offset);
        
        return ResponseEntity.ok(activity);
    }

    /**
     * Busca registros de auditoría según criterios (solo para administradores)
     * @param searchRequest Criterios de búsqueda
     * @return Lista de registros de auditoría
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditResponseDto.AuditLogSearchResponseDto> searchAuditLogs(
            @RequestBody AuditRequestDto.AuditLogSearchRequestDto searchRequest) {

        AuditResponseDto.AuditLogSearchResponseDto results = 
                securityAuditService.searchAuditLogs(searchRequest);
        
        return ResponseEntity.ok(results);
    }

    /**
     * Obtiene resumen de actividad sospechosa (solo para administradores)
     * @param startDate Fecha de inicio
     * @param endDate Fecha de fin
     * @return Resumen de actividad sospechosa
     */
    @GetMapping("/suspicious-activity")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuditResponseDto.SuspiciousActivitySummaryDto> getSuspiciousActivity(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        AuditResponseDto.SuspiciousActivitySummaryDto summary = 
                securityAuditService.getSuspiciousActivity(startDate, endDate);
        
        return ResponseEntity.ok(summary);
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
} 