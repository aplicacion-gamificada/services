package com.gamified.application.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Inicializa los stored procedures al iniciar la aplicación
 */
@Component
public class StoredProcedureInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(StoredProcedureInitializer.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final ResourceLoader resourceLoader;

    @Autowired
    public StoredProcedureInitializer(JdbcTemplate jdbcTemplate, ResourceLoader resourceLoader) {
        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeStoredProcedures();
    }

    private void initializeStoredProcedures() {
        log.info("Inicializando stored procedures...");
        
        // Solo cargar los stored procedures personalizados que no existen en db-numerino-sp.sql
        try {
            
            log.info("Stored procedures inicializados");
        } catch (Exception e) {
            log.error("Error al inicializar stored procedures: {}", e.getMessage(), e);
        }
    }
    
    private void loadStoredProcedure(String resourcePath) {
        try {
            Resource resource = resourceLoader.getResource(resourcePath);
            if (!resource.exists()) {
                log.warn("No se encontró el archivo: {}", resourcePath);
                return;
            }
            
            StringBuilder sqlBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sqlBuilder.append(line).append("\n");
                }
            }
            
            String sql = sqlBuilder.toString();
            
            // Verificar si el stored procedure ya existe
            String procedureName = extractProcedureName(resourcePath);
            if (procedureExists(procedureName)) {
                log.info("El stored procedure {} ya existe, actualizando...", procedureName);
                // Eliminar el stored procedure existente
                jdbcTemplate.execute("DROP PROCEDURE IF EXISTS " + procedureName);
            }
            
            // Ejecutar el script SQL
            jdbcTemplate.execute(sql);
            log.info("Stored procedure cargado: {}", resourcePath);
        } catch (IOException e) {
            log.error("Error al cargar el stored procedure {}: {}", resourcePath, e.getMessage(), e);
        }
    }
    
    private String extractProcedureName(String resourcePath) {
        // Extraer el nombre del procedimiento del path del recurso
        String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
        String baseName = fileName.replace(".sql", "");
        
        // Si el nombre contiene "_modified", usar el nombre base
        if (baseName.endsWith("_modified")) {
            return baseName.substring(0, baseName.indexOf("_modified"));
        }
        
        return baseName;
    }
    
    private boolean procedureExists(String procedureName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys.procedures WHERE name = ?", 
                Integer.class, 
                procedureName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error al verificar si existe el stored procedure {}: {}", procedureName, e.getMessage());
            return false;
        }
    }
} 