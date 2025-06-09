package com.gamified.application.auth.entity.mapper;

import com.gamified.application.auth.entity.core.Institution;
import com.gamified.application.auth.entity.core.Role;
import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.enums.RoleType;
import com.gamified.application.auth.entity.audit.AuditLog;
import com.gamified.application.auth.entity.audit.LoginHistory;
import com.gamified.application.auth.entity.enums.ActionType;
import com.gamified.application.auth.entity.security.EmailVerification;
import com.gamified.application.auth.entity.security.PasswordHistory;
import com.gamified.application.auth.entity.security.RefreshToken;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Clase utilitaria para mapear ResultSets de stored procedures a entidades CORE
 * Solo incluye mapeo para User, Role e Institution
 */
public interface CoreEntityMapper {

    RowMapper<User> USER_MAPPER = (ResultSet rs, int rowNum) -> new User(
            rs.getLong("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("profile_picture_url"),
            rs.getBoolean("status"),
            rs.getBoolean("email_verified"), // Assuming this field exists based on User entity
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

    RowMapper<Role> ROLE_MAPPER = (ResultSet rs, int rowNum) -> {
        Role role = new Role();
        role.setId(rs.getByte("id"));
        role.setName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        role.setCreatedAt(rs.getTimestamp("created_at"));
        role.setUpdatedAt(rs.getTimestamp("updated_at"));
        return role;
    };

    RowMapper<Institution> INSTITUTION_MAPPER = (ResultSet rs, int rowNum) -> {
        Institution institution = new Institution();
        institution.setId(rs.getLong("id"));
        institution.setName(rs.getString("name"));
        institution.setAddress(rs.getString("address"));
        institution.setCity(rs.getString("city"));
        institution.setState(rs.getString("state"));
        institution.setCountry(rs.getString("country"));
        institution.setPostalCode(rs.getString("postal_code"));
        institution.setPhone(rs.getString("phone"));
        institution.setEmail(rs.getString("email"));
        institution.setWebsite(rs.getString("website"));
        institution.setLogoUrl(rs.getString("logo_url"));
        institution.setStatus(rs.getBoolean("status"));
        institution.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        institution.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return institution;
    };

    /**
     * Mapea un ResultSet a un objeto User (datos básicos)
     */
    static User mapToUserBasic(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getByte("role_id"),
                rs.getLong("institution_id"),
                rs.getBoolean("status"),
                rs.getBoolean("email_verified")
        );
    }

    /**
     * Mapea un ResultSet a un objeto User (datos para autenticación)
     */
    static User mapToUserAuth(ResultSet rs) throws SQLException {
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
                getNullableInt(rs, "failed_login_attempts"),
                toTimestamp(getLocalDateTime(rs, "account_locked_until")),
                toTimestamp(getLocalDateTime(rs, "last_login_at")),
                rs.getString("last_login_ip")
        );
    }

    /**
     * Mapea un ResultSet a un objeto User (datos completos)
     */
    static User mapToUserComplete(ResultSet rs) throws SQLException {
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
                toTimestamp(getLocalDateTime(rs, "email_verification_expires_at")),
                rs.getString("password_reset_token"),
                toTimestamp(getLocalDateTime(rs, "password_reset_expires_at")),
                toTimestamp(getLocalDateTime(rs, "last_login_at")),
                rs.getString("last_login_ip"),
                getNullableInt(rs, "failed_login_attempts"),
                toTimestamp(getLocalDateTime(rs, "account_locked_until")),
                toTimestamp(getLocalDateTime(rs, "created_at")),
                toTimestamp(getLocalDateTime(rs, "updated_at")),
                rs.getByte("role_id"),
                rs.getLong("institution_id")
        );
    }

    /**
     * Mapea un ResultSet a un objeto User con relaciones (Role e Institution)
     */
    static User mapToUserWithRelations(ResultSet rs) throws SQLException {
        User user = mapToUserComplete(rs);

        // Mapear Role si está presente
        if (hasColumn(rs, "role_name")) {
            Role role = new Role();
            role.setId(rs.getByte("role_id")); // role_id in user table is Byte, Role id is Byte
            role.setName(rs.getString("role_name")); // RoleType.fromCode not needed as Role name is String
            role.setDescription(rs.getString("role_description"));
            role.setCreatedAt(toTimestamp(getLocalDateTime(rs, "role_created_at")));
            role.setUpdatedAt(toTimestamp(getLocalDateTime(rs, "role_updated_at")));
            user.setRole(role);
        }

        // Mapear Institution si está presente
        if (hasColumn(rs, "institution_name")) {
            Institution institution = new Institution();
            // Assuming Institution also uses setters if no builder
            institution.setId(rs.getLong("institution_id"));
            institution.setName(rs.getString("institution_name"));
            institution.setAddress(rs.getString("institution_address"));
            institution.setCity(rs.getString("institution_city"));
            institution.setState(rs.getString("institution_state"));
            institution.setCountry(rs.getString("institution_country"));
            institution.setPostalCode(rs.getString("institution_postal_code"));
            institution.setPhone(rs.getString("institution_phone"));
            institution.setEmail(rs.getString("institution_email"));
            institution.setWebsite(rs.getString("institution_website"));
            institution.setLogoUrl(rs.getString("institution_logo_url"));
            institution.setStatus(rs.getBoolean("institution_status"));
            institution.setCreatedAt(getLocalDateTime(rs, "institution_created_at"));
            institution.setUpdatedAt(getLocalDateTime(rs, "institution_updated_at"));
            user.setInstitution(institution);
        }

        return user;
    }

    /**
     * Mapea un ResultSet a un objeto Role
     */
    static Role mapToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setId(rs.getByte("id")); // Role id is Byte
        role.setName(rs.getString("name")); // RoleType.fromCode not needed as Role name is String
        role.setDescription(rs.getString("description"));
        role.setCreatedAt(toTimestamp(getLocalDateTime(rs, "created_at")));
        role.setUpdatedAt(toTimestamp(getLocalDateTime(rs, "updated_at")));
        return role;
    }

    /**
     * Mapea un ResultSet a un objeto Institution (datos básicos)
     */
    static Institution mapToInstitutionBasic(ResultSet rs) throws SQLException {
        return new Institution(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getBoolean("status")
        );
    }

    /**
     * Mapea un ResultSet a un objeto Institution (datos completos)
     */
    static Institution mapToInstitutionComplete(ResultSet rs) throws SQLException {
        // Assuming Institution also uses setters if no builder and has a no-arg constructor
        Institution institution = new Institution();
        institution.setId(rs.getLong("id"));
        institution.setName(rs.getString("name"));
        institution.setAddress(rs.getString("address"));
        institution.setCity(rs.getString("city"));
        institution.setState(rs.getString("state"));
        institution.setCountry(rs.getString("country"));
        institution.setPostalCode(rs.getString("postal_code"));
        institution.setPhone(rs.getString("phone"));
        institution.setEmail(rs.getString("email"));
        institution.setWebsite(rs.getString("website"));
        institution.setLogoUrl(rs.getString("logo_url"));
        institution.setStatus(rs.getBoolean("status"));
        institution.setCreatedAt(getLocalDateTime(rs, "created_at"));
        institution.setUpdatedAt(getLocalDateTime(rs, "updated_at"));
        return institution;
    }

    /**
     * Mapea un ResultSet a un objeto LoginHistory
     */
    static LoginHistory mapToLoginHistory(ResultSet rs) throws SQLException {
        return LoginHistory.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .loginTime(getLocalDateTime(rs, "login_time"))
                .ipAddress(rs.getString("ip_address"))
                .userAgent(rs.getString("user_agent"))
                .success(rs.getBoolean("success"))
                .failureReason(rs.getString("failure_reason"))
                .browser(rs.getString("browser"))
                .operatingSystem(rs.getString("operating_system"))
                .device(rs.getString("device"))
                .sessionDurationMinutes(getNullableInt(rs, "session_duration_minutes"))
                .logoutTime(getLocalDateTime(rs, "logout_time"))
                .build();
    }

    /**
     * Mapea un ResultSet a un objeto AuditLog
     */
    static AuditLog mapToAuditLog(ResultSet rs) throws SQLException {
        return AuditLog.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .actionType(ActionType.fromId(rs.getInt("action_type_id")))
                .entityType(rs.getString("entity_type"))
                .entityId(getNullableLong(rs, "entity_id"))
                .actionDetails(rs.getString("action_details"))
                .ipAddress(rs.getString("ip_address"))
                .userAgent(rs.getString("user_agent"))
                .performedAt(getLocalDateTime(rs, "performed_at"))
                .oldValues(rs.getString("old_values"))
                .newValues(rs.getString("new_values"))
                .description(rs.getString("description"))
                .requestId(rs.getString("request_id"))
                .sessionId(rs.getString("session_id"))
                .isSensitive(rs.getBoolean("is_sensitive"))
                .build();
    }

    /**
     * Mapea un ResultSet a un objeto RefreshToken
     */
    static RefreshToken mapToRefreshToken(ResultSet rs) throws SQLException {
        return RefreshToken.builder()
                .id(rs.getLong("id"))
                .token(rs.getString("token"))
                .userId(rs.getLong("user_id"))
                .expiresAt(toTimestamp(getLocalDateTime(rs, "expires_at")))
                .isRevoked(rs.getBoolean("is_revoked"))
                .revokedAt(getLocalDateTime(rs, "revoked_at"))
                .revokedReason(rs.getString("revoked_reason"))
                .createdAt(toTimestamp(getLocalDateTime(rs, "created_at")))
                .lastUsedAt(getLocalDateTime(rs, "last_used_at"))
                .ipAddress(rs.getString("ip_address"))
                .userAgent(rs.getString("user_agent"))
                .deviceInfo(rs.getString("device_info"))
                .sessionName(rs.getString("session_name"))
                .build();
    }

    /**
     * Mapea un ResultSet a un objeto PasswordHistory
     */
    static PasswordHistory mapToPasswordHistory(ResultSet rs) throws SQLException {
        return new PasswordHistory(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("password"),
                toTimestamp(getLocalDateTime(rs, "created_at")), // Assuming 'created_at' in DB maps to 'changedAt' in entity
                rs.getBoolean("changed_by_admin"),
                rs.getString("ip_address"),
                rs.getString("user_agent")
        );
    }

    /**
     * Mapea un ResultSet a un objeto EmailVerification
     */
    static EmailVerification mapToEmailVerification(ResultSet rs) throws SQLException {
        return new EmailVerification(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("verification_token"),
                toTimestamp(getLocalDateTime(rs, "expires_at")),
                rs.getBoolean("is_verified"),
                toTimestamp(getLocalDateTime(rs, "verified_at")),
                rs.getInt("attempt_count"),
                toTimestamp(getLocalDateTime(rs, "last_attempt_at")),
                toTimestamp(getLocalDateTime(rs, "created_at")),
                rs.getString("ip_address"),
                rs.getString("user_agent")
        );
    }

    // ===================================================================
    // MÉTODOS UTILITARIOS PARA MAPEO
    // ===================================================================

    /**
     * Obtiene un LocalDateTime del ResultSet manejando valores null
     */
    private static LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        var timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    /**
     * Convierte un LocalDateTime a Timestamp, manejando nulls.
     */
    private static Timestamp toTimestamp(LocalDateTime localDateTime) {
        return localDateTime == null ? null : Timestamp.valueOf(localDateTime);
    }

    /**
     * Obtiene un Integer del ResultSet manejando valores null
     */
    private static Integer getNullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Obtiene un Long del ResultSet manejando valores null
     */
    private static Long getNullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Verifica si una columna existe en el ResultSet
     */
    private static boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}