package com.gamified.application.auth.entity.composite;

import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.profiles.TeacherProfile;
import com.gamified.application.auth.entity.core.Role;
import com.gamified.application.auth.entity.core.Institution;
import java.sql.Timestamp;

public class CompleteTeacher {
    private User user;
    private TeacherProfile teacherProfile;

    // Constructor
    public CompleteTeacher(
            Long id, Byte roleId, Long institutionId, String firstName, String lastName,
            String email, String password, String profilePictureUrl, Timestamp createdAt,
            Timestamp updatedAt, Boolean status,
            // User specific security fields
            Boolean emailVerified, String emailVerificationToken, Timestamp emailVerificationExpiresAt,
            String passwordResetToken, Timestamp passwordResetExpiresAt, Timestamp lastLoginAt,
            String lastLoginIp, Integer failedLoginAttempts, Timestamp accountLockedUntil,
            // TeacherProfile specific fields
            Long teacherProfileId, Long teacherUserId, Boolean teacherEmailVerified, Byte stemAreaId, Timestamp teacherCreatedAt, Timestamp teacherUpdatedAt,
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
        this.teacherProfile = new TeacherProfile();
        this.teacherProfile.setId(teacherProfileId);
        this.teacherProfile.setUserId(teacherUserId);
        this.teacherProfile.setEmailVerified(teacherEmailVerified);
        this.teacherProfile.setStemAreaId(stemAreaId);
        this.teacherProfile.setCreatedAt(teacherCreatedAt);
        this.teacherProfile.setUpdatedAt(teacherUpdatedAt);

        // Set related objects if provided
        this.user.setRole(role);
        this.user.setInstitution(institution);
    }

    // Getters
    public User getUser() {
        return user;
    }

    public TeacherProfile getTeacherProfile() {
        return teacherProfile;
    }
} 