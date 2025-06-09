package com.gamified.application.auth.repository.composite;

import com.gamified.application.auth.entity.composite.CompleteStudent;
import com.gamified.application.auth.entity.composite.CompleteTeacher;
import com.gamified.application.auth.entity.composite.CompleteGuardian;
import com.gamified.application.auth.entity.core.Institution;
import com.gamified.application.auth.entity.core.Role;
import com.gamified.application.auth.entity.core.User;
import com.gamified.application.auth.entity.profiles.GuardianProfile;
import com.gamified.application.auth.entity.profiles.StudentProfile;
import com.gamified.application.auth.entity.profiles.TeacherProfile;
import com.gamified.application.auth.repository.interfaces.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del repositorio para operaciones compuestas
 */
@Repository
public class CompleteUserRepositoryImpl implements CompleteUserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CompleteUserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public Result<CompleteStudent> createCompleteStudent(CompleteStudent completeStudent) {
        try {
            // 0. Obtener el próximo valor de ID de usuario
            Long nextUserId = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(id), 0) + 1 FROM [user]", Long.class);
            
            // 1. Insertar usuario base
            User user = completeStudent.getUser();
            user.setId(nextUserId); // Establecer el ID generado
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO [user] (id, role_id, institution_id, first_name, last_name, email, password, " +
                    "profile_picture_url, created_at, updated_at, status, email_verified, username) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setLong(1, nextUserId);
                ps.setByte(2, user.getRoleId());
                ps.setLong(3, user.getInstitutionId());
                ps.setString(4, user.getFirstName());
                ps.setString(5, user.getLastName());
                ps.setString(6, user.getEmail());
                ps.setString(7, user.getPassword());
                ps.setString(8, user.getProfilePictureUrl());
                ps.setTimestamp(9, user.getCreatedAt());
                ps.setTimestamp(10, user.getUpdatedAt());
                ps.setBoolean(11, user.getStatus());
                ps.setBoolean(12, user.isEmailVerified());
                ps.setString(13, completeStudent.getStudentProfile().getUsername()); // Usar el username del perfil de estudiante
                return ps;
            });
            
            // 2. Insertar perfil de estudiante
            StudentProfile studentProfile = completeStudent.getStudentProfile();
            studentProfile.setUserId(nextUserId);
            
            // Obtener el próximo valor de ID para student_profiles
            Long nextStudentId = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(id), 0) + 1 FROM student_profile", Long.class);
            studentProfile.setId(nextStudentId);
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO student_profile (id, user_id, guardian_profile_id, username, birth_date, points_amount, " +
                    "created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setLong(1, nextStudentId);
                ps.setLong(2, nextUserId);
                // El guardian_profile_id puede ser null si no tiene guardián asignado
                if (studentProfile.getGuardianProfileId() != null) {
                    ps.setLong(3, studentProfile.getGuardianProfileId());
                } else {
                    ps.setNull(3, java.sql.Types.BIGINT);
                }
                ps.setString(4, studentProfile.getUsername());
                ps.setDate(5, studentProfile.getBirthDate());
                ps.setInt(6, studentProfile.getPointsAmount());
                ps.setTimestamp(7, studentProfile.getCreatedAt());
                ps.setTimestamp(8, studentProfile.getUpdatedAt());
                return ps;
            });
            
            return Result.success(completeStudent);
        } catch (Exception e) {
            return Result.failure("Error al crear estudiante: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<CompleteTeacher> createCompleteTeacher(CompleteTeacher completeTeacher) {
        try {
            // 0. Obtener el próximo valor de ID de usuario
            Long nextUserId = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(id), 0) + 1 FROM [user]", Long.class);
            
            // 1. Insertar usuario base
            User user = completeTeacher.getUser();
            user.setId(nextUserId); // Establecer el ID generado
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO [user] (id, role_id, institution_id, first_name, last_name, email, password, " +
                    "profile_picture_url, created_at, updated_at, status, email_verified) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setLong(1, nextUserId);
                ps.setByte(2, user.getRoleId());
                ps.setLong(3, user.getInstitutionId());
                ps.setString(4, user.getFirstName());
                ps.setString(5, user.getLastName());
                ps.setString(6, user.getEmail());
                ps.setString(7, user.getPassword());
                ps.setString(8, user.getProfilePictureUrl());
                ps.setTimestamp(9, user.getCreatedAt());
                ps.setTimestamp(10, user.getUpdatedAt());
                ps.setBoolean(11, user.getStatus());
                ps.setBoolean(12, user.isEmailVerified());
                return ps;
            });
            
            // 2. Insertar perfil de profesor
            TeacherProfile teacherProfile = completeTeacher.getTeacherProfile();
            teacherProfile.setUserId(nextUserId);
            
            // Obtener el próximo valor de ID para teacher_profile
            Long nextTeacherId = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(id), 0) + 1 FROM teacher_profile", Long.class);
            teacherProfile.setId(nextTeacherId);
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO teacher_profile (id, user_id, email_verified, stem_area_id, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setLong(1, nextTeacherId);
                ps.setLong(2, nextUserId);
                ps.setBoolean(3, teacherProfile.getEmailVerified());
                ps.setByte(4, teacherProfile.getStemAreaId());
                ps.setTimestamp(5, teacherProfile.getCreatedAt());
                ps.setTimestamp(6, teacherProfile.getUpdatedAt());
                return ps;
            });
            
            return Result.success(completeTeacher);
        } catch (Exception e) {
            return Result.failure("Error al crear profesor: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<CompleteGuardian> createCompleteGuardian(CompleteGuardian completeGuardian) {
        try {
            // 0. Obtener el próximo valor de ID de usuario
            Long nextUserId = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(id), 0) + 1 FROM [user]", Long.class);
            
            // 1. Insertar usuario base
            User user = completeGuardian.getUser();
            user.setId(nextUserId); // Establecer el ID generado
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO [user] (id, role_id, institution_id, first_name, last_name, email, password, " +
                    "profile_picture_url, created_at, updated_at, status, email_verified) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setLong(1, nextUserId);
                ps.setByte(2, user.getRoleId());
                ps.setLong(3, user.getInstitutionId());
                ps.setString(4, user.getFirstName());
                ps.setString(5, user.getLastName());
                ps.setString(6, user.getEmail());
                ps.setString(7, user.getPassword());
                ps.setString(8, user.getProfilePictureUrl());
                ps.setTimestamp(9, user.getCreatedAt());
                ps.setTimestamp(10, user.getUpdatedAt());
                ps.setBoolean(11, user.getStatus());
                ps.setBoolean(12, user.isEmailVerified());
                return ps;
            });
            
            // 2. Insertar perfil de guardián
            GuardianProfile guardianProfile = completeGuardian.getGuardianProfile();
            guardianProfile.setUserId(nextUserId);
            
            // Obtener el próximo valor de ID para guardian_profile
            Long nextGuardianId = jdbcTemplate.queryForObject(
                "SELECT ISNULL(MAX(id), 0) + 1 FROM guardian_profile", Long.class);
            guardianProfile.setId(nextGuardianId);
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO guardian_profile (id, user_id, phone, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                ps.setLong(1, nextGuardianId);
                ps.setLong(2, nextUserId);
                ps.setString(3, guardianProfile.getPhone());
                ps.setTimestamp(4, guardianProfile.getCreatedAt());
                ps.setTimestamp(5, guardianProfile.getUpdatedAt());
                return ps;
            });
            
            return Result.success(completeGuardian);
        } catch (Exception e) {
            return Result.failure("Error al crear guardián: " + e.getMessage());
        }
    }

    @Override
    public Optional<CompleteStudent> findCompleteStudentById(Long userId) {
        try {
            // 1. Consultar datos del usuario
            String userSql = "SELECT u.*, r.name as role_name, i.name as institution_name " +
                             "FROM [user] u " +
                             "JOIN role r ON u.role_id = r.id " +
                             "JOIN institution i ON u.institution_id = i.id " +
                             "WHERE u.id = ?";
            
            List<User> users = jdbcTemplate.query(userSql, new Object[]{userId}, (rs, rowNum) -> {
                User user = mapUserFromResultSet(rs);
                // Establecer objetos relacionados
                Role role = new Role();
                role.setId(user.getRoleId());
                role.setName(rs.getString("role_name"));
                user.setRole(role);
                
                Institution institution = new Institution();
                institution.setId(user.getInstitutionId());
                institution.setName(rs.getString("institution_name"));
                user.setInstitution(institution);
                
                return user;
            });
            
            if (users.isEmpty()) {
                return Optional.empty();
            }
            
            User user = users.get(0);
            
            // 2. Consultar datos del perfil de estudiante
            String profileSql = "SELECT * FROM student_profile WHERE user_id = ?";
            
            List<StudentProfile> profiles = jdbcTemplate.query(profileSql, new Object[]{userId}, (rs, rowNum) -> {
                StudentProfile profile = new StudentProfile();
                profile.setId(rs.getLong("id"));
                profile.setUserId(rs.getLong("user_id"));
                profile.setGuardianProfileId(rs.getLong("guardian_profile_id"));
                profile.setUsername(rs.getString("username"));
                profile.setBirthDate(rs.getDate("birth_date"));
                profile.setPointsAmount(rs.getInt("points_amount"));
                profile.setCreatedAt(rs.getTimestamp("created_at"));
                profile.setUpdatedAt(rs.getTimestamp("updated_at"));
                return profile;
            });
            
            if (profiles.isEmpty()) {
                return Optional.empty();
            }
            
            StudentProfile profile = profiles.get(0);
            
            // 3. Crear y retornar el objeto completo
            CompleteStudent completeStudent = new CompleteStudent(
                user.getId(), user.getRoleId(), user.getInstitutionId(),
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPassword(), user.getProfilePictureUrl(),
                user.getCreatedAt(), user.getUpdatedAt(), user.getStatus(),
                user.isEmailVerified(), null, null, // emailVerificationToken, emailVerificationExpiresAt
                null, null, user.getLastLoginAt(), // passwordResetToken, passwordResetExpiresAt
                user.getLastLoginIp(), 0, null, // failedLoginAttempts, accountLockedUntil
                profile.getId(), profile.getUserId(), profile.getGuardianProfileId(),
                profile.getUsername(), profile.getBirthDate(), profile.getPointsAmount(),
                profile.getCreatedAt(), profile.getUpdatedAt(),
                user.getRole(), user.getInstitution()
            );
            
            return Optional.of(completeStudent);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<CompleteTeacher> findCompleteTeacherById(Long userId) {
        try {
            // 1. Consultar datos del usuario
            String userSql = "SELECT u.*, r.name as role_name, i.name as institution_name " +
                             "FROM [user] u " +
                             "JOIN role r ON u.role_id = r.id " +
                             "JOIN institution i ON u.institution_id = i.id " +
                             "WHERE u.id = ?";
            
            List<User> users = jdbcTemplate.query(userSql, new Object[]{userId}, (rs, rowNum) -> {
                User user = mapUserFromResultSet(rs);
                // Establecer objetos relacionados
                Role role = new Role();
                role.setId(user.getRoleId());
                role.setName(rs.getString("role_name"));
                user.setRole(role);
                
                Institution institution = new Institution();
                institution.setId(user.getInstitutionId());
                institution.setName(rs.getString("institution_name"));
                user.setInstitution(institution);
                
                return user;
            });
            
            if (users.isEmpty()) {
                return Optional.empty();
            }
            
            User user = users.get(0);
            
            // 2. Consultar datos del perfil de profesor
            String profileSql = "SELECT * FROM teacher_profile WHERE user_id = ?";
            
            List<TeacherProfile> profiles = jdbcTemplate.query(profileSql, new Object[]{userId}, (rs, rowNum) -> {
                TeacherProfile profile = new TeacherProfile();
                profile.setId(rs.getLong("id"));
                profile.setUserId(rs.getLong("user_id"));
                profile.setEmailVerified(rs.getBoolean("email_verified"));
                profile.setStemAreaId(rs.getByte("stem_area_id"));
                profile.setCreatedAt(rs.getTimestamp("created_at"));
                profile.setUpdatedAt(rs.getTimestamp("updated_at"));
                return profile;
            });
            
            if (profiles.isEmpty()) {
                return Optional.empty();
            }
            
            TeacherProfile profile = profiles.get(0);
            
            // 3. Crear y retornar el objeto completo
            CompleteTeacher completeTeacher = new CompleteTeacher(
                user.getId(), user.getRoleId(), user.getInstitutionId(),
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPassword(), user.getProfilePictureUrl(),
                user.getCreatedAt(), user.getUpdatedAt(), user.getStatus(),
                user.isEmailVerified(), null, null, // emailVerificationToken, emailVerificationExpiresAt
                null, null, user.getLastLoginAt(), // passwordResetToken, passwordResetExpiresAt
                user.getLastLoginIp(), 0, null, // failedLoginAttempts, accountLockedUntil
                profile.getId(), profile.getUserId(), profile.getEmailVerified(), 
                profile.getStemAreaId(), profile.getCreatedAt(), profile.getUpdatedAt(),
                user.getRole(), user.getInstitution()
            );
            
            return Optional.of(completeTeacher);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<CompleteGuardian> findCompleteGuardianById(Long userId) {
        try {
            // 1. Consultar datos del usuario
            String userSql = "SELECT u.*, r.name as role_name, i.name as institution_name " +
                             "FROM [user] u " +
                             "JOIN role r ON u.role_id = r.id " +
                             "JOIN institution i ON u.institution_id = i.id " +
                             "WHERE u.id = ?";
            
            List<User> users = jdbcTemplate.query(userSql, new Object[]{userId}, (rs, rowNum) -> {
                User user = mapUserFromResultSet(rs);
                // Establecer objetos relacionados
                Role role = new Role();
                role.setId(user.getRoleId());
                role.setName(rs.getString("role_name"));
                user.setRole(role);
                
                Institution institution = new Institution();
                institution.setId(user.getInstitutionId());
                institution.setName(rs.getString("institution_name"));
                user.setInstitution(institution);
                
                return user;
            });
            
            if (users.isEmpty()) {
                return Optional.empty();
            }
            
            User user = users.get(0);
            
            // 2. Consultar datos del perfil de guardián
            String profileSql = "SELECT * FROM guardian_profile WHERE user_id = ?";
            
            List<GuardianProfile> profiles = jdbcTemplate.query(profileSql, new Object[]{userId}, (rs, rowNum) -> {
                GuardianProfile profile = new GuardianProfile();
                profile.setId(rs.getLong("id"));
                profile.setUserId(rs.getLong("user_id"));
                profile.setPhone(rs.getString("phone"));
                profile.setCreatedAt(rs.getTimestamp("created_at"));
                profile.setUpdatedAt(rs.getTimestamp("updated_at"));
                return profile;
            });
            
            if (profiles.isEmpty()) {
                return Optional.empty();
            }
            
            GuardianProfile profile = profiles.get(0);
            
            // 3. Crear y retornar el objeto completo
            CompleteGuardian completeGuardian = new CompleteGuardian(
                user.getId(), user.getRoleId(), user.getInstitutionId(),
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPassword(), user.getProfilePictureUrl(),
                user.getCreatedAt(), user.getUpdatedAt(), user.getStatus(),
                user.isEmailVerified(), null, null, // emailVerificationToken, emailVerificationExpiresAt
                null, null, user.getLastLoginAt(), // passwordResetToken, passwordResetExpiresAt
                user.getLastLoginIp(), 0, null, // failedLoginAttempts, accountLockedUntil
                profile.getId(), profile.getUserId(),
                profile.getPhone(), profile.getCreatedAt(), profile.getUpdatedAt(),
                user.getRole(), user.getInstitution()
            );
            
            return Optional.of(completeGuardian);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<CompleteStudent> findCompleteStudentByUsername(String username) {
        // TODO: Implementar búsqueda de estudiante por nombre de usuario
        return Optional.empty();
    }

    @Override
    public Optional<Object> findCompleteUserByEmail(String email) {
        // TODO: Implementar búsqueda de usuario por email
        return Optional.empty();
    }

    @Override
    public List<CompleteStudent> findStudentsByGuardian(Long guardianUserId) {
        try {
            // 1. Obtener el perfil del guardián
            String guardianProfileSql = "SELECT id FROM guardian_profile WHERE user_id = ?";
            Long guardianProfileId = jdbcTemplate.queryForObject(guardianProfileSql, new Object[]{guardianUserId}, Long.class);
            
            if (guardianProfileId == null) {
                return new ArrayList<>();
            }
            
            // 2. Obtener los IDs de los estudiantes asociados al guardián
            String studentSql = "SELECT user_id FROM student_profile WHERE guardian_profile_id = ?";
            List<Long> studentUserIds = jdbcTemplate.queryForList(studentSql, new Object[]{guardianProfileId}, Long.class);
            
            // 3. Obtener los objetos completos de cada estudiante
            List<CompleteStudent> students = new ArrayList<>();
            for (Long studentUserId : studentUserIds) {
                Optional<CompleteStudent> studentOpt = findCompleteStudentById(studentUserId);
                studentOpt.ifPresent(students::add);
            }
            
            return students;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public Result<CompleteStudent> updateCompleteStudent(CompleteStudent completeStudent) {
        // TODO: Implementar actualización de estudiante completo
        return Result.failure("Método no implementado aún");
    }

    @Override
    @Transactional
    public Result<CompleteTeacher> updateCompleteTeacher(CompleteTeacher completeTeacher) {
        // TODO: Implementar actualización de profesor completo
        return Result.failure("Método no implementado aún");
    }

    @Override
    @Transactional
    public Result<CompleteGuardian> updateCompleteGuardian(CompleteGuardian completeGuardian) {
        // TODO: Implementar actualización de guardián completo
        return Result.failure("Método no implementado aún");
    }
    
    /**
     * Mapea un ResultSet a un objeto User
     */
    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getLong("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("profile_picture_url"),
            rs.getBoolean("status"),
            rs.getBoolean("email_verified"),
            rs.getString("email_verification_token"),
            rs.getTimestamp("email_verification_expires_at"),
            rs.getString("password_reset_token"),
            rs.getTimestamp("password_reset_expires_at"),
            rs.getTimestamp("last_login_at"),
            rs.getString("last_login_ip"),
            rs.getInt("failed_login_attempts"),
            rs.getTimestamp("account_locked_until"),
            rs.getTimestamp("created_at"),
            rs.getTimestamp("updated_at"),
            rs.getByte("role_id"),
            rs.getLong("institution_id")
        );
        return user;
    }
} 