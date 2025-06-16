package com.gamified.application.auth.controller.advice;

import com.gamified.application.auth.dto.response.CommonResponseDto;
import com.gamified.application.auth.dto.response.ErrorResponseDto;
import com.gamified.application.auth.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para la aplicación
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de autenticación
     * @param ex Excepción de autenticación
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        log.warn("Excepción de autenticación: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Error de autenticación",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja excepciones de credenciales incorrectas
     * @param ex Excepción de credenciales incorrectas
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        
        log.warn("Credenciales incorrectas: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Credenciales incorrectas",
                "Usuario o contraseña incorrectos",
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja excepciones de acceso denegado
     * @param ex Excepción de acceso denegado
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        log.warn("Acceso denegado: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                "Acceso denegado",
                "No tiene permisos para acceder a este recurso",
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja excepciones de validación
     * @param ex Excepción de validación
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponseDto> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();
        
        // Obtener todos los errores de validación
        Map<String, String> validationErrors = new HashMap<>();
        fieldErrors.forEach(error -> 
            validationErrors.put(error.getField(), error.getDefaultMessage())
        );
        
        String errorMessage = "Errores de validación: " + 
                validationErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "));
        
        log.warn("Errores de validación: {}", errorMessage);
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                errorMessage,
                request.getRequestURI(),
                LocalDateTime.now(),
                validationErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Maneja excepciones de usuario no encontrado
     * @param ex Excepción de usuario no encontrado
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponseDto> handleUserNotFoundException(
            UserNotFoundException ex, HttpServletRequest request) {
        
        log.warn("Usuario no encontrado: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                "Usuario no encontrado",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Maneja excepciones de email no verificado
     * @param ex Excepción de email no verificado
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(EmailNotVerifiedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<ErrorResponseDto> handleEmailNotVerifiedException(
            EmailNotVerifiedException ex, HttpServletRequest request) {
        
        log.warn("Email no verificado: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                "Email no verificado",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Maneja excepciones de token inválido
     * @param ex Excepción de token inválido
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponseDto> handleInvalidTokenException(
            InvalidTokenException ex, HttpServletRequest request) {
        
        log.warn("Token inválido: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                "Token inválido",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja excepciones de conflicto (ejemplo: email ya existe)
     * @param ex Excepción de conflicto
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(ResourceConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponseDto> handleResourceConflictException(
            ResourceConflictException ex, HttpServletRequest request) {
        
        log.warn("Conflicto de recursos: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Conflicto de recursos",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja excepciones de email ya existente
     * @param ex Excepción de email ya existente
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponseDto> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex, HttpServletRequest request) {
        
        log.warn("Email ya existe: {}", ex.getMessage());
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                "Email ya registrado",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Maneja excepciones genéricas
     * @param ex Excepción
     * @param request Petición HTTP
     * @return Respuesta de error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDto> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        // Para errores internos del servidor, solo loguear el mensaje principal y no el stack trace completo
        // excepto si el nivel de log es DEBUG o inferior
        if (log.isDebugEnabled()) {
            log.error("Error interno del servidor: {}", ex.getMessage(), ex);
        } else {
            log.error("Error interno del servidor: {}", ex.getMessage());
        }
        
        // Crear una respuesta de error más amigable para el usuario
        String userMessage = "Ha ocurrido un error en el servidor. Por favor, inténtelo de nuevo más tarde.";
        
        // Si es un error conocido, mostrar un mensaje más específico
        if (ex.getMessage() != null && ex.getMessage().contains("Email already exists")) {
            userMessage = "El email ya está registrado en el sistema.";
        }
        
        ErrorResponseDto errorResponse = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                userMessage,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}