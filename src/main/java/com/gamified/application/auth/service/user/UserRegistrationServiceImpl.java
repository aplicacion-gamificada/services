package com.gamified.application.auth.service.user;

import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.entity.composite.CompleteStudent;
import com.gamified.application.auth.entity.composite.CompleteTeacher;
import com.gamified.application.auth.entity.composite.CompleteGuardian;
import com.gamified.application.auth.entity.enums.RoleType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Implementación del servicio de registro de usuarios
 */
@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {
    
    @Override
    public UserResponseDto.StudentResponseDto registerStudent(UserRequestDto.StudentRegistrationRequestDto studentRequest) {
        // Implementación temporal para pruebas
        return UserResponseDto.StudentResponseDto.builder()
                .id(1L)
                .firstName(studentRequest.getFirstName())
                .lastName(studentRequest.getLastName())
                .fullName(studentRequest.getFirstName() + " " + studentRequest.getLastName())
                .email(studentRequest.getEmail())
                .studentProfileId(1L)
                .username(studentRequest.getUsername())
                .birth_date(studentRequest.getBirth_date())
                .pointsAmount(0)
                .roleName(RoleType.STUDENT.getCode())
                .institutionName("Institución Demo")
                .status(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .recentAchievements(new ArrayList<>())
                .build();
    }
    
    @Override
    public UserResponseDto.TeacherResponseDto registerTeacher(UserRequestDto.TeacherRegistrationRequestDto teacherRequest) {
        // Implementación temporal para pruebas
        return UserResponseDto.TeacherResponseDto.builder()
                .id(1L)
                .firstName(teacherRequest.getFirstName())
                .lastName(teacherRequest.getLastName())
                .fullName(teacherRequest.getFirstName() + " " + teacherRequest.getLastName())
                .email(teacherRequest.getEmail())
                .teacherProfileId(1L)
                .stemAreaId(teacherRequest.getStemAreaId())
                .stemAreaName("Área STEM Demo")
                .roleName(RoleType.TEACHER.getCode())
                .institutionName("Institución Demo")
                .status(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .classroomsCount(0)
                .studentsCount(0)
                .build();
    }
    
    @Override
    public UserResponseDto.GuardianResponseDto registerGuardian(UserRequestDto.GuardianRegistrationRequestDto guardianRequest) {
        // Implementación temporal para pruebas
        return UserResponseDto.GuardianResponseDto.builder()
                .id(1L)
                .firstName(guardianRequest.getFirstName())
                .lastName(guardianRequest.getLastName())
                .fullName(guardianRequest.getFirstName() + " " + guardianRequest.getLastName())
                .email(guardianRequest.getEmail())
                .guardianProfileId(1L)
                .phone("123456789")
                .roleName(RoleType.GUARDIAN.getCode())
                .institutionName("Institución Demo")
                .status(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .studentsCount(0)
                .students(new ArrayList<>())
                .build();
    }
    
    @Override
    public CommonResponseDto associateStudentToGuardian(UserRequestDto.StudentGuardianAssociationRequestDto associationRequest) {
        // Implementación temporal para pruebas
        return CommonResponseDto.builder()
                .success(true)
                .message("Asociación simulada exitosa")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public boolean isEmailAvailable(String email) {
        // Implementación temporal para pruebas
        return true;
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        // Implementación temporal para pruebas
        return true;
    }
    
    @Override
    public UserResponseDto.StudentResponseDto mapToStudentResponseDto(CompleteStudent completeStudent) {
        // Implementación temporal para pruebas
        return UserResponseDto.StudentResponseDto.builder()
                .id(1L)
                .firstName("Estudiante")
                .lastName("Demo")
                .fullName("Estudiante Demo")
                .email("estudiante@demo.com")
                .studentProfileId(1L)
                .username("estudiante_demo")
                .pointsAmount(0)
                .roleName(RoleType.STUDENT.getCode())
                .institutionName("Institución Demo")
                .status(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .recentAchievements(new ArrayList<>())
                .build();
    }
    
    @Override
    public UserResponseDto.TeacherResponseDto mapToTeacherResponseDto(CompleteTeacher completeTeacher) {
        // Implementación temporal para pruebas
        return UserResponseDto.TeacherResponseDto.builder()
                .id(1L)
                .firstName("Profesor")
                .lastName("Demo")
                .fullName("Profesor Demo")
                .email("profesor@demo.com")
                .teacherProfileId(1L)
                .stemAreaId((byte) 1)
                .stemAreaName("Área STEM Demo")
                .roleName(RoleType.TEACHER.getCode())
                .institutionName("Institución Demo")
                .status(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .classroomsCount(0)
                .studentsCount(0)
                .build();
    }
    
    @Override
    public UserResponseDto.GuardianResponseDto mapToGuardianResponseDto(CompleteGuardian completeGuardian) {
        // Implementación temporal para pruebas
        return UserResponseDto.GuardianResponseDto.builder()
                .id(1L)
                .firstName("Tutor")
                .lastName("Demo")
                .fullName("Tutor Demo")
                .email("tutor@demo.com")
                .guardianProfileId(1L)
                .phone("123456789")
                .roleName(RoleType.GUARDIAN.getCode())
                .institutionName("Institución Demo")
                .status(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .studentsCount(0)
                .students(new ArrayList<>())
                .build();
    }
} 