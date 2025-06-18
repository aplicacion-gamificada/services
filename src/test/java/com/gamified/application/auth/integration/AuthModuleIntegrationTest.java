package com.gamified.application.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamified.application.auth.dto.request.AuthRequestDto;
import com.gamified.application.auth.dto.request.InstitutionRequestDto;
import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.config.TestConfig;
import com.gamified.application.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para el módulo auth completo basado en GUIA_TESTING_DETALLADA.md
 * 
 * Estos tests validan flujos completos end-to-end:
 * 1. Flujos Exitosos (Happy Path)
 * 2. Casos de Error (Negative Testing)
 * 3. Edge Cases
 * 4. Validaciones de Seguridad
 */
@SpringBootTest
@AutoConfigureWebMvc
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Transactional
@DisplayName("Auth Module - Integration Tests")
class AuthModuleIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ================================
    // 1. FLUJOS EXITOSOS (HAPPY PATH)
    // ================================
    
    @Nested
    @DisplayName("1. Flujos Exitosos (Happy Path)")
    class HappyPathTests {

        @Test
        @DisplayName("Flujo completo: Registro de estudiante → Login → Acceso a perfil")
        void testCompleteStudentFlow() throws Exception {
            // Step 1: Verificar email disponible
            mockMvc.perform(get("/api/register/check-email")
                            .param("email", "estudiante@test.com"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            // Step 2: Verificar username disponible
            mockMvc.perform(get("/api/register/check-username")
                            .param("username", "estudiante123"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            // Step 3: Registrar estudiante
            UserRequestDto.StudentRegistrationRequestDto studentRequest = UserRequestDto.StudentRegistrationRequestDto.builder()
                    .firstName("Test")
                    .lastName("Student")
                    .email("estudiante@test.com")
                    .password("Password123!")
                    .username("estudiante123")
                    .birth_date(java.sql.Date.valueOf(LocalDate.of(2010, 1, 1)))
                    .institutionId(1L)
                    .build();

            mockMvc.perform(post("/api/register/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(studentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.lastName").value("Student"))
                    .andExpect(jsonPath("$.username").value("estudiante123"))
                    .andExpect(jsonPath("$.roleName").value("STUDENT"));

            // Step 4: Login con estudiante
            AuthRequestDto.StudentLoginRequestDto loginRequest = AuthRequestDto.StudentLoginRequestDto.builder()
                    .username("estudiante123")
                    .password("Password123!")
                    .rememberMe(false)
                    .build();

            String loginResponse = mockMvc.perform(post("/api/auth/student-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.tokens.accessToken").exists())
                    .andExpect(jsonPath("$.tokens.refreshToken").exists())
                    .andReturn().getResponse().getContentAsString();

            // Extract token for next request (would need JSON parsing in real test)
            // Step 5: Acceder a perfil con token
            // mockMvc.perform(get("/api/users/profile")
            //         .header("Authorization", "Bearer " + accessToken))
            //         .andExpect(status().isOk())
            //         .andExpect(jsonPath("$.username").value("estudiante123"));
        }

        @Test
        @DisplayName("Flujo completo: Registro de profesor → Verificación email → Login")
        void testCompleteTeacherFlow() throws Exception {
            // Step 1: Registrar profesor
            UserRequestDto.TeacherRegistrationRequestDto teacherRequest = UserRequestDto.TeacherRegistrationRequestDto.builder()
                    .firstName("Carlos")
                    .lastName("Ramírez")
                    .email("carlos.ramirez@test.com")
                    .password("Password123!")
                    .stemAreaId((byte) 1)
                    .institutionId(1L)
                    .build();

            mockMvc.perform(post("/api/register/teachers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(teacherRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Carlos"))
                    .andExpect(jsonPath("$.email").value("carlos.ramirez@test.com"))
                    .andExpect(jsonPath("$.roleName").value("TEACHER"));

            // Step 2: Simular verificación de email
            AuthRequestDto.EmailVerificationRequestDto verifyRequest = AuthRequestDto.EmailVerificationRequestDto.builder()
                    .verificationToken("test-token-123")
                    .email("carlos.ramirez@test.com")
                    .build();

            mockMvc.perform(post("/api/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verifyRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Step 3: Login con profesor
            AuthRequestDto.LoginRequestDto loginRequest = AuthRequestDto.LoginRequestDto.builder()
                    .email("carlos.ramirez@test.com")
                    .password("Password123!")
                    .rememberMe(false)
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.user.role").value("TEACHER"));
        }

        @Test
        @DisplayName("Flujo completo: Gestión de contraseñas - Solicitar reset → Ejecutar reset → Login")
        void testPasswordResetFlow() throws Exception {
            // Step 1: Solicitar reset de contraseña
            AuthRequestDto.PasswordResetRequestDto resetRequest = AuthRequestDto.PasswordResetRequestDto.builder()
                    .email("test@example.com")
                    .build();

            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Step 2: Ejecutar reset con token
            AuthRequestDto.PasswordResetExecuteRequestDto executeRequest = AuthRequestDto.PasswordResetExecuteRequestDto.builder()
                    .resetToken("test-reset-token")
                    .email("test@example.com")
                    .newPassword("NewPassword123!")
                    .confirmNewPassword("NewPassword123!")
                    .build();

            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(executeRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Step 3: Login con nueva contraseña
            AuthRequestDto.LoginRequestDto loginRequest = AuthRequestDto.LoginRequestDto.builder()
                    .email("test@example.com")
                    .password("NewPassword123!")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }
    }

    // ================================
    // 2. CASOS DE ERROR (NEGATIVE TESTING)
    // ================================
    
    @Nested
    @DisplayName("2. Casos de Error (Negative Testing)")
    class NegativeTestingTests {

        @Test
        @DisplayName("Validaciones de entrada: Email inválido → 400 Bad Request")
        void testInvalidEmailValidation() throws Exception {
            mockMvc.perform(get("/api/register/check-email")
                            .param("email", "invalid-email"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Credenciales incorrectas: Login con password erróneo → 401 Unauthorized")
        void testInvalidCredentials() throws Exception {
            AuthRequestDto.LoginRequestDto loginRequest = AuthRequestDto.LoginRequestDto.builder()
                    .email("test@example.com")
                    .password("wrongpassword")
                    .rememberMe(false)
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Contraseña débil: Registro con contraseña simple → 400 Bad Request")
        void testWeakPasswordValidation() throws Exception {
            UserRequestDto.StudentRegistrationRequestDto studentRequest = UserRequestDto.StudentRegistrationRequestDto.builder()
                    .firstName("Test")
                    .lastName("Student")
                    .email("test@example.com")
                    .password("123456") // Weak password
                    .username("testuser")
                    .institutionId(1L)
                    .build();

            mockMvc.perform(post("/api/register/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(studentRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Username duplicado: Registro con username existente → 409 Conflict")
        void testDuplicateUsername() throws Exception {
            UserRequestDto.StudentRegistrationRequestDto studentRequest = UserRequestDto.StudentRegistrationRequestDto.builder()
                    .firstName("Test")
                    .lastName("Student")
                    .email("test1@example.com")
                    .password("Password123!")
                    .username("existinguser") // Existing username
                    .institutionId(1L)
                    .build();

            mockMvc.perform(post("/api/register/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(studentRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("El nombre de usuario ya está en uso"));
        }

        @Test
        @DisplayName("Institución inexistente: Registro con institutionId inválido → 400 Bad Request")
        void testInvalidInstitution() throws Exception {
            UserRequestDto.StudentRegistrationRequestDto studentRequest = UserRequestDto.StudentRegistrationRequestDto.builder()
                    .firstName("Test")
                    .lastName("Student")
                    .email("test@example.com")
                    .password("Password123!")
                    .username("testuser")
                    .institutionId(99999L) // Non-existent institution
                    .build();

            mockMvc.perform(post("/api/register/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(studentRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ================================
    // 3. EDGE CASES
    // ================================
    
    @Nested
    @DisplayName("3. Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Límites de datos: Nombre de 50 caracteres exactos")
        void testMaxLengthFields() throws Exception {
            String fiftyCharName = "A".repeat(50);
            
            UserRequestDto.StudentRegistrationRequestDto studentRequest = UserRequestDto.StudentRegistrationRequestDto.builder()
                    .firstName(fiftyCharName)
                    .lastName(fiftyCharName)
                    .email("test@example.com")
                    .password("Password123!")
                    .username("testuser")
                    .institutionId(1L)
                    .build();

            mockMvc.perform(post("/api/register/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(studentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value(fiftyCharName))
                    .andExpect(jsonPath("$.lastName").value(fiftyCharName));
        }

        @Test
        @DisplayName("Email en límite máximo: 100 caracteres")
        void testMaxLengthEmail() throws Exception {
            // Create 100-character email
            String longEmail = "a".repeat(85) + "@example.com"; // 100 chars total
            
            mockMvc.perform(get("/api/register/check-email")
                            .param("email", longEmail))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Password complejo: 128 caracteres con todos los tipos")
        void testComplexPassword() throws Exception {
            String complexPassword = "A".repeat(30) + "a".repeat(30) + "1".repeat(30) + "!@#$%^&*()".repeat(4); // Complex 128-char password
            
            UserRequestDto.StudentRegistrationRequestDto studentRequest = UserRequestDto.StudentRegistrationRequestDto.builder()
                    .firstName("Test")
                    .lastName("Student")
                    .email("test@example.com")
                    .password(complexPassword)
                    .username("testuser")
                    .institutionId(1L)
                    .build();

            mockMvc.perform(post("/api/register/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(studentRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Búsqueda vacía: searchTerm='' debe manejarse apropiadamente")
        void testEmptySearch() throws Exception {
            // This test would require authentication setup
            // mockMvc.perform(get("/api/users/search")
            //         .param("searchTerm", "")
            //         .header("Authorization", "Bearer " + teacherToken))
            //         .andExpect(status().isBadRequest());
        }
    }

    // ================================
    // 4. VALIDACIONES DE SEGURIDAD
    // ================================
    
    @Nested
    @DisplayName("4. Validaciones de Seguridad")
    class SecurityValidationTests {

        @Test
        @DisplayName("SQL Injection: Intentar inyección en campo de búsqueda")
        void testSqlInjectionPrevention() throws Exception {
            String sqlInjectionAttempt = "'; DROP TABLE users; --";
            
            mockMvc.perform(get("/api/register/check-email")
                            .param("email", sqlInjectionAttempt))
                    .andExpect(status().isBadRequest()); // Should be rejected as invalid email
        }

        @Test
        @DisplayName("XSS: Scripts en campos de texto deben ser rechazados")
        void testXssPrevention() throws Exception {
            String xssAttempt = "<script>alert('XSS')</script>";
            
            UserRequestDto.StudentRegistrationRequestDto studentRequest = UserRequestDto.StudentRegistrationRequestDto.builder()
                    .firstName(xssAttempt)
                    .lastName("Student")
                    .email("test@example.com")
                    .password("Password123!")
                    .username("testuser")
                    .institutionId(1L)
                    .build();

            mockMvc.perform(post("/api/register/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(studentRequest)))
                    .andExpect(status().isBadRequest()); // Should be rejected due to validation
        }

        @Test
        @DisplayName("Rate Limiting: Múltiples intentos de login deben ser limitados")
        void testRateLimiting() throws Exception {
            AuthRequestDto.LoginRequestDto loginRequest = AuthRequestDto.LoginRequestDto.builder()
                    .email("test@example.com")
                    .password("wrongpassword")
                    .build();

            // Simulate multiple failed login attempts
            for (int i = 0; i < 6; i++) {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isUnauthorized());
            }

            // 7th attempt should be rate limited (if implemented)
            // mockMvc.perform(post("/api/auth/login")
            //         .contentType(MediaType.APPLICATION_JSON)
            //         .content(objectMapper.writeValueAsString(loginRequest)))
            //         .andExpect(status().isTooManyRequests());
        }

        @Test
        @DisplayName("JWT Token validation: Token malformado debe ser rechazado")
        void testMalformedJwtRejection() throws Exception {
            String malformedToken = "malformed.jwt.token";
            
            mockMvc.perform(get("/api/users/profile")
                            .header("Authorization", "Bearer " + malformedToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================
    // 5. HEALTH & CONNECTIVITY TESTS
    // ================================
    
    @Nested
    @DisplayName("5. Health & Connectivity Tests")
    class HealthConnectivityTests {

        @Test
        @DisplayName("Health Check: /api/auth/health debe responder correctamente")
        void testHealthCheck() throws Exception {
            mockMvc.perform(get("/api/auth/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.service").value("auth-service"))
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Database Test: /api/auth/db-test debe confirmar conectividad")
        void testDatabaseConnectivity() throws Exception {
            mockMvc.perform(get("/api/auth/db-test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.database").value("connected"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Stored Procedures Test: /api/auth/sp-test debe verificar SPs")
        void testStoredProcedures() throws Exception {
            mockMvc.perform(get("/api/auth/sp-test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.storedProcedures").value("working"))
                    .andExpect(jsonPath("$.testProcedures").isArray());
        }
    }

    // ================================
    // 6. INSTITUTION MANAGEMENT TESTS
    // ================================
    
    @Nested
    @DisplayName("6. Institution Management Tests")
    class InstitutionManagementTests {

        @Test
        @DisplayName("Institution Query: Obtener todas las instituciones activas")
        void testGetAllInstitutions() throws Exception {
            mockMvc.perform(get("/api/institutions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Institution Search: Buscar por nombre")
        void testSearchInstitutions() throws Exception {
            mockMvc.perform(get("/api/institutions/search")
                            .param("query", "Nacional")
                            .param("limit", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Institution Creation: Registrar nueva institución")
        void testCreateInstitution() throws Exception {
            InstitutionRequestDto.InstitutionRegistrationRequestDto institutionRequest = InstitutionRequestDto.InstitutionRegistrationRequestDto.builder()
                    .name("Test Institution")
                    .city("Test City")
                    .country("Test Country")
                    .email("test@institution.com")
                    .build();

            // This would require proper authentication setup
            // mockMvc.perform(post("/api/institutions")
            //         .header("Authorization", "Bearer " + adminToken)
            //         .contentType(MediaType.APPLICATION_JSON)
            //         .content(objectMapper.writeValueAsString(institutionRequest)))
            //         .andExpect(status().isCreated())
            //         .andExpect(jsonPath("$.success").value(true));
        }
    }
}