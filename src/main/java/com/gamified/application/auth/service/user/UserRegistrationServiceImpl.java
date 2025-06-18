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
import com.gamified.application.auth.exception.EmailAlreadyExistsException;
import com.gamified.application.auth.repository.composite.CompleteUserRepository;
import com.gamified.application.auth.repository.core.InstitutionRepository;
import com.gamified.application.auth.repository.core.RoleRepository;
import com.gamified.application.auth.repository.core.UserRepository;
import com.gamified.application.auth.repository.interfaces.Result;
import com.gamified.application.auth.util.DatabaseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Implementación del servicio de registro de usuarios
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserRegistrationServiceImpl implements UserRegistrationService {
    
    private final CompleteUserRepository completeUserRepository;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public UserResponseDto.StudentResponseDto registerStudent(UserRequestDto.StudentRegistrationRequestDto studentRequest) {
        String email = studentRequest.getEmail();
        
        // Si el email es nulo o vacío, generar un pseudo-email único
        if (email == null || email.trim().isEmpty()) {
            email = "student_" + studentRequest.getUsername() + "_" + System.currentTimeMillis() + "@noemail.internal";
        }
        // Verificar si el email ya está en uso
        else if (!isEmailAvailable(email)) {
            throw new EmailAlreadyExistsException("El email " + email + " ya está registrado en el sistema");
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
        
        // Asegurarse de que el roleId es un Byte
        Byte roleId = role.getId();
        
        // Imprimir información detallada del rol
        System.out.println("DEBUG - Role details: ID=" + roleId + 
                           " (type: " + roleId.getClass().getName() + ")" + 
                           ", Name=" + role.getName() + 
                           ", Code=" + RoleType.STUDENT.getCode());

        // Crear usuario base y perfil de estudiante
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        // Crear el CompleteStudent
        CompleteStudent completeStudent = new CompleteStudent(
            null, // id (se asignará al guardar)
            roleId, // Asegurarnos de usar el Byte
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
        
        // Imprimir información del CompleteStudent antes de guardar
        System.out.println("DEBUG - CompleteStudent before saving: roleId=" + completeStudent.getUser().getRoleId() + 
                          " (type: " + (completeStudent.getUser().getRoleId() != null ? 
                                        completeStudent.getUser().getRoleId().getClass().getName() : "null") + ")");
        
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
            throw new EmailAlreadyExistsException("El email " + teacherRequest.getEmail() + " ya está registrado en el sistema");
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
            true, // emailVerified - Cambiado a true para permitir login inmediato
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
            true, // teacherEmailVerified - Cambiado a true para consistencia
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
        try {
            // Verificar si el email ya está en uso
            if (!isEmailAvailable(guardianRequest.getEmail())) {
                throw new EmailAlreadyExistsException("El email " + guardianRequest.getEmail() + " ya está registrado en el sistema");
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
            
            System.out.println("DEBUG - Guardian registration - Role: " + role.getName() + ", ID: " + role.getId());
            
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
            
            System.out.println("DEBUG - Guardian registration - CompleteGuardian created with role: " + 
                              completeGuardian.getUser().getRoleId() + ", institution: " + 
                              completeGuardian.getUser().getInstitutionId());
            
            // Guardar el guardian completo
            Result<CompleteGuardian> result = completeUserRepository.createCompleteGuardian(completeGuardian);
            
            if (!result.isSuccess()) {
                System.out.println("ERROR - Guardian registration failed: " + result.getErrorMessage());
                throw new RuntimeException("Error al registrar el guardián: " + result.getErrorMessage());
            }
            
            CompleteGuardian savedGuardian = result.getData();
            
            if (savedGuardian == null) {
                System.out.println("ERROR - Guardian registration - Saved guardian is null");
                
                // Intentar crear un DTO básico a partir de los datos disponibles
                try {
                    // Buscar el usuario recién creado por email
                    Optional<User> userOpt = userRepository.findByEmail(guardianRequest.getEmail());
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        System.out.println("DEBUG - Guardian registration - Found user by email: " + user);
                        
                        // Buscar el perfil de guardián por user_id
                        try {
                            GuardianProfile guardianProfile = jdbcTemplate.queryForObject(
                                "SELECT * FROM guardian_profile WHERE user_id = ?",
                                new Object[]{user.getId()},
                                (rs, rowNum) -> {
                                    GuardianProfile profile = new GuardianProfile();
                                    profile.setId(rs.getLong("id"));
                                    profile.setUserId(rs.getLong("user_id"));
                                    profile.setPhone(rs.getString("phone"));
                                    profile.setCreatedAt(rs.getTimestamp("created_at"));
                                    profile.setUpdatedAt(rs.getTimestamp("updated_at"));
                                    return profile;
                                }
                            );
                            
                            System.out.println("DEBUG - Guardian registration - Found guardian profile: " + guardianProfile);
                            
                            // Crear un DTO básico
                            return UserResponseDto.GuardianResponseDto.builder()
                                    .id(user.getId())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .fullName(user.getFirstName() + " " + user.getLastName())
                                    .email(user.getEmail())
                                    .guardianProfileId(guardianProfile.getId())
                                    .phone(guardianProfile.getPhone())
                                    .roleName("GUARDIAN")
                                    .institutionName(institution.getName())
                                    .status(true)
                                    .emailVerified(false)
                                    .createdAt(now.toLocalDateTime())
                                    .studentsCount(0)
                                    .students(new ArrayList<>())
                                    .build();
                        } catch (Exception e) {
                            System.out.println("ERROR - Guardian registration - Error finding guardian profile: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("ERROR - Guardian registration - Error creating basic DTO: " + e.getMessage());
                }
                
                throw new RuntimeException("Error al registrar el guardián: El objeto guardado es nulo");
            }
            
            System.out.println("DEBUG - Guardian registration - Saved guardian: " + savedGuardian);
            
            // Verificar si tenemos el usuario y el perfil de guardián
            if (savedGuardian.getUser() == null) {
                System.out.println("ERROR - Guardian registration - User is null in saved guardian");
                throw new RuntimeException("Error al registrar el guardián: El usuario guardado es nulo");
            }
            
            if (savedGuardian.getGuardianProfile() == null) {
                System.out.println("ERROR - Guardian registration - Guardian profile is null in saved guardian");
                throw new RuntimeException("Error al registrar el guardián: El perfil de guardián guardado es nulo");
            }
            
            try {
                // Mapear a DTO de respuesta
                return mapToGuardianResponseDto(savedGuardian);
            } catch (Exception e) {
                System.out.println("ERROR - Guardian registration - Error mapping to DTO: " + e.getMessage());
                
                // Si falla el mapeo, intentar crear un DTO básico
                User user = savedGuardian.getUser();
                GuardianProfile guardianProfile = savedGuardian.getGuardianProfile();
                
                return UserResponseDto.GuardianResponseDto.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .fullName(user.getFirstName() + " " + user.getLastName())
                        .email(user.getEmail())
                        .guardianProfileId(guardianProfile.getId())
                        .phone(guardianProfile.getPhone())
                        .roleName("GUARDIAN")
                        .institutionName(institution.getName())
                        .status(true)
                        .emailVerified(false)
                        .createdAt(now.toLocalDateTime())
                        .studentsCount(0)
                        .students(new ArrayList<>())
                        .build();
            }
        } catch (Exception e) {
            System.out.println("ERROR - Guardian registration - Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al registrar el guardián: " + e.getMessage(), e);
        }
    }
    
    @Override
    public CommonResponseDto associateStudentToGuardian(UserRequestDto.StudentGuardianAssociationRequestDto associationDto) {
        try {
            // Validar que los IDs sean válidos
            Long studentProfileId = associationDto.getStudentProfileId();
            Long guardianProfileId = associationDto.getGuardianProfileId();
            
            if (studentProfileId == null || guardianProfileId == null) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Los IDs de estudiante y guardián son obligatorios")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            
            // Verificar que el perfil de estudiante existe
            Optional<CompleteStudent> studentOpt = completeUserRepository.findCompleteStudentById(studentProfileId);
            if (studentOpt.isEmpty()) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("El perfil de estudiante no existe")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            
            // Verificar que el perfil de guardián existe
            Optional<CompleteGuardian> guardianOpt = completeUserRepository.findCompleteGuardianById(guardianProfileId);
            if (guardianOpt.isEmpty()) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("El perfil de guardián no existe")
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            
            // Actualizar la asociación
            jdbcTemplate.update(
                "UPDATE student_profile SET guardian_profile_id = ? WHERE id = ?",
                guardianProfileId,
                studentProfileId
            );
            
            return CommonResponseDto.builder()
                    .success(true)
                    .message("Estudiante asociado al guardián exitosamente")
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Error al asociar estudiante al guardián: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
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
        System.out.println("DEBUG - findUserById: Buscando usuario con ID: " + id);
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                System.out.println("DEBUG - findUserById: Usuario encontrado: " + userOpt.get());
                return userOpt.get();
            } else {
                System.out.println("WARN - findUserById: Usuario no encontrado con ID: " + id);
                
                // Intentar buscar el usuario directamente en la base de datos
                try {
                    User user = jdbcTemplate.queryForObject(
                        "SELECT * FROM [user] WHERE id = ?",
                        new Object[]{id},
                        (rs, rowNum) -> {
                            // Usar el constructor completo de User
                            return new User(
                                rs.getLong("id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("email"),
                                rs.getString("password"),
                                rs.getString("profile_picture_url"),
                                DatabaseUtils.safeToBoolean(rs.getObject("status")), // 🔧 CORREGIDO
                                DatabaseUtils.safeToBoolean(rs.getObject("email_verified")), // 🔧 CORREGIDO
                                rs.getString("email_verification_token"),
                                rs.getTimestamp("email_verification_expires_at"),
                                rs.getString("password_reset_token"),
                                rs.getTimestamp("password_reset_expires_at"),
                                rs.getTimestamp("last_login_at"),
                                rs.getString("last_login_ip"),
                                rs.getObject("failed_login_attempts", Integer.class),
                                rs.getTimestamp("account_locked_until"),
                                rs.getTimestamp("created_at"),
                                rs.getTimestamp("updated_at"),
                                rs.getByte("role_id"),
                                rs.getLong("institution_id")
                            );
                        }
                    );
                    
                    if (user != null) {
                        System.out.println("DEBUG - findUserById: Usuario encontrado directamente en la BD: " + user);
                        return user;
                    }
                } catch (Exception e) {
                    System.out.println("ERROR - findUserById: Error al buscar usuario directamente en la BD: " + e.getMessage());
                }
                
                throw new IllegalArgumentException("Usuario no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            System.out.println("ERROR - findUserById: Error al buscar usuario: " + e.getMessage());
            throw new IllegalArgumentException("Error al buscar usuario con ID: " + id + ": " + e.getMessage());
        }
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
        System.out.println("DEBUG - mapToGuardianResponseDto: Mapping guardian: " + completeGuardian);
        
        if (completeGuardian == null) {
            System.out.println("ERROR - mapToGuardianResponseDto: CompleteGuardian is null");
            throw new IllegalArgumentException("No se puede mapear un guardián nulo");
        }
        
        User user = completeGuardian.getUser();
        if (user == null) {
            System.out.println("ERROR - mapToGuardianResponseDto: User is null in CompleteGuardian");
            
            // Intentar crear un usuario básico a partir de los datos disponibles
            try {
                // CompleteGuardian no tiene un método getId(), intentamos obtener el ID del usuario
                // o del perfil de guardián directamente
                Long userId = null;
                
                // Si tenemos algún dato parcial, intentamos usarlo
                if (completeGuardian.getUser() != null && completeGuardian.getUser().getId() != null) {
                    userId = completeGuardian.getUser().getId();
                } else if (completeGuardian.getGuardianProfile() != null && completeGuardian.getGuardianProfile().getUserId() != null) {
                    userId = completeGuardian.getGuardianProfile().getUserId();
                } else {
                    // No tenemos suficiente información
                    throw new IllegalArgumentException("El usuario en el guardián es nulo y no hay información suficiente para recuperarlo");
                }
                
                if (userId != null) {
                    user = findUserById(userId);
                    if (user == null) {
                        throw new IllegalArgumentException("El usuario en el guardián es nulo y no se pudo recuperar de la base de datos");
                    }
                } else {
                    throw new IllegalArgumentException("El usuario en el guardián es nulo y no se tiene ID para recuperarlo");
                }
            } catch (Exception e) {
                System.out.println("ERROR - mapToGuardianResponseDto: Error al intentar recuperar usuario: " + e.getMessage());
                throw new IllegalArgumentException("El usuario en el guardián es nulo: " + e.getMessage());
            }
        }
        
        GuardianProfile guardianProfile = completeGuardian.getGuardianProfile();
        if (guardianProfile == null) {
            System.out.println("ERROR - mapToGuardianResponseDto: GuardianProfile is null in CompleteGuardian");
            
            // Intentar crear un perfil básico a partir de los datos disponibles
            try {
                Long userId = user.getId();
                if (userId != null) {
                    guardianProfile = jdbcTemplate.queryForObject(
                        "SELECT * FROM guardian_profile WHERE user_id = ?",
                        new Object[]{userId},
                        (rs, rowNum) -> {
                            GuardianProfile profile = new GuardianProfile();
                            profile.setId(rs.getLong("id"));
                            profile.setUserId(rs.getLong("user_id"));
                            profile.setPhone(rs.getString("phone"));
                            profile.setCreatedAt(rs.getTimestamp("created_at"));
                            profile.setUpdatedAt(rs.getTimestamp("updated_at"));
                            return profile;
                        }
                    );
                    
                    if (guardianProfile == null) {
                        throw new IllegalArgumentException("No se pudo recuperar el perfil de guardián de la base de datos");
                    }
                } else {
                    throw new IllegalArgumentException("No se puede recuperar el perfil de guardián sin ID de usuario");
                }
            } catch (Exception e) {
                System.out.println("ERROR - mapToGuardianResponseDto: Error al intentar recuperar perfil de guardián: " + e.getMessage());
                throw new IllegalArgumentException("El perfil de guardián es nulo: " + e.getMessage());
            }
        }
        
        System.out.println("DEBUG - mapToGuardianResponseDto: User ID: " + user.getId() + 
                          ", Guardian Profile ID: " + guardianProfile.getId());
        
        String institutionName = "Institución no encontrada";
        if (user.getInstitution() != null) {
            institutionName = user.getInstitution().getName();
        } else if (user.getInstitutionId() != null) {
            // Si la institución no está cargada en el usuario, buscarla
            try {
                Optional<Institution> institutionOpt = institutionRepository.findById(user.getInstitutionId());
                if (institutionOpt.isPresent()) {
                    institutionName = institutionOpt.get().getName();
                }
            } catch (Exception e) {
                System.out.println("WARN - mapToGuardianResponseDto: Error al buscar institución: " + e.getMessage());
            }
        }
        
        String roleName = "Guardián";
        if (user.getRole() != null) {
            roleName = user.getRole().getName();
        } else {
            try {
                Optional<Role> roleOpt = roleRepository.findById(user.getRoleId());
                if (roleOpt.isPresent()) {
                    roleName = roleOpt.get().getName();
                }
            } catch (Exception e) {
                System.out.println("WARN - mapToGuardianResponseDto: Error al buscar rol: " + e.getMessage());
            }
        }
        
        try {
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
                    .roleName(roleName)
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
        } catch (Exception e) {
            System.out.println("ERROR - mapToGuardianResponseDto: Error al construir DTO: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al mapear guardián a DTO: " + e.getMessage(), e);
        }
    }
}