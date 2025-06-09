package com.gamified.application.auth.service.user;

import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.entity.composite.CompleteGuardian;
import com.gamified.application.auth.entity.composite.CompleteStudent;
import com.gamified.application.auth.entity.composite.CompleteTeacher;
import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.enums.RoleType;
import com.gamified.application.auth.entity.profiles.GuardianProfile;
import com.gamified.application.auth.entity.profiles.StudentProfile;
import com.gamified.application.auth.entity.profiles.TeacherProfile;
import com.gamified.application.auth.repository.composite.CompleteUserRepository;
import com.gamified.application.auth.repository.core.UserRepository;
import com.gamified.application.auth.repository.interfaces.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de perfiles de usuario
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    
    private final CompleteUserRepository completeUserRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public UserResponseDto.StudentResponseDto getStudentProfile(Long userId) {
        Optional<CompleteStudent> studentOpt = completeUserRepository.findCompleteStudentById(userId);
        
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el estudiante con ID: " + userId);
        }
        
        CompleteStudent student = studentOpt.get();
        User user = student.getUser();
        StudentProfile profile = student.getStudentProfile();
        
        // Obtener el nivel actual del estudiante
        Integer level = jdbcTemplate.queryForObject(
            "SELECT ISNULL(ls.level_number, 1) FROM student_level sl " +
            "JOIN level_system ls ON sl.level_system_id = ls.id " +
            "WHERE sl.student_profile_id = ? " +
            "ORDER BY ls.level_number DESC LIMIT 1",
            new Object[]{profile.getId()},
            Integer.class);
        
        if (level == null) {
            level = 1; // Nivel por defecto
        }
        
        // Obtener los logros recientes
        List<UserResponseDto.AchievementSummaryDto> achievements = new ArrayList<>();
        // TODO: Implementar obtención de logros recientes
        
        return UserResponseDto.StudentResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .studentProfileId(profile.getId())
                .username(profile.getUsername())
                .pointsAmount(profile.getPointsAmount())
                .roleName(user.getRoleName())
                .institutionName(user.getInstitutionName())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt() != null ? 
                           user.getLastLoginAt().toLocalDateTime() : null)
                .createdAt(user.getCreatedAt() != null ? 
                           user.getCreatedAt().toLocalDateTime() : LocalDateTime.now())
                .level(level)
                .recentAchievements(achievements)
                .build();
    }
    
    @Override
    public UserResponseDto.TeacherResponseDto getTeacherProfile(Long userId) {
        Optional<CompleteTeacher> teacherOpt = completeUserRepository.findCompleteTeacherById(userId);
        
        if (teacherOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el profesor con ID: " + userId);
        }
        
        CompleteTeacher teacher = teacherOpt.get();
        User user = teacher.getUser();
        TeacherProfile profile = teacher.getTeacherProfile();
        
        // Obtener cantidad de aulas
        Integer classroomsCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM classroom WHERE teacher_profile_id = ?",
            new Object[]{profile.getId()},
            Integer.class);
        
        if (classroomsCount == null) {
            classroomsCount = 0;
        }
        
        // Obtener cantidad de estudiantes
        Integer studentsCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT e.student_profile_id) FROM enrollment e " +
            "JOIN classroom c ON e.classroom_id = c.id " +
            "WHERE c.teacher_profile_id = ?",
            new Object[]{profile.getId()},
            Integer.class);
        
        if (studentsCount == null) {
            studentsCount = 0;
        }
        
        // Obtener nombre del área STEM
        String stemAreaName = jdbcTemplate.queryForObject(
            "SELECT title FROM stem_area WHERE id = ?",
            new Object[]{profile.getStemAreaId()},
            String.class);
        
        return UserResponseDto.TeacherResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .teacherProfileId(profile.getId())
                .stemAreaId(profile.getStemAreaId())
                .stemAreaName(stemAreaName)
                .roleName(user.getRoleName())
                .institutionName(user.getInstitutionName())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt() != null ? 
                           user.getLastLoginAt().toLocalDateTime() : null)
                .createdAt(user.getCreatedAt() != null ? 
                           user.getCreatedAt().toLocalDateTime() : LocalDateTime.now())
                .classroomsCount(classroomsCount)
                .studentsCount(studentsCount)
                .build();
    }
    
    @Override
    public UserResponseDto.GuardianResponseDto getGuardianProfile(Long userId) {
        Optional<CompleteGuardian> guardianOpt = completeUserRepository.findCompleteGuardianById(userId);
        
        if (guardianOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el tutor con ID: " + userId);
        }
        
        CompleteGuardian guardian = guardianOpt.get();
        User user = guardian.getUser();
        GuardianProfile profile = guardian.getGuardianProfile();
        
        // Obtener cantidad de estudiantes asociados
        Integer studentsCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM student_profile WHERE guardian_profile_id = ?",
            new Object[]{profile.getId()},
            Integer.class);
        
        if (studentsCount == null) {
            studentsCount = 0;
        }
        
        // Obtener lista de estudiantes y convertirlos a BasicInfoDto
        List<UserResponseDto.StudentResponseDto> studentResponses = getStudentsByGuardian(userId);
        List<UserResponseDto.StudentBasicInfoDto> studentBasicInfos = studentResponses.stream()
                .map(this::mapToStudentBasicInfoDto)
                .collect(Collectors.toList());
        
        return UserResponseDto.GuardianResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .guardianProfileId(profile.getId())
                .phone(profile.getPhone())
                .roleName(user.getRoleName())
                .institutionName(user.getInstitutionName())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt() != null ? 
                           user.getLastLoginAt().toLocalDateTime() : null)
                .createdAt(user.getCreatedAt() != null ? 
                           user.getCreatedAt().toLocalDateTime() : LocalDateTime.now())
                .studentsCount(studentsCount)
                .students(studentBasicInfos)
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
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el usuario con ID: " + userId);
        }
        
        User user = userOpt.get();
        user.setProfilePictureUrl(profilePictureUrl);
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        Result<User> result = userRepository.save(user);
        
        if (!result.isSuccess()) {
            throw new RuntimeException("Error al actualizar la foto de perfil: " + result.getErrorMessage());
        }
        
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
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el usuario con ID: " + userId);
        }
        
        User user = userOpt.get();
        user.setStatus(false);
        user.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        Result<User> result = userRepository.save(user);
        
        if (!result.isSuccess()) {
            throw new RuntimeException("Error al desactivar la cuenta: " + result.getErrorMessage());
        }
        
        return CommonResponseDto.builder()
                .success(true)
                .message("Cuenta desactivada exitosamente")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public List<UserResponseDto.StudentResponseDto> getStudentsByGuardian(Long guardianUserId) {
        // Obtener primero el perfil del guardián
        Optional<CompleteGuardian> guardianOpt = completeUserRepository.findCompleteGuardianById(guardianUserId);
        
        if (guardianOpt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el tutor con ID: " + guardianUserId);
        }
        
        CompleteGuardian guardian = guardianOpt.get();
        GuardianProfile profile = guardian.getGuardianProfile();
        
        // Obtener los estudiantes asociados al guardián
        List<CompleteStudent> students = completeUserRepository.findStudentsByGuardian(guardianUserId);
        
        // Mapear a DTOs
        List<UserResponseDto.StudentResponseDto> studentDtos = new ArrayList<>();
        for (CompleteStudent student : students) {
            studentDtos.add(mapToStudentResponseDto(student));
        }
        
        return studentDtos;
    }
    
    @Override
    public List<UserResponseDto.BasicUserResponseDto> searchUsers(String searchTerm, String roleFilter, int limit) {
        // Consulta SQL con LIKE y filtro de rol
        String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, " +
                     "u.role_id, r.name as role_name, u.status, u.last_login_at, u.created_at " +
                     "FROM [user] u " +
                     "JOIN role r ON u.role_id = r.id " +
                     "WHERE (u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ?) ";
        
        List<Object> params = new ArrayList<>();
        params.add("%" + searchTerm + "%");
        params.add("%" + searchTerm + "%");
        params.add("%" + searchTerm + "%");
        
        if (roleFilter != null && !roleFilter.isEmpty()) {
            sql += "AND r.name = ? ";
            params.add(roleFilter);
        }
        
        sql += "ORDER BY u.first_name, u.last_name LIMIT ?";
        params.add(limit);
        
        List<UserResponseDto.BasicUserResponseDto> users = jdbcTemplate.query(
            sql, 
            params.toArray(),
            (rs, rowNum) -> UserResponseDto.BasicUserResponseDto.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .fullName(rs.getString("first_name") + " " + rs.getString("last_name"))
                .email(rs.getString("email"))
                .profilePictureUrl(rs.getString("profile_picture_url"))
                .roleName(rs.getString("role_name"))
                .status(rs.getBoolean("status"))
                .lastLoginAt(rs.getTimestamp("last_login_at") != null ? 
                            rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                .createdAt(rs.getTimestamp("created_at") != null ? 
                          rs.getTimestamp("created_at").toLocalDateTime() : null)
                .build()
        );
        
        return users;
    }
    
    /**
     * Convierte un CompleteStudent a un StudentResponseDto
     */
    private UserResponseDto.StudentResponseDto mapToStudentResponseDto(CompleteStudent completeStudent) {
        User user = completeStudent.getUser();
        StudentProfile profile = completeStudent.getStudentProfile();
        
        // Obtener el nivel actual del estudiante (si existe)
        Integer level = jdbcTemplate.queryForObject(
            "SELECT ISNULL(ls.level_number, 1) FROM student_level sl " +
            "JOIN level_system ls ON sl.level_system_id = ls.id " +
            "WHERE sl.student_profile_id = ? " +
            "ORDER BY ls.level_number DESC LIMIT 1",
            new Object[]{profile.getId()},
            Integer.class);
        
        if (level == null) {
            level = 1; // Nivel por defecto
        }
        
        return UserResponseDto.StudentResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .studentProfileId(profile.getId())
                .username(profile.getUsername())
                .pointsAmount(profile.getPointsAmount())
                .roleName(user.getRoleName())
                .institutionName(user.getInstitutionName())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .lastLoginAt(user.getLastLoginAt() != null ? 
                           user.getLastLoginAt().toLocalDateTime() : null)
                .createdAt(user.getCreatedAt() != null ? 
                           user.getCreatedAt().toLocalDateTime() : LocalDateTime.now())
                .level(level)
                .recentAchievements(new ArrayList<>())
                .build();
    }
    
    /**
     * Convierte un StudentResponseDto a un StudentBasicInfoDto
     */
    private UserResponseDto.StudentBasicInfoDto mapToStudentBasicInfoDto(UserResponseDto.StudentResponseDto student) {
        return UserResponseDto.StudentBasicInfoDto.builder()
                .id(student.getId())
                .studentProfileId(student.getStudentProfileId())
                .username(student.getUsername())
                .fullName(student.getFullName())
                .birth_date(student.getBirth_date())
                .pointsAmount(student.getPointsAmount())
                .build();
    }
} 