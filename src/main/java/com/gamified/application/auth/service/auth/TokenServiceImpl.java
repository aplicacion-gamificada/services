package com.gamified.application.auth.service.auth;

import com.gamified.application.config.JwtConfig;
import com.gamified.application.auth.dto.request.SessionRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.SessionResponseDto;
import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.security.RefreshToken;
import com.gamified.application.auth.repository.interfaces.Result;
import com.gamified.application.auth.repository.security.SecurityRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de tokens
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtConfig jwtConfig;
    private final SecurityRepository securityRepository;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;
    
    @Override
    public String generateAccessToken(User user, Map<String, Object> additionalClaims) {
        if (additionalClaims == null) {
            additionalClaims = new HashMap<>();
        }
        
        // Añadir claims comunes
        additionalClaims.put("userId", user.getId());
        additionalClaims.put("email", user.getEmail());
        additionalClaims.put("role", user.getRole().getName());
        
        return jwtConfig.generateToken(additionalClaims, user.getEmail());
    }
    
    @Override
    public RefreshToken generateRefreshToken(Long userId, String ipAddress, String userAgent, String deviceInfo, String sessionName) {
        Result<RefreshToken> result = securityRepository.createRefreshToken(
            userId, ipAddress, userAgent, deviceInfo, sessionName
        );
        
        if (!result.isSuccess()) {
            throw new RuntimeException("Error al crear refresh token: " + result.getErrorMessage());
        }
        
        return result.getData();
    }
    
    @Override
    public boolean validateToken(String token) {
        try {
            // Utilizar jwtConfig para validar el token
            return !jwtConfig.extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public Long extractUserId(String token) {
        try {
            return ((Number) jwtConfig.extractClaim(token, claims -> claims.get("userId"))).longValue();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public String extractUsername(String token) {
        try {
            return jwtConfig.extractUsername(token);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public Map<String, Object> extractClaims(String token) {
        try {
            Claims claims = jwtConfig.extractAllClaims(token);
            Map<String, Object> claimsMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                claimsMap.put(entry.getKey(), entry.getValue());
            }
            return claimsMap;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    @Override
    public List<SessionResponseDto.SessionInfoResponseDto> getActiveSessions(Long userId) {
        // Implementar la búsqueda de tokens en la base de datos
        // Por ahora devolvemos una lista simulada
        List<SessionResponseDto.SessionInfoResponseDto> sessions = new ArrayList<>();
        sessions.add(SessionResponseDto.SessionInfoResponseDto.builder()
                .deviceInfo("Dispositivo Simulado")
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla/5.0")
                .browser("Chrome")
                .operatingSystem("Windows")
                .sessionStartTime(LocalDateTime.now().minusHours(1))
                .build());
        return sessions;
    }
    
    @Override
    public CommonResponseDto renameSession(SessionRequestDto.RenameSessionRequestDto renameRequest) {
        // Implementar renombrado de sesión en la base de datos
        return CommonResponseDto.builder()
                .success(true)
                .message("Sesión renombrada exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public boolean revokeRefreshToken(String token, String reason) {
        Result<RefreshToken> result = securityRepository.revokeToken(token, reason);
        return result.isSuccess();
    }
    
    @Override
    public CommonResponseDto revokeSession(Long sessionId, Long userId) {
        // Implementar revocación de sesión específica
        return CommonResponseDto.builder()
                .success(true)
                .message("Sesión revocada exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public CommonResponseDto revokeAllSessions(Long userId) {
        int revokedCount = securityRepository.revokeAllUserTokens(userId, "Revocación manual de todas las sesiones");
        
        return CommonResponseDto.builder()
                .success(true)
                .message("Se han revocado " + revokedCount + " sesiones")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public CommonResponseDto revokeAllSessionsExceptCurrent(Long userId, String currentToken) {
        // Implementar revocación de todas las sesiones excepto la actual
        return CommonResponseDto.builder()
                .success(true)
                .message("Todas las demás sesiones han sido revocadas")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public Optional<RefreshToken> findRefreshToken(String token) {
        return securityRepository.findRefreshTokenByValue(token);
    }
} 