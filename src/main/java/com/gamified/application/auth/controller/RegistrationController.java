package com.gamified.application.auth.controller;

import com.gamified.application.user.model.dto.request.UserRequestDto;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import com.gamified.application.user.model.dto.response.UserResponseDto;
import com.gamified.application.user.service.UserRegistrationService;
import com.gamified.application.user.service.UserProfileService;
import com.gamified.application.clasroom.service.ClassroomService;
import com.gamified.application.clasroom.model.dto.request.ClassroomRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import com.gamified.application.shared.exception.EmailAlreadyExistsException;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para operaciones de registro de usuarios
 */
@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
@Tag(
        name = "Registration ",
        description = "Provides endpoints for user registration and management."
)
public class RegistrationController {

    private final UserRegistrationService userRegistrationService;
    private final UserProfileService userProfileService;
    private final ClassroomService classroomService;
    private final JdbcTemplate jdbcTemplate;
    
    // Production testing: IDs for automatic assignment
    private static final Long PRODUCTION_TEST_GUARDIAN_ID = 1L;
    private static final Integer PRODUCTION_TEST_CLASSROOM_ID = 1;

    /**
     * Endpoint temporal para modificar la tabla student_profile
     * IMPORTANTE: Este endpoint debe eliminarse después de su uso
     */
    @GetMapping("/fix-student-profile")
    public ResponseEntity<?> fixStudentProfile() {
        try {
            // Ejecutar el script SQL para modificar la tabla
            jdbcTemplate.execute("ALTER TABLE student_profile ALTER COLUMN guardian_profile_id INT NULL");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "La tabla student_profile ha sido modificada correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al modificar la tabla: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Endpoint para verificar disponibilidad de email
     * @param email Email a verificar
     * @return true si está disponible
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> isEmailAvailable(@RequestParam String email) {
        boolean isAvailable = userRegistrationService.isEmailAvailable(email);
        return ResponseEntity.ok(isAvailable);
    }

    /**
     * Endpoint para verificar disponibilidad de nombre de usuario
     * @param username Nombre de usuario a verificar
     * @return true si está disponible
     */
    @GetMapping("/check-username")
    public ResponseEntity<Boolean> isUsernameAvailable(@RequestParam String username) {
        boolean isAvailable = userRegistrationService.isUsernameAvailable(username);
        return ResponseEntity.ok(isAvailable);
    }

    /**
     * Endpoint para registrar un nuevo estudiante
     * @param studentRequest Datos del estudiante
     * @param request Request HTTP para obtener información del cliente
     * @return Perfil del estudiante creado
     */
    @PostMapping("/students")
    public ResponseEntity<?> registerStudent(
            @Valid @RequestBody UserRequestDto.StudentRegistrationRequestDto studentRequest,
            HttpServletRequest request) {
        
        // Verificar si el nombre de usuario ya está en uso
        if (!userRegistrationService.isUsernameAvailable(studentRequest.getUsername())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "El nombre de usuario ya está en uso");
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // Procesar registro de estudiante
            UserResponseDto.StudentResponseDto response = userRegistrationService.registerStudent(studentRequest);
            
            // Production testing: Automatically assign guardian ID 1, institution ID 1, and classroom ID 1
            try {
                Long studentProfileId = response.getStudentProfileId();
                
                // Get the student profile ID from the response
                if (studentProfileId != null && studentProfileId > 0) {
                    System.out.println("DEBUG - Auto-assigning production test data for student: " + studentProfileId);
                    
                    // 1. Assign guardian with ID 1
                    try {
                        boolean guardianAssigned = userProfileService.assignGuardianToStudent(
                                studentProfileId, 
                                PRODUCTION_TEST_GUARDIAN_ID
                        );
                        System.out.println("DEBUG - Guardian assignment result: " + guardianAssigned);
                    } catch (Exception e) {
                        System.out.println("WARN - Failed to assign guardian: " + e.getMessage());
                    }
                    
                    // 2. Assign to classroom with ID 1
                    try {
                        ClassroomRequestDto.EnrollStudentRequestDto enrollRequest = 
                                new ClassroomRequestDto.EnrollStudentRequestDto();
                        enrollRequest.setStudentProfileId(Math.toIntExact(studentProfileId));
                        
                        // Note: We're using a dummy user ID (1) as the teacher/admin ID for enrollment
                        // In production, this should be the actual teacher or admin ID
                        // For testing purposes, we assume user ID 1 is an admin or teacher
                        classroomService.enrollStudentByAdmin(
                                1L, // Admin user ID (assumed to be 1 for testing)
                                PRODUCTION_TEST_CLASSROOM_ID,
                                enrollRequest
                        );
                        System.out.println("DEBUG - Student enrollment result: success");
                    } catch (Exception e) {
                        System.out.println("WARN - Failed to enroll student in classroom: " + e.getMessage());
                    }
                    
                    // 3. Insert enrollment record directly into the enrollment table (backup/redundancy)
                    try {
                        String enrollmentSql = "INSERT INTO enrollment (classroom_id, student_profile_id, joined_at, status) " +
                                             "VALUES (?, ?, GETDATE(), 1)";
                        int rowsAffected = jdbcTemplate.update(enrollmentSql, 
                                PRODUCTION_TEST_CLASSROOM_ID, 
                                studentProfileId);
                        System.out.println("DEBUG - Direct enrollment table insert result: " + rowsAffected + " rows affected");
                    } catch (Exception e) {
                        System.out.println("WARN - Failed to insert enrollment record: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println("WARN - Error in production testing auto-assignment: " + e.getMessage());
                // Don't fail the registration if auto-assignment fails
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Endpoint para registrar un nuevo profesor
     * @param teacherRequest Datos del profesor
     * @param request Request HTTP para obtener información del cliente
     * @return Perfil del profesor creado
     */
    @PostMapping("/teachers")
    public ResponseEntity<?> registerTeacher(
            @Valid @RequestBody UserRequestDto.TeacherRegistrationRequestDto teacherRequest,
            HttpServletRequest request) {
        
        try {
            // Procesar registro de profesor
            UserResponseDto.TeacherResponseDto response = userRegistrationService.registerTeacher(teacherRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Endpoint para registrar un nuevo tutor
     * @param guardianRequest Datos del tutor
     * @param request Request HTTP para obtener información del cliente
     * @return Perfil del tutor creado con token de autenticación
     */
    @PostMapping("/guardians")
    public ResponseEntity<?> registerGuardian(
            @Valid @RequestBody UserRequestDto.GuardianRegistrationRequestDto guardianRequest) {
        try {
            System.out.println("DEBUG - RegistrationController: Recibida solicitud de registro de guardián: " + guardianRequest);
            
            UserResponseDto.GuardianResponseDto response = userRegistrationService.registerGuardian(guardianRequest);
            System.out.println("DEBUG - RegistrationController: Guardián registrado exitosamente: " + response);
            
            return ResponseEntity.ok(response);
        } catch (EmailAlreadyExistsException e) {
            System.out.println("WARN - RegistrationController: Email ya existe: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            System.out.println("ERROR - RegistrationController: Error al registrar guardián: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar el guardián: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            // Agregar información adicional para depuración
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("exceptionType", e.getClass().getName());
            if (e.getCause() != null) {
                debugInfo.put("cause", e.getCause().getMessage());
                debugInfo.put("causeType", e.getCause().getClass().getName());
            }
            errorResponse.put("debug", debugInfo);
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Extrae información básica del dispositivo desde el User-Agent
     * @param userAgent Cadena User-Agent
     * @return Información del dispositivo
     */
    private String extractDeviceInfo(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }
        
        // Extraer información básica para identificar el dispositivo
        String deviceInfo = "Unknown";
        
        if (userAgent.contains("Windows")) {
            deviceInfo = "Windows";
        } else if (userAgent.contains("Mac")) {
            deviceInfo = "Mac";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            deviceInfo = "iOS";
        } else if (userAgent.contains("Android")) {
            deviceInfo = "Android";
        } else if (userAgent.contains("Linux")) {
            deviceInfo = "Linux";
        }
        
        return deviceInfo;
    }

    /**
     * Endpoint para asociar un estudiante a un tutor
     * @param associationRequest Datos de la asociación
     * @return Resultado de la operación
     */
    @PostMapping("/associate-student-to-guardian")
    public ResponseEntity<CommonResponseDto> associateStudentToGuardian(@RequestBody Map<String, Object> requestBody) {
        try {
            Long studentId = Long.valueOf(requestBody.get("studentId").toString());
            Long guardianId = Long.valueOf(requestBody.get("guardianId").toString());
            
            // Crear el DTO para la asociación
            UserRequestDto.StudentGuardianAssociationRequestDto associationDto = new UserRequestDto.StudentGuardianAssociationRequestDto();
            associationDto.setStudentProfileId(studentId);
            associationDto.setGuardianProfileId(guardianId);
            
            CommonResponseDto result = userRegistrationService.associateStudentToGuardian(associationDto);
            
            if (Boolean.TRUE.equals(result.getSuccess())) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponseDto.builder()
                            .success(false)
                            .message("Error al asociar estudiante al guardián: " + e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @PostMapping("/alter-student-profile-table")
    public ResponseEntity<CommonResponseDto> alterStudentProfileTable() {
        try {
            // Cargar el script SQL desde el archivo
            ClassPathResource resource = new ClassPathResource("sql/alter_student_profile_table.sql");
            String sqlScript = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // Ejecutar el script SQL
            jdbcTemplate.execute(sqlScript);
            
            return ResponseEntity.ok(CommonResponseDto.builder()
                    .success(true)
                    .message("Tabla student_profile modificada exitosamente")
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponseDto.builder()
                            .success(false)
                            .message("Error al modificar la tabla: " + e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build());
        }
    }

    @GetMapping("/debug/user/{id}")
    public ResponseEntity<?> debugUser(@PathVariable Long id) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Consultar usuario directamente en la base de datos
            try {
                Map<String, Object> userData = jdbcTemplate.queryForMap(
                    "SELECT * FROM [user] WHERE id = ?", id
                );
                result.put("user", userData);
            } catch (Exception e) {
                result.put("userError", e.getMessage());
            }
            
            // Consultar perfil de guardián directamente en la base de datos
            try {
                Map<String, Object> guardianData = jdbcTemplate.queryForMap(
                    "SELECT * FROM guardian_profile WHERE user_id = ?", id
                );
                result.put("guardianProfile", guardianData);
            } catch (Exception e) {
                result.put("guardianProfileError", e.getMessage());
            }
            
            // Consultar perfil de estudiante directamente en la base de datos
            try {
                Map<String, Object> studentData = jdbcTemplate.queryForMap(
                    "SELECT * FROM student_profile WHERE user_id = ?", id
                );
                result.put("studentProfile", studentData);
            } catch (Exception e) {
                result.put("studentProfileError", e.getMessage());
            }
            
            // Consultar perfil de profesor directamente en la base de datos
            try {
                Map<String, Object> teacherData = jdbcTemplate.queryForMap(
                    "SELECT * FROM teacher_profile WHERE user_id = ?", id
                );
                result.put("teacherProfile", teacherData);
            } catch (Exception e) {
                result.put("teacherProfileError", e.getMessage());
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 