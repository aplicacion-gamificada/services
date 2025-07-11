package com.gamified.application.clasroom.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs para responses del módulo classroom
 */
public class ClassroomResponseDto {

    /**
     * DTO básico de respuesta para un aula
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomDto {
        private Integer classroomId;
        private String name;
        private String grade;
        private String section;
        private String year;
        private Integer enrolledStudentsCount;
        private Boolean status;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
    }

    /**
     * DTO detallado de un aula con sus estudiantes
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomDetailDto {
        private Integer classroomId;
        private String name;
        private String grade;
        private String section;
        private String year;
        private Integer enrolledStudentsCount;
        private Boolean status;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        
        private List<StudentInClassroomDto> students;
    }

    /**
     * DTO para estudiante dentro de un aula
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentInClassroomDto {
        private Integer studentProfileId;
        private Integer userId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String username;
        private String email;
        private Integer pointsAmount;
        private String profilePictureUrl;
        private Boolean status;
        private Boolean emailVerified;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime enrolledAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;
        
        // Información opcional del guardian
        private GuardianInfoDto guardian;
    }

    /**
     * DTO para información del guardian
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuardianInfoDto {
        private Integer guardianProfileId;
        private Integer userId;
        private String firstName;
        private String lastName;
        private String fullName;
        private String email;
        private String phone;
        private String profilePictureUrl;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginAt;
    }

    /**
     * DTO para respuesta de inscripción
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnrollmentResponseDto {
        private Integer enrollmentId;
        private Integer classroomId;
        private Integer studentProfileId;
        private String studentName;
        private String classroomName;
        private Boolean success;
        private String message;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime enrolledAt;
    }

    /**
     * DTO para estadísticas del aula
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomStatsDto {
        private Integer classroomId;
        private String classroomName;
        private Integer totalStudents;
        private Integer activeStudents;
        private Integer totalExercisesCompleted;
        private Double averageProgress;
        private Integer totalPointsEarned;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastActivity;
    }
} 