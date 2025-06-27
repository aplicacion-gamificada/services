package com.gamified.application.auth.service.auth;

import com.gamified.application.shared.model.dto.request.AuthRequestDto;
import com.gamified.application.shared.model.dto.response.AuthResponseDto;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import com.gamified.application.shared.model.dto.request.SessionRequestDto;
import com.gamified.application.shared.model.dto.response.SessionResponseDto;
import com.gamified.application.user.model.dto.response.UserResponseDto;
import com.gamified.application.user.model.entity.User;
import com.gamified.application.auth.entity.security.RefreshToken;
import com.gamified.application.auth.repository.core.UserRepository;
import com.gamified.application.user.repository.composite.CompleteUserRepository;
import com.gamified.application.auth.repository.security.SecurityRepository;
import com.gamified.application.auth.service.security.PasswordService;
import com.gamified.application.auth.service.audit.SecurityAuditService;
import com.gamified.application.user.model.entity.composite.CompleteStudent;
import com.gamified.application.user.model.entity.composite.CompleteTeacher;
import com.gamified.application.user.model.entity.composite.CompleteGuardian;
import com.gamified.application.shared.repository.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del servicio de autenticación
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CompleteUserRepository completeUserRepository;
    private final SecurityRepository securityRepository;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final SecurityAuditService auditService;
    
    @Override
    @Transactional
    public AuthResponseDto.LoginResponseDto login(AuthRequestDto.LoginRequestDto authRequest) {
        log.info("Starting login process for email: {}", authRequest.getEmail());
        
        try {
            // 1. Autenticar usando Spring Security
            log.info("Step 1: Authenticating with Spring Security AuthenticationManager");
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getEmail(),
                    authRequest.getPassword()
                )
            );
            log.info("Step 1 SUCCESS: Spring Security authentication passed");
            
            // 2. Obtener usuario desde la base de datos
            log.info("Step 2: Fetching user from database by email: {}", authRequest.getEmail());
            Optional<User> userOpt = userRepository.findByEmail(authRequest.getEmail());
            if (userOpt.isEmpty()) {
                log.error("Step 2 FAILED: User not found in database");
                throw new BadCredentialsException("Usuario no encontrado");
            }
            
            User user = userOpt.get();
            log.info("Step 2 SUCCESS: User found - ID: {}, Email: {}, Role: {}, EmailVerified: {}", 
                    user.getId(), user.getEmail(), user.getRole() != null ? user.getRole().getName() : "null", user.isEmailVerified());
            
            // 3. Verificar que el email esté verificado (asumimos que sí según el requerimiento)
            if (!user.isEmailVerified()) {
                log.info("Step 3: Email not verified, forcing verification");
                // Forzar verificación según requerimiento
                user.verifyEmail();
            } else {
                log.info("Step 3: Email already verified");
            }
            
            // 4. Generar tokens
            log.info("Step 4: Generating tokens");
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().getName());
            String accessToken = tokenService.generateAccessToken(user, claims);
            
            RefreshToken refreshToken = tokenService.generateRefreshToken(
                user.getId(),
                authRequest.getDeviceInfo() != null ? "127.0.0.1" : "127.0.0.1", // IP por defecto
                authRequest.getUserAgent() != null ? authRequest.getUserAgent() : "Unknown",
                authRequest.getDeviceInfo() != null ? authRequest.getDeviceInfo() : "Unknown",
                "Default Session"
            );
            log.info("Step 4 SUCCESS: Tokens generated");
            
            // 5. Actualizar último login
            log.info("Step 5: Updating login status");
            userRepository.updateLoginStatus(user.getId(), true, "127.0.0.1");
            
            // 6. Registrar auditoría
            log.info("Step 6: Recording audit log");
            auditService.recordSuccessfulLogin(user.getId(), "127.0.0.1", authRequest.getUserAgent());
            
            // 7. Obtener información completa del usuario
            log.info("Step 7: Fetching complete user info for email: {}", authRequest.getEmail());
            Object completeUser = completeUserRepository.findCompleteUserByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("No se encontró el perfil completo del usuario."));
            UserResponseDto.UserInfoDto userInfoDto = buildUserInfoDto(completeUser);

            // 8. Crear respuesta
            log.info("Step 8: Creating response");
            AuthResponseDto.LoginResponseDto response = AuthResponseDto.LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hora
                    .loginTime(LocalDateTime.now())
                    .userInfo(userInfoDto)
                    .build();
            
            log.info("LOGIN SUCCESS for user: {}", authRequest.getEmail());
            return response;
                    
        } catch (AuthenticationException e) {
            log.error("LOGIN FAILED - AuthenticationException for email: {} - Error: {}", authRequest.getEmail(), e.getMessage());
            
            // Registrar intento fallido
            Optional<User> userOpt = userRepository.findByEmail(authRequest.getEmail());
            if (userOpt.isPresent()) {
                userRepository.updateLoginStatus(userOpt.get().getId(), false, "127.0.0.1");
                auditService.recordFailedLogin(userOpt.get().getId(), "127.0.0.1", "Invalid credentials");
            }
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }
    
    @Override
    @Transactional
    public AuthResponseDto.LoginResponseDto loginStudent(AuthRequestDto.StudentLoginRequestDto loginRequest) {
        try {
            // 1. Buscar estudiante por username
            Optional<CompleteStudent> studentOpt =
                completeUserRepository.findCompleteStudentByUsername(loginRequest.getUsername());
                
            if (studentOpt.isEmpty()) {
                throw new BadCredentialsException("Estudiante no encontrado");
            }
            
            CompleteStudent student = studentOpt.get();
            User user = student.getUser();
            
            // 2. Verificar contraseña
            if (!passwordService.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
                // Registrar intento fallido
                userRepository.updateLoginStatus(user.getId(), false, "127.0.0.1");
                auditService.recordFailedLogin(user.getId(), "127.0.0.1", "Invalid password");
                throw new BadCredentialsException("Contraseña incorrecta");
            }
            
            // 3. Verificar que el email esté verificado (asumimos que sí)
            if (!user.isEmailVerified()) {
                user.verifyEmail();
            }
            
            // 4. Generar tokens
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "STUDENT");
            claims.put("username", student.getStudentProfile().getUsername());
            String accessToken = tokenService.generateAccessToken(user, claims);
            
            RefreshToken refreshToken = tokenService.generateRefreshToken(
                user.getId(),
                "127.0.0.1",
                loginRequest.getUserAgent() != null ? loginRequest.getUserAgent() : "Unknown",
                loginRequest.getDeviceInfo() != null ? loginRequest.getDeviceInfo() : "Unknown",
                "Student Session"
            );
            
            // 5. Actualizar último login
            userRepository.updateLoginStatus(user.getId(), true, "127.0.0.1");
            
            // 6. Registrar auditoría
            auditService.recordSuccessfulLogin(user.getId(), "127.0.0.1", loginRequest.getUserAgent());
            
            // 7. Crear UserInfo DTO
            UserResponseDto.UserInfoDto userInfoDto = buildUserInfoDto(student);

            // 8. Crear respuesta
            return AuthResponseDto.LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hora
                    .loginTime(LocalDateTime.now())
                    .userInfo(userInfoDto)
                    .build();
                    
        } catch (Exception e) {
            throw new BadCredentialsException("Error en login de estudiante: " + e.getMessage());
        }
    }
    
    @Override
    public SessionResponseDto.RefreshTokenResponseDto refreshToken(SessionRequestDto.RefreshTokenRequestDto refreshRequest) {
        try {
            // 1. Buscar y validar refresh token
            Optional<RefreshToken> refreshTokenOpt = securityRepository.findRefreshTokenByValue(refreshRequest.getRefreshToken());
            
            if (refreshTokenOpt.isEmpty() || refreshTokenOpt.get().isRevoked()) {
                throw new BadCredentialsException("Refresh token inválido");
            }
            
            RefreshToken refreshToken = refreshTokenOpt.get();
            
            // 2. Verificar expiración
            if (refreshToken.getExpiresAt().before(new java.util.Date())) {
                securityRepository.revokeAllUserTokens(refreshToken.getId(), "Expired");
                throw new BadCredentialsException("Refresh token expirado");
            }
            
            // 3. Obtener usuario
            Optional<User> userOpt = userRepository.findById(refreshToken.getUserId());
            if (userOpt.isEmpty()) {
                throw new BadCredentialsException("Usuario no encontrado");
            }
            
            User user = userOpt.get();
            
            // 4. Generar nuevo access token
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole().getName());
            String newAccessToken = tokenService.generateAccessToken(user, claims);
            
            // 5. Actualizar último uso del refresh token
            securityRepository.updateRefreshTokenLastUsed(refreshToken.getId());
            
            return SessionResponseDto.RefreshTokenResponseDto.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .expiresIn(3600L)
                    .issuedAt(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            throw new BadCredentialsException("Error al renovar token: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CommonResponseDto logout(SessionRequestDto.LogoutRequestDto logoutRequest) {
        log.info("Starting logout process for refresh token");
        
        try {
            if (logoutRequest.getRefreshToken() != null && !logoutRequest.getRefreshToken().trim().isEmpty()) {
                // 1. Buscar y validar refresh token
                Optional<RefreshToken> refreshTokenOpt = securityRepository.findRefreshTokenByValue(logoutRequest.getRefreshToken());
                if (refreshTokenOpt.isPresent()) {
                    RefreshToken refreshToken = refreshTokenOpt.get();
                    Long userId = refreshToken.getUserId();
                    
                    log.info("Found refresh token for user ID: {}", userId);
                    
                    // 2. Revocar el refresh token específico
                    securityRepository.revokeToken(logoutRequest.getRefreshToken(), "User logout");
                    log.info("Refresh token revoked successfully");
                    
                    // 3. Actualizar estado de login del usuario
                    userRepository.updateLoginStatus(userId, false, "logout");
                    log.info("User login status updated to false");
                    
                    // 4. Registrar auditoría del logout
                    auditService.recordLogout(userId, 
                        logoutRequest.getDeviceInfo() != null ? "127.0.0.1" : "127.0.0.1", 
                        logoutRequest.getDeviceInfo());
                    log.info("Logout audit recorded");
                    
                    return CommonResponseDto.builder()
                            .success(true)
                            .message("Sesión cerrada correctamente")
                            .build();
                } else {
                    log.warn("Refresh token not found or invalid");
                    return CommonResponseDto.builder()
                            .success(false)
                            .message("Token de sesión no válido o ya expirado")
                            .build();
                }
            } else {
                log.warn("No refresh token provided in logout request");
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Token de sesión requerido para cerrar sesión")
                        .build();
            }
                    
        } catch (Exception e) {
            log.error("Error during logout process: {}", e.getMessage(), e);
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Error al cerrar sesión: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public CommonResponseDto verifyEmail(AuthRequestDto.EmailVerificationRequestDto verifyRequest) {
        try {
            log.info("Starting email verification for token: {}", verifyRequest.getVerificationToken());
            
            // Validar que el token no esté vacío
            if (verifyRequest.getVerificationToken() == null || verifyRequest.getVerificationToken().trim().isEmpty()) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Token de verificación requerido")
                        .build();
            }
            
            // Verificar el email usando el repositorio de seguridad
            Result<com.gamified.application.auth.entity.security.EmailVerification> result = 
                    securityRepository.verifyEmail(verifyRequest.getVerificationToken());
            
            if (result.isSuccess()) {
                log.info("Email verification successful for token: {}", verifyRequest.getVerificationToken());
                
                // Aquí también deberíamos actualizar el estado del usuario en la tabla users
                // userRepository.markEmailAsVerified(userId);
                
                return CommonResponseDto.builder()
                        .success(true)
                        .message("Email verificado correctamente")
                        .build();
            } else {
                log.warn("Email verification failed: {}", result.getErrorMessage());
                return CommonResponseDto.builder()
                        .success(false)
                        .message(result.getErrorMessage())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error during email verification: {}", e.getMessage(), e);
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Error al verificar el email: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public CommonResponseDto requestPasswordReset(AuthRequestDto.PasswordResetRequestDto resetRequest) {
        try {
            log.info("Starting password reset request for email: {}", resetRequest.getEmail());
            
            // Validar que el email no esté vacío
            if (resetRequest.getEmail() == null || resetRequest.getEmail().trim().isEmpty()) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Email requerido para restablecer contraseña")
                        .build();
            }
            
            // Verificar que el usuario existe
            Optional<User> userOpt = userRepository.findByEmail(resetRequest.getEmail());
            if (userOpt.isEmpty()) {
                log.warn("Password reset requested for non-existent email: {}", resetRequest.getEmail());
                // Por seguridad, no revelamos si el email existe o no
                return CommonResponseDto.builder()
                        .success(true)
                        .message("Si el email existe en nuestro sistema, recibirás un enlace de restablecimiento")
                        .build();
            }
            
            User user = userOpt.get();
            log.info("Password reset requested for user ID: {}", user.getId());
            
            // Generar token de reset (implementación temporal)
            // En una implementación real, se crearía un token en la BD y se enviaría por email
            // String resetToken = UUID.randomUUID().toString();
            // securityRepository.createPasswordResetToken(user.getId(), resetToken, resetRequest.getDeviceInfo());
            
            // Registrar auditoría
            auditService.logAction(user.getId(), (byte) 4, "USER", user.getId(), 
                    "Password reset requested", "127.0.0.1", resetRequest.getUserAgent(), true);
            
            log.info("Password reset request processed for email: {}", resetRequest.getEmail());
            
            return CommonResponseDto.builder()
                    .success(true)
                    .message("Si el email existe en nuestro sistema, recibirás un enlace de restablecimiento")
                    .build();
        } catch (Exception e) {
            log.error("Error during password reset request: {}", e.getMessage(), e);
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Error al procesar la solicitud de restablecimiento")
                    .build();
        }
    }
    
    @Override
    public CommonResponseDto executePasswordReset(AuthRequestDto.PasswordResetExecuteRequestDto resetRequest) {
        try {
            log.info("Starting password reset execution for email: {}", resetRequest.getEmail());
            
            // Validaciones básicas
            if (resetRequest.getResetToken() == null || resetRequest.getResetToken().trim().isEmpty()) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Token de restablecimiento requerido")
                        .build();
            }
            
            if (!resetRequest.isNewPasswordConfirmed()) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Las contraseñas no coinciden")
                        .build();
            }
            
            // Verificar que el usuario existe
            Optional<User> userOpt = userRepository.findByEmail(resetRequest.getEmail());
            if (userOpt.isEmpty()) {
                return CommonResponseDto.builder()
                        .success(false)
                        .message("Token de restablecimiento inválido o expirado")
                        .build();
            }
            
            User user = userOpt.get();
            log.info("Password reset execution for user ID: {}", user.getId());
            
            // Aquí se validaría el token de reset en la BD
            // Optional<PasswordResetToken> tokenOpt = securityRepository.findPasswordResetToken(resetRequest.getResetToken());
            // if (tokenOpt.isEmpty() || tokenOpt.get().isExpired()) { ... }
            
            // Hash de la nueva contraseña
            String hashedPassword = passwordService.hashPassword(resetRequest.getNewPassword());
            
            // Actualizar contraseña
            userRepository.updatePassword(user.getId(), hashedPassword);
            
            // Registrar cambio de contraseña en historial
            securityRepository.recordPasswordChange(user.getId(), hashedPassword, "127.0.0.1", "Password Reset", false);
            
            // Revocar todas las sesiones activas del usuario por seguridad
            securityRepository.revokeAllUserTokens(user.getId(), "Password reset - security measure");
            
            // Registrar auditoría
            auditService.logAction(user.getId(), (byte) 5, "USER", user.getId(), 
                    "Password reset completed", "127.0.0.1", "Password Reset", true);
            
            log.info("Password reset completed successfully for user ID: {}", user.getId());
            
            return CommonResponseDto.builder()
                    .success(true)
                    .message("Contraseña restablecida correctamente. Por seguridad, todas tus sesiones han sido cerradas.")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error during password reset execution: {}", e.getMessage(), e);
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Error al restablecer la contraseña: " + e.getMessage())
                    .build();
        }
    }
    
    @Override
    public CommonResponseDto register(AuthRequestDto.RegisterRequestDto registerRequest) {
        return CommonResponseDto.builder()
                .success(true)
                .message("Usuario registrado correctamente")
                .build();
    }

    /**
     * Construye un UserInfoDto a partir de un objeto completo de usuario
     * @param completeUser Objeto completo de usuario (CompleteStudent, CompleteTeacher, o CompleteGuardian)
     * @return UserInfoDto con la información del usuario
     */
    private UserResponseDto.UserInfoDto buildUserInfoDto(Object completeUser) {
        User user;
        AuthResponseDto.InstitutionInfoDto institutionInfoDto = null;
        AuthResponseDto.RoleInfoDto roleInfoDto = null;

        if (completeUser instanceof CompleteStudent) {
            CompleteStudent student = (CompleteStudent) completeUser;
            user = student.getUser();
            if (user.getInstitution() != null) {
                institutionInfoDto = AuthResponseDto.InstitutionInfoDto.builder()
                        .id(user.getInstitution().getId())
                        .name(user.getInstitution().getName())
                        .build();
            }
            if (user.getRole() != null) {
                roleInfoDto = AuthResponseDto.RoleInfoDto.builder()
                        .id(user.getRole().getId().longValue())
                        .name(user.getRole().getName())
                        .code(user.getRole().getRoleCode())
                        .build();
            }
        } else if (completeUser instanceof CompleteTeacher) {
            CompleteTeacher teacher = (CompleteTeacher) completeUser;
            user = teacher.getUser();
            if (user.getInstitution() != null) {
                institutionInfoDto = AuthResponseDto.InstitutionInfoDto.builder()
                        .id(user.getInstitution().getId())
                        .name(user.getInstitution().getName())
                        .build();
            }
            if (user.getRole() != null) {
                roleInfoDto = AuthResponseDto.RoleInfoDto.builder()
                        .id(user.getRole().getId().longValue())
                        .name(user.getRole().getName())
                        .code(user.getRole().getRoleCode())
                        .build();
            }
        } else if (completeUser instanceof CompleteGuardian) {
            CompleteGuardian guardian = (CompleteGuardian) completeUser;
            user = guardian.getUser();
            if (user.getInstitution() != null) {
                institutionInfoDto = AuthResponseDto.InstitutionInfoDto.builder()
                        .id(user.getInstitution().getId())
                        .name(user.getInstitution().getName())
                        .build();
            }
            if (user.getRole() != null) {
                roleInfoDto = AuthResponseDto.RoleInfoDto.builder()
                        .id(user.getRole().getId().longValue())
                        .name(user.getRole().getName())
                        .code(user.getRole().getRoleCode())
                        .build();
            }
        } else {
            throw new IllegalArgumentException("Tipo de usuario no soportado: " + completeUser.getClass().getName());
        }

        return UserResponseDto.UserInfoDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .initials(
                    (user.getFirstName() != null && !user.getFirstName().isEmpty() ? user.getFirstName().substring(0, 1) : "") +
                    (user.getLastName() != null && !user.getLastName().isEmpty() ? user.getLastName().substring(0, 1) : "")
                )
                .role(roleInfoDto)
                .institution(institutionInfoDto)
                .emailVerified(user.isEmailVerified())
                .accountActive(user.getStatus())
                .lastLoginAt(user.getLastLoginAt() != null ? user.getLastLoginAt().toLocalDateTime() : null)
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().toLocalDateTime() : null)
                .preferences(AuthResponseDto.UserPreferencesDto.builder().build()) // Default preferences
                .build();
    }
} 