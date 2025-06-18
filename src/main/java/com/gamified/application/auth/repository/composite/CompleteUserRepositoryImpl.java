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
import com.gamified.application.auth.repository.core.UserRepository;
import com.gamified.application.auth.repository.interfaces.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del repositorio para operaciones compuestas
 */
@Repository
public class CompleteUserRepositoryImpl implements CompleteUserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    @Autowired
    public CompleteUserRepositoryImpl(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Result<CompleteStudent> createCompleteStudent(CompleteStudent completeStudent) {
        try {
            // Extraer datos del usuario y perfil de estudiante
            User user = completeStudent.getUser();
            StudentProfile studentProfile = completeStudent.getStudentProfile();
            
            // Imprimir el roleId para debugging
            System.out.println("DEBUG - Creating student with roleId: " + user.getRoleId());
            
            // 1. Crear usuario base usando sp_create_user_base existente
            Map<String, Object> userParams = new HashMap<>();
            userParams.put("email", user.getEmail());
            userParams.put("password", user.getPassword());
            userParams.put("role_id", user.getRoleId());
            userParams.put("first_name", user.getFirstName());
            userParams.put("last_name", user.getLastName());
            userParams.put("institution_id", user.getInstitutionId());
            
            // Imprimir los parámetros que se envían al stored procedure
            System.out.println("DEBUG - User params: " + userParams);
            
            SimpleJdbcCall createUserCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_create_user_base")
                    .returningResultSet("result", (rs, rowNum) -> {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("success", rs.getInt("success"));
                        resultMap.put("message", rs.getString("message"));
                        if (rs.getMetaData().getColumnCount() > 2) {
                            try {
                                resultMap.put("user_id", rs.getLong("user_id"));
                            } catch (SQLException e) {
                                // La columna user_id podría no existir o ser NULL
                            }
                        }
                        return resultMap;
                    });
            
            Map<String, Object> userResult = createUserCall.execute(userParams);
            
            // Imprimir el resultado del stored procedure
            System.out.println("DEBUG - User creation result: " + userResult);
            
            // Verificar si la creación del usuario fue exitosa
            Long userId = null;
            
            if (userResult.containsKey("result")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) userResult.get("result");
                if (!resultList.isEmpty()) {
                    Map<String, Object> result = resultList.get(0);
                    Integer success = (Integer) result.get("success");
                    if (success == null || success != 1) {
                        String errorMessage = (String) result.get("message");
                        return Result.failure("Error al crear usuario: " + errorMessage);
                    }
                    
                    // Intentar obtener el ID del usuario del resultado
                    if (result.containsKey("user_id") && result.get("user_id") != null) {
                        userId = ((Number) result.get("user_id")).longValue();
                    }
                }
            } else {
                // Verificar el resultado directamente
                Integer success = (Integer) userResult.get("success");
                if (success == null || success != 1) {
                    String errorMessage = (String) userResult.get("message");
                    return Result.failure("Error al crear usuario: " + errorMessage);
                }
                
                // Intentar obtener el ID del usuario del resultado
                if (userResult.containsKey("user_id") && userResult.get("user_id") != null) {
                    userId = ((Number) userResult.get("user_id")).longValue();
                }
            }
            
            // Si no se pudo obtener el ID del usuario del stored procedure, buscarlo por email
            if (userId == null) {
                Optional<User> createdUserOpt = userRepository.findByEmail(user.getEmail());
                if (createdUserOpt.isEmpty()) {
                    return Result.failure("No se pudo encontrar el usuario recién creado");
                }
                userId = createdUserOpt.get().getId();
            }
            
            user.setId(userId);
            
            // Imprimir el usuario creado para verificar el rol
            System.out.println("DEBUG - Created user: " + user);
            System.out.println("DEBUG - Created user ID: " + userId);
            System.out.println("DEBUG - Created user roleId: " + user.getRoleId());
            
            // 3. Crear perfil de estudiante usando sp_create_student_profile existente
            Map<String, Object> studentParams = new HashMap<>();
            studentParams.put("user_id", userId);
            studentParams.put("guardian_profile_id", studentProfile.getGuardianProfileId());
            studentParams.put("username", studentProfile.getUsername());
            studentParams.put("birth_date", studentProfile.getBirthDate());
            studentParams.put("points_amount", studentProfile.getPointsAmount());
            
            // Imprimir los parámetros del perfil de estudiante
            System.out.println("DEBUG - Student params: " + studentParams);
            
            SimpleJdbcCall createStudentCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_create_student_profile")
                    .returningResultSet("result", (rs, rowNum) -> {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("success", rs.getInt("success"));
                        resultMap.put("message", rs.getString("message"));
                        return resultMap;
                    });
            
            Map<String, Object> studentResult = createStudentCall.execute(studentParams);
            
            // Imprimir el resultado de la creación del perfil
            System.out.println("DEBUG - Student profile creation result: " + studentResult);
            
            // Verificar si la creación del perfil fue exitosa
            if (studentResult.containsKey("result")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) studentResult.get("result");
                if (!resultList.isEmpty()) {
                    Map<String, Object> result = resultList.get(0);
                    Integer success = (Integer) result.get("success");
                    if (success == null || success != 1) {
                        String errorMessage = (String) result.get("message");
                        return Result.failure("Error al crear perfil de estudiante: " + errorMessage);
                    }
                }
            } else {
                // Verificar el resultado directamente
                Integer success = (Integer) studentResult.get("success");
                if (success == null || success != 1) {
                    String errorMessage = (String) studentResult.get("message");
                    return Result.failure("Error al crear perfil de estudiante: " + errorMessage);
                }
            }
            
            // 4. Cargar el perfil completo
            Optional<CompleteStudent> completeStudentOpt = findCompleteStudentById(userId);
            if (completeStudentOpt.isPresent()) {
                return Result.success(completeStudentOpt.get());
            }
            
            // Si no se puede cargar, devolver el objeto original con los IDs actualizados
            return Result.success(completeStudent);
            
        } catch (Exception e) {
            e.printStackTrace(); // Imprimir la excepción completa
            return Result.failure("Error al crear estudiante: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<CompleteTeacher> createCompleteTeacher(CompleteTeacher completeTeacher) {
        try {
            // Extraer datos del usuario y perfil de profesor
            User user = completeTeacher.getUser();
            TeacherProfile teacherProfile = completeTeacher.getTeacherProfile();
            
            // 1. Crear usuario base usando sp_create_user_base existente
            Map<String, Object> userParams = new HashMap<>();
            userParams.put("email", user.getEmail());
            userParams.put("password", user.getPassword());
            userParams.put("role_id", user.getRoleId());
            userParams.put("first_name", user.getFirstName());
            userParams.put("last_name", user.getLastName());
            userParams.put("institution_id", user.getInstitutionId());
            
            SimpleJdbcCall createUserCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_create_user_base")
                    .returningResultSet("result", (rs, rowNum) -> {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("success", rs.getInt("success"));
                        resultMap.put("message", rs.getString("message"));
                        if (rs.getMetaData().getColumnCount() > 2) {
                            try {
                                resultMap.put("user_id", rs.getLong("user_id"));
                            } catch (SQLException e) {
                                // La columna user_id podría no existir o ser NULL
                            }
                        }
                        return resultMap;
                    });
            
            Map<String, Object> userResult = createUserCall.execute(userParams);
            
            // Verificar si la creación del usuario fue exitosa
            Long userId = null;
            
            if (userResult.containsKey("result")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) userResult.get("result");
                if (!resultList.isEmpty()) {
                    Map<String, Object> result = resultList.get(0);
                    Integer success = (Integer) result.get("success");
                    if (success == null || success != 1) {
                        String errorMessage = (String) result.get("message");
                        return Result.failure("Error al crear usuario: " + errorMessage);
                    }
                    
                    // Intentar obtener el ID del usuario del resultado
                    if (result.containsKey("user_id") && result.get("user_id") != null) {
                        userId = ((Number) result.get("user_id")).longValue();
                    }
                }
            } else {
                // Verificar el resultado directamente
                Integer success = (Integer) userResult.get("success");
                if (success == null || success != 1) {
                    String errorMessage = (String) userResult.get("message");
                    return Result.failure("Error al crear usuario: " + errorMessage);
                }
                
                // Intentar obtener el ID del usuario del resultado
                if (userResult.containsKey("user_id") && userResult.get("user_id") != null) {
                    userId = ((Number) userResult.get("user_id")).longValue();
                }
            }
            
            // Si no se pudo obtener el ID del usuario del stored procedure, buscarlo por email
            if (userId == null) {
                Optional<User> createdUserOpt = userRepository.findByEmail(user.getEmail());
                if (createdUserOpt.isEmpty()) {
                    return Result.failure("No se pudo encontrar el usuario recién creado");
                }
                userId = createdUserOpt.get().getId();
            }
            
            user.setId(userId);
            
            // 3. Crear perfil de profesor usando sp_create_teacher_profile existente
            Map<String, Object> teacherParams = new HashMap<>();
            teacherParams.put("user_id", userId);
            teacherParams.put("email_verified", teacherProfile.getEmailVerified());
            teacherParams.put("stem_area_id", teacherProfile.getStemAreaId());
            
            SimpleJdbcCall createTeacherCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_create_teacher_profile")
                    .returningResultSet("result", (rs, rowNum) -> {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("success", rs.getInt("success"));
                        resultMap.put("message", rs.getString("message"));
                        return resultMap;
                    });
            
            Map<String, Object> teacherResult = createTeacherCall.execute(teacherParams);
            
            // Verificar si la creación del perfil fue exitosa
            if (teacherResult.containsKey("result")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) teacherResult.get("result");
                if (!resultList.isEmpty()) {
                    Map<String, Object> result = resultList.get(0);
                    Integer success = (Integer) result.get("success");
                    if (success == null || success != 1) {
                        String errorMessage = (String) result.get("message");
                        return Result.failure("Error al crear perfil de profesor: " + errorMessage);
                    }
                }
            } else {
                // Verificar el resultado directamente
                Integer success = (Integer) teacherResult.get("success");
                if (success == null || success != 1) {
                    String errorMessage = (String) teacherResult.get("message");
                    return Result.failure("Error al crear perfil de profesor: " + errorMessage);
                }
            }
            
            // 4. Cargar el perfil completo
            Optional<CompleteTeacher> completeTeacherOpt = findCompleteTeacherById(userId);
            if (completeTeacherOpt.isPresent()) {
                return Result.success(completeTeacherOpt.get());
            }
            
            // Si no se puede cargar, devolver el objeto original con los IDs actualizados
            return Result.success(completeTeacher);
            
        } catch (Exception e) {
            return Result.failure("Error al crear profesor: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<CompleteGuardian> createCompleteGuardian(CompleteGuardian completeGuardian) {
        try {
            // Extraer datos del usuario y perfil de tutor
            User user = completeGuardian.getUser();
            GuardianProfile guardianProfile = completeGuardian.getGuardianProfile();
            
            // Imprimir el roleId para debugging
            System.out.println("DEBUG - Creating guardian with roleId: " + user.getRoleId());
            
            // 1. Crear usuario base usando sp_create_user_base existente
            Map<String, Object> userParams = new HashMap<>();
            userParams.put("email", user.getEmail());
            userParams.put("password", user.getPassword());
            userParams.put("role_id", user.getRoleId());
            userParams.put("first_name", user.getFirstName());
            userParams.put("last_name", user.getLastName());
            userParams.put("institution_id", user.getInstitutionId());
            
            // Imprimir los parámetros que se envían al stored procedure
            System.out.println("DEBUG - User params: " + userParams);
            
            SimpleJdbcCall createUserCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_create_user_base")
                    .returningResultSet("result", (rs, rowNum) -> {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("success", rs.getInt("success"));
                        resultMap.put("message", rs.getString("message"));
                        if (rs.getMetaData().getColumnCount() > 2) {
                            try {
                                resultMap.put("user_id", rs.getLong("user_id"));
                            } catch (SQLException e) {
                                // La columna user_id podría no existir o ser NULL
                            }
                        }
                        return resultMap;
                    });
            
            Map<String, Object> userResult = createUserCall.execute(userParams);
            
            // Imprimir el resultado del stored procedure
            System.out.println("DEBUG - User creation result: " + userResult);
            
            // Verificar si la creación del usuario fue exitosa
            Long userId = null;
            
            if (userResult.containsKey("result")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) userResult.get("result");
                if (!resultList.isEmpty()) {
                    Map<String, Object> result = resultList.get(0);
                    Integer success = (Integer) result.get("success");
                    if (success == null || success != 1) {
                        String errorMessage = (String) result.get("message");
                        return Result.failure("Error al crear usuario: " + errorMessage);
                    }
                    
                    // Intentar obtener el ID del usuario del resultado
                    if (result.containsKey("user_id") && result.get("user_id") != null) {
                        userId = ((Number) result.get("user_id")).longValue();
                    }
                }
            } else {
                // Verificar el resultado directamente
                Integer success = (Integer) userResult.get("success");
                if (success == null || success != 1) {
                    String errorMessage = (String) userResult.get("message");
                    return Result.failure("Error al crear usuario: " + errorMessage);
                }
                
                // Intentar obtener el ID del usuario del resultado
                if (userResult.containsKey("user_id") && userResult.get("user_id") != null) {
                    userId = ((Number) userResult.get("user_id")).longValue();
                }
            }
            
            // Si no se pudo obtener el ID del usuario del stored procedure, buscarlo por email
            if (userId == null) {
                Optional<User> createdUserOpt = userRepository.findByEmail(user.getEmail());
                if (createdUserOpt.isEmpty()) {
                    return Result.failure("No se pudo encontrar el usuario recién creado");
                }
                userId = createdUserOpt.get().getId();
            }
            
            user.setId(userId);
            
            // Imprimir el usuario creado para verificar el rol
            System.out.println("DEBUG - Created user: " + user);
            System.out.println("DEBUG - Created user ID: " + userId);
            System.out.println("DEBUG - Created user roleId: " + user.getRoleId());
            
            // 3. Crear perfil de tutor usando sp_create_guardian_profile existente
            Map<String, Object> guardianParams = new HashMap<>();
            guardianParams.put("user_id", userId);
            guardianParams.put("phone", guardianProfile.getPhone());
            
            // Imprimir los parámetros del perfil de tutor
            System.out.println("DEBUG - Guardian params: " + guardianParams);
            
            SimpleJdbcCall createGuardianCall = new SimpleJdbcCall(jdbcTemplate)
                    .withProcedureName("sp_create_guardian_profile")
                    .returningResultSet("result", (rs, rowNum) -> {
                        Map<String, Object> resultMap = new HashMap<>();
                        resultMap.put("success", rs.getInt("success"));
                        resultMap.put("message", rs.getString("message"));
                        if (rs.getMetaData().getColumnCount() > 2) {
                            try {
                                resultMap.put("guardian_profile_id", rs.getLong("guardian_profile_id"));
                            } catch (SQLException e) {
                                // La columna guardian_profile_id podría no existir o ser NULL
                            }
                        }
                        return resultMap;
                    });
            
            Map<String, Object> guardianResult = createGuardianCall.execute(guardianParams);
            
            // Imprimir el resultado de la creación del perfil
            System.out.println("DEBUG - Guardian profile creation result: " + guardianResult);
            
            // Verificar si la creación del perfil fue exitosa
            Long guardianProfileId = null;
            
            if (guardianResult.containsKey("result")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> resultList = (List<Map<String, Object>>) guardianResult.get("result");
                if (!resultList.isEmpty()) {
                    Map<String, Object> result = resultList.get(0);
                    Integer success = (Integer) result.get("success");
                    if (success == null || success != 1) {
                        String errorMessage = (String) result.get("message");
                        return Result.failure("Error al crear perfil de tutor: " + errorMessage);
                    }
                    
                    // Intentar obtener el ID del perfil de guardián del resultado
                    if (result.containsKey("guardian_profile_id") && result.get("guardian_profile_id") != null) {
                        guardianProfileId = ((Number) result.get("guardian_profile_id")).longValue();
                    }
                }
            } else {
                // Verificar el resultado directamente
                Integer success = (Integer) guardianResult.get("success");
                if (success == null || success != 1) {
                    String errorMessage = (String) guardianResult.get("message");
                    return Result.failure("Error al crear perfil de tutor: " + errorMessage);
                }
                
                // Intentar obtener el ID del perfil de guardián del resultado
                if (guardianResult.containsKey("guardian_profile_id") && guardianResult.get("guardian_profile_id") != null) {
                    guardianProfileId = ((Number) guardianResult.get("guardian_profile_id")).longValue();
                }
            }
            
            // Si no se pudo obtener el ID del perfil de guardián, buscarlo en la base de datos
            if (guardianProfileId == null) {
                try {
                    guardianProfileId = jdbcTemplate.queryForObject(
                        "SELECT id FROM guardian_profile WHERE user_id = ?", 
                        Long.class, 
                        userId
                    );
                } catch (Exception e) {
                    System.out.println("DEBUG - Error al buscar el ID del perfil de guardián: " + e.getMessage());
                    // Continuamos sin el ID, no es crítico para la respuesta
                }
            }
            
            // Establecer los IDs en los objetos
            guardianProfile.setId(guardianProfileId);
            guardianProfile.setUserId(userId);
            
            // No podemos acceder a los repositorios de rol e institución desde aquí
            // ni establecer directamente los valores en CompleteGuardian
            // Actualizamos los valores que podemos
            user.setId(userId);
            guardianProfile.setId(guardianProfileId);
            completeGuardian.getUser().setId(userId);
            completeGuardian.getGuardianProfile().setId(guardianProfileId);
            
            return Result.success(completeGuardian);
        } catch (Exception e) {
            e.printStackTrace(); // Imprimir la excepción completa
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
            System.out.println("DEBUG - findCompleteGuardianById: Buscando guardián con userId: " + userId);
            
            // 1. Consultar datos del usuario
            String userSql = "SELECT u.*, r.name as role_name, i.name as institution_name " +
                             "FROM [user] u " +
                             "LEFT JOIN role r ON u.role_id = r.id " +
                             "LEFT JOIN institution i ON u.institution_id = i.id " +
                             "WHERE u.id = ?";
            
            System.out.println("DEBUG - findCompleteGuardianById: Ejecutando consulta de usuario: " + userSql);
            
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
                System.out.println("DEBUG - findCompleteGuardianById: No se encontró el usuario con ID: " + userId);
                return Optional.empty();
            }
            
            User user = users.get(0);
            System.out.println("DEBUG - findCompleteGuardianById: Usuario encontrado: " + user);
            
            // 2. Consultar datos del perfil de guardián
            String profileSql = "SELECT * FROM guardian_profile WHERE user_id = ?";
            
            System.out.println("DEBUG - findCompleteGuardianById: Ejecutando consulta de perfil de guardián: " + profileSql);
            
            List<GuardianProfile> profiles = jdbcTemplate.query(profileSql, new Object[]{userId}, (rs, rowNum) -> {
                GuardianProfile profile = new GuardianProfile();
                profile.setId(rs.getLong("id"));
                profile.setUserId(rs.getLong("user_id"));
                profile.setPhone(rs.getString("phone"));
                
                // Verificar si existe la columna relationship
                try {
                    String relationship = rs.getString("relationship");
                    if (relationship != null) {
                        System.out.println("DEBUG - findCompleteGuardianById: Relación encontrada: " + relationship);
                    }
                } catch (Exception e) {
                    // La columna relationship podría no existir
                    System.out.println("DEBUG - findCompleteGuardianById: La columna 'relationship' no existe en la tabla guardian_profile");
                }
                
                profile.setCreatedAt(rs.getTimestamp("created_at"));
                profile.setUpdatedAt(rs.getTimestamp("updated_at"));
                return profile;
            });
            
            if (profiles.isEmpty()) {
                System.out.println("DEBUG - findCompleteGuardianById: No se encontró el perfil de guardián para el usuario con ID: " + userId);
                return Optional.empty();
            }
            
            GuardianProfile profile = profiles.get(0);
            System.out.println("DEBUG - findCompleteGuardianById: Perfil de guardián encontrado: " + profile);
            
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
            
            System.out.println("DEBUG - findCompleteGuardianById: CompleteGuardian creado exitosamente");
            
            return Optional.of(completeGuardian);
        } catch (Exception e) {
            System.out.println("ERROR - findCompleteGuardianById: Error al buscar guardián: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<CompleteStudent> findCompleteStudentByUsername(String username) {
        try {
            // 1. Buscar el perfil de estudiante por username
            String profileSql = "SELECT * FROM student_profile WHERE username = ?";
            
            List<StudentProfile> profiles = jdbcTemplate.query(profileSql, new Object[]{username}, (rs, rowNum) -> {
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
            
            // 2. Buscar el usuario asociado
            String userSql = "SELECT u.*, r.name as role_name, i.name as institution_name " +
                             "FROM [user] u " +
                             "JOIN role r ON u.role_id = r.id " +
                             "JOIN institution i ON u.institution_id = i.id " +
                             "WHERE u.id = ?";
            
            List<User> users = jdbcTemplate.query(userSql, new Object[]{profile.getUserId()}, (rs, rowNum) -> {
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
            
            // 3. Crear y retornar el objeto completo
            CompleteStudent completeStudent = new CompleteStudent(
                user.getId(), user.getRoleId(), user.getInstitutionId(),
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getPassword(), user.getProfilePictureUrl(),
                user.getCreatedAt(), user.getUpdatedAt(), user.getStatus(),
                user.isEmailVerified(), null, null,
                null, null, user.getLastLoginAt(),
                user.getLastLoginIp(), 0, null,
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
    public Optional<Object> findCompleteUserByEmail(String email) {
        // TODO: Implementar búsqueda de usuario por email
        String userSql = "SELECT u.*, r.name as role_name, i.name as institution_name " +
                         "FROM [user] u " +
                         "JOIN role r ON u.role_id = r.id " +
                         "JOIN institution i ON u.institution_id = i.id " +
                         "WHERE u.email = ?";

        List<User> users = jdbcTemplate.query(userSql, new Object[]{email}, (rs, rowNum) -> {
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
        
        return Optional.of(user);
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