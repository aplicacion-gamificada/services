package com.gamified.application.shared.util;

import com.gamified.application.user.repository.composite.CompleteUserRepository;
import com.gamified.application.user.model.entity.composite.CompleteStudent;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utilidades para trabajar con información de autenticación
 */
@Component
public class AuthenticationUtils {

    private final CompleteUserRepository completeUserRepository;

    @Autowired
    public AuthenticationUtils(CompleteUserRepository completeUserRepository) {
        this.completeUserRepository = completeUserRepository;
    }

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
     * Extrae el email del usuario de la autenticación
     * Basado en que el JWT contiene el email como subject
     * @param authentication Objeto de autenticación
     * @return Email del usuario
     */
    public static String getUserEmail(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        // El authentication.getName() contiene el email (subject del JWT)
        return authentication.getName();
    }
    
    /**
     * Extrae el ID del perfil de estudiante de la autenticación
     * @param authentication Objeto de autenticación
     * @return ID del perfil de estudiante
     */
    public Integer getStudentProfileIdFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        
        try {
            // 1. Obtener el email del JWT
            String email = authentication.getName();
            
            // 2. Buscar el usuario completo por email
            Optional<Object> userOpt = completeUserRepository.findCompleteUserByEmail(email);
            
            if (userOpt.isPresent() && userOpt.get() instanceof CompleteStudent) {
                CompleteStudent completeStudent = (CompleteStudent) userOpt.get();
                // 3. Obtener el student_profile_id
                return completeStudent.getStudentProfile().getId().intValue();
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Error obteniendo studentProfileId del usuario autenticado: " + e.getMessage());
            return null;
        }
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