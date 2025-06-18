package com.gamified.application.auth.repository.security;

import com.gamified.application.auth.entity.audit.LoginHistory;
import com.gamified.application.auth.entity.security.EmailVerification;
import com.gamified.application.auth.entity.security.PasswordHistory;
import com.gamified.application.auth.entity.security.RefreshToken;
import com.gamified.application.auth.repository.interfaces.Result;
import com.gamified.application.auth.util.DatabaseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Implementación JDBC del repositorio de seguridad
 */
@Repository
@RequiredArgsConstructor
public class JdbcSecurityRepositoryImpl implements SecurityRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DataSource dataSource;

    // RowMapper para RefreshToken
    private final RowMapper<RefreshToken> refreshTokenRowMapper = (rs, rowNum) -> 
        new RefreshToken(
            rs.getLong("id"),
            rs.getString("token"),
            rs.getLong("user_id"),
            rs.getTimestamp("expires_at"),
            DatabaseUtils.safeToBoolean(rs.getObject("is_revoked")),
            rs.getObject("revoked_at", LocalDateTime.class),
            rs.getString("revoked_reason"),
            rs.getTimestamp("created_at"),
            rs.getObject("last_used_at", LocalDateTime.class),
            rs.getString("ip_address"),
            rs.getString("user_agent"),
            rs.getString("device_info"),
            rs.getString("session_name")
        );

    // RowMapper para EmailVerification
    private final RowMapper<EmailVerification> emailVerificationRowMapper = (rs, rowNum) ->
        new EmailVerification(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("email"),
            rs.getString("verification_token"),
            rs.getTimestamp("expires_at"),
            DatabaseUtils.safeToBoolean(rs.getObject("is_verified")),
            rs.getTimestamp("verified_at"),
            rs.getInt("attempt_count"),
            rs.getTimestamp("last_attempt_at"),
            rs.getTimestamp("created_at"),
            rs.getString("ip_address"),
            rs.getString("user_agent")
        );

    // RowMapper para PasswordHistory
    private final RowMapper<PasswordHistory> passwordHistoryRowMapper = (rs, rowNum) ->
        new PasswordHistory(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("password_hash"),
            rs.getTimestamp("changed_at"),
            DatabaseUtils.safeToBoolean(rs.getObject("changed_by_admin")),
            rs.getString("ip_address"),
            rs.getString("user_agent")
        );

    @Override
    public Result<EmailVerification> createEmailVerification(Long userId, String email, String ipAddress, String userAgent) {
        try {
            // Por ahora implementación temporal
            Timestamp now = new Timestamp(System.currentTimeMillis());
            Timestamp expiry = new Timestamp(System.currentTimeMillis() + 86400000); // +24 horas
            
            EmailVerification verification = new EmailVerification(
                1L, // id simulado
                userId,
                email,
                UUID.randomUUID().toString(), // token
                expiry, // expiresAt
                false, // isVerified
                null, // verifiedAt
                0, // attemptCount
                null, // lastAttemptAt
                now, // createdAt
                ipAddress,
                userAgent
            );
            
            return Result.success(verification);
        } catch (Exception e) {
            return Result.failure("Error al crear la verificación: " + e.getMessage());
        }
    }

    @Override
    public Result<EmailVerification> verifyEmail(String token) {
        try {
            Optional<EmailVerification> verification = findEmailVerificationByToken(token);
            if (verification.isEmpty()) {
                return Result.failure("Token de verificación no encontrado");
            }
            
            EmailVerification emailVerification = verification.get();
            if (emailVerification.isExpired()) {
                return Result.failure("El token de verificación ha expirado");
            }
            
            if (emailVerification.isVerified()) {
                return Result.failure("El email ya ha sido verificado");
            }
            
            emailVerification.markAsVerified();
            
            // Simulamos actualización en la BD
            return Result.success(emailVerification);
        } catch (Exception e) {
            return Result.failure("Error al verificar el email: " + e.getMessage());
        }
    }

    @Override
    public Optional<EmailVerification> findEmailVerificationByToken(String token) {
        try {
            // Implementación temporal
            if (token != null && !token.isEmpty()) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                Timestamp expiry = new Timestamp(System.currentTimeMillis() + 86400000); // +24 horas
                
                EmailVerification verification = new EmailVerification(
                    1L, // id
                    1L, // userId
                    "test@example.com", // email
                    token, // token
                    expiry, // expiresAt
                    false, // isVerified
                    null, // verifiedAt
                    0, // attemptCount
                    null, // lastAttemptAt
                    new Timestamp(now.getTime() - 3600000), // createdAt (-1 hora)
                    "127.0.0.1", // ipAddress
                    "Mozilla/5.0" // userAgent
                );
                
                return Optional.of(verification);
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Result<RefreshToken> createRefreshToken(Long userId, String ipAddress, String userAgent, String deviceInfo, String sessionName) {
        try {
            // Para implementación real, usar SimpleJdbcInsert o JdbcTemplate para insertar en la BD
            // Aquí simularemos la creación de un token
            
            LocalDateTime expiryDateTime = LocalDateTime.now().plusDays(7); // 7 días
            Timestamp expiryTimestamp = Timestamp.valueOf(expiryDateTime);
            
            RefreshToken refreshToken = RefreshToken.builder()
                    .id(new Random().nextLong(1000) + 1) // ID aleatorio para simulación
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
            
            return Result.success(refreshToken);
        } catch (Exception e) {
            return Result.failure("Error al crear refresh token: " + e.getMessage());
        }
    }

    @Override
    public Optional<RefreshToken> findRefreshTokenByValue(String tokenValue) {
        try {
            // Implementación temporal
            if (tokenValue != null && !tokenValue.isEmpty()) {
                RefreshToken refreshToken = RefreshToken.builder()
                        .id(1L)
                        .token(tokenValue)
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
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Result<RefreshToken> revokeToken(String tokenValue, String reason) {
        try {
            Optional<RefreshToken> tokenOpt = findRefreshTokenByValue(tokenValue);
            if (tokenOpt.isEmpty()) {
                return Result.failure("Token no encontrado");
            }
            
            RefreshToken token = tokenOpt.get();
            if (token.isRevoked()) {
                return Result.failure("El token ya ha sido revocado");
            }
            
            token.revoke(reason);
            
            // Simulamos la actualización en la BD
            return Result.success(token);
        } catch (Exception e) {
            return Result.failure("Error al revocar el token: " + e.getMessage());
        }
    }

    @Override
    public int revokeAllUserTokens(Long userId, String reason) {
        // Implementación temporal
        // En una implementación real, se actualizarían todos los tokens del usuario
        return 3; // Simulamos que se revocaron 3 tokens
    }

    @Override
    public Result<PasswordHistory> recordPasswordChange(Long userId, String passwordHash, String ipAddress, String userAgent, boolean changedByAdmin) {
        try {
            // Implementación temporal
            Timestamp now = new Timestamp(System.currentTimeMillis());
            
            PasswordHistory history = new PasswordHistory(
                1L, // id
                userId,
                passwordHash,
                now, // changedAt
                changedByAdmin,
                ipAddress,
                userAgent
            );
            
            return Result.success(history);
        } catch (Exception e) {
            return Result.failure("Error al registrar el cambio de contraseña: " + e.getMessage());
        }
    }

    @Override
    public List<PasswordHistory> getPasswordHistory(Long userId, int limit) {
        // Implementación temporal
        return new ArrayList<>();
    }

    @Override
    public boolean isPasswordPreviouslyUsed(Long userId, String passwordHash) {
        // Implementación temporal
        return false;
    }

    @Override
    public void updateRefreshTokenLastUsed(Long tokenId) {
        try {
            // Implementación temporal - en una implementación real actualizaría el campo last_used_at
            // String sql = "UPDATE refresh_token SET last_used_at = ? WHERE id = ?";
            // jdbcTemplate.update(sql, new Timestamp(System.currentTimeMillis()), tokenId);
        } catch (Exception e) {
            // Log error in real implementation
        }
    }
} 