package com.gamified.application.auth.controller;

import com.gamified.application.auth.dto.request.UserRequestDto;
import com.gamified.application.auth.dto.response.UserResponseDto;
import com.gamified.application.auth.service.user.UserRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para operaciones de registro de usuarios
 */
@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRegistrationService userRegistrationService;

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
    public ResponseEntity<UserResponseDto.StudentResponseDto> registerStudent(
            @Valid @RequestBody UserRequestDto.StudentRegistrationRequestDto studentRequest,
            HttpServletRequest request) {
        
        // Procesar registro de estudiante
        UserResponseDto.StudentResponseDto response = userRegistrationService.registerStudent(studentRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registrar un nuevo profesor
     * @param teacherRequest Datos del profesor
     * @param request Request HTTP para obtener información del cliente
     * @return Perfil del profesor creado
     */
    @PostMapping("/teachers")
    public ResponseEntity<UserResponseDto.TeacherResponseDto> registerTeacher(
            @Valid @RequestBody UserRequestDto.TeacherRegistrationRequestDto teacherRequest,
            HttpServletRequest request) {
        
        // Procesar registro de profesor
        UserResponseDto.TeacherResponseDto response = userRegistrationService.registerTeacher(teacherRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registrar un nuevo tutor
     * @param guardianRequest Datos del tutor
     * @param request Request HTTP para obtener información del cliente
     * @return Perfil del tutor creado
     */
    @PostMapping("/guardians")
    public ResponseEntity<UserResponseDto.GuardianResponseDto> registerGuardian(
            @Valid @RequestBody UserRequestDto.GuardianRegistrationRequestDto guardianRequest,
            HttpServletRequest request) {
        
        // Procesar registro de tutor
        UserResponseDto.GuardianResponseDto response = userRegistrationService.registerGuardian(guardianRequest);
        return ResponseEntity.ok(response);
    }
} 