package com.gamified.application.exercise.repository;

import com.gamified.application.exercise.model.entity.GeneratedExercise;
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
 * Implementación del repositorio GeneratedExercise con JDBC y SQL Server
 */
@Repository
@RequiredArgsConstructor
public class GeneratedExerciseRepositoryImpl implements GeneratedExerciseRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Optional<GeneratedExercise> findById(Long id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("id", id, Types.BIGINT);

            String sql = """
                SELECT id, exercise_template_id, generated_content_json, 
                       correct_answer_hash, generation_prompt, ai_model_version, created_at
                FROM generated_exercise 
                WHERE id = :id
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicio generado por ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<GeneratedExercise> findAvailableByTemplateAndStudent(Integer exerciseTemplateId, Integer studentProfileId, String difficulty) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_template_id", exerciseTemplateId, Types.INTEGER);
            // Nota: En el esquema real no hay student_profile_id ni difficulty en generated_exercise
            // Los ejercicios generados son genéricos y se filtran por disponibilidad en exercise_attempt

            String sql = """
                SELECT TOP 10 id, exercise_template_id, generated_content_json, 
                       correct_answer_hash, generation_prompt, ai_model_version, created_at
                FROM generated_exercise 
                WHERE exercise_template_id = :exercise_template_id 
                  AND id NOT IN (
                      SELECT DISTINCT ea.generated_exercise_id 
                      FROM exercise_attempt ea 
                      WHERE ea.generated_exercise_id IS NOT NULL
                        AND ea.student_profile_id = :student_profile_id
                  )
                ORDER BY created_at ASC
                """;
            
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<GeneratedExercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapFromResultMap(row));
            }
            
            return exercises;
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicios disponibles: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<GeneratedExercise> findByTemplateAndDifficulty(Integer exerciseTemplateId, String difficulty, int limit) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_template_id", exerciseTemplateId, Types.INTEGER);
            parameters.addValue("limit", limit, Types.INTEGER);
            // Nota: En el esquema real no hay difficulty en generated_exercise

            String sql = """
                SELECT TOP (:limit) id, exercise_template_id, generated_content_json, 
                       correct_answer_hash, generation_prompt, ai_model_version, created_at
                FROM generated_exercise 
                WHERE exercise_template_id = :exercise_template_id 
                ORDER BY created_at ASC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<GeneratedExercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapFromResultMap(row));
            }
            
            return exercises;
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicios por plantilla y dificultad: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Long save(GeneratedExercise exercise) {
        try {
            if (exercise.getId() == null) {
                return create(exercise);
            } else {
                updateExercise(exercise);
                return exercise.getId();
            }
        } catch (Exception e) {
            System.err.println("Error al guardar ejercicio generado: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void markAsUsed(Long id) {
        try {
            // En el esquema real no hay status ni used_at
            // Marcar como usado se hace mediante exercise_attempt
            System.err.println("markAsUsed - En el esquema real no hay status. Se marca como usado mediante exercise_attempt");
        } catch (Exception e) {
            System.err.println("Error al marcar ejercicio como usado con ID " + id + ": " + e.getMessage());
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("id", id, Types.BIGINT);

            String sql = "DELETE FROM generated_exercise WHERE id = :id";
            
            namedParameterJdbcTemplate.update(sql, parameters);
        } catch (Exception e) {
            System.err.println("Error al eliminar ejercicio generado con ID " + id + ": " + e.getMessage());
        }
    }

    @Override
    public List<GeneratedExercise> findAvailableByTemplate(Integer exerciseTemplateId, Integer limit) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_template_id", exerciseTemplateId, Types.INTEGER);
            parameters.addValue("limit", limit, Types.INTEGER);

            String sql = """
                SELECT TOP (:limit) id, exercise_template_id, generated_content_json, 
                       correct_answer_hash, generation_prompt, ai_model_version, created_at
                FROM generated_exercise 
                WHERE exercise_template_id = :exercise_template_id 
                ORDER BY created_at ASC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<GeneratedExercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapFromResultMap(row));
            }
            
            return exercises;
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicios disponibles por plantilla: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void markAsUsed(Long generatedExerciseId, Integer attemptId) {
        try {
            // En el esquema real no hay status ni used_at
            // Marcar como usado se hace mediante exercise_attempt 
            System.err.println("markAsUsed - En el esquema real no hay status. Se marca como usado mediante exercise_attempt");
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
                SELECT id, exercise_template_id, generated_content_json, 
                       correct_answer_hash, generation_prompt, ai_model_version, created_at
                FROM generated_exercise 
                WHERE exercise_template_id = :exercise_template_id 
                ORDER BY created_at DESC
                OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<GeneratedExercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapFromResultMap(row));
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
                    COUNT(*) as total_in_pool,
                    COUNT(*) as available_in_pool,
                    0 as used_in_pool
                FROM generated_exercise
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, new MapSqlParameterSource());
            
            if (results.isEmpty()) {
                return new PoolStats(0, 0, 0, 0.0, 0.0);
            }
            
            Map<String, Object> result = results.get(0);
            Integer totalGenerated = ((Number) result.get("total_in_pool")).intValue();
            Integer availableInPool = ((Number) result.get("available_in_pool")).intValue();
            Integer usedInPool = ((Number) result.get("used_in_pool")).intValue();
            
            Double cacheHitRate = totalGenerated > 0 ? (usedInPool.doubleValue() / totalGenerated.doubleValue()) * 100 : 0.0;
            
            return new PoolStats(totalGenerated, usedInPool, availableInPool, cacheHitRate, 0.0);
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

            // Ya que no hay columna status, buscaremos las plantillas más usadas basándonos en la cantidad de ejercicios generados
            String sql = """
                SELECT TOP (:limit) exercise_template_id, COUNT(*) as usage_count
                FROM generated_exercise 
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

            // Eliminar ejercicios antiguos sin referencia a columna status
            String sql = """
                DELETE FROM generated_exercise 
                WHERE created_at < DATEADD(day, -:days_old, GETDATE())
                """;
            
            return namedParameterJdbcTemplate.update(sql, parameters);
        } catch (Exception e) {
            System.err.println("Error al eliminar ejercicios antiguos: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Integer countAvailableByTemplate(Integer exerciseTemplateId, String difficulty) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_template_id", exerciseTemplateId, Types.INTEGER);
            // Nota: En el esquema real no hay difficulty en generated_exercise

            String sql = """
                SELECT COUNT(*) 
                FROM generated_exercise 
                WHERE exercise_template_id = :exercise_template_id
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return 0;
            }
            
            Object count = results.get(0).values().iterator().next();
            return count != null ? ((Number) count).intValue() : 0;
        } catch (Exception e) {
            System.err.println("Error al contar ejercicios disponibles: " + e.getMessage());
            return 0;
        }
    }

    // ===================================================================
    // MÉTODOS PRIVADOS
    // ===================================================================

    private Long create(GeneratedExercise exercise) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("exercise_template_id", exercise.getExerciseTemplateId(), Types.INTEGER);
        parameters.addValue("generated_content_json", exercise.getGeneratedContentJson(), Types.VARCHAR);
        parameters.addValue("correct_answer_hash", exercise.getCorrectAnswerHash(), Types.VARCHAR);
        parameters.addValue("generation_prompt", exercise.getGenerationPrompt(), Types.VARCHAR);
        parameters.addValue("ai_model_version", exercise.getAiModelVersion(), Types.VARCHAR);

        String sql = """
            INSERT INTO generated_exercise (exercise_template_id, generated_content_json, 
                                          correct_answer_hash, generation_prompt, ai_model_version, created_at)
            VALUES (:exercise_template_id, :generated_content_json, 
                    :correct_answer_hash, :generation_prompt, :ai_model_version, GETDATE())
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    private void updateExercise(GeneratedExercise exercise) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("id", exercise.getId(), Types.BIGINT);
        parameters.addValue("exercise_template_id", exercise.getExerciseTemplateId(), Types.INTEGER);
        parameters.addValue("generated_content_json", exercise.getGeneratedContentJson(), Types.VARCHAR);
        parameters.addValue("correct_answer_hash", exercise.getCorrectAnswerHash(), Types.VARCHAR);
        parameters.addValue("generation_prompt", exercise.getGenerationPrompt(), Types.VARCHAR);
        parameters.addValue("ai_model_version", exercise.getAiModelVersion(), Types.VARCHAR);

        String sql = """
            UPDATE generated_exercise 
            SET exercise_template_id = :exercise_template_id, 
                generated_content_json = :generated_content_json,
                correct_answer_hash = :correct_answer_hash,
                generation_prompt = :generation_prompt,
                ai_model_version = :ai_model_version
            WHERE id = :id
            """;

        namedParameterJdbcTemplate.update(sql, parameters);
    }

    private GeneratedExercise mapFromResultMap(Map<String, Object> data) {
        return GeneratedExercise.builder()
                .id(data.get("id") != null ? ((Number) data.get("id")).longValue() : null)
                .exerciseTemplateId((Integer) data.get("exercise_template_id"))
                .generatedContentJson((String) data.get("generated_content_json"))
                .correctAnswerHash((String) data.get("correct_answer_hash"))
                .generationPrompt((String) data.get("generation_prompt"))
                .aiModelVersion((String) data.get("ai_model_version"))
                .createdAt(data.get("created_at") != null ? ((java.sql.Timestamp) data.get("created_at")).toLocalDateTime() : null)
                .build();
    }
} 