package com.gamified.application.auth.entity.composite;

import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.profiles.StudentProfile;
import com.gamified.application.auth.entity.core.Role;
import com.gamified.application.auth.entity.core.Institution;
import java.sql.Timestamp;
import java.sql.Date;

public class CompleteStudent {
    private User user;
    private StudentProfile studentProfile;

    // Constructor
    public CompleteStudent(
            Long id, Byte roleId, Long institutionId, String firstName, String lastName,
            String email, String password, String profilePictureUrl, Timestamp createdAt,
            Timestamp updatedAt, Boolean status,
            // User specific security fields
            Boolean emailVerified, String emailVerificationToken, Timestamp emailVerificationExpiresAt,
            String passwordResetToken, Timestamp passwordResetExpiresAt, Timestamp lastLoginAt,
            String lastLoginIp, Integer failedLoginAttempts, Timestamp accountLockedUntil,
            // StudentProfile specific fields
            Long studentProfileId, Long studentUserId, Long guardianProfileId,
            String username, Date birth_date, Integer pointsAmount, Timestamp studentCreatedAt, Timestamp studentUpdatedAt,
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
        this.studentProfile = new StudentProfile();
        this.studentProfile.setId(studentProfileId);
        this.studentProfile.setUserId(studentUserId);
        this.studentProfile.setGuardianProfileId(guardianProfileId);
        this.studentProfile.setUsername(username);
        this.studentProfile.setBirthDate(birth_date);
        this.studentProfile.setPointsAmount(pointsAmount);
        this.studentProfile.setCreatedAt(studentCreatedAt);
        this.studentProfile.setUpdatedAt(studentUpdatedAt);
        
        // Set related objects if provided
        this.user.setRole(role);
        this.user.setInstitution(institution);
    }

    // Getters
    public User getUser() {
        return user;
    }

    public StudentProfile getStudentProfile() {
        return studentProfile;
    }
} 