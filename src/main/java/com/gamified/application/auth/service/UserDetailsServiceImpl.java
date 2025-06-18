package com.gamified.application.auth.service;

import com.gamified.application.auth.util.DatabaseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        log.info("Loading user by identifier: {}", identifier);
        
        try {
            // Primero intentar encontrar por email (para teachers/guardians)
            String sql = "SELECT u.id, u.email, u.password, u.is_active, u.email_verified, u.status, r.name as role_name " +
                    "FROM [user] u " +
                    "JOIN role r ON u.role_id = r.id " +
                    "WHERE u.email = ? AND u.is_active = 1";

            try {
                UserDetails userDetails = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                    String roleName = rs.getString("role_name");
                    
                    // Usar DatabaseUtils para conversión segura de campos boolean (int en BD)
                    boolean isActive = DatabaseUtils.safeToBoolean(rs.getObject("is_active"));
                    boolean emailVerified = DatabaseUtils.safeToBoolean(rs.getObject("email_verified"));
                    boolean status = DatabaseUtils.safeToBoolean(rs.getObject("status"));
                    
                    log.info("Found user by email - Role: {}, Active: {}, EmailVerified: {}, Status: {}", 
                            roleName, isActive, emailVerified, status);
                    
                    // Para teachers y guardians, no requerir email_verified inicialmente
                    // Para estudiantes, seguir la lógica original (aunque usan username)
                    boolean enabled;
                    if ("TEACHER".equals(roleName) || "GUARDIAN".equals(roleName)) {
                        enabled = isActive && status;
                        log.info("Teacher/Guardian - Enabled: {} (Active: {}, Status: {})", enabled, isActive, status);
                    } else {
                        enabled = isActive && emailVerified && status;
                        log.info("Other role - Enabled: {} (Active: {}, EmailVerified: {}, Status: {})", 
                                enabled, isActive, emailVerified, status);
                    }
                    
                    Long userId = rs.getLong("id");
                    String password = rs.getString("password");
                    
                    log.info("Creating UserDetails for userId: {}, enabled: {}", userId, enabled);
                    
                    return new User(
                            String.valueOf(userId),
                            password,
                            enabled,
                            true,
                            true,
                            status,
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + roleName))
                    );
                }, identifier);
                
                log.info("Successfully loaded user details for email: {}", identifier);
                return userDetails;
                
            } catch (Exception e) {
                log.info("User not found by email, trying username for student: {}", identifier);
                
                // Si no se encuentra por email, intentar por username (para estudiantes)
                String studentSql = "SELECT u.id, u.email, u.password, u.is_active, u.email_verified, u.status, r.name as role_name " +
                        "FROM [user] u " +
                        "JOIN role r ON u.role_id = r.id " +
                        "JOIN student_profile sp ON u.id = sp.user_id " +
                        "WHERE sp.username = ? AND u.is_active = 1";

                UserDetails studentDetails = jdbcTemplate.queryForObject(studentSql, (rs, rowNum) -> {
                    // Para estudiantes, no requerir email_verified, usar DatabaseUtils para conversión segura
                    boolean isActive = DatabaseUtils.safeToBoolean(rs.getObject("is_active"));
                    boolean status = DatabaseUtils.safeToBoolean(rs.getObject("status"));
                    boolean enabled = isActive && status;
                    
                    Long userId = rs.getLong("id");
                    String roleName = rs.getString("role_name");
                    
                    log.info("Found student by username - UserId: {}, Enabled: {}, Role: {}", userId, enabled, roleName);
                    
                    return new User(
                            String.valueOf(userId),
                            rs.getString("password"),
                            enabled,
                            true,
                            true,
                            status,
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + roleName))
                    );
                }, identifier);
                
                log.info("Successfully loaded student details for username: {}", identifier);
                return studentDetails;
            }

        } catch (Exception e) {
            log.error("Failed to load user with identifier: {} - Error: {}", identifier, e.getMessage());
            throw new UsernameNotFoundException("User not found with identifier: " + identifier);
        }
    }
} 