package com.gamified.application.user.service;

import com.gamified.application.user.model.dto.request.UserRequestDto;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import com.gamified.application.user.model.dto.response.UserResponseDto;
import com.gamified.application.user.model.entity.composite.CompleteGuardian;
import com.gamified.application.user.model.entity.composite.CompleteStudent;
import com.gamified.application.user.model.entity.composite.CompleteTeacher;
import com.gamified.application.user.model.entity.User;
import com.gamified.application.user.model.entity.profiles.GuardianProfile;
import com.gamified.application.user.model.entity.profiles.StudentProfile;
import com.gamified.application.user.model.entity.profiles.TeacherProfile;
import com.gamified.application.user.repository.composite.CompleteUserRepository;
import com.gamified.application.auth.repository.core.UserRepository;
import com.gamified.application.shared.repository.Result;
import com.gamified.application.shared.util.DatabaseUtils;
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
        Integer level = getStudentLevel(profile.getId());
        
        // Obtener los logros recientes
        List<UserResponseDto.AchievementSummaryDto> achievements = getRecentStudentAchievements(userId, 5);
        
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
        Integer classroomsCount;
        try {
            classroomsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM classroom WHERE teacher_profile_id = ?",
                new Object[]{profile.getId()},
                Integer.class);
        } catch (Exception e) {
            classroomsCount = 0;
        }
        
        if (classroomsCount == null) {
            classroomsCount = 0;
        }
        
        // Obtener cantidad de estudiantes
        Integer studentsCount;
        try {
            studentsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT e.student_profile_id) FROM enrollment e " +
                "JOIN classroom c ON e.classroom_id = c.id " +
                "WHERE c.teacher_profile_id = ?",
                new Object[]{profile.getId()},
                Integer.class);
        } catch (Exception e) {
            studentsCount = 0;
        }
        
        if (studentsCount == null) {
            studentsCount = 0;
        }
        
        // Obtener nombre del área STEM
        String stemAreaName = getStemAreaName(profile.getStemAreaId());
        
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
        Integer studentsCount;
        try {
            studentsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_profile WHERE guardian_profile_id = ?",
                new Object[]{profile.getId()},
                Integer.class);
        } catch (Exception e) {
            studentsCount = 0;
        }
        
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
    public List<UserResponseDto.StudentResponseDto> getStudentsByGuardianProfile(Long guardianProfileId) {
        String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, " +
                     "sp.id as student_profile_id, sp.username, sp.birth_date, sp.points_amount, " +
                     "r.name as role_name, i.name as institution_name, u.status, u.email_verified, " +
                     "u.last_login_at, u.created_at, " +
                     "gp.id as guardian_profile_id, gu.first_name + ' ' + gu.last_name as guardian_name, gu.email as guardian_email " +
                     "FROM [user] u " +
                     "JOIN role r ON u.role_id = r.id " +
                     "JOIN student_profile sp ON u.id = sp.user_id " +
                     "JOIN institution i ON u.institution_id = i.id " +
                     "JOIN guardian_profile gp ON sp.guardian_profile_id = gp.id " +
                     "JOIN [user] gu ON gp.user_id = gu.id " +
                     "WHERE gp.id = ? " +
                     "ORDER BY u.first_name, u.last_name";
        
        try {
            System.out.println("🔍 Ejecutando getStudentsByGuardianProfile:");
            System.out.println("SQL: " + sql);
            System.out.println("Parámetros: guardianProfileId=" + guardianProfileId);
            
            List<UserResponseDto.StudentResponseDto> result = jdbcTemplate.query(sql, new Object[]{guardianProfileId}, (rs, rowNum) -> 
                UserResponseDto.StudentResponseDto.builder()
                    .id(rs.getLong("id"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .fullName(rs.getString("first_name") + " " + rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .profilePictureUrl(rs.getString("profile_picture_url"))
                    .studentProfileId(rs.getLong("student_profile_id"))
                    .username(rs.getString("username"))
                    .birth_date(rs.getDate("birth_date"))
                    .pointsAmount(rs.getInt("points_amount"))
                    .guardianProfileId(rs.getLong("guardian_profile_id"))
                    .guardianName(rs.getString("guardian_name"))
                    .guardianEmail(rs.getString("guardian_email"))
                    .roleName(rs.getString("role_name"))
                    .institutionName(rs.getString("institution_name"))
                    .status(DatabaseUtils.safeToBoolean(rs.getObject("status")))
                    .emailVerified(DatabaseUtils.safeToBoolean(rs.getObject("email_verified")))
                    .lastLoginAt(rs.getTimestamp("last_login_at") != null ? 
                                rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                    .createdAt(rs.getTimestamp("created_at") != null ? 
                              rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .level(1)
                    .recentAchievements(getRecentStudentAchievements(rs.getLong("id"), 3))
                    .build()
            );
            
            System.out.println("📊 Resultado getStudentsByGuardianProfile: " + result.size() + " estudiantes encontrados");
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error getting students by guardian profile: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<UserResponseDto.BasicUserResponseDto> searchUsers(String searchTerm, String roleFilter, int limit) {
        // Consulta SQL con LIKE y filtro de rol (usando TOP para SQL Server)
        String sql = "SELECT TOP " + limit + " u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, " +
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
        
        sql += "ORDER BY u.first_name, u.last_name";
        
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
                .status(DatabaseUtils.safeToBoolean(rs.getObject("status"))) // 🔧 CORREGIDO
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
        
        // Obtener el nivel actual del estudiante
        Integer level = getStudentLevel(profile.getId());
        
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
                .recentAchievements(getRecentStudentAchievements(user.getId(), 3))
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

    // ==================== IMPLEMENTACIÓN MÉTODOS INSTITUCIONALES ====================

    @Override
    public java.util.Map<String, Integer> getUserCountsByRoleForInstitution(Long institutionId) {
        String sql = "SELECT r.name as role_name, COUNT(*) as count " +
                     "FROM [user] u " +
                     "JOIN role r ON u.role_id = r.id " +
                     "WHERE u.institution_id = ? AND u.status = 1 " +
                     "GROUP BY r.name";
        
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        
        try {
            System.out.println("🔍 Ejecutando consulta getUserCountsByRoleForInstitution:");
            System.out.println("SQL: " + sql);
            System.out.println("Parámetros: institutionId=" + institutionId);
            
            jdbcTemplate.query(sql, new Object[]{institutionId}, (rs) -> {
                String roleName = rs.getString("role_name");
                int count = rs.getInt("count");
                System.out.println("✅ Encontrado: " + roleName + " = " + count);
                counts.put(roleName, count);
            });
            
            System.out.println("📊 Resultado final getUserCountsByRoleForInstitution: " + counts);
        } catch (Exception e) {
            System.err.println("❌ Error getting user counts by role: " + e.getMessage());
            e.printStackTrace();
        }
        
        return counts;
    }

    @Override
    public List<UserResponseDto.BasicUserResponseDto> getUsersByInstitution(Long institutionId, String role, Boolean unassigned) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, ");
        sql.append("r.name as role_name, u.status, u.email_verified, u.last_login_at, u.created_at ");
        sql.append("FROM [user] u ");
        sql.append("JOIN role r ON u.role_id = r.id ");
        
        if (Boolean.TRUE.equals(unassigned)) {
            sql.append("LEFT JOIN student_profile sp ON u.id = sp.user_id ");
        }
        
        sql.append("WHERE u.institution_id = ? ");
        
        List<Object> params = new ArrayList<>();
        params.add(institutionId);
        
        if (role != null && !role.isEmpty()) {
            sql.append("AND r.name = ? ");
            params.add(role);
        }
        
        if (Boolean.TRUE.equals(unassigned)) {
            sql.append("AND r.name = 'STUDENT' AND sp.guardian_profile_id IS NULL ");
        }
        
        sql.append("ORDER BY u.first_name, u.last_name");
        
        try {
            System.out.println("🔍 Ejecutando consulta getUsersByInstitution:");
            System.out.println("SQL: " + sql.toString());
            System.out.println("Parámetros: " + params);
            
            List<UserResponseDto.BasicUserResponseDto> result = jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> 
                UserResponseDto.BasicUserResponseDto.builder()
                    .id(rs.getLong("id"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .fullName(rs.getString("first_name") + " " + rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .profilePictureUrl(rs.getString("profile_picture_url"))
                    .roleName(rs.getString("role_name"))
                    .status(DatabaseUtils.safeToBoolean(rs.getObject("status")))
                    .lastLoginAt(rs.getTimestamp("last_login_at") != null ? 
                                rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                    .createdAt(rs.getTimestamp("created_at") != null ? 
                              rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .build()
            );
            
            System.out.println("📊 Resultado getUsersByInstitution: " + result.size() + " usuarios encontrados");
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error getting users by institution: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<UserResponseDto.StudentResponseDto> getUnassignedStudentsByInstitution(Long institutionId) {
        String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, " +
                     "sp.id as student_profile_id, sp.username, sp.birth_date, sp.points_amount, " +
                     "r.name as role_name, i.name as institution_name, u.status, u.email_verified, " +
                     "u.last_login_at, u.created_at " +
                     "FROM [user] u " +
                     "JOIN role r ON u.role_id = r.id " +
                     "JOIN student_profile sp ON u.id = sp.user_id " +
                     "JOIN institution i ON u.institution_id = i.id " +
                     "WHERE u.institution_id = ? AND r.name = 'STUDENT' AND sp.guardian_profile_id IS NULL " +
                     "ORDER BY u.first_name, u.last_name";
        
        try {
            System.out.println("🔍 Ejecutando consulta getUnassignedStudentsByInstitution:");
            System.out.println("SQL: " + sql);
            System.out.println("Parámetros: institutionId=" + institutionId);
            
            List<UserResponseDto.StudentResponseDto> result = jdbcTemplate.query(sql, new Object[]{institutionId}, (rs, rowNum) -> 
                UserResponseDto.StudentResponseDto.builder()
                    .id(rs.getLong("id"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .fullName(rs.getString("first_name") + " " + rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .profilePictureUrl(rs.getString("profile_picture_url"))
                    .studentProfileId(rs.getLong("student_profile_id"))
                    .username(rs.getString("username"))
                    .birth_date(rs.getDate("birth_date"))
                    .pointsAmount(rs.getInt("points_amount"))
                    .roleName(rs.getString("role_name"))
                    .institutionName(rs.getString("institution_name"))
                    .status(DatabaseUtils.safeToBoolean(rs.getObject("status")))
                    .emailVerified(DatabaseUtils.safeToBoolean(rs.getObject("email_verified")))
                    .lastLoginAt(rs.getTimestamp("last_login_at") != null ? 
                                rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                    .createdAt(rs.getTimestamp("created_at") != null ? 
                              rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .level(1)
                    .recentAchievements(getRecentStudentAchievements(rs.getLong("id"), 3))
                    .build()
            );
            
            System.out.println("📊 Resultado getUnassignedStudentsByInstitution: " + result.size() + " estudiantes encontrados");
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error getting unassigned students: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public List<UserResponseDto.GuardianResponseDto> getAvailableGuardiansByInstitution(Long institutionId) {
        String sql = "SELECT u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, " +
                     "gp.id as guardian_profile_id, gp.phone, " +
                     "r.name as role_name, i.name as institution_name, u.status, u.email_verified, " +
                     "u.last_login_at, u.created_at, " +
                     "COUNT(sp.id) as students_count " +
                     "FROM [user] u " +
                     "JOIN role r ON u.role_id = r.id " +
                     "JOIN guardian_profile gp ON u.id = gp.user_id " +
                     "JOIN institution i ON u.institution_id = i.id " +
                     "LEFT JOIN student_profile sp ON gp.id = sp.guardian_profile_id " +
                     "WHERE u.institution_id = ? AND r.name = 'GUARDIAN' AND u.status = 1 " +
                     "GROUP BY u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, " +
                     "gp.id, gp.phone, r.name, i.name, u.status, u.email_verified, " +
                     "u.last_login_at, u.created_at " +
                     "ORDER BY students_count ASC, u.first_name, u.last_name";
        
        try {
            return jdbcTemplate.query(sql, new Object[]{institutionId}, (rs, rowNum) -> {
                int studentsCount = rs.getInt("students_count");
                
                // Obtener lista básica de estudiantes del guardián
                List<UserResponseDto.StudentBasicInfoDto> students = new ArrayList<>();
                if (studentsCount > 0) {
                    // Aquí podríamos obtener la lista detallada si es necesario
                }
                
                return UserResponseDto.GuardianResponseDto.builder()
                    .id(rs.getLong("id"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .fullName(rs.getString("first_name") + " " + rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .profilePictureUrl(rs.getString("profile_picture_url"))
                    .guardianProfileId(rs.getLong("guardian_profile_id"))
                    .phone(rs.getString("phone"))
                    .roleName(rs.getString("role_name"))
                    .institutionName(rs.getString("institution_name"))
                    .status(DatabaseUtils.safeToBoolean(rs.getObject("status")))
                    .emailVerified(DatabaseUtils.safeToBoolean(rs.getObject("email_verified")))
                    .lastLoginAt(rs.getTimestamp("last_login_at") != null ? 
                                rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                    .createdAt(rs.getTimestamp("created_at") != null ? 
                              rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .studentsCount(studentsCount)
                    .students(students)
                    .build();
            });
        } catch (Exception e) {
            System.err.println("Error getting available guardians: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean assignGuardianToStudent(Long studentProfileId, Long guardianProfileId) {
        String sql = "UPDATE student_profile SET guardian_profile_id = ?, updated_at = GETDATE() " +
                     "WHERE id = ? AND guardian_profile_id IS NULL";
        
        try {
            System.out.println("🔍 Ejecutando assignGuardianToStudent:");
            System.out.println("SQL: " + sql);
            System.out.println("Parámetros: guardianProfileId=" + guardianProfileId + ", studentProfileId=" + studentProfileId);
            
            int rowsAffected = jdbcTemplate.update(sql, guardianProfileId, studentProfileId);
            System.out.println("📊 Filas afectadas en assignGuardianToStudent: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("❌ Error assigning guardian to student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean reassignGuardianToStudent(Long studentProfileId, Long previousGuardianProfileId, Long newGuardianProfileId) {
        String sql = "UPDATE student_profile SET guardian_profile_id = ?, updated_at = GETDATE() " +
                     "WHERE id = ? AND guardian_profile_id = ?";
        
        try {
            System.out.println("🔍 Ejecutando reassignGuardianToStudent:");
            System.out.println("SQL: " + sql);
            System.out.println("Parámetros: newGuardianId=" + newGuardianProfileId + 
                             ", studentProfileId=" + studentProfileId + 
                             ", previousGuardianId=" + previousGuardianProfileId);
            
            int rowsAffected = jdbcTemplate.update(sql, newGuardianProfileId, studentProfileId, previousGuardianProfileId);
            System.out.println("📊 Filas afectadas en reassignGuardianToStudent: " + rowsAffected);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("❌ Error reassigning guardian to student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene el nivel actual del estudiante desde las tablas student_level y level_system
     * @param studentProfileId ID del perfil del estudiante
     * @return Nivel del estudiante (1 por defecto si no existe)
     */
    private Integer getStudentLevel(Long studentProfileId) {
        try {
            String sql = """
                SELECT ls.level_number
                FROM student_level sl
                JOIN level_system ls ON sl.level_system_id = ls.id
                WHERE sl.student_profile_id = ?
                """;
            
            List<Integer> levels = jdbcTemplate.queryForList(sql, new Object[]{studentProfileId}, Integer.class);
            
            if (levels.isEmpty()) {
                // Si no hay registro de nivel, crear uno por defecto en nivel 1
                try {
                    // Buscar el level_system_id para nivel 1
                    String levelSystemSql = "SELECT id FROM level_system WHERE level_number = 1";
                    List<Integer> levelSystemIds = jdbcTemplate.queryForList(levelSystemSql, Integer.class);
                    
                    if (!levelSystemIds.isEmpty()) {
                        Integer levelSystemId = levelSystemIds.get(0);
                        String insertSql = "INSERT INTO student_level (student_profile_id, level_system_id, current_xp, created_at, updated_at) VALUES (?, ?, 0, GETDATE(), GETDATE())";
                        jdbcTemplate.update(insertSql, studentProfileId, levelSystemId);
                        return 1;
                    }
                } catch (Exception insertError) {
                    System.err.println("Error creando registro de nivel para estudiante " + studentProfileId + ": " + insertError.getMessage());
                }
                return 1; // Nivel por defecto
            }
            
            return levels.get(0);
        } catch (Exception e) {
            System.err.println("Error obteniendo nivel del estudiante " + studentProfileId + ": " + e.getMessage());
            return 1; // Nivel por defecto
        }
    }
    
    /**
     * Obtiene los logros recientes del estudiante usando el stored procedure existente
     * @param userId ID del usuario
     * @param limit Número máximo de logros a retornar
     * @return Lista de logros recientes
     */
    private List<UserResponseDto.AchievementSummaryDto> getRecentStudentAchievements(Long userId, int limit) {
        try {
            // En SQL Server, no se puede usar SELECT TOP ? con parámetros
            // Usamos una subconsulta con OFFSET/FETCH para manejar el límite dinámico
            String sql = String.format("""
                SELECT TOP %d 
                    sa.id,
                    a.achievement_name,
                    a.achievement_description,
                    a.points_value,
                    rt.rarity_name,
                    sa.earned_at
                FROM student_achievement sa
                LEFT JOIN achievement a ON a.id = sa.achievement_id
                LEFT JOIN achievement_type aty ON aty.id = a.achievement_type_id
                LEFT JOIN rarity_tier rt ON rt.id = a.rarity_tier_id
                LEFT JOIN student_profile sp ON sp.id = sa.student_profile_id
                WHERE sp.user_id = ?
                    AND a.is_active = 1
                    AND sa.is_active = 1
                ORDER BY sa.earned_at DESC
                """, limit);
            
            return jdbcTemplate.query(sql, new Object[]{userId}, (rs, rowNum) -> 
                UserResponseDto.AchievementSummaryDto.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("achievement_name"))
                    .description(rs.getString("achievement_description"))
                    .pointsValue(rs.getInt("points_value"))
                    .rarityTier(rs.getString("rarity_name"))
                    .earnedAt(rs.getTimestamp("earned_at") != null ? 
                             rs.getTimestamp("earned_at").toLocalDateTime() : null)
                    .build()
            );
        } catch (Exception e) {
            System.err.println("Error obteniendo logros recientes del usuario " + userId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public Integer executeCountQuery(String sql, Object... params) {
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, params);
        } catch (Exception e) {
            System.err.println("Error ejecutando consulta de conteo: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public java.sql.Timestamp executeTimestampQuery(String sql, Object... params) {
        try {
            return jdbcTemplate.queryForObject(sql, java.sql.Timestamp.class, params);
        } catch (Exception e) {
            System.err.println("Error ejecutando consulta de timestamp: " + e.getMessage());
            return null;
        }
    }

    @Override
    public String executeStringQuery(String sql, Object... params) {
        try {
            return jdbcTemplate.queryForObject(sql, String.class, params);
        } catch (Exception e) {
            System.err.println("Error ejecutando consulta de string: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el nombre del área STEM por ID
     * @param stemAreaId ID del área STEM
     * @return Nombre del área STEM
     */
    private String getStemAreaName(Byte stemAreaId) {
        if (stemAreaId == null) {
            return "Área no definida";
        }
        
        try {
            String sql = "SELECT title FROM stem_area WHERE id = ? AND status = 1";
            String stemAreaName = jdbcTemplate.queryForObject(sql, new Object[]{stemAreaId.intValue()}, String.class);
            return stemAreaName != null ? stemAreaName : "Área no definida";
        } catch (Exception e) {
            System.err.println("Error obteniendo nombre del área STEM " + stemAreaId + ": " + e.getMessage());
            return "Área no definida";
        }
    }

    @Override
    public List<UserResponseDto.BasicUserResponseDto> searchUsersInTeacherScope(Long teacherUserId, String searchTerm, String roleFilter, int limit) {
        try {
            // Consulta SQL que busca usuarios limitados al scope del profesor
            // Solo busca estudiantes que están en las clases del profesor
            String sql = """
                SELECT TOP ? u.id, u.first_name, u.last_name, u.email, u.profile_picture_url, 
                       u.role_id, r.name as role_name, u.status, u.last_login_at, u.created_at,
                       sp.username as student_username
                FROM [user] u 
                JOIN role r ON u.role_id = r.id 
                JOIN student_profile sp ON sp.user_id = u.id
                JOIN enrollment e ON e.student_profile_id = sp.id
                JOIN classroom c ON c.id = e.classroom_id
                JOIN teacher_profile tp ON tp.id = c.teacher_profile_id
                WHERE tp.user_id = ?
                  AND (u.first_name LIKE ? OR u.last_name LIKE ? OR u.email LIKE ? OR sp.username LIKE ?)
                  AND e.status = 1
                  AND u.status = 1
                """;
            
            List<Object> params = new ArrayList<>();
            params.add(limit);
            params.add(teacherUserId);
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
            
            // Agregar filtro de rol si se especifica
            if (roleFilter != null && !roleFilter.isEmpty()) {
                sql += " AND r.name = ?";
                params.add(roleFilter);
            }
            
            sql += " ORDER BY u.first_name, u.last_name";
            
            System.out.println("🔍 Ejecutando consulta searchUsersInTeacherScope:");
            System.out.println("SQL: " + sql);
            System.out.println("Parámetros: " + params);
            
            List<UserResponseDto.BasicUserResponseDto> result = jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> 
                UserResponseDto.BasicUserResponseDto.builder()
                    .id(rs.getLong("id"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .fullName(rs.getString("first_name") + " " + rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .profilePictureUrl(rs.getString("profile_picture_url"))
                    .roleName(rs.getString("role_name"))
                    .status(DatabaseUtils.safeToBoolean(rs.getObject("status")))
                    .lastLoginAt(rs.getTimestamp("last_login_at") != null ? 
                                rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                    .createdAt(rs.getTimestamp("created_at") != null ? 
                              rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .build()
            );
            
            System.out.println("📊 Resultado searchUsersInTeacherScope: " + result.size() + " usuarios encontrados");
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error searching users in teacher scope: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
} 