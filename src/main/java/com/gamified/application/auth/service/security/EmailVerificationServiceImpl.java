package com.gamified.application.auth.service.security;

import com.gamified.application.auth.entity.security.EmailVerification;
import com.gamified.application.auth.repository.security.SecurityRepository;
import com.gamified.application.shared.repository.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del servicio de verificación de email
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {
    
    private final SecurityRepository securityRepository;
    
    @Override
    public Result<EmailVerification> createEmailVerification(Long userId, String email, String ipAddress, String userAgent) {
        try {
            log.info("Creating email verification for user ID: {} and email: {}", userId, email);
            
            // Validaciones básicas
            if (userId == null || email == null || email.trim().isEmpty()) {
                return Result.failure("Datos de usuario y email requeridos");
            }
            
            // Crear verificación usando el repositorio
            Result<EmailVerification> result = securityRepository.createEmailVerification(
                userId, email, ipAddress, userAgent);
            
            if (result.isSuccess()) {
                log.info("Email verification created successfully for user ID: {}", userId);
            } else {
                log.error("Failed to create email verification: {}", result.getErrorMessage());
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error creating email verification: {}", e.getMessage(), e);
            return Result.failure("Error al crear la verificación de email: " + e.getMessage());
        }
    }
    
    @Override
    public Result<EmailVerification> verifyEmail(String token) {
        try {
            log.info("Attempting to verify email with token");
            
            if (token == null || token.trim().isEmpty()) {
                return Result.failure("Token de verificación requerido");
            }
            
            // Usar el repositorio para verificar
            Result<EmailVerification> result = securityRepository.verifyEmail(token);
            
            if (result.isSuccess()) {
                log.info("Email verification successful");
            } else {
                log.warn("Email verification failed: {}", result.getErrorMessage());
            }
            
            return result;
        } catch (Exception e) {
            log.error("Error during email verification: {}", e.getMessage(), e);
            return Result.failure("Error al verificar el email: " + e.getMessage());
        }
    }
    
    @Override
    public Optional<EmailVerification> findByToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return Optional.empty();
            }
            
            return securityRepository.findEmailVerificationByToken(token);
        } catch (Exception e) {
            log.error("Error finding email verification by token: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public boolean isTokenValid(String token) {
        try {
            Optional<EmailVerification> verificationOpt = findByToken(token);
            if (verificationOpt.isEmpty()) {
                return false;
            }
            
            EmailVerification verification = verificationOpt.get();
            return !verification.isExpired() && !verification.isVerified();
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Result<EmailVerification> resendVerificationEmail(Long userId, String email, String ipAddress, String userAgent) {
        try {
            log.info("Resending verification email for user ID: {}", userId);
            
            // Invalidar tokens previos si existen
            // En una implementación real se marcarían como expirados
            
            // Crear nuevo token de verificación
            return createEmailVerification(userId, email, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("Error resending verification email: {}", e.getMessage(), e);
            return Result.failure("Error al reenviar email de verificación: " + e.getMessage());
        }
    }
    
    @Override
    public String generateRandomToken() {
        // Generar token único y seguro
        return UUID.randomUUID().toString().replace("-", "") + 
               System.currentTimeMillis();
    }
    
    @Override
    public LocalDateTime calculateExpiryDate(int hoursValid) {
        return LocalDateTime.now().plusHours(hoursValid);
    }
} 