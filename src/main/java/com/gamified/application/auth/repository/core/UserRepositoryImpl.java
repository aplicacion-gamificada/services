package com.gamified.application.auth.repository.core;

import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.repository.interfaces.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de usuarios con JDBC
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            String sql = "SELECT * FROM [user] WHERE id = ?";
            List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), id);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Result<User> save(User user) {
        try {
            if (user.getId() == null) {
                return insert(user);
            } else {
                return update(user);
            }
        } catch (Exception e) {
            return Result.failure("Error al guardar usuario: " + e.getMessage(), e);
        }
    }

    private Result<User> insert(User user) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO [user] (role_id, institution_id, first_name, last_name, email, password, " +
                    "profile_picture_url, created_at, updated_at, status, email_verified) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setByte(1, user.getRoleId());
                ps.setLong(2, user.getInstitutionId());
                ps.setString(3, user.getFirstName());
                ps.setString(4, user.getLastName());
                ps.setString(5, user.getEmail());
                ps.setString(6, user.getPassword());
                ps.setString(7, user.getProfilePictureUrl());
                
                Timestamp now = new Timestamp(System.currentTimeMillis());
                ps.setTimestamp(8, user.getCreatedAt() != null ? user.getCreatedAt() : now);
                ps.setTimestamp(9, user.getUpdatedAt() != null ? user.getUpdatedAt() : now);
                
                ps.setBoolean(10, user.getStatus() != null ? user.getStatus() : true);
                ps.setBoolean(11, user.isEmailVerified());
                return ps;
            }, keyHolder);
            
            Long userId = keyHolder.getKey().longValue();
            user.setId(userId);
            
            return Result.success(user);
        } catch (Exception e) {
            return Result.failure("Error al insertar usuario: " + e.getMessage(), e);
        }
    }

    private Result<User> update(User user) {
        try {
            int rowsAffected = jdbcTemplate.update(
                "UPDATE [user] SET role_id = ?, institution_id = ?, first_name = ?, last_name = ?, " +
                "email = ?, password = ?, profile_picture_url = ?, status = ?, email_verified = ?, " +
                "email_verification_token = ?, email_verification_expires_at = ?, " +
                "password_reset_token = ?, password_reset_expires_at = ?, " +
                "last_login_at = ?, last_login_ip = ?, failed_login_attempts = ?, " +
                "account_locked_until = ?, updated_at = ? WHERE id = ?",
                user.getRoleId(),
                user.getInstitutionId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPassword(),
                user.getProfilePictureUrl(),
                user.getStatus(),
                user.isEmailVerified(),
                null, // emailVerificationToken - no getter available
                null, // emailVerificationExpiresAt - no getter available
                null, // passwordResetToken - no getter available
                null, // passwordResetExpiresAt - no getter available
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                0, // failedLoginAttempts - no getter available
                null, // accountLockedUntil - no getter available
                new Timestamp(System.currentTimeMillis()),
                user.getId()
            );
            
            if (rowsAffected > 0) {
                return Result.success(user);
            } else {
                return Result.failure("No se encontró el usuario con ID: " + user.getId());
            }
        } catch (Exception e) {
            return Result.failure("Error al actualizar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<Boolean> delete(Long id) {
        try {
            int rowsAffected = jdbcTemplate.update("DELETE FROM [user] WHERE id = ?", id);
            if (rowsAffected > 0) {
                return Result.success(true);
            } else {
                return Result.failure("No se encontró el usuario con ID: " + id);
            }
        } catch (Exception e) {
            return Result.failure("Error al eliminar usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> findAll() {
        try {
            return jdbcTemplate.query("SELECT * FROM [user]", (rs, rowNum) -> mapUser(rs));
        } catch (Exception e) {
            // Log error
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            String sql = "SELECT * FROM [user] WHERE email = ?";
            List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), email);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findForAuthentication(String email) {
        try {
            String sql = "SELECT * FROM [user] WHERE email = ?";
            List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> mapUserForAuth(rs), email);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Result<Boolean> updateLoginStatus(Long userId, boolean successful, String ipAddress) {
        try {
            if (successful) {
                jdbcTemplate.update(
                    "UPDATE [user] SET last_login_at = ?, last_login_ip = ?, failed_login_attempts = 0, " +
                    "account_locked_until = NULL WHERE id = ?",
                    new Timestamp(System.currentTimeMillis()),
                    ipAddress,
                    userId
                );
            } else {
                // Primero obtenemos los intentos fallidos actuales
                Integer failedAttempts = jdbcTemplate.queryForObject(
                    "SELECT failed_login_attempts FROM [user] WHERE id = ?",
                    Integer.class, 
                    userId
                );
                
                if (failedAttempts == null) {
                    failedAttempts = 0;
                }
                
                int newFailedAttempts = failedAttempts + 1;
                
                // Si supera el límite, bloqueamos la cuenta
                if (newFailedAttempts >= 5) {
                    // Bloquear por 30 minutos
                    LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(30);
                    jdbcTemplate.update(
                        "UPDATE [user] SET failed_login_attempts = ?, account_locked_until = ? WHERE id = ?",
                        newFailedAttempts,
                        Timestamp.valueOf(lockUntil),
                        userId
                    );
                } else {
                    jdbcTemplate.update(
                        "UPDATE [user] SET failed_login_attempts = ? WHERE id = ?",
                        newFailedAttempts,
                        userId
                    );
                }
            }
            
            return Result.success(true);
        } catch (Exception e) {
            return Result.failure("Error al actualizar estado de login: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Result<Boolean> verifyEmail(Long userId, String verificationToken) {
        try {
            // Verificar que el token sea válido
            int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM [user] WHERE id = ? AND email_verification_token = ? " +
                "AND email_verification_expires_at > ?",
                Integer.class,
                userId,
                verificationToken,
                new Timestamp(System.currentTimeMillis())
            );
            
            if (count > 0) {
                jdbcTemplate.update(
                    "UPDATE [user] SET email_verified = TRUE, email_verification_token = NULL, " +
                    "email_verification_expires_at = NULL WHERE id = ?",
                    userId
                );
                return Result.success(true);
            } else {
                return Result.failure("Token de verificación inválido o expirado");
            }
        } catch (Exception e) {
            return Result.failure("Error al verificar email: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Optional<String> generatePasswordResetToken(Long userId, int expirationHours) {
        try {
            String token = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);
            
            int rowsAffected = jdbcTemplate.update(
                "UPDATE [user] SET password_reset_token = ?, password_reset_expires_at = ? WHERE id = ?",
                token,
                Timestamp.valueOf(expiresAt),
                userId
            );
            
            if (rowsAffected > 0) {
                return Optional.of(token);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Result<Boolean> resetPassword(Long userId, String resetToken, String newPasswordHash) {
        try {
            // Verificar que el token sea válido
            int count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM [user] WHERE id = ? AND password_reset_token = ? " +
                "AND password_reset_expires_at > ?",
                Integer.class,
                userId,
                resetToken,
                new Timestamp(System.currentTimeMillis())
            );
            
            if (count > 0) {
                jdbcTemplate.update(
                    "UPDATE [user] SET password = ?, password_reset_token = NULL, " +
                    "password_reset_expires_at = NULL WHERE id = ?",
                    newPasswordHash,
                    userId
                );
                return Result.success(true);
            } else {
                return Result.failure("Token de reseteo inválido o expirado");
            }
        } catch (Exception e) {
            return Result.failure("Error al resetear password: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> searchUsers(String searchTerm, int limit) {
        try {
            String sql = "SELECT * FROM [user] WHERE (first_name LIKE ? OR last_name LIKE ? OR email LIKE ?) LIMIT ?";
            String term = "%" + searchTerm + "%";
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), term, term, term, limit);
        } catch (Exception e) {
            // Log error
            return new ArrayList<>();
        }
    }
    
    // Mapeo de ResultSet a User
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("profile_picture_url"),
            rs.getBoolean("status"),
            rs.getBoolean("email_verified"),
            rs.getString("email_verification_token"),
            rs.getTimestamp("email_verification_expires_at"),
            rs.getString("password_reset_token"),
            rs.getTimestamp("password_reset_expires_at"),
            rs.getTimestamp("last_login_at"),
            rs.getString("last_login_ip"),
            rs.getInt("failed_login_attempts"),
            rs.getTimestamp("account_locked_until"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at"),
            rs.getByte("role_id"),
            rs.getLong("institution_id")
        );
    }
    
    // Mapeo específico para autenticación
    private User mapUserForAuth(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getByte("role_id"),
            rs.getLong("institution_id"),
            rs.getBoolean("status"),
            rs.getBoolean("email_verified"),
            rs.getInt("failed_login_attempts"),
            rs.getTimestamp("account_locked_until"),
            rs.getTimestamp("last_login_at"),
            rs.getString("last_login_ip")
        );
    }
} 