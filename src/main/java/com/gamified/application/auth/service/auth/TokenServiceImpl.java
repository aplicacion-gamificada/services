package com.gamified.application.auth.service.auth;

import com.gamified.application.auth.dto.request.SessionRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.SessionResponseDto;
import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.security.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación del servicio de tokens
 */
@Service
public class TokenServiceImpl implements TokenService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;
    
    @Override
    public String generateAccessToken(User user, Map<String, Object> additionalClaims) {
        // Implementación temporal para pruebas
        return "simulated-jwt-token-" + UUID.randomUUID();
    }
    
    @Override
    public RefreshToken generateRefreshToken(Long userId, String ipAddress, String userAgent, String deviceInfo, String sessionName) {
        // Implementación temporal para pruebas
        LocalDateTime expiryDateTime = LocalDateTime.now().plusDays(refreshTokenExpiration / (1000 * 60 * 60 * 24));
        Timestamp expiryTimestamp = Timestamp.valueOf(expiryDateTime);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .id(1L)
                .token(UUID.randomUUID().toString())
                .userId(userId)
                .expiresAt(expiryTimestamp)
                .isRevoked(false)
                .createdAt(Timestamp.valueOf(LocalDateTime.now()))
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceInfo(deviceInfo)
                .sessionName(sessionName)
                .build();
        
        return refreshToken;
    }
    
    @Override
    public boolean validateToken(String token) {
        // Implementación temporal para pruebas
        return token != null && !token.isEmpty() && token.startsWith("simulated-jwt-token-");
    }
    
    @Override
    public Long extractUserId(String token) {
        // Implementación temporal para pruebas
        return 1L;
    }
    
    @Override
    public String extractUsername(String token) {
        // Implementación temporal para pruebas
        return "test_user@example.com";
    }
    
    @Override
    public Map<String, Object> extractClaims(String token) {
        // Implementación temporal para pruebas
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 1L);
        claims.put("email", "test_user@example.com");
        claims.put("role", "STUDENT");
        return claims;
    }
    
    @Override
    public List<SessionResponseDto.SessionInfoResponseDto> getActiveSessions(Long userId) {
        // Implementación temporal para pruebas
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
        // Implementación temporal para pruebas
        return CommonResponseDto.builder()
                .success(true)
                .message("Sesión renombrada exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public boolean revokeRefreshToken(String token, String reason) {
        // Implementación temporal para pruebas
        return true;
    }
    
    @Override
    public CommonResponseDto revokeSession(Long sessionId, Long userId) {
        // Implementación temporal para pruebas
        return CommonResponseDto.builder()
                .success(true)
                .message("Sesión revocada exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public CommonResponseDto revokeAllSessions(Long userId) {
        // Implementación temporal para pruebas
        return CommonResponseDto.builder()
                .success(true)
                .message("Todas las sesiones han sido revocadas")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public CommonResponseDto revokeAllSessionsExceptCurrent(Long userId, String currentToken) {
        // Implementación temporal para pruebas
        return CommonResponseDto.builder()
                .success(true)
                .message("Todas las demás sesiones han sido revocadas")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public Optional<RefreshToken> findRefreshToken(String token) {
        // Implementación temporal para pruebas
        if (token != null && !token.isEmpty()) {
            RefreshToken refreshToken = RefreshToken.builder()
                    .id(1L)
                    .token(token)
                    .userId(1L)
                    .expiresAt(Timestamp.valueOf(LocalDateTime.now().plusDays(7)))
                    .isRevoked(false)
                    .createdAt(Timestamp.valueOf(LocalDateTime.now().minusDays(1)))
                    .ipAddress("127.0.0.1")
                    .userAgent("Mozilla/5.0")
                    .deviceInfo("Chrome en Windows")
                    .sessionName("Sesión de prueba")
                    .build();
            return Optional.of(refreshToken);
        }
        return Optional.empty();
    }
} 