package com.gamified.application.auth.entity.composite;

import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.profiles.GuardianProfile;
import com.gamified.application.auth.entity.core.Role;
import com.gamified.application.auth.entity.core.Institution;
import java.sql.Timestamp;

public class CompleteGuardian {
    private User user;
    private GuardianProfile guardianProfile;

    // Constructor
    public CompleteGuardian(
            Long id, Byte roleId, Long institutionId, String firstName, String lastName,
            String email, String password, String profilePictureUrl, Timestamp createdAt,
            Timestamp updatedAt, Boolean status,
            // User specific security fields
            Boolean emailVerified, String emailVerificationToken, Timestamp emailVerificationExpiresAt,
            String passwordResetToken, Timestamp passwordResetExpiresAt, Timestamp lastLoginAt,
            String lastLoginIp, Integer failedLoginAttempts, Timestamp accountLockedUntil,
            // GuardianProfile specific fields
            Long guardianProfileId, Long guardianUserId, String phone, Timestamp guardianCreatedAt, Timestamp guardianUpdatedAt,
            // Related objects for User (optional, for convenience)
            Role role, Institution institution
    ) {
        this.user = new User(
                id, firstName, lastName, email, password, profilePictureUrl, status,
                emailVerified, emailVerificationToken, emailVerificationExpiresAt,
                passwordResetToken, passwordResetExpiresAt, lastLoginAt, lastLoginIp,
                failedLoginAttempts, accountLockedUntil, createdAt, updatedAt,
                roleId, institutionId
        );
        this.guardianProfile = new GuardianProfile();
        this.guardianProfile.setId(guardianProfileId);
        this.guardianProfile.setUserId(guardianUserId);
        this.guardianProfile.setPhone(phone);
        this.guardianProfile.setCreatedAt(guardianCreatedAt);
        this.guardianProfile.setUpdatedAt(guardianUpdatedAt);

        // Set related objects if provided
        this.user.setRole(role);
        this.user.setInstitution(institution);
    }

    // Getters
    public User getUser() {
        return user;
    }

    public GuardianProfile getGuardianProfile() {
        return guardianProfile;
    }
} 