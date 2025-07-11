package com.gamified.application.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public SimpleJdbcCall simpleJdbcCall(DataSource dataSource) {
        return new SimpleJdbcCall(dataSource);
    }

    @Bean
    public boolean checkStoredProcedures() {
        JdbcTemplate jdbcTemplate = jdbcTemplate();
        
        // Lista de stored procedures que deberían existir
        List<String> requiredProcedures = Arrays.asList(
            "sp_create_institution",
            "sp_update_institution",
            "sp_get_institution_by_name",
            "sp_get_institution_by_id",
            "sp_get_all_institutions",
            "sp_get_active_institutions",
            "sp_authenticate_user_complete",
            "sp_get_achievements_by_user"
        );
        
        logger.info("Verificando stored procedures...");
        
        try {
            for (String procedure : requiredProcedures) {
                try {
                    List<Map<String, Object>> results = jdbcTemplate.queryForList(
                        "SELECT OBJECT_ID(?) AS object_id, HAS_PERMS_BY_NAME(?, 'OBJECT', 'EXECUTE') AS has_execute_permission",
                        procedure, procedure
                    );
                    
                    if (!results.isEmpty()) {
                        Map<String, Object> result = results.get(0);
                        Object objectId = result.get("object_id");
                        Integer hasPermission = (Integer) result.get("has_execute_permission");
                        
                        if (objectId == null) {
                            logger.warn("Stored procedure '{}' no existe en la base de datos", procedure);
                        } else if (hasPermission == 0) {
                            logger.warn("No hay permisos de ejecución para el stored procedure '{}'", procedure);
                        } else {
                            logger.info("Stored procedure '{}' existe y tiene permisos de ejecución", procedure);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error al verificar el stored procedure '{}': {}", procedure, e.getMessage());
                }
            }
            
            logger.info("Verificación de stored procedures completada");
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar los stored procedures: {}", e.getMessage());
            return false;
        }
    }
} 