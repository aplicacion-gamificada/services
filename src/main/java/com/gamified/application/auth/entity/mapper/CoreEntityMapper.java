package com.gamified.application.auth.entity.mapper;


import com.gamified.application.auth.entity.*;
import com.gamified.application.auth.entity.enums.RoleType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Clase utilitaria para mapear ResultSets de stored procedures a entidades CORE
 * Solo incluye mapeo para User, Role e Institution
 */
public class CoreEntityMapper {

    /**
     * Mapea un ResultSet a un objeto User (datos básicos)
     */
    public static User mapToUserBasic(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getLong("role_id"),
                rs.getLong("institution_id"),
                rs.getBoolean("status"),
                rs.getBoolean("email_verified")
        );
    }

    /**
     * Mapea un ResultSet a un objeto User (datos para autenticación)
     */
    public static User mapToUserAuth(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getLong("role_id"),
                rs.getLong("institution_id"),
                rs.getBoolean("status"),
                rs.getBoolean("email_verified"),
                getNullableInt(rs, "failed_login_attempts"),
                getLocalDateTime(rs, "account_locked_until"),
                getLocalDateTime(rs, "last_login_at"),
                rs.getString("last_login_ip")
        );
    }

    /**
     * Mapea un ResultSet a un objeto User (datos completos)
     */
    public static User mapToUserComplete(ResultSet rs) throws SQLException {
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
                .failedLoginAttempts(getNullableInt(rs, "failed_login_attempts"))
                .accountLockedUntil(getLocalDateTime(rs, "account_locked_until"))
                .createdAt(getLocalDateTime(rs, "created_at"))
                .updatedAt(getLocalDateTime(rs, "updated_at"))
                .roleId(rs.getLong("role_id"))
                .institutionId(rs.getLong("institution_id"))
                .build();
    }

    /**
     * Mapea un ResultSet a un objeto User con relaciones (Role e Institution)
     */
    public static User mapToUserWithRelations(ResultSet rs) throws SQLException {
        User user = mapToUserComplete(rs);

        // Mapear Role si está presente
        if (hasColumn(rs, "role_name")) {
            Role role = Role.builder()
                    .id(rs.getLong("role_id"))
                    .name(RoleType.fromCode(rs.getString("role_name")))
                    .description(rs.getString("role_description"))
                    .createdAt(getLocalDateTime(rs, "role_created_at"))
                    .updatedAt(getLocalDateTime(rs, "role_updated_at"))
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
                    .postalCode(rs.getString("institution_postal_code"))
                    .phone(rs.getString("institution_phone"))
                    .email(rs.getString("institution_email"))
                    .website(rs.getString("institution_website"))
                    .logoUrl(rs.getString("institution_logo_url"))
                    .status(rs.getBoolean("institution_status"))
                    .createdAt(getLocalDateTime(rs, "institution_created_at"))
                    .updatedAt(getLocalDateTime(rs, "institution_updated_at"))
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
     * Mapea un ResultSet a un objeto Institution (datos básicos)
     */
    public static Institution mapToInstitutionBasic(ResultSet rs) throws SQLException {
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
    public static Institution mapToInstitutionComplete(ResultSet rs) throws SQLException {
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