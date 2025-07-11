package com.gamified.application.auth.repository.security;

import com.gamified.application.auth.entity.security.EmailVerification;
import com.gamified.application.auth.entity.security.PasswordHistory;
import com.gamified.application.auth.entity.security.RefreshToken;
import com.gamified.application.shared.repository.Result;
import com.gamified.application.shared.util.DatabaseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource; 
import java.sql.PreparedStatement;
import java.sql.Statement;
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
                    "0.0.0.0", // ipAddress - IP desconocida por defecto
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
            String tokenValue = UUID.randomUUID().toString();
            LocalDateTime expiryDateTime = LocalDateTime.now().plusDays(7); // 7 días
            Timestamp expiryTimestamp = Timestamp.valueOf(expiryDateTime);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            
            String sql = """
                INSERT INTO refresh_token (
                    token, user_id, expires_at, is_revoked, created_at, 
                    ip_address, user_agent, device_info, session_name
                ) VALUES (?, ?, ?, 0, ?, ?, ?, ?, ?)
                """;
            
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            int result = jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, tokenValue);
                ps.setLong(2, userId);
                ps.setTimestamp(3, expiryTimestamp);
                ps.setTimestamp(4, now);
                ps.setString(5, ipAddress);
                ps.setString(6, userAgent);
                ps.setString(7, deviceInfo);
                ps.setString(8, sessionName);
                return ps;
            }, keyHolder);
            
            if (result > 0) {
                Long generatedId = keyHolder.getKey().longValue();
                
                RefreshToken refreshToken = RefreshToken.builder()
                        .id(generatedId)
                        .token(tokenValue)
                        .userId(userId)
                        .expiresAt(expiryTimestamp)
                        .isRevoked(false)
                        .createdAt(now)
                        .ipAddress(ipAddress)
                        .userAgent(userAgent)
                        .deviceInfo(deviceInfo)
                        .sessionName(sessionName)
                        .build();
                
                return Result.success(refreshToken);
            } else {
                return Result.failure("No se pudo insertar el refresh token");
            }
        } catch (Exception e) {
            return Result.failure("Error al crear refresh token: " + e.getMessage());
        }
    }

    @Override
    public Optional<RefreshToken> findRefreshTokenByValue(String tokenValue) {
        try {
            if (tokenValue == null || tokenValue.isEmpty()) {
                return Optional.empty();
            }
            
            String sql = """
                SELECT id, token, user_id, expires_at, is_revoked, revoked_at, 
                       revoked_reason, created_at, last_used_at, ip_address, 
                       user_agent, device_info, session_name
                FROM refresh_token 
                WHERE token = ? AND is_revoked = 0
                """;
            
            List<RefreshToken> tokens = jdbcTemplate.query(sql, refreshTokenRowMapper, tokenValue);
            return tokens.isEmpty() ? Optional.empty() : Optional.of(tokens.get(0));
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
            
            String sql = """
                UPDATE refresh_token 
                SET is_revoked = 1, 
                    revoked_at = GETDATE(), 
                    revoked_reason = ?
                WHERE token = ?
                """;
            
            int result = jdbcTemplate.update(sql, reason, tokenValue);
            if (result > 0) {
                token.revoke(reason);
                return Result.success(token);
            } else {
                return Result.failure("No se pudo revocar el token");
            }
        } catch (Exception e) {
            return Result.failure("Error al revocar el token: " + e.getMessage());
        }
    }

    @Override
    public int revokeAllUserTokens(Long userId, String reason) {
        try {
            String sql = """
                UPDATE refresh_token 
                SET is_revoked = 1, 
                    revoked_at = GETDATE(), 
                    revoked_reason = ?
                WHERE user_id = ? AND is_revoked = 0
                """;
            
            int revokedCount = jdbcTemplate.update(sql, reason, userId);
            return revokedCount;
        } catch (Exception e) {
            // Log del error en implementación real
            return 0;
        }
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
            String sql = "UPDATE refresh_token SET last_used_at = GETDATE() WHERE id = ?";
            jdbcTemplate.update(sql, tokenId);
        } catch (Exception e) {
            // Log error in real implementation
        }
    }
    
    @Override
    public Long findUserIdByEmail(String email) {
        try {
            String sql = "SELECT id FROM [user] WHERE email = ? AND status = 1";
            return jdbcTemplate.queryForObject(sql, Long.class, email);
        } catch (Exception e) {
            throw new IllegalArgumentException("Usuario no encontrado con email: " + email, e);
        }
    }
} 