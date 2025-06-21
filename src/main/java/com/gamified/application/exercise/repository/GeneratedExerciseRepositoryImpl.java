package com.gamified.application.exercise.repository;

import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.GeneratedExercise;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * Implementación del repositorio GeneratedExercise con JDBC y SQL Server
 */
@Repository
@RequiredArgsConstructor
public class GeneratedExerciseRepositoryImpl implements GeneratedExerciseRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Long save(GeneratedExercise generatedExercise) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_template_id", generatedExercise.getExerciseTemplateId(), Types.INTEGER);
            parameters.addValue("generated_content_json", generatedExercise.getGeneratedContentJson(), Types.VARCHAR);
            parameters.addValue("correct_answer_hash", generatedExercise.getCorrectAnswerHash(), Types.VARCHAR);
            parameters.addValue("generation_prompt", generatedExercise.getGenerationPrompt(), Types.VARCHAR);
            parameters.addValue("ai_model_version", generatedExercise.getAiModelVersion(), Types.VARCHAR);
            parameters.addValue("created_at", generatedExercise.getCreatedAt(), Types.TIMESTAMP);

            String sql = """
                INSERT INTO generated_exercise (exercise_template_id, generated_content_json, 
                                              correct_answer_hash, generation_prompt, ai_model_version, created_at)
                VALUES (:exercise_template_id, :generated_content_json, :correct_answer_hash, 
                        :generation_prompt, :ai_model_version, :created_at)
                """;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
            
            Number key = keyHolder.getKey();
            return key != null ? key.longValue() : null;
        } catch (Exception e) {
            System.err.println("Error al guardar ejercicio generado: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<GeneratedExercise> findById(Long id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("id", id, Types.BIGINT);

            String sql = """
                SELECT id, exercise_template_id, generated_content_json, correct_answer_hash,
                       generation_prompt, ai_model_version, created_at
                FROM generated_exercise 
                WHERE id = :id
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapGeneratedExerciseFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicio generado por ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<GeneratedExercise> findAvailableByTemplate(Integer exerciseTemplateId, Integer limit) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_template_id", exerciseTemplateId, Types.INTEGER);
            parameters.addValue("limit", limit, Types.INTEGER);

            String sql = """
                SELECT TOP (@limit) id, exercise_template_id, generated_content_json, correct_answer_hash,
                       generation_prompt, ai_model_version, created_at
                FROM generated_exercise 
                WHERE exercise_template_id = :exercise_template_id
                  AND id NOT IN (
                      SELECT DISTINCT generated_exercise_id 
                      FROM exercise_attempt 
                      WHERE generated_exercise_id IS NOT NULL
                  )
                ORDER BY created_at DESC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<GeneratedExercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapGeneratedExerciseFromResultMap(row));
            }
            
            return exercises;
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicios disponibles por plantilla " + exerciseTemplateId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void markAsUsed(Long generatedExerciseId, Integer attemptId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("generated_exercise_id", generatedExerciseId, Types.BIGINT);
            parameters.addValue("attempt_id", attemptId, Types.INTEGER);

            String sql = """
                UPDATE exercise_attempt 
                SET generated_exercise_id = :generated_exercise_id
                WHERE id = :attempt_id
                """;

            namedParameterJdbcTemplate.update(sql, parameters);
        } catch (Exception e) {
            System.err.println("Error al marcar ejercicio como usado: " + e.getMessage());
        }
    }

    @Override
    public List<GeneratedExercise> findByTemplateWithPagination(Integer exerciseTemplateId, Integer offset, Integer limit) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_template_id", exerciseTemplateId, Types.INTEGER);
            parameters.addValue("offset", offset, Types.INTEGER);
            parameters.addValue("limit", limit, Types.INTEGER);

            String sql = """
                SELECT id, exercise_template_id, generated_content_json, correct_answer_hash,
                       generation_prompt, ai_model_version, created_at
                FROM generated_exercise 
                WHERE exercise_template_id = :exercise_template_id
                ORDER BY created_at DESC
                OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<GeneratedExercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapGeneratedExerciseFromResultMap(row));
            }
            
            return exercises;
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicios con paginación: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Integer countByTemplate(Integer exerciseTemplateId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_template_id", exerciseTemplateId, Types.INTEGER);

            String sql = "SELECT COUNT(*) FROM generated_exercise WHERE exercise_template_id = :exercise_template_id";
            
            return namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
        } catch (Exception e) {
            System.err.println("Error al contar ejercicios por plantilla: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public PoolStats getPoolStats() {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as total_generated,
                    COUNT(CASE WHEN ea.generated_exercise_id IS NOT NULL THEN 1 END) as total_used,
                    COUNT(CASE WHEN ea.generated_exercise_id IS NULL THEN 1 END) as available_in_pool
                FROM generated_exercise ge
                LEFT JOIN exercise_attempt ea ON ge.id = ea.generated_exercise_id
                """;
            
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            
            Integer totalGenerated = (Integer) result.get("total_generated");
            Integer totalUsed = (Integer) result.get("total_used");
            Integer availableInPool = (Integer) result.get("available_in_pool");
            
            Double cacheHitRate = totalGenerated > 0 ? (totalUsed.doubleValue() / totalGenerated.doubleValue()) * 100 : 0.0;
            
            return new PoolStats(totalGenerated, totalUsed, availableInPool, cacheHitRate, 0.0);
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas del pool: " + e.getMessage());
            return new PoolStats(0, 0, 0, 0.0, 0.0);
        }
    }

    @Override
    public List<Integer> findMostDemandedTemplates(Integer limit) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("limit", limit, Types.INTEGER);

            String sql = """
                SELECT TOP (@limit) exercise_template_id, COUNT(*) as usage_count
                FROM generated_exercise ge
                INNER JOIN exercise_attempt ea ON ge.id = ea.generated_exercise_id
                GROUP BY exercise_template_id
                ORDER BY usage_count DESC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Integer> templateIds = new ArrayList<>();
            for (Map<String, Object> row : results) {
                templateIds.add((Integer) row.get("exercise_template_id"));
            }
            
            return templateIds;
        } catch (Exception e) {
            System.err.println("Error al buscar plantillas más demandadas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Integer deleteOldExercises(Integer daysOld) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("days_old", daysOld, Types.INTEGER);

            String sql = """
                DELETE FROM generated_exercise 
                WHERE created_at < DATEADD(day, -:days_old, GETDATE())
                  AND id NOT IN (
                      SELECT DISTINCT generated_exercise_id 
                      FROM exercise_attempt 
                      WHERE generated_exercise_id IS NOT NULL
                  )
                """;
            
            return namedParameterJdbcTemplate.update(sql, parameters);
        } catch (Exception e) {
            System.err.println("Error al eliminar ejercicios antiguos: " + e.getMessage());
            return 0;
        }
    }

    private GeneratedExercise mapGeneratedExerciseFromResultMap(Map<String, Object> data) {
        return GeneratedExercise.builder()
                .id(data.get("id") != null ? ((Number) data.get("id")).longValue() : null)
                .exerciseTemplateId(data.get("exercise_template_id") != null ? (Integer) data.get("exercise_template_id") : null)
                .generatedContentJson((String) data.get("generated_content_json"))
                .correctAnswerHash((String) data.get("correct_answer_hash"))
                .generationPrompt((String) data.get("generation_prompt"))
                .aiModelVersion((String) data.get("ai_model_version"))
                .createdAt(data.get("created_at") != null ? ((java.sql.Timestamp) data.get("created_at")).toLocalDateTime() : null)
                .build();
    }
} 