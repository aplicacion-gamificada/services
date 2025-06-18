package com.gamified.application.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.service.user.UserProfileService;
import com.gamified.application.config.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests completos para UserController con casos de uso realistas
 */
@WebMvcTest(UserController.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@DisplayName("UserController - Tests Completos")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        // Reset mocks
        reset(userProfileService);
    }

    // ================================
    // 1. CONSULTA DE PERFILES
    // ================================
    
    @Nested
    @DisplayName("1. Consulta de Perfiles")
    class ProfileConsultationTests {

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("GET /api/users/profile - Should return student profile when authenticated as student")
        void testGetProfile_Student() throws Exception {
            // Given
            Long userId = 456L;
            UserResponseDto.StudentResponseDto mockStudentProfile = UserResponseDto.StudentResponseDto.builder()
                    .id(userId)
                    .firstName("María")
                    .lastName("González")
                    .fullName("María González")
                    .email("maria.gonzalez@email.com")
                    .profilePictureUrl("https://example.com/profile/maria.jpg")
                    .username("maria_gonzalez")
                    .birth_date(Date.valueOf(LocalDate.of(2010, 5, 15)))
                    .pointsAmount(150)
                    .studentProfileId(789L)
                    .roleName("STUDENT")
                    .institutionName("Universidad Nacional")
                    .status(true)
                    .emailVerified(false)
                    .lastLoginAt(LocalDateTime.now().minusHours(2))
                    .level(1)
                    .recentAchievements(Collections.emptyList())
                    .createdAt(LocalDateTime.now().minusDays(30))
                    .build();

            when(userProfileService.getStudentProfile(userId)).thenReturn(mockStudentProfile);

            // When & Then
            mockMvc.perform(get("/api/users/profile")
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(456))
                    .andExpect(jsonPath("$.firstName").value("María"))
                    .andExpect(jsonPath("$.lastName").value("González"))
                    .andExpect(jsonPath("$.fullName").value("María González"))
                    .andExpect(jsonPath("$.email").value("maria.gonzalez@email.com"))
                    .andExpect(jsonPath("$.profilePictureUrl").value("https://example.com/profile/maria.jpg"))
                    .andExpect(jsonPath("$.username").value("maria_gonzalez"))
                    .andExpect(jsonPath("$.birth_date").exists())
                    .andExpect(jsonPath("$.pointsAmount").value(150))
                    .andExpect(jsonPath("$.studentProfileId").value(789))
                    .andExpect(jsonPath("$.roleName").value("STUDENT"))
                    .andExpect(jsonPath("$.institutionName").value("Universidad Nacional"))
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.emailVerified").value(false))
                    .andExpect(jsonPath("$.lastLoginAt").exists())
                    .andExpect(jsonPath("$.level").value(1))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.recentAchievements").isArray());

            verify(userProfileService, times(1)).getStudentProfile(userId);
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("GET /api/users/profile - Should return teacher profile when authenticated as teacher")
        void testGetProfile_Teacher() throws Exception {
            // Given
            Long userId = 789L;
            UserResponseDto.TeacherResponseDto mockTeacherProfile = UserResponseDto.TeacherResponseDto.builder()
                    .id(userId)
                    .firstName("Carlos")
                    .lastName("Ramírez")
                    .fullName("Carlos Ramírez")
                    .email("carlos.ramirez@email.com")
                    .profilePictureUrl("https://example.com/profile/carlos.jpg")
                    .teacherProfileId(123L)
                    .stemAreaId((byte) 1)
                    .stemAreaName("Matemáticas")
                    .roleName("TEACHER")
                    .institutionName("Universidad Nacional")
                    .status(true)
                    .emailVerified(true)
                    .lastLoginAt(LocalDateTime.now().minusMinutes(30))
                    .classroomsCount(3)
                    .studentsCount(45)
                    .createdAt(LocalDateTime.now().minusMonths(6))
                    .build();

            when(userProfileService.getTeacherProfile(userId)).thenReturn(mockTeacherProfile);

            // When & Then
            mockMvc.perform(get("/api/users/profile")
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(789))
                    .andExpect(jsonPath("$.firstName").value("Carlos"))
                    .andExpect(jsonPath("$.lastName").value("Ramírez"))
                    .andExpect(jsonPath("$.fullName").value("Carlos Ramírez"))
                    .andExpect(jsonPath("$.email").value("carlos.ramirez@email.com"))
                    .andExpect(jsonPath("$.profilePictureUrl").value("https://example.com/profile/carlos.jpg"))
                    .andExpect(jsonPath("$.teacherProfileId").value(123))
                    .andExpect(jsonPath("$.stemAreaId").value(1))
                    .andExpect(jsonPath("$.roleName").value("TEACHER"))
                    .andExpect(jsonPath("$.stemAreaName").value("Matemáticas"))
                    .andExpect(jsonPath("$.institutionName").value("Universidad Nacional"))
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.emailVerified").value(true))
                    .andExpect(jsonPath("$.lastLoginAt").exists())
                    .andExpect(jsonPath("$.classroomsCount").value(3))
                    .andExpect(jsonPath("$.studentsCount").value(45))
                    .andExpect(jsonPath("$.createdAt").exists());

            verify(userProfileService, times(1)).getTeacherProfile(userId);
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("GET /api/users/profile - Should return guardian profile when authenticated as guardian")
        void testGetProfile_Guardian() throws Exception {
            // Given
            Long userId = 321L;
            UserResponseDto.GuardianResponseDto mockGuardianProfile = UserResponseDto.GuardianResponseDto.builder()
                    .id(userId)
                    .firstName("Ana")
                    .lastName("Martínez")
                    .fullName("Ana Martínez")
                    .email("ana.martinez@email.com")
                    .profilePictureUrl("https://example.com/profile/ana.jpg")
                    .phone("+51123456789")
                    .guardianProfileId(456L)
                    .roleName("GUARDIAN")
                    .institutionName("Universidad Nacional")
                    .status(true)
                    .emailVerified(true)
                    .lastLoginAt(LocalDateTime.now().minusHours(1))
                    .studentsCount(2)
                    .students(Arrays.asList(
                            UserResponseDto.StudentBasicInfoDto.builder()
                                    .id(101L)
                                    .studentProfileId(201L)
                                    .username("student1")
                                    .fullName("Estudiante Uno")
                                    .birth_date(Date.valueOf(LocalDate.of(2010, 3, 15)))
                                    .pointsAmount(100)
                                    .build(),
                            UserResponseDto.StudentBasicInfoDto.builder()
                                    .id(102L)
                                    .studentProfileId(202L)
                                    .username("student2")
                                    .fullName("Estudiante Dos")
                                    .birth_date(Date.valueOf(LocalDate.of(2012, 7, 20)))
                                    .pointsAmount(75)
                                    .build()
                    ))
                    .createdAt(LocalDateTime.now().minusYears(1))
                    .build();

            when(userProfileService.getGuardianProfile(userId)).thenReturn(mockGuardianProfile);

            // When & Then
            mockMvc.perform(get("/api/users/profile")
                            .param("userId", userId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(321))
                    .andExpect(jsonPath("$.firstName").value("Ana"))
                    .andExpect(jsonPath("$.lastName").value("Martínez"))
                    .andExpect(jsonPath("$.fullName").value("Ana Martínez"))
                    .andExpect(jsonPath("$.email").value("ana.martinez@email.com"))
                    .andExpect(jsonPath("$.profilePictureUrl").value("https://example.com/profile/ana.jpg"))
                    .andExpect(jsonPath("$.phone").value("+51123456789"))
                    .andExpect(jsonPath("$.guardianProfileId").value(456))
                    .andExpect(jsonPath("$.roleName").value("GUARDIAN"))
                    .andExpect(jsonPath("$.institutionName").value("Universidad Nacional"))
                    .andExpect(jsonPath("$.status").value(true))
                    .andExpect(jsonPath("$.emailVerified").value(true))
                    .andExpect(jsonPath("$.lastLoginAt").exists())
                    .andExpect(jsonPath("$.studentsCount").value(2))
                    .andExpect(jsonPath("$.students").isArray())
                    .andExpect(jsonPath("$.students[0].id").value(101))
                    .andExpect(jsonPath("$.students[0].username").value("student1"))
                    .andExpect(jsonPath("$.students[0].fullName").value("Estudiante Uno"))
                    .andExpect(jsonPath("$.students[1].id").value(102))
                    .andExpect(jsonPath("$.students[1].username").value("student2"))
                    .andExpect(jsonPath("$.createdAt").exists());

            verify(userProfileService, times(1)).getGuardianProfile(userId);
        }

        @Test
        @DisplayName("GET /api/users/profile - Should return 401 when not authenticated")
        void testGetProfile_Unauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/profile"))
                    .andExpect(status().isUnauthorized());

            verify(userProfileService, never()).getStudentProfile(any());
            verify(userProfileService, never()).getTeacherProfile(any());
            verify(userProfileService, never()).getGuardianProfile(any());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("GET /api/users/students/{userId} - Should return student profile")
        void testGetStudentProfile_Success() throws Exception {
            // Given
            Long userId = 456L;
            UserResponseDto.StudentResponseDto mockStudent = UserResponseDto.StudentResponseDto.builder()
                    .id(456L)
                    .firstName("María")
                    .lastName("González")
                    .fullName("María González")
                    .email("maria.gonzalez@email.com")
                    .username("maria_gonzalez")
                    .pointsAmount(150)
                    .roleName("STUDENT")
                    .status(true)
                    .build();

            when(userProfileService.getStudentProfile(userId)).thenReturn(mockStudent);

            // When & Then
            mockMvc.perform(get("/api/users/students/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(456))
                    .andExpect(jsonPath("$.firstName").value("María"))
                    .andExpect(jsonPath("$.roleName").value("STUDENT"));

            verify(userProfileService, times(1)).getStudentProfile(userId);
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("GET /api/users/students/{userId} - Should return 403 when student tries to access another student")
        void testGetStudentProfile_Forbidden() throws Exception {
            // Given
            Long userId = 999L; // Different user

            when(userProfileService.getStudentProfile(userId))
                    .thenThrow(new RuntimeException("Access denied"));

            // When & Then
            mockMvc.perform(get("/api/users/students/{userId}", userId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("GET /api/users/students/{userId} - Should return 404 when student not found")
        void testGetStudentProfile_NotFound() throws Exception {
            // Given
            Long userId = 999L;

            when(userProfileService.getStudentProfile(userId))
                    .thenThrow(new RuntimeException("Student not found"));

            // When & Then
            mockMvc.perform(get("/api/users/students/{userId}", userId))
                    .andExpect(status().isNotFound());
        }
    }

    // ================================
    // 2. BÚSQUEDA DE USUARIOS
    // ================================
    
    @Nested
    @DisplayName("2. Búsqueda de Usuarios")
    class UserSearchTests {

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("GET /api/users/search - Should search users successfully")
        void testSearchUsers_Success() throws Exception {
            // Given
            String searchTerm = "María";
            String roleFilter = "STUDENT";
            int limit = 10;

            List<UserResponseDto.BasicUserResponseDto> mockResults = Arrays.asList(
                    UserResponseDto.BasicUserResponseDto.builder()
                            .id(1L)
                            .firstName("María")
                            .lastName("González")
                            .email("maria.gonzalez@email.com")
                            .roleName("STUDENT")
                            .institutionName("Universidad Nacional")
                            .status(true)
                            .build(),
                    UserResponseDto.BasicUserResponseDto.builder()
                            .id(2L)
                            .firstName("María")
                            .lastName("Rodríguez")
                            .email("maria.rodriguez@email.com")
                            .roleName("STUDENT")
                            .institutionName("Universidad Nacional")
                            .status(true)
                            .build()
            );

            when(userProfileService.searchUsers(searchTerm, roleFilter, limit)).thenReturn(mockResults);

            // When & Then
            mockMvc.perform(get("/api/users/search")
                            .param("searchTerm", searchTerm)
                            .param("roleFilter", roleFilter)
                            .param("limit", String.valueOf(limit)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].firstName").value("María"))
                    .andExpect(jsonPath("$[0].lastName").value("González"))
                    .andExpect(jsonPath("$[1].firstName").value("María"))
                    .andExpect(jsonPath("$[1].lastName").value("Rodríguez"));

            verify(userProfileService, times(1)).searchUsers(searchTerm, roleFilter, limit);
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("GET /api/users/search - Should use default values when optional params not provided")
        void testSearchUsers_DefaultValues() throws Exception {
            // Given
            String searchTerm = "test";
            List<UserResponseDto.BasicUserResponseDto> mockResults = Collections.emptyList();

            when(userProfileService.searchUsers(eq(searchTerm), isNull(), eq(20))).thenReturn(mockResults);

            // When & Then
            mockMvc.perform(get("/api/users/search")
                            .param("searchTerm", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(0));

            verify(userProfileService, times(1)).searchUsers(searchTerm, null, 20);
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("GET /api/users/search - Should return 400 when searchTerm is missing")
        void testSearchUsers_MissingSearchTerm() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/search"))
                    .andExpect(status().isBadRequest());

            verify(userProfileService, never()).searchUsers(anyString(), any(), anyInt());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("GET /api/users/search - Should return 403 when student tries to search")
        void testSearchUsers_ForbiddenForStudent() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/search")
                            .param("searchTerm", "test"))
                    .andExpect(status().isForbidden());

            verify(userProfileService, never()).searchUsers(anyString(), any(), anyInt());
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("GET /api/users/search - Should return empty list when no matches")
        void testSearchUsers_NoResults() throws Exception {
            // Given
            String searchTerm = "nonexistent";
            List<UserResponseDto.BasicUserResponseDto> emptyResults = Collections.emptyList();

            when(userProfileService.searchUsers(searchTerm, null, 20)).thenReturn(emptyResults);

            // When & Then
            mockMvc.perform(get("/api/users/search")
                            .param("searchTerm", searchTerm))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(0));

            verify(userProfileService, times(1)).searchUsers(searchTerm, null, 20);
        }
    }

    // ================================
    // 3. RELACIONES GUARDIAN-STUDENT
    // ================================
    
    @Nested
    @DisplayName("3. Relaciones Guardian-Student")
    class GuardianStudentRelationshipTests {

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("GET /api/users/guardians/{guardianUserId}/students - Should return students for guardian")
        void testGetStudentsForGuardian_Success() throws Exception {
            // Given
            Long guardianUserId = 123L;
            List<UserResponseDto.StudentResponseDto> mockStudents = Arrays.asList(
                    UserResponseDto.StudentResponseDto.builder()
                            .id(1L)
                            .firstName("María")
                            .lastName("González")
                            .username("maria_gonzalez")
                            .pointsAmount(150)
                            .roleName("STUDENT")
                            .build(),
                    UserResponseDto.StudentResponseDto.builder()
                            .id(2L)
                            .firstName("Juan")
                            .lastName("González")
                            .username("juan_gonzalez")
                            .pointsAmount(200)
                            .roleName("STUDENT")
                            .build()
            );

            when(userProfileService.getStudentsByGuardian(guardianUserId)).thenReturn(mockStudents);

            // When & Then
            mockMvc.perform(get("/api/users/guardians/{guardianUserId}/students", guardianUserId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].firstName").value("María"))
                    .andExpect(jsonPath("$[1].firstName").value("Juan"));

            verify(userProfileService, times(1)).getStudentsByGuardian(guardianUserId);
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("GET /api/users/guardians/{guardianUserId}/students - Should return own students")
        void testGetStudentsForGuardian_OwnStudents() throws Exception {
            // Given
            Long guardianUserId = 123L;
            List<UserResponseDto.StudentResponseDto> mockStudents = Arrays.asList(
                    UserResponseDto.StudentResponseDto.builder()
                            .id(1L)
                            .firstName("María")
                            .lastName("González")
                            .build()
            );

            when(userProfileService.getStudentsByGuardian(guardianUserId)).thenReturn(mockStudents);

            // When & Then
            mockMvc.perform(get("/api/users/guardians/{guardianUserId}/students", guardianUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(userProfileService, times(1)).getStudentsByGuardian(guardianUserId);
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("POST /api/users/guardians/associate-student - Should associate student successfully")
        void testAssociateStudent_Success() throws Exception {
            // Given
            UserRequestDto.StudentGuardianAssociationRequestDto associationRequest = 
                    UserRequestDto.StudentGuardianAssociationRequestDto.builder()
                            .studentProfileId(456L)
                            .guardianProfileId(789L)
                            .build();

            CommonResponseDto<Object> mockResponse = CommonResponseDto.<Object>builder()
                    .success(true)
                    .message("Estudiante asociado exitosamente al guardian")
                    .timestamp(LocalDateTime.now())
                    .build();

            // Note: The actual service doesn't have associateStudentToGuardian method
            // This would need to be implemented or the test should be adjusted

            // When & Then
            mockMvc.perform(post("/api/users/guardians/associate-student")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(associationRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Estudiante asociado exitosamente al guardian"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("POST /api/users/guardians/associate-student - Should return 403 for non-guardian")
        void testAssociateStudent_ForbiddenForStudent() throws Exception {
            // Given
            UserRequestDto.StudentGuardianAssociationRequestDto associationRequest = 
                    UserRequestDto.StudentGuardianAssociationRequestDto.builder()
                            .studentProfileId(456L)
                            .guardianProfileId(789L)
                            .build();

            // When & Then
            mockMvc.perform(post("/api/users/guardians/associate-student")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(associationRequest)))
                    .andExpect(status().isForbidden());
        }
    }

    // ================================
    // 4. ACTUALIZACIONES DE PERFIL
    // ================================
    
    @Nested
    @DisplayName("4. Actualizaciones de Perfil")
    class ProfileUpdateTests {

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("PUT /api/users/students/{userId} - Should update student profile successfully")
        void testUpdateStudentProfile_Success() throws Exception {
            // Given
            Long userId = 456L;
            UserRequestDto.StudentUpdateRequestDto updateRequest = UserRequestDto.StudentUpdateRequestDto.builder()
                    .firstName("María Actualizada")
                    .lastName("González Actualizada")
                    .username("maria_gonzalez_new")
                    .build();

            UserResponseDto.StudentResponseDto updatedStudent = UserResponseDto.StudentResponseDto.builder()
                    .id(userId)
                    .firstName("María Actualizada")
                    .lastName("González Actualizada")
                    .username("maria_gonzalez_new")
                    .email("maria.gonzalez@email.com")
                    .roleName("STUDENT")
                    .status(true)
                    .build();

            when(userProfileService.updateStudentProfile(userId, updateRequest)).thenReturn(updatedStudent);

            // When & Then
            mockMvc.perform(put("/api/users/students/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.firstName").value("María Actualizada"))
                    .andExpect(jsonPath("$.lastName").value("González Actualizada"))
                    .andExpect(jsonPath("$.username").value("maria_gonzalez_new"));

            verify(userProfileService, times(1)).updateStudentProfile(userId, updateRequest);
        }

        @Test
        @WithMockUser(roles = "TEACHER")
        @DisplayName("PUT /api/users/teachers/{userId} - Should update teacher profile successfully")
        void testUpdateTeacherProfile_Success() throws Exception {
            // Given
            Long userId = 789L;
            UserRequestDto.TeacherUpdateRequestDto updateRequest = UserRequestDto.TeacherUpdateRequestDto.builder()
                    .firstName("Carlos Actualizado")
                    .lastName("Ramírez Actualizado")
                    .stemAreaId((byte) 2)
                    .build();

            UserResponseDto.TeacherResponseDto updatedTeacher = UserResponseDto.TeacherResponseDto.builder()
                    .id(userId)
                    .firstName("Carlos Actualizado")
                    .lastName("Ramírez Actualizado")
                    .stemAreaId((byte) 2)
                    .stemAreaName("Ciencias")
                    .email("carlos.ramirez@email.com")
                    .roleName("TEACHER")
                    .status(true)
                    .build();

            when(userProfileService.updateTeacherProfile(userId, updateRequest)).thenReturn(updatedTeacher);

            // When & Then
            mockMvc.perform(put("/api/users/teachers/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.firstName").value("Carlos Actualizado"))
                    .andExpect(jsonPath("$.lastName").value("Ramírez Actualizado"))
                    .andExpect(jsonPath("$.stemAreaName").value("Ciencias"));

            verify(userProfileService, times(1)).updateTeacherProfile(userId, updateRequest);
        }

        @Test
        @WithMockUser(roles = "GUARDIAN")
        @DisplayName("PUT /api/users/guardians/{userId} - Should update guardian profile successfully")
        void testUpdateGuardianProfile_Success() throws Exception {
            // Given
            Long userId = 321L;
            UserRequestDto.GuardianUpdateRequestDto updateRequest = UserRequestDto.GuardianUpdateRequestDto.builder()
                    .firstName("Ana Actualizada")
                    .lastName("Martínez Actualizada")
                    .phone("+57-300-7654321")
                    .build();

            UserResponseDto.GuardianResponseDto updatedGuardian = UserResponseDto.GuardianResponseDto.builder()
                    .id(userId)
                    .firstName("Ana Actualizada")
                    .lastName("Martínez Actualizada")
                    .phone("+57-300-7654321")
                    .email("ana.martinez@email.com")
                    .roleName("GUARDIAN")
                    .status(true)
                    .build();

            when(userProfileService.updateGuardianProfile(userId, updateRequest)).thenReturn(updatedGuardian);

            // When & Then
            mockMvc.perform(put("/api/users/guardians/{userId}", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.firstName").value("Ana Actualizada"))
                    .andExpect(jsonPath("$.lastName").value("Martínez Actualizada"))
                    .andExpect(jsonPath("$.phone").value("+57-300-7654321"));

            verify(userProfileService, times(1)).updateGuardianProfile(userId, updateRequest);
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("PUT /api/users/{userId}/password - Should change password successfully")
        void testChangePassword_Success() throws Exception {
            // Given
            Long userId = 456L;
            UserRequestDto.PasswordUpdateRequestDto passwordRequest = UserRequestDto.PasswordUpdateRequestDto.builder()
                    .currentPassword("OldPassword123!")
                    .newPassword("NewPassword123!")
                    .confirmPassword("NewPassword123!")
                    .build();

            CommonResponseDto<Object> mockResponse = CommonResponseDto.<Object>builder()
                    .success(true)
                    .message("Contraseña actualizada exitosamente")
                    .timestamp(LocalDateTime.now())
                    .build();

            when(userProfileService.updatePassword(eq(userId), any(UserRequestDto.PasswordUpdateRequestDto.class)))
                    .thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(put("/api/users/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(passwordRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Contraseña actualizada exitosamente"));

            verify(userProfileService, times(1)).updatePassword(eq(userId), any(UserRequestDto.PasswordUpdateRequestDto.class));
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("PUT /api/users/{userId}/profile-picture - Should update profile picture successfully")
        void testUpdateProfilePicture_Success() throws Exception {
            // Given
            Long userId = 456L;
            String newPictureUrl = "https://example.com/new-picture.jpg";

            CommonResponseDto<Object> mockResponse = CommonResponseDto.<Object>builder()
                    .success(true)
                    .message("Foto de perfil actualizada exitosamente")
                    .timestamp(LocalDateTime.now())
                    .build();

            when(userProfileService.updateProfilePicture(userId, newPictureUrl)).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(put("/api/users/{userId}/profile-picture", userId)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content("\"" + newPictureUrl + "\""))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Foto de perfil actualizada exitosamente"))
                    .andExpect(jsonPath("$.timestamp").exists());

            verify(userProfileService, times(1)).updateProfilePicture(userId, newPictureUrl);
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("PUT /api/users/{userId}/profile-picture - Should return 400 for invalid URL")
        void testUpdateProfilePicture_InvalidUrl() throws Exception {
            // Given
            Long userId = 456L;
            String invalidUrl = "not-a-valid-url";

            when(userProfileService.updateProfilePicture(userId, invalidUrl))
                    .thenThrow(new IllegalArgumentException("URL de imagen inválida"));

            // When & Then
            mockMvc.perform(put("/api/users/{userId}/profile-picture", userId)
                              .contentType(MediaType.APPLICATION_JSON)
                              .content("\"" + invalidUrl + "\""))
                    .andExpect(status().isBadRequest());

            verify(userProfileService, times(1)).updateProfilePicture(userId, invalidUrl);
        }
    }

    // ================================
    // 5. ELIMINACIÓN DE USUARIOS
    // ================================
    
    @Nested
    @DisplayName("5. Eliminación de Usuarios")
    class UserDeletionTests {

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("DELETE /api/users/{userId} - Should deactivate user account successfully")
        void testDeleteUser_Success() throws Exception {
            // Given
            Long userId = 456L;

            CommonResponseDto<Object> mockResponse = CommonResponseDto.<Object>builder()
                    .success(true)
                    .message("Cuenta desactivada exitosamente")
                    .timestamp(LocalDateTime.now())
                    .build();

            when(userProfileService.deactivateAccount(userId)).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(delete("/api/users/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Cuenta desactivada exitosamente"))
                .andExpect(jsonPath("$.timestamp").exists());

            verify(userProfileService, times(1)).deactivateAccount(userId);
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("DELETE /api/users/{userId} - Should return 403 when trying to delete another user")
        void testDeleteUser_Forbidden() throws Exception {
            // Given
            Long userId = 999L; // Different user

            when(userProfileService.deactivateAccount(userId))
                    .thenThrow(new RuntimeException("Access denied"));

            // When & Then
            mockMvc.perform(delete("/api/users/{userId}", userId))
                    .andExpect(status().isForbidden());

            verify(userProfileService, times(1)).deactivateAccount(userId);
        }

        @Test
        @WithMockUser(roles = "STUDENT")
        @DisplayName("DELETE /api/users/{userId} - Should return 404 when user not found")
        void testDeleteUser_NotFound() throws Exception {
            // Given
            Long userId = 999L;

            when(userProfileService.deactivateAccount(userId))
                    .thenThrow(new RuntimeException("User not found"));

            // When & Then
            mockMvc.perform(delete("/api/users/{userId}", userId))
                    .andExpect(status().isNotFound());

            verify(userProfileService, times(1)).deactivateAccount(userId);
        }
    }
}