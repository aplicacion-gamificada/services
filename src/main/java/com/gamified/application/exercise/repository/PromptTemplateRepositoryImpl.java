package com.gamified.application.exercise.repository;

import com.gamified.application.exercise.model.entity.PromptTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del repositorio PromptTemplate con JDBC y SQL Server
 */
@Repository
@RequiredArgsConstructor
public class PromptTemplateRepositoryImpl implements PromptTemplateRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Optional<PromptTemplate> findById(Integer id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("id", id, Types.INTEGER);

            String sql = """
                SELECT id, name, template_text, exercise_type_id, created_at, updated_at
                FROM prompt_template 
                WHERE id = :id
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar plantilla de prompt por ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<PromptTemplate> findByExerciseTypeId(Integer exerciseTypeId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_type_id", exerciseTypeId, Types.INTEGER);

            String sql = """
                SELECT TOP 50 id, name, template_text, exercise_type_id, created_at, updated_at
                FROM prompt_template 
                WHERE exercise_type_id = :exercise_type_id
                ORDER BY created_at DESC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<PromptTemplate> templates = new ArrayList<>();
            for (Map<String, Object> row : results) {
                templates.add(mapFromResultMap(row));
            }
            
            return templates;
        } catch (Exception e) {
            System.err.println("Error al buscar plantillas por tipo de ejercicio " + exerciseTypeId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<PromptTemplate> findAll() {
        try {
            String sql = """
                SELECT TOP 100 id, name, template_text, exercise_type_id, created_at, updated_at
                FROM prompt_template 
                ORDER BY created_at DESC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, new MapSqlParameterSource());
            
            List<PromptTemplate> templates = new ArrayList<>();
            for (Map<String, Object> row : results) {
                templates.add(mapFromResultMap(row));
            }
            
            return templates;
        } catch (Exception e) {
            System.err.println("Error al obtener todas las plantillas de prompt: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Integer save(PromptTemplate template) {
        try {
            if (template.getId() == null) {
                return create(template);
            } else {
                update(template);
                return template.getId();
            }
        } catch (Exception e) {
            System.err.println("Error al guardar plantilla de prompt: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<PromptTemplate> findByName(String name) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("name", name, Types.VARCHAR);

            String sql = """
                SELECT id, name, template_text, exercise_type_id, created_at, updated_at
                FROM prompt_template 
                WHERE name = :name
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar plantilla de prompt por nombre " + name + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean update(PromptTemplate template) {
        try {
            updateTemplate(template);
            return true;
        } catch (Exception e) {
            System.err.println("Error al actualizar plantilla de prompt: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Integer id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("id", id, Types.INTEGER);

            String sql = "DELETE FROM prompt_template WHERE id = :id";
            
            int rowsAffected = namedParameterJdbcTemplate.update(sql, parameters);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("Error al eliminar plantilla de prompt con ID " + id + ": " + e.getMessage());
            return false;
        }
    }

    // ===================================================================
    // MÉTODOS PRIVADOS
    // ===================================================================

    private Integer create(PromptTemplate template) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("name", template.getName(), Types.VARCHAR);
        parameters.addValue("template_text", template.getTemplateText(), Types.VARCHAR);
        parameters.addValue("exercise_type_id", template.getExerciseTypeId(), Types.INTEGER);

        String sql = """
            INSERT INTO prompt_template (name, template_text, exercise_type_id, created_at, updated_at)
            VALUES (:name, :template_text, :exercise_type_id, GETDATE(), GETDATE())
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        
        Number key = keyHolder.getKey();
        return key != null ? key.intValue() : null;
    }

    private void updateTemplate(PromptTemplate template) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("id", template.getId(), Types.INTEGER);
        parameters.addValue("name", template.getName(), Types.VARCHAR);
        parameters.addValue("template_text", template.getTemplateText(), Types.VARCHAR);
        parameters.addValue("exercise_type_id", template.getExerciseTypeId(), Types.INTEGER);

        String sql = """
            UPDATE prompt_template 
            SET name = :name, 
                template_text = :template_text, 
                exercise_type_id = :exercise_type_id,
                updated_at = GETDATE()
            WHERE id = :id
            """;

        namedParameterJdbcTemplate.update(sql, parameters);
    }

    private PromptTemplate mapFromResultMap(Map<String, Object> data) {
        return PromptTemplate.builder()
                .id((Integer) data.get("id"))
                .name((String) data.get("name"))
                .templateText((String) data.get("template_text"))
                .exerciseTypeId((Integer) data.get("exercise_type_id"))
                .createdAt(data.get("created_at") != null ? ((java.sql.Timestamp) data.get("created_at")).toLocalDateTime() : null)
                .updatedAt(data.get("updated_at") != null ? ((java.sql.Timestamp) data.get("updated_at")).toLocalDateTime() : null)
                .build();
    }
} 