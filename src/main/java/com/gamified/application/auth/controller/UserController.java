package com.gamified.application.auth.controller;

import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.service.user.UserProfileService;
import com.gamified.application.auth.service.user.UserRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para operaciones relacionadas con usuarios
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileService userProfileService;
    private final UserRegistrationService userRegistrationService;

    /**
     * Obtiene el perfil del usuario actual
     * @param authentication Datos de autenticación del usuario
     * @return Perfil del usuario según su rol
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        
        if (userRole.equals("ROLE_STUDENT")) {
            UserResponseDto.StudentResponseDto profile = userProfileService.getStudentProfile(userId);
            return ResponseEntity.ok(profile);
        } else if (userRole.equals("ROLE_TEACHER")) {
            UserResponseDto.TeacherResponseDto profile = userProfileService.getTeacherProfile(userId);
            return ResponseEntity.ok(profile);
        } else if (userRole.equals("ROLE_GUARDIAN")) {
            UserResponseDto.GuardianResponseDto profile = userProfileService.getGuardianProfile(userId);
            return ResponseEntity.ok(profile);
        } else {
            return ResponseEntity.badRequest().body(
                    new CommonResponseDto(false, "Rol de usuario no válido", null)
            );
        }
    }
    
    /**
     * Obtiene el perfil de un estudiante específico
     * @param userId ID del usuario
     * @return Perfil del estudiante
     */
    @GetMapping("/students/{userId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'GUARDIAN') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponseDto.StudentResponseDto> getStudentProfile(@PathVariable Long userId) {
        UserResponseDto.StudentResponseDto profile = userProfileService.getStudentProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Obtiene el perfil de un profesor específico
     * @param userId ID del usuario
     * @return Perfil del profesor
     */
    @GetMapping("/teachers/{userId}")
    @PreAuthorize("hasRole('TEACHER') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponseDto.TeacherResponseDto> getTeacherProfile(@PathVariable Long userId) {
        UserResponseDto.TeacherResponseDto profile = userProfileService.getTeacherProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Obtiene el perfil de un tutor específico
     * @param userId ID del usuario
     * @return Perfil del tutor
     */
    @GetMapping("/guardians/{userId}")
    @PreAuthorize("hasRole('TEACHER') or #userId == authentication.principal.id")
    public ResponseEntity<UserResponseDto.GuardianResponseDto> getGuardianProfile(@PathVariable Long userId) {
        UserResponseDto.GuardianResponseDto profile = userProfileService.getGuardianProfile(userId);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Actualiza el perfil de un estudiante
     * @param userId ID del usuario
     * @param updateRequest Datos a actualizar
     * @return Perfil actualizado
     */
    @PutMapping("/students/{userId}")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('GUARDIAN')")
    public ResponseEntity<UserResponseDto.StudentResponseDto> updateStudentProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserRequestDto.StudentUpdateRequestDto updateRequest) {
        
        UserResponseDto.StudentResponseDto profile = userProfileService.updateStudentProfile(userId, updateRequest);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Actualiza el perfil de un profesor
     * @param userId ID del usuario
     * @param updateRequest Datos a actualizar
     * @return Perfil actualizado
     */
    @PutMapping("/teachers/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<UserResponseDto.TeacherResponseDto> updateTeacherProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserRequestDto.TeacherUpdateRequestDto updateRequest) {
        
        UserResponseDto.TeacherResponseDto profile = userProfileService.updateTeacherProfile(userId, updateRequest);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Actualiza el perfil de un tutor
     * @param userId ID del usuario
     * @param updateRequest Datos a actualizar
     * @return Perfil actualizado
     */
    @PutMapping("/guardians/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<UserResponseDto.GuardianResponseDto> updateGuardianProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UserRequestDto.GuardianUpdateRequestDto updateRequest) {
        
        UserResponseDto.GuardianResponseDto profile = userProfileService.updateGuardianProfile(userId, updateRequest);
        return ResponseEntity.ok(profile);
    }
    
    /**
     * Actualiza la contraseña del usuario
     * @param userId ID del usuario
     * @param updateRequest Datos para actualizar la contraseña
     * @return Resultado de la operación
     */
    @PutMapping("/{userId}/password")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<CommonResponseDto> updatePassword(
            @PathVariable Long userId,
            @Valid @RequestBody UserRequestDto.PasswordUpdateRequestDto updateRequest,
            HttpServletRequest request) {
        
        // Validar que las contraseñas coincidan
        if (!updateRequest.getNewPassword().equals(updateRequest.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(
                    new CommonResponseDto(false, "Las contraseñas no coinciden", null)
            );
        }
        
        CommonResponseDto response = userProfileService.updatePassword(userId, updateRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Actualiza la foto de perfil del usuario
     * @param userId ID del usuario
     * @param profilePictureUrl URL de la nueva foto
     * @return Resultado de la operación
     */
    @PutMapping("/{userId}/profile-picture")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<CommonResponseDto> updateProfilePicture(
            @PathVariable Long userId,
            @RequestBody String profilePictureUrl) {
        
        CommonResponseDto response = userProfileService.updateProfilePicture(userId, profilePictureUrl);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Desactiva la cuenta del usuario
     * @param userId ID del usuario
     * @return Resultado de la operación
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("#userId == authentication.principal.id")
    public ResponseEntity<CommonResponseDto> deactivateAccount(@PathVariable Long userId) {
        CommonResponseDto response = userProfileService.deactivateAccount(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene los estudiantes asociados a un tutor
     * @param guardianUserId ID del usuario tutor
     * @return Lista de perfiles de estudiantes
     */
    @GetMapping("/guardians/{guardianUserId}/students")
    @PreAuthorize("hasRole('TEACHER') or #guardianUserId == authentication.principal.id")
    public ResponseEntity<List<UserResponseDto.StudentResponseDto>> getStudentsByGuardian(
            @PathVariable Long guardianUserId) {
        
        List<UserResponseDto.StudentResponseDto> students = userProfileService.getStudentsByGuardian(guardianUserId);
        return ResponseEntity.ok(students);
    }
    
    /**
     * Asocia un estudiante a un tutor
     * @param associationRequest Datos de la asociación
     * @return Resultado de la operación
     */
    @PostMapping("/guardians/associate-student")
    @PreAuthorize("hasRole('GUARDIAN')")
    public ResponseEntity<CommonResponseDto> associateStudentToGuardian(
            @Valid @RequestBody UserRequestDto.StudentGuardianAssociationRequestDto associationRequest) {
        
        CommonResponseDto response = userRegistrationService.associateStudentToGuardian(associationRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Busca usuarios por término de búsqueda
     * @param searchTerm Término de búsqueda
     * @param roleFilter Filtro de rol (opcional)
     * @param limit Límite de resultados (opcional)
     * @return Lista de perfiles que coinciden
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<UserResponseDto.BasicUserResponseDto>> searchUsers(
            @RequestParam String searchTerm,
            @RequestParam(required = false) String roleFilter,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        
        List<UserResponseDto.BasicUserResponseDto> users = userProfileService.searchUsers(searchTerm, roleFilter, limit);
        return ResponseEntity.ok(users);
    }
    
    /**
     * Extrae el ID de usuario de la autenticación
     * @param authentication Autenticación actual
     * @return ID del usuario
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // Esto dependerá de cómo se almacene el ID de usuario en el objeto Authentication
        // Por ahora, asumiremos que es un Long que se puede obtener del principal
        return Long.valueOf(authentication.getName());
    }
} 