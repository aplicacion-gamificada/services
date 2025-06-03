package com.gamified.application.auth.service.user;

import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.entity.enums.RoleType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de perfiles de usuario
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {
    
    @Override
    public UserResponseDto.StudentResponseDto getStudentProfile(Long userId) {
        // Implementación temporal para pruebas
        return UserResponseDto.StudentResponseDto.builder()
                .id(userId)
                .firstName("Estudiante")
                .lastName("Demo")
                .fullName("Estudiante Demo")
                .email("estudiante@demo.com")
                .profilePictureUrl("https://example.com/avatar.jpg")
                .studentProfileId(1L)
                .username("estudiante_demo")
                .pointsAmount(100)
                .roleName(RoleType.STUDENT.getCode())
                .institutionName("Institución Demo")
                .status(true)
                .emailVerified(true)
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusMonths(1))
                .level(2)
                .recentAchievements(new ArrayList<>())
                .build();
    }
    
    @Override
    public UserResponseDto.TeacherResponseDto getTeacherProfile(Long userId) {
        // Implementación temporal para pruebas
        return UserResponseDto.TeacherResponseDto.builder()
                .id(userId)
                .firstName("Profesor")
                .lastName("Demo")
                .fullName("Profesor Demo")
                .email("profesor@demo.com")
                .profilePictureUrl("https://example.com/avatar.jpg")
                .teacherProfileId(1L)
                .stemAreaId((byte) 1)
                .stemAreaName("Matemáticas")
                .roleName(RoleType.TEACHER.getCode())
                .institutionName("Institución Demo")
                .status(true)
                .emailVerified(true)
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusMonths(1))
                .classroomsCount(3)
                .studentsCount(50)
                .build();
    }
    
    @Override
    public UserResponseDto.GuardianResponseDto getGuardianProfile(Long userId) {
        // Implementación temporal para pruebas
        return UserResponseDto.GuardianResponseDto.builder()
                .id(userId)
                .firstName("Tutor")
                .lastName("Demo")
                .fullName("Tutor Demo")
                .email("tutor@demo.com")
                .profilePictureUrl("https://example.com/avatar.jpg")
                .guardianProfileId(1L)
                .phone("123456789")
                .roleName(RoleType.GUARDIAN.getCode())
                .institutionName("Institución Demo")
                .status(true)
                .emailVerified(true)
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusMonths(1))
                .studentsCount(2)
                .students(new ArrayList<>())
                .build();
    }
    
    @Override
    public UserResponseDto.StudentResponseDto updateStudentProfile(Long userId, UserRequestDto.StudentUpdateRequestDto updateRequest) {
        // Implementación temporal para pruebas
        return getStudentProfile(userId);
    }
    
    @Override
    public UserResponseDto.TeacherResponseDto updateTeacherProfile(Long userId, UserRequestDto.TeacherUpdateRequestDto updateRequest) {
        // Implementación temporal para pruebas
        return getTeacherProfile(userId);
    }
    
    @Override
    public UserResponseDto.GuardianResponseDto updateGuardianProfile(Long userId, UserRequestDto.GuardianUpdateRequestDto updateRequest) {
        // Implementación temporal para pruebas
        return getGuardianProfile(userId);
    }
    
    @Override
    public CommonResponseDto updateProfilePicture(Long userId, String profilePictureUrl) {
        // Implementación temporal para pruebas
        return CommonResponseDto.builder()
                .success(true)
                .message("Foto de perfil actualizada exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public CommonResponseDto updatePassword(Long userId, UserRequestDto.PasswordUpdateRequestDto updateRequest) {
        // Implementación temporal para pruebas
        return CommonResponseDto.builder()
                .success(true)
                .message("Contraseña actualizada exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public CommonResponseDto deactivateAccount(Long userId) {
        // Implementación temporal para pruebas
        return CommonResponseDto.builder()
                .success(true)
                .message("Cuenta desactivada exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public List<UserResponseDto.StudentResponseDto> getStudentsByGuardian(Long guardianUserId) {
        // Implementación temporal para pruebas
        List<UserResponseDto.StudentResponseDto> students = new ArrayList<>();
        students.add(getStudentProfile(1L));
        students.add(getStudentProfile(2L));
        return students;
    }
    
    @Override
    public List<UserResponseDto.BasicUserResponseDto> searchUsers(String searchTerm, String roleFilter, int limit) {
        // Implementación temporal para pruebas
        List<UserResponseDto.BasicUserResponseDto> users = new ArrayList<>();
        users.add(UserResponseDto.BasicUserResponseDto.builder()
                .id(1L)
                .firstName("Usuario")
                .lastName("Uno")
                .fullName("Usuario Uno")
                .email("usuario1@demo.com")
                .profilePictureUrl("https://example.com/avatar1.jpg")
                .roleName(RoleType.STUDENT.getCode())
                .status(true)
                .lastLoginAt(LocalDateTime.now().minusDays(1))
                .createdAt(LocalDateTime.now().minusMonths(1))
                .build());
        
        users.add(UserResponseDto.BasicUserResponseDto.builder()
                .id(2L)
                .firstName("Usuario")
                .lastName("Dos")
                .fullName("Usuario Dos")
                .email("usuario2@demo.com")
                .profilePictureUrl("https://example.com/avatar2.jpg")
                .roleName(RoleType.TEACHER.getCode())
                .status(true)
                .lastLoginAt(LocalDateTime.now().minusDays(2))
                .createdAt(LocalDateTime.now().minusMonths(2))
                .build());
        
        return users;
    }
} 