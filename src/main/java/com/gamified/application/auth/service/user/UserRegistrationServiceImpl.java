package com.gamified.application.auth.service.user;

import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.entity.composite.CompleteStudent;
import com.gamified.application.auth.entity.composite.CompleteTeacher;
import com.gamified.application.auth.entity.composite.CompleteGuardian;
import com.gamified.application.auth.entity.core.Institution;
import com.gamified.application.auth.entity.core.Role;
import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.enums.RoleType;
import com.gamified.application.auth.entity.profiles.GuardianProfile;
import com.gamified.application.auth.repository.composite.CompleteUserRepository;
import com.gamified.application.auth.repository.core.InstitutionRepository;
import com.gamified.application.auth.repository.core.RoleRepository;
import com.gamified.application.auth.repository.core.UserRepository;
import com.gamified.application.auth.repository.interfaces.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Implementación del servicio de registro de usuarios
 */
@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {
    
    private final CompleteUserRepository completeUserRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
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
        // Verificar si el email ya está en uso
        if (!isEmailAvailable(guardianRequest.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }
        
        // Obtener la institución
        Optional<Institution> institutionOpt = institutionRepository.findById(guardianRequest.getInstitutionId());
        if (institutionOpt.isEmpty()) {
            throw new IllegalArgumentException("La institución especificada no existe");
        }
        Institution institution = institutionOpt.get();
        
        // Obtener el rol de guardian
        Optional<Role> roleOpt = roleRepository.findByName(RoleType.GUARDIAN.getCode());
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("El rol de guardián no está configurado en el sistema");
        }
        Role role = roleOpt.get();
        
        // Crear usuario base y perfil de guardián
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        // Crear el CompleteGuardian
        CompleteGuardian completeGuardian = new CompleteGuardian(
            null, // id (se asignará al guardar)
            role.getId(), 
            institution.getId(),
            guardianRequest.getFirstName(),
            guardianRequest.getLastName(),
            guardianRequest.getEmail(),
            passwordEncoder.encode(guardianRequest.getPassword()),
            null, // profilePictureUrl
            now, // createdAt
            now, // updatedAt
            true, // status
            false, // emailVerified
            null, // emailVerificationToken
            null, // emailVerificationExpiresAt
            null, // passwordResetToken
            null, // passwordResetExpiresAt
            null, // lastLoginAt
            null, // lastLoginIp
            0, // failedLoginAttempts
            null, // accountLockedUntil
            null, // guardianProfileId (se asignará al guardar)
            null, // guardianUserId (se asignará al guardar)
            guardianRequest.getPhone(),
            now, // guardianCreatedAt
            now, // guardianUpdatedAt
            role, // Role
            institution // Institution
        );
        
        // Guardar el guardian completo
        Result<CompleteGuardian> result = completeUserRepository.createCompleteGuardian(completeGuardian);
        
        if (!result.isSuccess()) {
            throw new RuntimeException("Error al registrar el guardián: " + result.getErrorMessage());
        }
        
        CompleteGuardian savedGuardian = result.getData();
        
        // Mapear a DTO de respuesta
        return mapToGuardianResponseDto(savedGuardian);
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
        return userRepository.findByEmail(email).isEmpty();
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
        User user = completeGuardian.getUser();
        GuardianProfile guardianProfile = completeGuardian.getGuardianProfile();
        
        String institutionName = "Institución no encontrada";
        if (user.getInstitution() != null) {
            institutionName = user.getInstitution().getName();
        } else if (user.getInstitutionId() != null) {
            // Si la institución no está cargada en el usuario, buscarla
            Optional<Institution> institutionOpt = institutionRepository.findById(user.getInstitutionId());
            if (institutionOpt.isPresent()) {
                institutionName = institutionOpt.get().getName();
            }
        }
        
        return UserResponseDto.GuardianResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .guardianProfileId(guardianProfile.getId())
                .phone(guardianProfile.getPhone())
                .roleName(user.getRoleName())
                .institutionName(institutionName)
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(null) // Usuario nuevo, no tiene último login
                .createdAt(user.getCreatedAt() != null ? 
                           user.getCreatedAt().toLocalDateTime() : LocalDateTime.now())
                .studentsCount(0)
                .students(new ArrayList<>())
                .build();
    }
} 