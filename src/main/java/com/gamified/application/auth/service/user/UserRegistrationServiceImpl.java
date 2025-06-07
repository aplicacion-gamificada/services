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
import com.gamified.application.auth.entity.profiles.StudentProfile;
import com.gamified.application.auth.entity.profiles.TeacherProfile;
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
        String email = studentRequest.getEmail();
        
        // Si el email es nulo o vacío, generar un pseudo-email único
        if (email == null || email.trim().isEmpty()) {
            email = "student_" + studentRequest.getUsername() + "_" + System.currentTimeMillis() + "@noemail.internal";
        }
        // Verificar si el email ya está en uso
        else if (!isEmailAvailable(email)) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        // Obtener la institución
        Optional<Institution> institutionOpt = institutionRepository.findById(studentRequest.getInstitutionId());
        if (institutionOpt.isEmpty()) {
            throw new IllegalArgumentException("La institución especificada no existe");
        }
        Institution institution = institutionOpt.get();

        // Obtener el rol de estudiante
        Optional<Role> roleOpt = roleRepository.findByName(RoleType.STUDENT.getCode());
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("El rol de estudiante no está configurado en el sistema");
        }
        Role role = roleOpt.get();

        // Crear usuario base y perfil de estudiante
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        // Crear el CompleteStudent
        CompleteStudent completeStudent = new CompleteStudent(
            null, // id (se asignará al guardar)
            role.getId(),
            institution.getId(),
            studentRequest.getFirstName(),
            studentRequest.getLastName(),
            email, // Email real o generado único
            passwordEncoder.encode(studentRequest.getPassword()),
            null, // profilePictureUrl
            now, // createdAt
            now, // updatedAt
            true, // status
            true, // emailVerified: Por pruebas true
            null, // emailVerificationToken
            null, // emailVerificationExpiresAt
            null, // passwordResetToken
            null, // passwordResetExpiresAt
            null, // lastLoginAt
            null, // lastLoginIp
            null, // failedLoginAttempts
            null, // accountLockedUntil
            null, // studentProfileId (se asignará al guardar)
            null, // studentUserId (se asignará al guardar)
            studentRequest.getGuardianProfileId(), // Aquí asignamos el guardianProfileId que podría ser nulo
            studentRequest.getUsername(),
            studentRequest.getBirth_date(),
            0, // pointsAmount (inicializado en 0)
            now, // studentCreatedAt
            now, // studentUpdatedAt
            role, // Role
            institution // Institution
        );
        
        // Guardar el estudiante completo
        Result<CompleteStudent> result = completeUserRepository.createCompleteStudent(completeStudent);
        
        if (!result.isSuccess()) {
            throw new RuntimeException("Error al registrar el estudiante: " + result.getErrorMessage());
        }
        
        CompleteStudent savedStudent = result.getData();
        
        // Mapear a DTO de respuesta
        return mapToStudentResponseDto(savedStudent);
    }
    
    @Override
    public UserResponseDto.TeacherResponseDto registerTeacher(UserRequestDto.TeacherRegistrationRequestDto teacherRequest) {
        // Verificar si el email ya está en uso
        if (!isEmailAvailable(teacherRequest.getEmail())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }

        // Obtener la institución
        Optional<Institution> institutionOpt = institutionRepository.findById(teacherRequest.getInstitutionId());
        if (institutionOpt.isEmpty()) {
            throw new IllegalArgumentException("La institución especificada no existe");
        }
        Institution institution = institutionOpt.get();

        // Obtener el rol de profesor
        Optional<Role> roleOpt = roleRepository.findByName(RoleType.TEACHER.getCode());
        if (roleOpt.isEmpty()) {
            throw new IllegalArgumentException("El rol de profesor no está configurado en el sistema");
        }
        Role role = roleOpt.get();

        // Crear usuario base y perfil de profesor
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        // Crear el CompleteTeacher
        CompleteTeacher completeTeacher = new CompleteTeacher(
            null, // id (se asignará al guardar)
            role.getId(),
            institution.getId(),
            teacherRequest.getFirstName(),
            teacherRequest.getLastName(),
            teacherRequest.getEmail(),
            passwordEncoder.encode(teacherRequest.getPassword()),
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
            null, // failedLoginAttempts
            null, // accountLockedUntil
            null, // teacherProfileId (se asignará al guardar)
            null, // teacherUserId (se asignará al guardar)
            null, // classroomId (se asignará al guardar)
            teacherRequest.getStemAreaId(),
            now, // teacherCreatedAt
            now, // teacherUpdatedAt
            role, // Role
            institution // Institution
        );
        
        // Guardar el profesor completo
        Result<CompleteTeacher> result = completeUserRepository.createCompleteTeacher(completeTeacher);
        
        if (!result.isSuccess()) {
            throw new RuntimeException("Error al registrar el profesor: " + result.getErrorMessage());
        }

        CompleteTeacher savedTeacher = result.getData();

        // Mapear a DTO de respuesta
        return mapToTeacherResponseDto(savedTeacher);
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
        // Si el email es nulo o vacío, verificar si ya existe un usuario con email nulo
        if (email == null || email.trim().isEmpty()) {
            // Aquí deberíamos verificar si ya existe un usuario con email NULL
            // Como esto podría requerir un query especial que no está disponible en UserRepository,
            // lo manejamos como un caso especial generando un email único basado en un timestamp
            return true;
        }
        return userRepository.findByEmail(email).isEmpty();
    }
    
    @Override
    public boolean isUsernameAvailable(String username) {
        // Implementación temporal para pruebas
        return true;
    }
    
    @Override
    public User findUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> 
            new IllegalArgumentException("Usuario no encontrado con ID: " + id)
        );
    }
    
    @Override
    public UserResponseDto.StudentResponseDto mapToStudentResponseDto(CompleteStudent completeStudent) {
        User user = completeStudent.getUser();
        StudentProfile studentProfile = completeStudent.getStudentProfile();
        
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
        
        return UserResponseDto.StudentResponseDto.builder()
                // Información básica del usuario
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
        
                // Información específica del estudiante
                .username(studentProfile.getUsername())
                .birth_date(studentProfile.getBirthDate())
                .pointsAmount(0) //Aqui seria 0, se debe implementar en el constructor de CompleteStudent
        
                // Información de institución y rol
                .roleName(user.getRoleName())
                .institutionName(institutionName)
        
                // Estado de la cuenta
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt() != null ? 
                           user.getCreatedAt().toLocalDateTime() : LocalDateTime.now())
                .recentAchievements(new ArrayList<>())
                .build();
    }
    
    @Override
    public UserResponseDto.TeacherResponseDto mapToTeacherResponseDto(CompleteTeacher completeTeacher) {
        User user = completeTeacher.getUser();
        TeacherProfile teacherProfile = completeTeacher.getTeacherProfile();

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

        return UserResponseDto.TeacherResponseDto.builder()
                // Información básica del usuario
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
        
                // Información específica del profesor
                .teacherProfileId(teacherProfile.getId())
                .stemAreaId(teacherProfile.getStemAreaId())
                .stemAreaName("STEM Area")//No implementado, se necesita crear un endpoint para obtener el nombre de la área STEM y un repositorio para la tabla de áreas STEM
        
                // Información de institución y rol
                .roleName(user.getRoleName())
                .institutionName(institutionName)
        
                // Estado de la cuenta
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt() != null ? 
                           user.getCreatedAt().toLocalDateTime() : LocalDateTime.now())
                
                // Implementación temporal para pruebas
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
                // Información básica del usuario
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
        
                // Información específica del tutor
                .guardianProfileId(guardianProfile.getId())
                .phone(guardianProfile.getPhone())
        
                // Información de institución y rol
                .roleName(user.getRoleName())
                .institutionName(institutionName)
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(null) // Usuario nuevo, no tiene último login
                .createdAt(user.getCreatedAt() != null ? 
                           user.getCreatedAt().toLocalDateTime() : LocalDateTime.now())

                // Información de estudiantes asociados
                .studentsCount(0)
                .students(new ArrayList<>())
                .build();
    }
}