package com.gamified.application.clasroom.repository;

import com.gamified.application.clasroom.model.entity.Classroom;
import com.gamified.application.clasroom.model.entity.Enrollment;
import com.gamified.application.clasroom.model.dto.response.ClassroomResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del repositorio de Classroom usando JDBC Template
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ClassroomRepositoryImpl implements ClassroomRepository {

    private final JdbcTemplate jdbcTemplate;

    // ===================================================================
    // CLASSROOM OPERATIONS
    // ===================================================================

    @Override
    public Integer createClassroom(Classroom classroom) {
        String sql = """
            INSERT INTO classroom (teacher_profile_id, grade, section, year, name, status, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, 1, GETDATE(), GETDATE())
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, classroom.getTeacherProfileId());
            ps.setString(2, classroom.getGrade());
            ps.setString(3, classroom.getSection());
            ps.setString(4, classroom.getYear());
            ps.setString(5, classroom.getName());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public Optional<Classroom> findClassroomById(Integer classroomId) {
        String sql = """
            SELECT id, teacher_profile_id, grade, section, year, name, status, created_at, updated_at
            FROM classroom
            WHERE id = ?
            """;

        try {
            Classroom classroom = jdbcTemplate.queryForObject(sql, classroomRowMapper(), classroomId);
            return Optional.ofNullable(classroom);
        } catch (Exception e) {
            log.debug("Classroom not found with ID: {}", classroomId);
            return Optional.empty();
        }
    }

    @Override
    public List<ClassroomResponseDto.ClassroomDto> findClassroomsByTeacher(Integer teacherProfileId) {
        String sql = """
            SELECT c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at,
                   COUNT(e.student_profile_id) as enrolled_count
            FROM classroom c
            LEFT JOIN enrollment e ON c.id = e.classroom_id AND e.status = 1
            WHERE c.teacher_profile_id = ? AND c.status = 1
            GROUP BY c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at
            ORDER BY c.created_at DESC
            """;

        return jdbcTemplate.query(sql, classroomDtoRowMapper(), teacherProfileId);
    }

    @Override
    public boolean updateClassroom(Classroom classroom) {
        String sql = """
            UPDATE classroom
            SET grade = ?, section = ?, year = ?, name = ?, status = ?, updated_at = GETDATE()
            WHERE id = ?
            """;

        int rowsAffected = jdbcTemplate.update(sql,
                classroom.getGrade(),
                classroom.getSection(),
                classroom.getYear(),
                classroom.getName(),
                classroom.getStatus(),
                classroom.getId());

        return rowsAffected > 0;
    }

    @Override
    public boolean verifyClassroomOwnership(Integer classroomId, Integer teacherProfileId) {
        String sql = "SELECT COUNT(*) FROM classroom WHERE id = ? AND teacher_profile_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, classroomId, teacherProfileId);
        return count != null && count > 0;
    }

    @Override
    public Optional<ClassroomResponseDto.ClassroomDetailDto> findClassroomDetailById(Integer classroomId) {
        // Primero obtenemos los datos básicos del classroom
        String classroomSql = """
            SELECT c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at,
                   COUNT(e.student_profile_id) as enrolled_count
            FROM classroom c
            LEFT JOIN enrollment e ON c.id = e.classroom_id AND e.status = 1
            WHERE c.id = ?
            GROUP BY c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at
            """;

        try {
            ClassroomResponseDto.ClassroomDto classroomDto = jdbcTemplate.queryForObject(classroomSql, classroomDtoRowMapper(), classroomId);
            
            // Obtenemos los estudiantes del classroom
            List<ClassroomResponseDto.StudentInClassroomDto> students = findStudentsByClassroom(classroomId);

            ClassroomResponseDto.ClassroomDetailDto detail = ClassroomResponseDto.ClassroomDetailDto.builder()
                    .classroomId(classroomDto.getClassroomId())
                    .name(classroomDto.getName())
                    .grade(classroomDto.getGrade())
                    .section(classroomDto.getSection())
                    .year(classroomDto.getYear())
                    .enrolledStudentsCount(students.size())
                    .status(classroomDto.getStatus())
                    .createdAt(classroomDto.getCreatedAt())
                    .updatedAt(classroomDto.getUpdatedAt())
                    .students(students)
                    .build();

            return Optional.of(detail);
        } catch (Exception e) {
            log.debug("Classroom detail not found for ID: {}", classroomId);
            return Optional.empty();
        }
    }

    // ===================================================================
    // ENROLLMENT OPERATIONS
    // ===================================================================

    @Override
    public Integer enrollStudent(Enrollment enrollment) {
        String sql = """
            INSERT INTO enrollment (classroom_id, student_profile_id, joined_at, status)
            VALUES (?, ?, GETDATE(), 1)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, enrollment.getClassroomId());
            ps.setInt(2, enrollment.getStudentProfileId());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    @Override
    public boolean isStudentEnrolled(Integer classroomId, Integer studentProfileId) {
        String sql = "SELECT COUNT(*) FROM enrollment WHERE classroom_id = ? AND student_profile_id = ? AND status = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, classroomId, studentProfileId);
        return count != null && count > 0;
    }

    @Override
    public List<ClassroomResponseDto.StudentInClassroomDto> findStudentsByClassroom(Integer classroomId) {
        String sql = """
            SELECT 
                u.id as student_user_id, sp.id as student_profile_id,
                u.first_name, u.last_name, u.email, u.profile_picture_url,
                sp.username, sp.birth_date, sp.points_amount,
                u.status, u.email_verified, u.last_login_at, e.joined_at,
                -- Guardian info
                gu.id as guardian_user_id, gp.id as guardian_profile_id,
                gu.first_name as guardian_first_name, gu.last_name as guardian_last_name,
                gu.email as guardian_email, gp.phone as guardian_phone,
                gu.profile_picture_url as guardian_picture, gu.last_login_at as guardian_last_login
            FROM enrollment e
            JOIN student_profile sp ON e.student_profile_id = sp.id
            JOIN [user] u ON sp.user_id = u.id
            LEFT JOIN guardian_profile gp ON sp.guardian_profile_id = gp.id
            LEFT JOIN [user] gu ON gp.user_id = gu.id
            WHERE e.classroom_id = ? AND e.status = 1
            ORDER BY u.first_name, u.last_name
            """;

        return jdbcTemplate.query(sql, studentInClassroomRowMapper(), classroomId);
    }

    @Override
    public boolean unenrollStudent(Integer classroomId, Integer studentProfileId) {
        String sql = "UPDATE enrollment SET status = 0 WHERE classroom_id = ? AND student_profile_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, classroomId, studentProfileId);
        return rowsAffected > 0;
    }

    @Override
    public Integer countActiveStudentsInClassroom(Integer classroomId) {
        String sql = "SELECT COUNT(*) FROM enrollment WHERE classroom_id = ? AND status = 1";
        return jdbcTemplate.queryForObject(sql, Integer.class, classroomId);
    }

    @Override
    public List<ClassroomResponseDto.ClassroomDto> findClassroomsByStudent(Integer studentProfileId) {
        String sql = """
            SELECT c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at,
                   COUNT(e2.student_profile_id) as enrolled_count
            FROM classroom c
            JOIN enrollment e ON c.id = e.classroom_id AND e.status = 1
            LEFT JOIN enrollment e2 ON c.id = e2.classroom_id AND e2.status = 1
            WHERE e.student_profile_id = ? AND c.status = 1
            GROUP BY c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at
            ORDER BY c.created_at DESC
            """;

        return jdbcTemplate.query(sql, classroomDtoRowMapper(), studentProfileId);
    }

    @Override
    public Optional<String> findStudentNameByProfileId(Integer studentProfileId) {
        String sql = """
            SELECT u.first_name + ' ' + u.last_name as full_name
            FROM student_profile sp
            JOIN [user] u ON sp.user_id = u.id
            WHERE sp.id = ?
            """;

        try {
            String fullName = jdbcTemplate.queryForObject(sql, String.class, studentProfileId);
            return Optional.ofNullable(fullName);
        } catch (Exception e) {
            log.debug("Student name not found for profile ID: {}", studentProfileId);
            return Optional.empty();
        }
    }

    // ===================================================================
    // STATISTICS OPERATIONS
    // ===================================================================

    @Override
    public Optional<ClassroomResponseDto.ClassroomStatsDto> getClassroomStats(Integer classroomId) {
        // Esta implementación es básica. Puede expandirse según necesidades
        String sql = """
            SELECT 
                c.id as classroom_id,
                c.name as classroom_name,
                COUNT(e.student_profile_id) as total_students,
                COUNT(CASE WHEN u.status = 1 THEN 1 END) as active_students,
                COALESCE(SUM(sp.points_amount), 0) as total_points_earned,
                MAX(u.last_login_at) as last_activity
            FROM classroom c
            LEFT JOIN enrollment e ON c.id = e.classroom_id AND e.status = 1
            LEFT JOIN student_profile sp ON e.student_profile_id = sp.id
            LEFT JOIN [user] u ON sp.user_id = u.id
            WHERE c.id = ?
            GROUP BY c.id, c.name
            """;

        try {
            ClassroomResponseDto.ClassroomStatsDto stats = jdbcTemplate.queryForObject(sql, classroomStatsRowMapper(), classroomId);
            return Optional.ofNullable(stats);
        } catch (Exception e) {
            log.debug("Classroom stats not found for ID: {}", classroomId);
            return Optional.empty();
        }
    }

    // ===================================================================
    // ROW MAPPERS
    // ===================================================================

    private RowMapper<Classroom> classroomRowMapper() {
        return (rs, rowNum) -> Classroom.builder()
                .id(rs.getInt("id"))
                .teacherProfileId(rs.getInt("teacher_profile_id"))
                .grade(rs.getString("grade"))
                .section(rs.getString("section"))
                .year(rs.getString("year"))
                .name(rs.getString("name"))
                .status(rs.getInt("status"))
                .createdAt(rs.getTimestamp("created_at") != null ? 
                        rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ? 
                        rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }

    private RowMapper<ClassroomResponseDto.ClassroomDto> classroomDtoRowMapper() {
        return (rs, rowNum) -> ClassroomResponseDto.ClassroomDto.builder()
                .classroomId(rs.getInt("id"))
                .name(rs.getString("name"))
                .grade(rs.getString("grade"))
                .section(rs.getString("section"))
                .year(rs.getString("year"))
                .enrolledStudentsCount(rs.getInt("enrolled_count"))
                .status(rs.getBoolean("status"))
                .createdAt(rs.getTimestamp("created_at") != null ? 
                        rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ? 
                        rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }

    private RowMapper<ClassroomResponseDto.StudentInClassroomDto> studentInClassroomRowMapper() {
        return (rs, rowNum) -> {
            ClassroomResponseDto.StudentInClassroomDto.StudentInClassroomDtoBuilder builder = 
                    ClassroomResponseDto.StudentInClassroomDto.builder()
                    .studentProfileId(rs.getInt("student_profile_id"))
                    .userId(rs.getInt("student_user_id"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .fullName(rs.getString("first_name") + " " + rs.getString("last_name"))
                    .username(rs.getString("username"))
                    .email(rs.getString("email"))
                    .pointsAmount(rs.getInt("points_amount"))
                    .profilePictureUrl(rs.getString("profile_picture_url"))
                    .status(rs.getBoolean("status"))
                    .emailVerified(rs.getBoolean("email_verified"))
                    .enrolledAt(rs.getTimestamp("joined_at") != null ? 
                            rs.getTimestamp("joined_at").toLocalDateTime() : null)
                    .lastLoginAt(rs.getTimestamp("last_login_at") != null ? 
                            rs.getTimestamp("last_login_at").toLocalDateTime() : null);

            // Guardian info (opcional)
            if (rs.getObject("guardian_profile_id") != null) {
                ClassroomResponseDto.GuardianInfoDto guardian = ClassroomResponseDto.GuardianInfoDto.builder()
                        .guardianProfileId(rs.getInt("guardian_profile_id"))
                        .userId(rs.getInt("guardian_user_id"))
                        .firstName(rs.getString("guardian_first_name"))
                        .lastName(rs.getString("guardian_last_name"))
                        .fullName(rs.getString("guardian_first_name") + " " + rs.getString("guardian_last_name"))
                        .email(rs.getString("guardian_email"))
                        .phone(rs.getString("guardian_phone"))
                        .profilePictureUrl(rs.getString("guardian_picture"))
                        .lastLoginAt(rs.getTimestamp("guardian_last_login") != null ? 
                                rs.getTimestamp("guardian_last_login").toLocalDateTime() : null)
                        .build();
                builder.guardian(guardian);
            }

            return builder.build();
        };
    }

    private RowMapper<ClassroomResponseDto.ClassroomStatsDto> classroomStatsRowMapper() {
        return (rs, rowNum) -> ClassroomResponseDto.ClassroomStatsDto.builder()
                .classroomId(rs.getInt("classroom_id"))
                .classroomName(rs.getString("classroom_name"))
                .totalStudents(rs.getInt("total_students"))
                .activeStudents(rs.getInt("active_students"))
                .totalExercisesCompleted(0) // Implementar cuando se integre con exercise module
                .averageProgress(0.0) // Implementar cuando se integre con progress module
                .totalPointsEarned(rs.getInt("total_points_earned"))
                .lastActivity(rs.getTimestamp("last_activity") != null ? 
                        rs.getTimestamp("last_activity").toLocalDateTime() : null)
                .build();
    }
} 