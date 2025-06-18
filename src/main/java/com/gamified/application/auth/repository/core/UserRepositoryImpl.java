package com.gamified.application.auth.repository.core;

import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.core.Role;
import com.gamified.application.auth.repository.interfaces.Result;
import com.gamified.application.auth.util.DatabaseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del repositorio de usuarios con JDBC
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("user_id", id, Types.BIGINT);

            String sql = "EXEC sp_get_complete_user_by_id @user_id = :user_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            Map<String, Object> userData = results.getFirst();
            
            // Mapear datos básicos del usuario
            User user = mapUserFromResultMap(userData);
            
            return Optional.of(user);
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
        System.out.println("UserRepository.findByEmail - Starting search for email: " + email);
        
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("email", email, Types.VARCHAR);

            String sql = "EXEC sp_get_user_by_email @email = :email";
            
            System.out.println("UserRepository.findByEmail - Executing SP: " + sql);
            System.out.println("UserRepository.findByEmail - Parameters: email=" + email);
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            System.out.println("UserRepository.findByEmail - SP Results count: " + results.size());
            
            if (results.isEmpty()) {
                System.out.println("UserRepository.findByEmail - No results found, returning empty");
                return Optional.empty();
            }
            
            Map<String, Object> userData = results.getFirst();
            System.out.println("UserRepository.findByEmail - Found user data: " + userData);
            
            // Mapear datos del usuario según la estructura del sp_get_user_by_email corregido
            Long id = ((Number) userData.get("id")).longValue();
            String firstName = (String) userData.get("first_name");
            String lastName = (String) userData.get("last_name");
            String password = (String) userData.get("password");
            Byte roleId = ((Number) userData.get("role_id")).byteValue();
            String roleName = (String) userData.get("role_name");
            
            // Manejar campos boolean que vienen como Integer desde SQL Server
            Boolean status = DatabaseUtils.safeToBoolean(userData.get("status"));
            Boolean emailVerified = DatabaseUtils.safeToBoolean(userData.get("email_verified"));
            Long institutionId = userData.get("institution_id") != null ? ((Number) userData.get("institution_id")).longValue() : null;
            
            System.out.println("UserRepository.findByEmail - Mapped user: id=" + id + ", firstName=" + firstName + 
                              ", lastName=" + lastName + ", roleId=" + roleId + ", roleName=" + roleName + 
                              ", status=" + status + ", emailVerified=" + emailVerified);
            
            // Crear usuario con datos básicos
            User user = new User(
                id, firstName, lastName, email,
                roleId, institutionId, status, emailVerified
            );
            
            // Establecer password si está presente
            if (password != null) {
                user.setPassword(password);
            }
            
            // Establecer datos adicionales si están presentes
            if (userData.get("last_login_at") != null) {
                user.setLastLoginAt((Timestamp) userData.get("last_login_at"));
            }
            
            if (userData.get("created_at") != null) {
                user.setCreatedAt((Timestamp) userData.get("created_at"));
            }
            
            if (userData.get("updated_at") != null) {
                user.setUpdatedAt((Timestamp) userData.get("updated_at"));
            }
            
            if (userData.get("profile_picture_url") != null) {
                user.setProfilePictureUrl((String) userData.get("profile_picture_url"));
            }
            
            if (userData.get("last_login_ip") != null) {
                user.setLastLoginIp((String) userData.get("last_login_ip"));
            }
            
            // Establecer el rol si está presente el nombre del rol
            if (roleName != null) {
                Role role = new Role();
                role.setId(roleId);
                role.setName(roleName);
                user.setRole(role);
            }
            
            System.out.println("UserRepository.findByEmail - Successfully created User object, returning");
            return Optional.of(user);
            
        } catch (Exception e) {
            System.out.println("UserRepository.findByEmail - Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findForAuthentication(String email) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("email", email, Types.VARCHAR);
            parameters.addValue("password", "", Types.VARCHAR); // Password se verifica en la capa de servicio con BCrypt

            String sql = "EXEC sp_authenticate_user_complete @email = :email, @password = :password";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            Map<String, Object> result = results.getFirst();
            
            // Verificar si la autenticación fue exitosa
            Integer success = (Integer) result.get("success");
            if (success != 1) {
                return Optional.empty();
            }
            
            // Obtener datos del usuario
            Long userId = ((Number) result.get("user_id")).longValue();
            String passwordHash = (String) result.get("password_hash");
            Byte roleId = ((Number) result.get("role_id")).byteValue();
            
            // Crear usuario con constructor para autenticación
            // Usamos el constructor mínimo y luego completamos los datos necesarios
            User user = new User(userId, null, null, email, passwordHash, roleId, null, true, false, 0, null, null, null);
            
            return Optional.of(user);
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Result<Boolean> updateLoginStatus(Long userId, boolean successful, String ipAddress) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("user_id", userId, Types.BIGINT);
            parameters.addValue("success", successful ? 1 : 0, Types.BIT);
            parameters.addValue("ip_address", ipAddress, Types.VARCHAR);
            parameters.addValue("user_agent", "Web Application", Types.VARCHAR); // Podría pasarse como parámetro adicional
            parameters.addValue("login_time", new Timestamp(System.currentTimeMillis()), Types.TIMESTAMP);

            String sql = "EXEC sp_record_login_attempt @user_id = :user_id, @success = :success, " +
                         "@ip_address = :ip_address, @user_agent = :user_agent, @login_time = :login_time";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Result.failure("No se pudo actualizar el estado de login");
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
            DatabaseUtils.safeToBoolean(rs.getObject("status")),
            DatabaseUtils.safeToBoolean(rs.getObject("email_verified")),
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
            DatabaseUtils.safeToBoolean(rs.getObject("status")),
            DatabaseUtils.safeToBoolean(rs.getObject("email_verified")),
            rs.getInt("failed_login_attempts"),
            rs.getTimestamp("account_locked_until"),
            rs.getTimestamp("last_login_at"),
            rs.getString("last_login_ip")
        );
    }

    /**
     * Mapea un Map<String, Object> a un objeto User
     */
    private User mapUserFromResultMap(Map<String, Object> userData) {
        Long id = ((Number) userData.get("id")).longValue();
        String firstName = (String) userData.get("first_name");
        String lastName = (String) userData.get("last_name");
        String email = (String) userData.get("email");
        Byte roleId = ((Number) userData.get("role_id")).byteValue();
        Long institutionId = ((Number) userData.get("institution_id")).longValue();
        Boolean status = DatabaseUtils.safeToBoolean(userData.get("status"));
        Boolean emailVerified = DatabaseUtils.safeToBoolean(userData.get("email_verified"));
        
        // Crear usuario con constructor básico
        User user = new User(
            id, firstName, lastName, email,
            roleId, institutionId, status, emailVerified
        );
        
        // Establecer datos adicionales si están presentes
        if (userData.get("profile_picture_url") != null) {
            user.setProfilePictureUrl((String) userData.get("profile_picture_url"));
        }
        
        if (userData.get("last_login_at") != null) {
            user.setLastLoginAt((Timestamp) userData.get("last_login_at"));
        }
        
        if (userData.get("created_at") != null) {
            user.setCreatedAt((Timestamp) userData.get("created_at"));
        }
        
        if (userData.get("updated_at") != null) {
            user.setUpdatedAt((Timestamp) userData.get("updated_at"));
        }
        
        return user;
    }
} 