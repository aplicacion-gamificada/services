package com.gamified.application.auth.controller;

import com.gamified.application.auth.dto.request.AuthRequestDto;
import com.gamified.application.auth.dto.request.SessionRequestDto;
import com.gamified.application.auth.dto.response.AuthResponseDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.SessionResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.service.auth.AuthenticationService;
import com.gamified.application.config.TestConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test completo para AuthController basado en GUIA_TESTING_DETALLADA.md
 * 
 * Secciones de test:
 * 1. Health & Connectivity Endpoints
 * 2. Authentication Endpoints  
 * 3. Email Verification Endpoints
 * 4. Password Management Endpoints
 */
@WebMvcTest(AuthController.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@DisplayName("AuthController - Tests Completos")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ================================
    // 1. HEALTH & CONNECTIVITY ENDPOINTS
    // ================================
    
    @Nested
    @DisplayName("1. Health & Connectivity Endpoints")
    class HealthConnectivityTests {

        @Test
        @DisplayName("GET /api/auth/health - Should return service health status")
        void testHealthCheck_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/auth/health"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.service").value("auth-service"))
                    .andExpect(jsonPath("$.status").value("UP"))
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.version").exists())
                    .andExpect(jsonPath("$.database").exists())
                    .andExpect(jsonPath("$.memory").exists());
        }

        @Test
        @DisplayName("GET /api/auth/db-test - Should test database connectivity")
        void testDatabaseConnection_Success() throws Exception {
            // Given
            when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

            // When & Then
            mockMvc.perform(get("/api/auth/db-test"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.database").value("connected"))
                    .andExpect(jsonPath("$.driver").exists())
                    .andExpect(jsonPath("$.url").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.queryTime").exists());
        }

        @Test
        @DisplayName("GET /api/auth/db-test - Should handle database error")
        void testDatabaseConnection_Error() throws Exception {
            // Given
            when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/auth/db-test"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("GET /api/auth/sp-test - Should test stored procedures")
        void testStoredProcedures_Success() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/auth/sp-test"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.storedProcedures").value("working"))
                    .andExpect(jsonPath("$.testProcedures").isArray())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.executionTime").exists());
        }

        @Test
        @DisplayName("GET /api/auth/sp-test - Should handle stored procedure error")
        void testStoredProcedures_Error() throws Exception {
            // Given
            doThrow(new RuntimeException("SP execution failed")).when(jdbcTemplate).execute(anyString());

            // When & Then
            mockMvc.perform(get("/api/auth/sp-test"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // ================================
    // 2. AUTHENTICATION ENDPOINTS
    // ================================
    
    @Nested
    @DisplayName("2. Authentication Endpoints")
    class AuthenticationTests {

        @Test
        @DisplayName("POST /api/auth/login - Should login teacher/guardian successfully")
        void testLogin_Success() throws Exception {
            // Given
            AuthRequestDto.LoginRequestDto loginRequest = AuthRequestDto.LoginRequestDto.builder()
                    .email("teacher@example.com")
                    .password("Password123!")
                    .rememberMe(false)
                    .build();

            AuthResponseDto.LoginResponseDto expectedResponse = AuthResponseDto.LoginResponseDto.builder()
                    .accessToken("eyJhbGciOiJIUzI1NiIs...")
                    .refreshToken("dGhpcyBpcyBhIHJlZnJl...")
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .userInfo(UserResponseDto.UserInfoDto.builder()
                            .id(123L)
                            .firstName("Juan")
                            .lastName("Pérez")
                            .email("teacher@example.com")
                            .role(AuthResponseDto.RoleInfoDto.builder()
                                    .name("TEACHER")
                                    .code("TEACHER")
                                    .build())
                            .institution(AuthResponseDto.InstitutionInfoDto.builder()
                                    .name("Universidad Nacional")
                                    .build())
                            .build())
                    .loginTime(LocalDateTime.now())
                    .sessionId("session_12345")
                    .rememberMe(false)
                    .sessionInfo(SessionResponseDto.SessionInfoResponseDto.builder()
                            .deviceInfo("Windows PC")
                            .ipAddress("192.168.1.100")
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                            .browser("Chrome")
                            .operatingSystem("Windows 10")
                            .sessionStartTime(LocalDateTime.now())
                            .build())
                    .build();

            when(authenticationService.login(any(AuthRequestDto.LoginRequestDto.class)))
                    .thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.userInfo.id").value(123))
                    .andExpect(jsonPath("$.userInfo.firstName").value("Juan"))
                    .andExpect(jsonPath("$.userInfo.lastName").value("Pérez"))
                    .andExpect(jsonPath("$.userInfo.email").value("teacher@example.com"))
                    .andExpect(jsonPath("$.userInfo.role.name").value("TEACHER"))
                    .andExpect(jsonPath("$.userInfo.institution.name").value("Universidad Nacional"))
                    .andExpect(jsonPath("$.expiresIn").value(3600))
                    .andExpect(jsonPath("$.loginTime").exists())
                    .andExpect(jsonPath("$.sessionId").value("session_12345"))
                    .andExpect(jsonPath("$.rememberMe").value(false))
                    .andExpect(jsonPath("$.sessionInfo").exists())
                    .andExpect(jsonPath("$.sessionInfo.deviceInfo").value("Windows PC"))
                    .andExpect(jsonPath("$.sessionInfo.ipAddress").value("192.168.1.100"))
                    .andExpect(jsonPath("$.sessionInfo.userAgent").value("Mozilla/5.0 (Windows NT 10.0; Win64; x64)"))
                    .andExpect(jsonPath("$.sessionInfo.browser").value("Chrome"))
                    .andExpect(jsonPath("$.sessionInfo.operatingSystem").value("Windows 10"))
                    .andExpect(jsonPath("$.sessionInfo.sessionStartTime").exists());

            verify(authenticationService, times(1)).login(any(AuthRequestDto.LoginRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/auth/login - Should fail with invalid credentials")
        void testLogin_InvalidCredentials() throws Exception {
            // Given
            AuthRequestDto.LoginRequestDto loginRequest = AuthRequestDto.LoginRequestDto.builder()
                    .email("teacher@example.com")
                    .password("wrongpassword")
                    .rememberMe(false)
                    .build();

            when(authenticationService.login(any(AuthRequestDto.LoginRequestDto.class)))
                    .thenThrow(new RuntimeException("Credenciales incorrectas"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/auth/login - Should validate required fields")
        void testLogin_ValidationErrors() throws Exception {
            // Given - Request with missing email
            AuthRequestDto.LoginRequestDto invalidRequest = AuthRequestDto.LoginRequestDto.builder()
                    .password("Password123!")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/student-login - Should login student successfully")
        void testStudentLogin_Success() throws Exception {
            // Given
            AuthRequestDto.StudentLoginRequestDto studentLoginRequest = AuthRequestDto.StudentLoginRequestDto.builder()
                    .username("student123")
                    .password("Password123!")
                    .rememberMe(true)
                    .build();

            AuthResponseDto.LoginResponseDto expectedResponse = AuthResponseDto.LoginResponseDto.builder()
                    .accessToken("eyJhbGciOiJIUzI1NiIs...")
                    .refreshToken("dGhpcyBpcyBhIHJlZnJl...")
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .userInfo(UserResponseDto.UserInfoDto.builder()
                            .id(456L)
                            .firstName("María")
                            .lastName("González")
                            .role(AuthResponseDto.RoleInfoDto.builder()
                                    .name("STUDENT")
                                    .code("STUDENT")
                                    .build())
                            .institution(AuthResponseDto.InstitutionInfoDto.builder()
                                    .name("Universidad Nacional")
                                    .build())
                            .build())
                    .loginTime(LocalDateTime.now())
                    .sessionId("session_student_789")
                    .rememberMe(true)
                    .sessionInfo(SessionResponseDto.SessionInfoResponseDto.builder()
                            .deviceInfo("Mobile Android")
                            .ipAddress("192.168.1.101")
                            .userAgent("Mozilla/5.0 (Linux; Android 11)")
                            .browser("Chrome Mobile")
                            .operatingSystem("Android 11")
                            .sessionStartTime(LocalDateTime.now())
                            .build())
                    .build();

            when(authenticationService.loginStudent(any(AuthRequestDto.StudentLoginRequestDto.class)))
                    .thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/student-login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(studentLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(3600))
                    .andExpect(jsonPath("$.userInfo.id").value(456))
                    .andExpect(jsonPath("$.userInfo.firstName").value("María"))
                    .andExpect(jsonPath("$.userInfo.lastName").value("González"))
                    .andExpect(jsonPath("$.userInfo.role.name").value("STUDENT"))
                    .andExpect(jsonPath("$.userInfo.role.code").value("STUDENT"))
                    .andExpect(jsonPath("$.userInfo.institution.name").value("Universidad Nacional"))
                    .andExpect(jsonPath("$.sessionId").value("session_student_789"))
                    .andExpect(jsonPath("$.rememberMe").value(true))
                    .andExpect(jsonPath("$.sessionInfo").exists())
                    .andExpect(jsonPath("$.sessionInfo.deviceInfo").value("Mobile Android"))
                    .andExpect(jsonPath("$.sessionInfo.ipAddress").value("192.168.1.101"))
                    .andExpect(jsonPath("$.sessionInfo.browser").value("Chrome Mobile"))
                    .andExpect(jsonPath("$.sessionInfo.operatingSystem").value("Android 11"))
                    .andExpect(jsonPath("$.loginTime").exists());

            verify(authenticationService, times(1)).loginStudent(any(AuthRequestDto.StudentLoginRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/auth/refresh-token - Should refresh token successfully")
        void testRefreshToken_Success() throws Exception {
            // Given
            SessionRequestDto.RefreshTokenRequestDto refreshRequest = SessionRequestDto.RefreshTokenRequestDto.builder()
                    .refreshToken("dGhpcyBpcyBhIHJlZnJl...")
                    .build();

            SessionResponseDto.RefreshTokenResponseDto expectedResponse = SessionResponseDto.RefreshTokenResponseDto.builder()
                    .accessToken("eyJhbGciOiJIUzI1NiIs...")
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .issuedAt(LocalDateTime.now())
                    .build();

            when(authenticationService.refreshToken(any(SessionRequestDto.RefreshTokenRequestDto.class)))
                    .thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(3600))
                    .andExpect(jsonPath("$.issuedAt").exists());

            verify(authenticationService, times(1)).refreshToken(any(SessionRequestDto.RefreshTokenRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/auth/refresh-token - Should fail with invalid token")
        void testRefreshToken_InvalidToken() throws Exception {
            // Given
            SessionRequestDto.RefreshTokenRequestDto refreshRequest = SessionRequestDto.RefreshTokenRequestDto.builder()
                    .refreshToken("invalid_token")
                    .build();

            when(authenticationService.refreshToken(any(SessionRequestDto.RefreshTokenRequestDto.class)))
                    .thenThrow(new RuntimeException("Invalid refresh token"));

            // When & Then
            mockMvc.perform(post("/api/auth/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/auth/logout - Should logout successfully")
        void testLogout_Success() throws Exception {
            // Given
            SessionRequestDto.LogoutRequestDto logoutRequest = SessionRequestDto.LogoutRequestDto.builder()
                    .refreshToken("dGhpcyBpcyBhIHJlZnJl...")
                    .build();

            CommonResponseDto expectedResponse = CommonResponseDto.builder()
                    .success(true)
                    .message("Sesión cerrada exitosamente")
                    .timestamp(LocalDateTime.now())
                    .build();

            when(authenticationService.logout(any(SessionRequestDto.LogoutRequestDto.class)))
                    .thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(logoutRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Sesión cerrada exitosamente"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(authenticationService, times(1)).logout(any(SessionRequestDto.LogoutRequestDto.class));
        }
    }

    // ================================
    // 3. EMAIL VERIFICATION ENDPOINTS
    // ================================
    
    @Nested
    @DisplayName("3. Email Verification Endpoints")
    class EmailVerificationTests {

        @Test
        @DisplayName("POST /api/auth/verify-email - Should verify email successfully")
        void testVerifyEmail_Success() throws Exception {
            // Given
            AuthRequestDto.EmailVerificationRequestDto verifyRequest = AuthRequestDto.EmailVerificationRequestDto.builder()
                    .verificationToken("abc123def456...")
                    .email("user@example.com")
                    .build();

            CommonResponseDto expectedResponse = CommonResponseDto.builder()
                    .success(true)
                    .message("Email verificado exitosamente")
                    .timestamp(LocalDateTime.now())
                    .build();

            when(authenticationService.verifyEmail(any(AuthRequestDto.EmailVerificationRequestDto.class)))
                    .thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verifyRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Email verificado exitosamente"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(authenticationService, times(1)).verifyEmail(any(AuthRequestDto.EmailVerificationRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/auth/verify-email - Should fail with invalid token")
        void testVerifyEmail_InvalidToken() throws Exception {
            // Given
            AuthRequestDto.EmailVerificationRequestDto verifyRequest = AuthRequestDto.EmailVerificationRequestDto.builder()
                    .verificationToken("invalid_token")
                    .email("user@example.com")
                    .build();

            when(authenticationService.verifyEmail(any(AuthRequestDto.EmailVerificationRequestDto.class)))
                    .thenThrow(new RuntimeException("Invalid verification token"));

            // When & Then
            mockMvc.perform(post("/api/auth/verify-email")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(verifyRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/auth/resend-verification - Should resend verification email")
        void testResendVerification_Success() throws Exception {
            // Given
            AuthRequestDto.ResendVerificationRequestDto resendRequest = AuthRequestDto.ResendVerificationRequestDto.builder()
                    .email("user@example.com")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/resend-verification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resendRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Se ha enviado un nuevo email de verificación. Por favor, revise su bandeja de entrada."));
        }
    }

    // ================================
    // 4. PASSWORD MANAGEMENT ENDPOINTS
    // ================================
    
    @Nested
    @DisplayName("4. Password Management Endpoints")
    class PasswordManagementTests {

        @Test
        @DisplayName("POST /api/auth/forgot-password - Should request password reset")
        void testForgotPassword_Success() throws Exception {
            // Given
            AuthRequestDto.PasswordResetRequestDto resetRequest = AuthRequestDto.PasswordResetRequestDto.builder()
                    .email("user@example.com")
                    .build();

            CommonResponseDto expectedResponse = CommonResponseDto.builder()
                    .success(true)
                    .message("Si el email existe, recibirá instrucciones para resetear su contraseña")
                    .timestamp(LocalDateTime.now())
                    .build();

            when(authenticationService.requestPasswordReset(any(AuthRequestDto.PasswordResetRequestDto.class)))
                    .thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Si el email existe, recibirá instrucciones para resetear su contraseña"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(authenticationService, times(1)).requestPasswordReset(any(AuthRequestDto.PasswordResetRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/auth/reset-password - Should reset password successfully")
        void testResetPassword_Success() throws Exception {
            // Given
            AuthRequestDto.PasswordResetExecuteRequestDto resetRequest = AuthRequestDto.PasswordResetExecuteRequestDto.builder()
                    .resetToken("xyz789abc123...")
                    .email("user@example.com")
                    .newPassword("NewPassword123!")
                    .confirmNewPassword("NewPassword123!")
                    .build();

            CommonResponseDto expectedResponse = CommonResponseDto.builder()
                    .success(true)
                    .message("Contraseña actualizada exitosamente")
                    .timestamp(LocalDateTime.now())
                    .build();

            when(authenticationService.executePasswordReset(any(AuthRequestDto.PasswordResetExecuteRequestDto.class)))
                    .thenReturn(expectedResponse);

            // When & Then
            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Contraseña actualizada exitosamente"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(authenticationService, times(1)).executePasswordReset(any(AuthRequestDto.PasswordResetExecuteRequestDto.class));
        }

        @Test
        @DisplayName("POST /api/auth/reset-password - Should fail with mismatched passwords")
        void testResetPassword_PasswordMismatch() throws Exception {
            // Given
            AuthRequestDto.PasswordResetExecuteRequestDto resetRequest = AuthRequestDto.PasswordResetExecuteRequestDto.builder()
                    .resetToken("xyz789abc123...")
                    .email("user@example.com")
                    .newPassword("NewPassword123!")
                    .confirmNewPassword("DifferentPassword123!")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Las contraseñas no coinciden"));
        }

        @Test
        @DisplayName("POST /api/auth/reset-password - Should fail with invalid token")
        void testResetPassword_InvalidToken() throws Exception {
            // Given
            AuthRequestDto.PasswordResetExecuteRequestDto resetRequest = AuthRequestDto.PasswordResetExecuteRequestDto.builder()
                    .resetToken("invalid_token")
                    .email("user@example.com")
                    .newPassword("NewPassword123!")
                    .confirmNewPassword("NewPassword123!")
                    .build();

            when(authenticationService.executePasswordReset(any(AuthRequestDto.PasswordResetExecuteRequestDto.class)))
                    .thenThrow(new RuntimeException("Invalid reset token"));

            // When & Then
            mockMvc.perform(post("/api/auth/reset-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
}