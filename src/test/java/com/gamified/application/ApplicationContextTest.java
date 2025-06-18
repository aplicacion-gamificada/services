package com.gamified.application;

import com.gamified.application.config.TestConfig;
import com.gamified.application.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test simple para verificar que el ApplicationContext se puede cargar correctamente
 */
@SpringBootTest
@ActiveProfiles("test")
@Import({TestConfig.class, TestSecurityConfig.class})
public class ApplicationContextTest {

    @Test
    void contextLoads() {
        // Si llegamos aquí, significa que el contexto se cargó correctamente
        System.out.println("✅ ApplicationContext loaded successfully!");
    }
} 