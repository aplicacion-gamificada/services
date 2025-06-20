package com.gamified.application.user.service;

import com.gamified.application.user.model.dto.response.UserResponseDto;
import com.gamified.application.user.repository.composite.CompleteUserRepository;
import com.gamified.application.user.model.entity.composite.CompleteTeacher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para operaciones específicas de teachers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherServiceImpl implements TeacherService {

    private final JdbcTemplate jdbcTemplate;
    private final CompleteUserRepository completeUserRepository;

    @Override
    public UserResponseDto.TeacherRelatedUsersDto getRelatedUsers(Long teacherUserId) {
        try {
            log.info("Obteniendo usuarios relacionados para teacher: {}", teacherUserId);

            // 1. Obtener el teacher profile ID
            Optional<CompleteTeacher> teacherOpt = completeUserRepository.findCompleteTeacherById(teacherUserId);
            if (teacherOpt.isEmpty()) {
                throw new IllegalArgumentException("Teacher no encontrado con ID: " + teacherUserId);
            }

            CompleteTeacher teacher = teacherOpt.get();
            Long teacherProfileId = teacher.getTeacherProfile().getId();
            String teacherName = teacher.getUser().getFirstName() + " " + teacher.getUser().getLastName();

            // 2. Obtener classrooms con estudiantes
            List<UserResponseDto.ClassroomWithStudentsDto> classrooms = getClassroomsWithStudents(teacherProfileId);

            // 3. Recopilar todos los guardianes únicos
            Set<Long> guardianIds = new HashSet<>();
            int totalStudents = 0;
            
            for (UserResponseDto.ClassroomWithStudentsDto classroom : classrooms) {
                totalStudents += classroom.getStudents().size();
                for (UserResponseDto.StudentWithGuardianDto student : classroom.getStudents()) {
                    if (student.getGuardian() != null) {
                        guardianIds.add(student.getGuardian().getGuardianUserId());
                    }
                }
            }

            List<UserResponseDto.GuardianSummaryDto> uniqueGuardians = getGuardiansSummary(guardianIds);

            // 4. Generar estadísticas
            UserResponseDto.TeacherStatsDto stats = generateTeacherStats(teacherProfileId, totalStudents, 
                    uniqueGuardians.size(), classrooms.size());

            return UserResponseDto.TeacherRelatedUsersDto.builder()
                    .teacherId(teacherUserId)
                    .teacherName(teacherName)
                    .classroomsCount(classrooms.size())
                    .totalStudents(totalStudents)
                    .totalGuardians(uniqueGuardians.size())
                    .classrooms(classrooms)
                    .uniqueGuardians(uniqueGuardians)
                    .statistics(stats)
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error obteniendo usuarios relacionados para teacher {}: {}", teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener usuarios relacionados: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserResponseDto.ClassroomDto> getTeacherClassrooms(Long teacherUserId) {
        try {
            // Obtener teacher profile ID
            Optional<CompleteTeacher> teacherOpt = completeUserRepository.findCompleteTeacherById(teacherUserId);
            if (teacherOpt.isEmpty()) {
                throw new IllegalArgumentException("Teacher no encontrado con ID: " + teacherUserId);
            }

            Long teacherProfileId = teacherOpt.get().getTeacherProfile().getId();

            String sql = """
                SELECT c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at,
                       COUNT(e.student_profile_id) as enrolled_count
                FROM classroom c
                LEFT JOIN enrollment e ON c.id = e.classroom_id
                WHERE c.teacher_profile_id = ? AND c.status = 1
                GROUP BY c.id, c.name, c.grade, c.section, c.year, c.status, c.created_at, c.updated_at
                ORDER BY c.created_at DESC
                """;

            return jdbcTemplate.query(sql, new Object[]{teacherProfileId}, (rs, rowNum) -> {
                return UserResponseDto.ClassroomDto.builder()
                        .classroomId(rs.getLong("id"))
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
            });

        } catch (Exception e) {
            log.error("Error obteniendo classrooms para teacher {}: {}", teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener classrooms: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UserResponseDto.StudentWithGuardianDto> getStudentsByClassroom(Long teacherUserId, Long classroomId) {
        try {
            // Verificar ownership del classroom
            if (!verifyClassroomOwnership(teacherUserId, classroomId)) {
                throw new IllegalArgumentException("El classroom no pertenece al teacher especificado");
            }

            return getStudentsForClassroom(classroomId);

        } catch (Exception e) {
            log.error("Error obteniendo estudiantes del classroom {} para teacher {}: {}", 
                    classroomId, teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener estudiantes del classroom: " + e.getMessage(), e);
        }
    }

    @Override
    public UserResponseDto.TeacherStatsDto getTeacherStats(Long teacherUserId) {
        try {
            // Obtener teacher profile ID
            Optional<CompleteTeacher> teacherOpt = completeUserRepository.findCompleteTeacherById(teacherUserId);
            if (teacherOpt.isEmpty()) {
                throw new IllegalArgumentException("Teacher no encontrado con ID: " + teacherUserId);
            }

            Long teacherProfileId = teacherOpt.get().getTeacherProfile().getId();
            return generateTeacherStats(teacherProfileId, null, null, null);

        } catch (Exception e) {
            log.error("Error obteniendo estadísticas para teacher {}: {}", teacherUserId, e.getMessage(), e);
            throw new RuntimeException("Error al obtener estadísticas: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyClassroomOwnership(Long teacherUserId, Long classroomId) {
        try {
            // Obtener teacher profile ID
            Optional<CompleteTeacher> teacherOpt = completeUserRepository.findCompleteTeacherById(teacherUserId);
            if (teacherOpt.isEmpty()) {
                return false;
            }

            Long teacherProfileId = teacherOpt.get().getTeacherProfile().getId();

            String sql = "SELECT COUNT(*) FROM classroom WHERE id = ? AND teacher_profile_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, new Object[]{classroomId, teacherProfileId}, Integer.class);
            
            return count != null && count > 0;

        } catch (Exception e) {
            log.error("Error verificando ownership del classroom {} para teacher {}: {}", 
                    classroomId, teacherUserId, e.getMessage(), e);
            return false;
        }
    }

    // ===========================
    // MÉTODOS PRIVADOS DE APOYO
    // ===========================

    private List<UserResponseDto.ClassroomWithStudentsDto> getClassroomsWithStudents(Long teacherProfileId) {
        String sql = """
            SELECT c.id, c.name, c.grade, c.section, c.year, c.created_at,
                   COUNT(e.student_profile_id) as enrolled_count
            FROM classroom c
            LEFT JOIN enrollment e ON c.id = e.classroom_id AND e.status = 1
            WHERE c.teacher_profile_id = ? AND c.status = 1
            GROUP BY c.id, c.name, c.grade, c.section, c.year, c.created_at
            ORDER BY c.created_at DESC
            """;

        List<UserResponseDto.ClassroomWithStudentsDto> classrooms = jdbcTemplate.query(sql, 
                new Object[]{teacherProfileId}, (rs, rowNum) -> {
            return UserResponseDto.ClassroomWithStudentsDto.builder()
                    .classroomId(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .grade(rs.getString("grade"))
                    .section(rs.getString("section"))
                    .year(rs.getString("year"))
                    .enrolledStudentsCount(rs.getInt("enrolled_count"))
                    .createdAt(rs.getTimestamp("created_at") != null ? 
                            rs.getTimestamp("created_at").toLocalDateTime() : null)
                    .students(new ArrayList<>())
                    .build();
        });

        // Para cada classroom, obtener sus estudiantes
        for (UserResponseDto.ClassroomWithStudentsDto classroom : classrooms) {
            List<UserResponseDto.StudentWithGuardianDto> students = getStudentsForClassroom(classroom.getClassroomId());
            classroom.setStudents(students);
            classroom.setEnrolledStudentsCount(students.size());
        }

        return classrooms;
    }

    private List<UserResponseDto.StudentWithGuardianDto> getStudentsForClassroom(Long classroomId) {
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

        return jdbcTemplate.query(sql, new Object[]{classroomId}, (rs, rowNum) -> {
            UserResponseDto.GuardianSummaryDto guardian = null;
            
            // Si hay guardián, crear el objeto
            if (rs.getLong("guardian_user_id") != 0) {
                guardian = UserResponseDto.GuardianSummaryDto.builder()
                        .guardianUserId(rs.getLong("guardian_user_id"))
                        .guardianProfileId(rs.getLong("guardian_profile_id"))
                        .firstName(rs.getString("guardian_first_name"))
                        .lastName(rs.getString("guardian_last_name"))
                        .fullName(rs.getString("guardian_first_name") + " " + rs.getString("guardian_last_name"))
                        .email(rs.getString("guardian_email"))
                        .phone(rs.getString("guardian_phone"))
                        .profilePictureUrl(rs.getString("guardian_picture"))
                        .lastLoginAt(rs.getTimestamp("guardian_last_login") != null ? 
                                rs.getTimestamp("guardian_last_login").toLocalDateTime() : null)
                        .build();
            }

            return UserResponseDto.StudentWithGuardianDto.builder()
                    .studentUserId(rs.getLong("student_user_id"))
                    .studentProfileId(rs.getLong("student_profile_id"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .fullName(rs.getString("first_name") + " " + rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .username(rs.getString("username"))
                    .birthDate(rs.getDate("birth_date"))
                    .pointsAmount(rs.getInt("points_amount"))
                    .profilePictureUrl(rs.getString("profile_picture_url"))
                    .guardian(guardian)
                    .status(rs.getBoolean("status"))
                    .emailVerified(rs.getBoolean("email_verified"))
                    .enrolledAt(rs.getTimestamp("joined_at") != null ? 
                            rs.getTimestamp("joined_at").toLocalDateTime() : null)
                    .lastLoginAt(rs.getTimestamp("last_login_at") != null ? 
                            rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                    .build();
        });
    }

    private List<UserResponseDto.GuardianSummaryDto> getGuardiansSummary(Set<Long> guardianIds) {
        if (guardianIds.isEmpty()) {
            return new ArrayList<>();
        }

        String inClause = guardianIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = "SELECT " +
                "u.id as guardian_user_id, gp.id as guardian_profile_id, " +
                "u.first_name, u.last_name, u.email, gp.phone, " +
                "u.profile_picture_url, u.last_login_at, " +
                "COUNT(sp.id) as total_students " +
                "FROM [user] u " +
                "JOIN guardian_profile gp ON u.id = gp.user_id " +
                "LEFT JOIN student_profile sp ON gp.id = sp.guardian_profile_id " +
                "WHERE u.id IN (" + inClause + ") " +
                "GROUP BY u.id, gp.id, u.first_name, u.last_name, u.email, gp.phone, " +
                "u.profile_picture_url, u.last_login_at " +
                "ORDER BY u.first_name, u.last_name";

        return jdbcTemplate.query(sql, guardianIds.toArray(), (rs, rowNum) -> {
            return UserResponseDto.GuardianSummaryDto.builder()
                    .guardianUserId(rs.getLong("guardian_user_id"))
                    .guardianProfileId(rs.getLong("guardian_profile_id"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .fullName(rs.getString("first_name") + " " + rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .phone(rs.getString("phone"))
                    .profilePictureUrl(rs.getString("profile_picture_url"))
                    .totalStudentsCount(rs.getInt("total_students"))
                    .lastLoginAt(rs.getTimestamp("last_login_at") != null ? 
                            rs.getTimestamp("last_login_at").toLocalDateTime() : null)
                    .build();
        });
    }

    private UserResponseDto.TeacherStatsDto generateTeacherStats(Long teacherProfileId, 
            Integer totalStudents, Integer totalGuardians, Integer totalClassrooms) {
        
        try {
            // Si los parámetros no fueron proporcionados, calcularlos
            if (totalClassrooms == null) {
                String classroomSql = "SELECT COUNT(*) FROM classroom WHERE teacher_profile_id = ? AND status = 1";
                totalClassrooms = jdbcTemplate.queryForObject(classroomSql, new Object[]{teacherProfileId}, Integer.class);
                if (totalClassrooms == null) totalClassrooms = 0;
            }

            if (totalStudents == null) {
                String studentSql = """
                    SELECT COUNT(DISTINCT sp.id) 
                    FROM enrollment e
                    JOIN classroom c ON e.classroom_id = c.id
                    JOIN student_profile sp ON e.student_profile_id = sp.id
                    WHERE c.teacher_profile_id = ? AND e.status = 1 AND c.status = 1
                    """;
                totalStudents = jdbcTemplate.queryForObject(studentSql, new Object[]{teacherProfileId}, Integer.class);
                if (totalStudents == null) totalStudents = 0;
            }

            if (totalGuardians == null) {
                String guardianSql = """
                    SELECT COUNT(DISTINCT gp.id)
                    FROM enrollment e
                    JOIN classroom c ON e.classroom_id = c.id
                    JOIN student_profile sp ON e.student_profile_id = sp.id
                    JOIN guardian_profile gp ON sp.guardian_profile_id = gp.id
                    WHERE c.teacher_profile_id = ? AND e.status = 1 AND c.status = 1
                    """;
                totalGuardians = jdbcTemplate.queryForObject(guardianSql, new Object[]{teacherProfileId}, Integer.class);
                if (totalGuardians == null) totalGuardians = 0;
            }

            // Calcular estudiantes con y sin guardián
            String studentsWithGuardianSql = """
                SELECT COUNT(DISTINCT sp.id)
                FROM enrollment e
                JOIN classroom c ON e.classroom_id = c.id
                JOIN student_profile sp ON e.student_profile_id = sp.id
                WHERE c.teacher_profile_id = ? AND e.status = 1 AND c.status = 1 
                AND sp.guardian_profile_id IS NOT NULL
                """;
            Integer studentsWithGuardian = jdbcTemplate.queryForObject(studentsWithGuardianSql, 
                    new Object[]{teacherProfileId}, Integer.class);
            if (studentsWithGuardian == null) studentsWithGuardian = 0;

            Integer studentsWithoutGuardian = totalStudents - studentsWithGuardian;
            Double guardianAssignmentPercentage = totalStudents > 0 ? 
                    (studentsWithGuardian.doubleValue() / totalStudents.doubleValue()) * 100 : 0.0;

            Integer averageStudentsPerClassroom = totalClassrooms > 0 ? 
                    totalStudents / totalClassrooms : 0;

            // Actividad reciente (última semana)
            String activeStudentsSql = """
                SELECT COUNT(DISTINCT sp.id)
                FROM enrollment e
                JOIN classroom c ON e.classroom_id = c.id
                JOIN student_profile sp ON e.student_profile_id = sp.id
                JOIN [user] u ON sp.user_id = u.id
                WHERE c.teacher_profile_id = ? AND e.status = 1 AND c.status = 1
                AND u.last_login_at >= DATEADD(day, -7, GETDATE())
                """;
            Integer activeStudentsLastWeek = jdbcTemplate.queryForObject(activeStudentsSql, 
                    new Object[]{teacherProfileId}, Integer.class);
            if (activeStudentsLastWeek == null) activeStudentsLastWeek = 0;

            String activeGuardiansSql = """
                SELECT COUNT(DISTINCT gp.id)
                FROM enrollment e
                JOIN classroom c ON e.classroom_id = c.id
                JOIN student_profile sp ON e.student_profile_id = sp.id
                JOIN guardian_profile gp ON sp.guardian_profile_id = gp.id
                JOIN [user] gu ON gp.user_id = gu.id
                WHERE c.teacher_profile_id = ? AND e.status = 1 AND c.status = 1
                AND gu.last_login_at >= DATEADD(day, -7, GETDATE())
                """;
            Integer activeGuardiansLastWeek = jdbcTemplate.queryForObject(activeGuardiansSql, 
                    new Object[]{teacherProfileId}, Integer.class);
            if (activeGuardiansLastWeek == null) activeGuardiansLastWeek = 0;

            return UserResponseDto.TeacherStatsDto.builder()
                    .totalClassrooms(totalClassrooms)
                    .totalStudents(totalStudents)
                    .totalGuardians(totalGuardians)
                    .studentsWithGuardian(studentsWithGuardian)
                    .studentsWithoutGuardian(studentsWithoutGuardian)
                    .guardianAssignmentPercentage(guardianAssignmentPercentage)
                    .averageStudentsPerClassroom(averageStudentsPerClassroom)
                    .activeStudentsLastWeek(activeStudentsLastWeek)
                    .activeGuardiansLastWeek(activeGuardiansLastWeek)
                    .generatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error generando estadísticas para teacher profile {}: {}", teacherProfileId, e.getMessage(), e);
            // Retornar estadísticas vacías en caso de error
            return UserResponseDto.TeacherStatsDto.builder()
                    .totalClassrooms(0)
                    .totalStudents(0)
                    .totalGuardians(0)
                    .studentsWithGuardian(0)
                    .studentsWithoutGuardian(0)
                    .guardianAssignmentPercentage(0.0)
                    .averageStudentsPerClassroom(0)
                    .activeStudentsLastWeek(0)
                    .activeGuardiansLastWeek(0)
                    .generatedAt(LocalDateTime.now())
                    .build();
        }
    }
} 