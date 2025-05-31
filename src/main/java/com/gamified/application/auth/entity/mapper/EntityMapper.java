package com.gamified.application.auth.entity.mapper;

import com.gamified.application.auth.entity.*;
import com.gamified.application.auth.entity.audit.AuditLog;
import com.gamified.application.auth.entity.audit.LoginHistory;
import com.gamified.application.auth.entity.enums.ActionType;
import com.gamified.application.auth.entity.enums.RoleType;
import com.gamified.application.auth.entity.security.EmailVerification;
import com.gamified.application.auth.entity.security.PasswordHistory;
import com.gamified.application.auth.entity.security.RefreshToken;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Clase utilitaria para mapear ResultSets de stored procedures a entidades
 */
public class EntityMapper {

    /**
     * Mapea un ResultSet a un objeto User
     */
    public static User mapToUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("email"))
                .password(rs.getString("password"))
                .profilePictureUrl(rs.getString("profile_picture_url"))
                .status(rs.getBoolean("status"))
                .emailVerified(rs.getBoolean("email_verified"))
                .emailVerificationToken(rs.getString("email_verification_token"))
                .emailVerificationExpiresAt(getLocalDateTime(rs, "email_verification_expires_at"))
                .passwordResetToken(rs.getString("password_reset_token"))
                .passwordResetExpiresAt(getLocalDateTime(rs, "password_reset_expires_at"))
                .lastLoginAt(getLocalDateTime(rs, "last_login_at"))
                .lastLoginIp(rs.getString("last_login_ip"))
                .failedLoginAttempts(rs.getInt("failed_login_attempts"))
                .accountLockedUntil(getLocalDateTime(rs, "account_locked_until"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .updatedAt(getLocalDateTime(rs, "updated_at"))
                .roleId(rs.getLong("role_id"))
                .institutionId(rs.getLong("institution_id"))
                .build();
    }

    /**
     * Mapea un ResultSet a un objeto User con información de Role e Institution
     */
    public static User mapToUserWithRelations(ResultSet rs) throws SQLException {
        User user = mapToUser(rs);

        // Mapear Role si está presente
        if (hasColumn(rs, "role_name")) {
            Role role = Role.builder()
                    .id(rs.getLong("role_id"))
                    .name(RoleType.fromCode(rs.getString("role_name")))
                    .description(rs.getString("role_description"))
                    .build();
            user.setRole(role);
        }

        // Mapear Institution si está presente
        if (hasColumn(rs, "institution_name")) {
            Institution institution = Institution.builder()
                    .id(rs.getLong("institution_id"))
                    .name(rs.getString("institution_name"))
                    .address(rs.getString("institution_address"))
                    .city(rs.getString("institution_city"))
                    .state(rs.getString("institution_state"))
                    .country(rs.getString("institution_country"))
                    .phone(rs.getString("institution_phone"))
                    .email(rs.getString("institution_email"))
                    .status(rs.getBoolean("institution_status"))
                    .build();
            user.setInstitution(institution);
        }

        return user;
    }

    /**
     * Mapea un ResultSet a un objeto Role
     */
    public static Role mapToRole(ResultSet rs) throws SQLException {
        return Role.builder()
                .id(rs.getLong("id"))
                .name(RoleType.fromCode(rs.getString("name")))
                .description(rs.getString("description"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .updatedAt(getLocalDateTime(rs, "updated_at"))
                .build();
    }

    /**
     * Mapea un ResultSet a un objeto Institution
     */
    public static Institution mapToInstitution(ResultSet rs) throws SQLException {
        return Institution.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .address(rs.getString("address"))
                .city(rs.getString("city"))
                .state(rs.getString("state"))
                .country(rs.getString("country"))
                .postalCode(rs.getString("postal_code"))
                .phone(rs.getString("phone"))
                .email(rs.getString("email"))
                .website(rs.getString("website"))
                .logoUrl(rs.getString("logo_url"))
                .status(rs.getBoolean("status"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .updatedAt(getLocalDateTime(rs, "updated_at"))
                .build();
    }

    /**
     * Mapea un ResultSet a un objeto LoginHistory
     */
    public static LoginHistory mapToLoginHistory(ResultSet rs) throws SQLException {
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
    public static AuditLog mapToAuditLog(ResultSet rs) throws SQLException {
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
    public static RefreshToken mapToRefreshToken(ResultSet rs) throws SQLException {
        return RefreshToken.builder()
                .id(rs.getLong("id"))
                .token(rs.getString("token"))
                .userId(rs.getLong("user_id"))
                .expiresAt(getLocalDateTime(rs, "expires_at"))
                .isRevoked(rs.getBoolean("is_revoked"))
                .revokedAt(getLocalDateTime(rs, "revoked_at"))
                .revokedReason(rs.getString("revoked_reason"))
                .createdAt(getLocalDateTime(rs, "created_at"))
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
    public static PasswordHistory mapToPasswordHistory(ResultSet rs) throws SQLException {
        return PasswordHistory.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .passwordHash(rs.getString("password_hash"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .changedByAdmin(rs.getBoolean("changed_by_admin"))
                .ipAddress(rs.getString("ip_address"))
                .userAgent(rs.getString("user_agent"))
                .build();
    }

    /**
     * Mapea un ResultSet a un objeto EmailVerification
     */
    public static EmailVerification mapToEmailVerification(ResultSet rs) throws SQLException {
        return EmailVerification.builder()
                .id(rs.getLong("id"))
                .userId(rs.getLong("user_id"))
                .email(rs.getString("email"))
                .verificationToken(rs.getString("verification_token"))
                .expiresAt(getLocalDateTime(rs, "expires_at"))
                .isVerified(rs.getBoolean("is_verified"))
                .verifiedAt(getLocalDateTime(rs, "verified_at"))
                .attemptCount(rs.getInt("attempt_count"))
                .lastAttemptAt(getLocalDateTime(rs, "last_attempt_at"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .ipAddress(rs.getString("ip_address"))
                .userAgent(rs.getString("user_agent"))
                .build();
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