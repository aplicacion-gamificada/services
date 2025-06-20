package com.gamified.application.auth.service.auth;

import com.gamified.application.shared.model.dto.request.AuthRequestDto;
import com.gamified.application.shared.model.dto.response.AuthResponseDto;
import com.gamified.application.shared.model.dto.response.CommonResponseDto;
import com.gamified.application.shared.model.dto.request.SessionRequestDto;
import com.gamified.application.shared.model.dto.response.SessionResponseDto;
import com.gamified.application.user.model.entity.User;
import com.gamified.application.auth.entity.security.RefreshToken;
import com.gamified.application.auth.repository.core.UserRepository;
import com.gamified.application.user.repository.composite.CompleteUserRepository;
import com.gamified.application.auth.repository.security.SecurityRepository;
import com.gamified.application.auth.service.security.PasswordService;
import com.gamified.application.auth.service.audit.SecurityAuditService;
import com.gamified.application.user.model.entity.composite.CompleteStudent;
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
            
            // 7. Crear respuesta
            log.info("Step 7: Creating response");
            AuthResponseDto.LoginResponseDto response = AuthResponseDto.LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hora
                    .loginTime(LocalDateTime.now())
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
            
            // 7. Crear respuesta
            return AuthResponseDto.LoginResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(3600L) // 1 hora
                    .loginTime(LocalDateTime.now())
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
        try {
            if (logoutRequest.getRefreshToken() != null) {
                // Revocar refresh token específico
                Optional<RefreshToken> refreshTokenOpt = securityRepository.findRefreshTokenByValue(logoutRequest.getRefreshToken());
                if (refreshTokenOpt.isPresent()) {
                    securityRepository.revokeAllUserTokens(refreshTokenOpt.get().getId(), "User logout");
                    
                    if (logoutRequest.isLogoutAllDevices()) {
                        // Revocar todos los tokens del usuario
                        securityRepository.revokeAllUserTokens(refreshTokenOpt.get().getUserId(), "Logout all devices");
                    }
                }
            }
            
            return CommonResponseDto.builder()
                    .success(true)
                    .message("Logout exitoso")
                    .timestamp(LocalDateTime.now())
                    .build();
                    
        } catch (Exception e) {
            return CommonResponseDto.builder()
                    .success(false)
                    .message("Error durante logout: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
    
    // Los métodos de verificación de email y reset de contraseñas se mantienen como stub
    // ya que asumimos que el email siempre está verificado según el requerimiento
    
    @Override
    public CommonResponseDto verifyEmail(AuthRequestDto.EmailVerificationRequestDto verifyRequest) {
        return CommonResponseDto.builder()
                .success(true)
                .message("Email verificado (simulado)")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public CommonResponseDto requestPasswordReset(AuthRequestDto.PasswordResetRequestDto resetRequest) {
        return CommonResponseDto.builder()
                .success(true)
                .message("Solicitud de reset enviada (simulado)")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public CommonResponseDto executePasswordReset(AuthRequestDto.PasswordResetExecuteRequestDto resetRequest) {
        return CommonResponseDto.builder()
                .success(true)
                .message("Password reseteado (simulado)")
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    @Override
    public CommonResponseDto register(AuthRequestDto.RegisterRequestDto registerRequest) {
        // Este método no se usa ya que el registro se maneja en RegistrationController
        return CommonResponseDto.builder()
                .success(true)
                .message("Registro manejado por RegistrationController")
                .timestamp(LocalDateTime.now())
                .build();
    }
} 