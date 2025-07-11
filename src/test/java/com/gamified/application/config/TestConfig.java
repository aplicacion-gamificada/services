package com.gamified.application.config;

import com.gamified.application.auth.service.auth.AuthenticationService;
import com.gamified.application.auth.service.auth.TokenService;
import com.gamified.application.user.service.UserProfileService;
import com.gamified.application.user.service.UserRegistrationService;
import com.gamified.application.institution.service.InstitutionService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

/**
 * Configuración específica para tests que provee mocks de servicios críticos
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public AuthenticationService authenticationService() {
        return mock(AuthenticationService.class);
    }

    @Bean
    @Primary
    public UserProfileService userProfileService() {
        return mock(UserProfileService.class);
    }

    @Bean
    @Primary
    public UserRegistrationService userRegistrationService() {
        return mock(UserRegistrationService.class);
    }

    @Bean
    @Primary
    public TokenService tokenService() {
        return mock(TokenService.class);
    }

    @Bean
    @Primary
    public InstitutionService institutionService() {
        return mock(InstitutionService.class);
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Mock JwtConfig para evitar problemas de dependencias en tests
     */
    @Bean
    @Primary
    public JwtConfig jwtConfig() {
        return mock(JwtConfig.class);
    }

    /**
     * Mock UserDetailsService para evitar problemas con la base de datos durante las pruebas
     * Al mockear este servicio, no necesitamos el bean jdbcTemplate real
     */
    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // Return a mock user for testing
                return User.builder()
                        .username(username)
                        .password("$2a$10$mock.password.hash")
                        .authorities("ROLE_USER")
                        .build();
            }
        };
    }
} 