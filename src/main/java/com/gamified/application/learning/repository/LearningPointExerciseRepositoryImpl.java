package com.gamified.application.learning.repository;

import com.gamified.application.learning.model.entity.LearningPointExercise;
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
 * Implementación del repositorio para la tabla intermedia learning_point_exercise
 */
@Repository
@RequiredArgsConstructor
public class LearningPointExerciseRepositoryImpl implements LearningPointExerciseRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Integer assignExerciseToLearningPoint(LearningPointExercise assignment) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", assignment.getLearningPointId(), Types.INTEGER);
            parameters.addValue("exercise_template_id", assignment.getExerciseTemplateId(), Types.INTEGER);
            parameters.addValue("sequence_order", assignment.getSequenceOrder(), Types.INTEGER);
            parameters.addValue("generated_exercise_id", assignment.getGeneratedExerciseId(), Types.BIGINT);
            parameters.addValue("is_completed", assignment.getIsCompleted() != null ? assignment.getIsCompleted() : 0, Types.INTEGER);
            parameters.addValue("is_active", assignment.getIsActive() != null ? assignment.getIsActive() : 1, Types.INTEGER);

            String sql = """
                INSERT INTO learning_point_exercise (learning_point_id, exercise_template_id, sequence_order,
                                                   generated_exercise_id, is_completed, is_active, assigned_at)
                VALUES (:learning_point_id, :exercise_template_id, :sequence_order, 
                        :generated_exercise_id, :is_completed, :is_active, GETDATE())
                """;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
            
            Number key = keyHolder.getKey();
            return key != null ? key.intValue() : null;
        } catch (Exception e) {
            System.err.println("Error al asignar ejercicio a learning point: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<LearningPointExercise> findByLearningPointId(Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = """
                SELECT id, learning_point_id, exercise_template_id, sequence_order, generated_exercise_id,
                       is_completed, assigned_at, completed_at, is_active
                FROM learning_point_exercise 
                WHERE learning_point_id = :learning_point_id AND is_active = 1
                ORDER BY sequence_order ASC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<LearningPointExercise> assignments = new ArrayList<>();
            for (Map<String, Object> row : results) {
                assignments.add(mapFromResultMap(row));
            }
            
            return assignments;
        } catch (Exception e) {
            System.err.println("Error al obtener ejercicios del learning point " + learningPointId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<LearningPointExercise> findById(Integer id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("id", id, Types.INTEGER);

            String sql = """
                SELECT id, learning_point_id, exercise_template_id, sequence_order, generated_exercise_id,
                       is_completed, assigned_at, completed_at, is_active
                FROM learning_point_exercise 
                WHERE id = :id
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar asignación de ejercicio por ID " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean updateAssignment(LearningPointExercise assignment) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("id", assignment.getId(), Types.INTEGER);
            parameters.addValue("generated_exercise_id", assignment.getGeneratedExerciseId(), Types.BIGINT);
            parameters.addValue("sequence_order", assignment.getSequenceOrder(), Types.INTEGER);
            parameters.addValue("is_completed", assignment.getIsCompleted(), Types.INTEGER);
            parameters.addValue("completed_at", assignment.getCompletedAt(), Types.TIMESTAMP);
            parameters.addValue("is_active", assignment.getIsActive(), Types.INTEGER);

            String sql = """
                UPDATE learning_point_exercise 
                SET generated_exercise_id = :generated_exercise_id,
                    sequence_order = :sequence_order,
                    is_completed = :is_completed,
                    completed_at = :completed_at,
                    is_active = :is_active
                WHERE id = :id
                """;

            int rowsAffected = namedParameterJdbcTemplate.update(sql, parameters);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("Error al actualizar asignación de ejercicio: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean markAsCompleted(Integer learningPointId, Integer exerciseTemplateId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);
            parameters.addValue("exercise_template_id", exerciseTemplateId, Types.INTEGER);

            String sql = """
                UPDATE learning_point_exercise 
                SET is_completed = 1, completed_at = GETDATE()
                WHERE learning_point_id = :learning_point_id 
                  AND exercise_template_id = :exercise_template_id
                  AND is_active = 1
                """;

            int rowsAffected = namedParameterJdbcTemplate.update(sql, parameters);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("Error al marcar ejercicio como completado: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean assignGeneratedExercise(Integer assignmentId, Long generatedExerciseId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("id", assignmentId, Types.INTEGER);
            parameters.addValue("generated_exercise_id", generatedExerciseId, Types.BIGINT);

            String sql = """
                UPDATE learning_point_exercise 
                SET generated_exercise_id = :generated_exercise_id
                WHERE id = :id
                """;

            int rowsAffected = namedParameterJdbcTemplate.update(sql, parameters);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("Error al asignar ejercicio generado: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Integer countCompletedExercisesByLearningPoint(Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = """
                SELECT COUNT(*) 
                FROM learning_point_exercise 
                WHERE learning_point_id = :learning_point_id 
                  AND is_completed = 1 
                  AND is_active = 1
                """;
            
            Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("Error al contar ejercicios completados: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Integer countTotalExercisesByLearningPoint(Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = """
                SELECT COUNT(*) 
                FROM learning_point_exercise 
                WHERE learning_point_id = :learning_point_id 
                  AND is_active = 1
                """;
            
            Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            System.err.println("Error al contar total de ejercicios: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public Optional<LearningPointExercise> findNextPendingExercise(Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = """
                SELECT TOP 1 id, learning_point_id, exercise_template_id, sequence_order, generated_exercise_id,
                       is_completed, assigned_at, completed_at, is_active
                FROM learning_point_exercise 
                WHERE learning_point_id = :learning_point_id 
                  AND is_completed = 0 
                  AND is_active = 1
                ORDER BY sequence_order ASC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar siguiente ejercicio pendiente: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteAssignment(Integer id) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("id", id, Types.INTEGER);

            String sql = """
                UPDATE learning_point_exercise 
                SET is_active = 0 
                WHERE id = :id
                """;

            int rowsAffected = namedParameterJdbcTemplate.update(sql, parameters);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("Error al eliminar asignación de ejercicio: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean reorderExercises(Integer learningPointId, List<Integer> exerciseTemplateIds) {
        try {
            // Actualizar sequence_order para cada ejercicio en el orden especificado
            for (int i = 0; i < exerciseTemplateIds.size(); i++) {
                MapSqlParameterSource parameters = new MapSqlParameterSource();
                parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);
                parameters.addValue("exercise_template_id", exerciseTemplateIds.get(i), Types.INTEGER);
                parameters.addValue("sequence_order", i + 1, Types.INTEGER);

                String sql = """
                    UPDATE learning_point_exercise 
                    SET sequence_order = :sequence_order 
                    WHERE learning_point_id = :learning_point_id 
                      AND exercise_template_id = :exercise_template_id
                      AND is_active = 1
                    """;

                namedParameterJdbcTemplate.update(sql, parameters);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error al reordenar ejercicios: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mapea resultado de BD a entidad LearningPointExercise
     */
    private LearningPointExercise mapFromResultMap(Map<String, Object> data) {
        return LearningPointExercise.builder()
                .id(((Number) data.get("id")).intValue())
                .learningPointId(((Number) data.get("learning_point_id")).intValue())
                .exerciseTemplateId(((Number) data.get("exercise_template_id")).intValue())
                .sequenceOrder(((Number) data.get("sequence_order")).intValue())
                .generatedExerciseId(data.get("generated_exercise_id") != null ? 
                    ((Number) data.get("generated_exercise_id")).longValue() : null)
                .isCompleted(data.get("is_completed") != null ? 
                    ((Number) data.get("is_completed")).intValue() : 0)
                .assignedAt(data.get("assigned_at") != null ? 
                    ((java.sql.Timestamp) data.get("assigned_at")).toLocalDateTime() : null)
                .completedAt(data.get("completed_at") != null ? 
                    ((java.sql.Timestamp) data.get("completed_at")).toLocalDateTime() : null)
                .isActive(data.get("is_active") != null ? 
                    ((Number) data.get("is_active")).intValue() : 1)
                .build();
    }
} 