package com.gamified.application.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Utilidades para trabajar con información de autenticación
 */
@Component
public class AuthenticationUtils {

    /**
     * Extrae el ID del usuario de la autenticación
     * @param authentication Objeto de autenticación
     * @return ID del usuario
     */
    public static Long getUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        // Obtener el ID del usuario del principal
        // La implementación exacta dependerá de cómo se almacena el ID en el objeto Authentication
        return Long.valueOf(authentication.getName());
    }
    
    /**
     * Extrae el token JWT de la petición HTTP
     * @param request Petición HTTP
     * @return Token JWT o null si no existe
     */
    public static String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * Obtiene los roles del usuario autenticado
     * @param authentication Objeto de autenticación
     * @return Lista de roles como cadenas
     */
    public static Collection<String> getUserRoles(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
    
    /**
     * Verifica si el usuario tiene un rol específico
     * @param authentication Objeto de autenticación
     * @param role Rol a verificar
     * @return true si el usuario tiene el rol
     */
    public static boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
    
    /**
     * Extrae información básica del dispositivo desde el User-Agent
     * @param userAgent Cadena User-Agent
     * @return Información del dispositivo
     */
    public static String extractDeviceInfo(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        
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