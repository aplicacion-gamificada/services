package com.gamified.application.exercise.repository;

import com.gamified.application.exercise.model.entity.Exercise;
import com.gamified.application.exercise.model.entity.ExerciseAttempt;
import com.gamified.application.exercise.model.entity.ExerciseType;
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
 * Implementación del repositorio Exercise con JDBC y SQL Server
 */
@Repository
@RequiredArgsConstructor
public class ExerciseRepositoryImpl implements ExerciseRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    // ===================================================================
    // EXERCISE TYPES
    // ===================================================================

    @Override
    public List<ExerciseType> findAllActiveExerciseTypes() {
        try {
            // Corregido según el esquema UML: exercise_type solo tiene id, name, description, created_at
            String sql = "SELECT TOP 100 id, name, description, created_at " +
                        "FROM exercise_type ORDER BY name";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            List<ExerciseType> exerciseTypes = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exerciseTypes.add(mapExerciseTypeFromResultMap(row));
            }
            
            return exerciseTypes;
        } catch (Exception e) {
            System.err.println("Error al obtener tipos de ejercicio activos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<ExerciseType> findExerciseTypeById(Integer exerciseTypeId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_type_id", exerciseTypeId, Types.INTEGER);

            // Corregido según el esquema UML: exercise_type solo tiene id, name, description, created_at
            String sql = "SELECT id, name, description, created_at " +
                        "FROM exercise_type WHERE id = :exercise_type_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapExerciseTypeFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar tipo de ejercicio por ID " + exerciseTypeId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    // ===================================================================
    // EXERCISES
    // ===================================================================

    @Override
    public Optional<Exercise> findNextExerciseForLearningPoint(Integer studentProfileId, Integer learningPointId, String difficulty) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);
            parameters.addValue("difficulty", difficulty != null ? difficulty : "medium", Types.VARCHAR);

            // Buscar ejercicio template que no haya sido completado exitosamente por el estudiante
            // Corregido para usar la estructura real de la tabla según el UML
            String sql = """
                SELECT TOP 1 e.id, e.learning_point_id, e.exercise_type_id, e.competency_id,
                       e.difficulty_level_id, e.title, e.description, e.instructions,
                       e.estimated_time, e.points_value, e.sequence_order, e.prompt_template_id,
                       e.created_at, e.updated_at
                FROM exercise e
                WHERE e.learning_point_id = :learning_point_id 
                  AND NOT EXISTS (
                      SELECT 1 FROM exercise_attempt ea 
                      WHERE ea.exercise_template_id = e.id 
                        AND ea.student_profile_id = :student_profile_id 
                        AND ea.is_correct = 1
                  )
                ORDER BY e.sequence_order, NEWID()
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapExerciseFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar siguiente ejercicio: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Exercise> findExerciseById(Integer exerciseId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_id", exerciseId, Types.INTEGER);

            String sql = """
                SELECT id, exercise_type_id, competency_id, difficulty_level_id, title, 
                       description, instructions, estimated_time, 
                       points_value, created_at, updated_at
                FROM exercise WHERE id = :exercise_id
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapExerciseFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicio por ID " + exerciseId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Integer createExercise(Exercise exercise) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_type_id", exercise.getExerciseTypeId(), Types.INTEGER);
            parameters.addValue("competency_id", exercise.getCompetencyId(), Types.INTEGER);
            parameters.addValue("difficulty_level_id", exercise.getDifficultyLevelId(), Types.INTEGER);
            parameters.addValue("title", exercise.getTitle(), Types.VARCHAR);
            parameters.addValue("description", exercise.getDescription(), Types.VARCHAR);
            parameters.addValue("instructions", exercise.getInstructions(), Types.VARCHAR);
            parameters.addValue("unit_id", exercise.getUnitId(), Types.INTEGER);
            parameters.addValue("estimated_time", exercise.getEstimatedTime(), Types.INTEGER);
            parameters.addValue("points_value", exercise.getPointsValue(), Types.INTEGER);
            parameters.addValue("prompt_template_id", exercise.getPromptTemplateId(), Types.INTEGER);
            parameters.addValue("exercise_config", exercise.getExerciseConfig(), Types.VARCHAR);
            parameters.addValue("exercise_subtype", exercise.getExerciseSubtype(), Types.VARCHAR);
            parameters.addValue("generation_rules", exercise.getGenerationRules(), Types.VARCHAR);
            parameters.addValue("difficulty_parameters", exercise.getDifficultyParameters(), Types.VARCHAR);

            String sql = """
                INSERT INTO exercise (exercise_type_id, competency_id, difficulty_level_id, title, 
                                    description, instructions, unit_id, estimated_time, 
                                    points_value, prompt_template_id, 
                                    exercise_config, exercise_subtype, generation_rules, difficulty_parameters,
                                    created_at, updated_at)
                VALUES (:exercise_type_id, :competency_id, :difficulty_level_id, :title, 
                        :description, :instructions, :unit_id, :estimated_time, 
                        :points_value, :prompt_template_id,
                        :exercise_config, :exercise_subtype, :generation_rules, :difficulty_parameters,
                        GETDATE(), GETDATE())
                """;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
            
            Number key = keyHolder.getKey();
            return key != null ? key.intValue() : null;
        } catch (Exception e) {
            System.err.println("Error al crear ejercicio: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Exercise> findExercisesByUnit(Integer unitId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("unit_id", unitId, Types.INTEGER);

            String sql = """
                SELECT TOP 100 id, exercise_type_id, competency_id, difficulty_level_id, title, 
                       description, instructions, unit_id, estimated_time, 
                       points_value, prompt_template_id, exercise_config, exercise_subtype,
                       generation_rules, difficulty_parameters, created_at, updated_at
                FROM exercise 
                WHERE unit_id = :unit_id
                ORDER BY created_at DESC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Exercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapExerciseFromResultMap(row));
            }
            
            return exercises;
        } catch (Exception e) {
            System.err.println("Error al obtener ejercicios por unit " + unitId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Deprecated
    public List<Exercise> findExercisesByLearningPoint(Integer learningPointId) {
        // MÉTODO DEPRECATED - ahora los ejercicios están en learning_point_exercise
        // Retornar lista vacía para compatibilidad
        System.err.println("DEPRECATED: findExercisesByLearningPoint - usar LearningPointExerciseRepository");
        return new ArrayList<>();
    }

    @Override
    public Integer countCompletedExercisesByStudentAndLearningPoint(Integer studentProfileId, Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = """
                SELECT COUNT(DISTINCT e.id)
                FROM exercise e
                INNER JOIN exercise_attempt ea ON e.id = ea.exercise_id
                WHERE e.learning_point_id = :learning_point_id 
                  AND ea.student_profile_id = :student_profile_id 
                  AND ea.is_correct = 1
                """;

            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return 0;
            }
            
            Object count = results.get(0).values().iterator().next();
            return count != null ? ((Number) count).intValue() : 0;
        } catch (Exception e) {
            System.err.println("Error al contar ejercicios completados: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<Exercise> findByExerciseTypeIdAndSubtype(Integer exerciseTypeId, String exerciseSubtype) {
        try {
            String sql = """
                SELECT * FROM exercise 
                WHERE exercise_type_id = :exercise_type_id 
                AND exercise_subtype = :exercise_subtype
                ORDER BY difficulty_level_id ASC, sequence_order ASC
                """;
            
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_type_id", exerciseTypeId, Types.INTEGER);
            parameters.addValue("exercise_subtype", exerciseSubtype, Types.VARCHAR);
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            return results.stream().map(this::mapExerciseFromResultMap).toList();
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicios por tipo y subtipo: " + e.getMessage());
            return List.of();
        }
    }

    // ===================================================================
    // EXERCISE ATTEMPTS
    // ===================================================================

    @Override
    public Integer createExerciseAttempt(ExerciseAttempt attempt) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("exercise_id", attempt.getExerciseId(), Types.INTEGER);
            parameters.addValue("student_profile_id", attempt.getStudentProfileId(), Types.INTEGER);
            parameters.addValue("submitted_answer", attempt.getSubmittedAnswer(), Types.VARCHAR);
            parameters.addValue("is_correct", attempt.getIsCorrect(), Types.BOOLEAN);
            parameters.addValue("time_spent_seconds", attempt.getTimeSpentSeconds(), Types.INTEGER);
            parameters.addValue("hints_used", attempt.getHintsUsed(), Types.INTEGER);
            parameters.addValue("score", attempt.getScore(), Types.DOUBLE);
            parameters.addValue("feedback", attempt.getFeedback(), Types.VARCHAR);
            parameters.addValue("attempt_number", attempt.getAttemptNumber(), Types.INTEGER);

            String sql = """
                INSERT INTO exercise_attempt (exercise_id, student_profile_id, submitted_answer, 
                                            is_correct, time_spent_seconds, hints_used, score, 
                                            feedback, attempt_number, submitted_at, created_at)
                VALUES (:exercise_id, :student_profile_id, :submitted_answer, 
                        :is_correct, :time_spent_seconds, :hints_used, :score, 
                        :feedback, :attempt_number, GETDATE(), GETDATE())
                """;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
            
            Number key = keyHolder.getKey();
            return key != null ? key.intValue() : null;
        } catch (Exception e) {
            System.err.println("Error al crear intento de ejercicio: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<ExerciseAttempt> findAttemptsByStudentAndExercise(Integer studentProfileId, Integer exerciseId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);
            parameters.addValue("exercise_id", exerciseId, Types.INTEGER);

            String sql = """
                SELECT TOP 50 id, exercise_id, student_profile_id, submitted_answer, 
                       is_correct, time_spent_seconds, hints_used, score, feedback, 
                       attempt_number, submitted_at, created_at
                FROM exercise_attempt 
                WHERE student_profile_id = :student_profile_id AND exercise_id = :exercise_id
                ORDER BY submitted_at DESC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<ExerciseAttempt> attempts = new ArrayList<>();
            for (Map<String, Object> row : results) {
                attempts.add(mapExerciseAttemptFromResultMap(row));
            }
            
            return attempts;
        } catch (Exception e) {
            System.err.println("Error al obtener intentos por estudiante y ejercicio: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ExerciseAttempt> findAttemptHistoryByStudent(Integer studentProfileId, Integer limit) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);
            parameters.addValue("limit", limit != null ? limit : 50, Types.INTEGER);

            String sql = """
                SELECT TOP (:limit) id, exercise_id, student_profile_id, submitted_answer, 
                       is_correct, time_spent_seconds, hints_used, score, feedback, 
                       attempt_number, submitted_at, created_at
                FROM exercise_attempt 
                WHERE student_profile_id = :student_profile_id
                ORDER BY submitted_at DESC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<ExerciseAttempt> attempts = new ArrayList<>();
            for (Map<String, Object> row : results) {
                attempts.add(mapExerciseAttemptFromResultMap(row));
            }
            
            return attempts;
        } catch (Exception e) {
            System.err.println("Error al obtener historial de intentos: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Exercise> findCompletedExercisesByStudent(Integer studentProfileId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);

            String sql = """
                SELECT DISTINCT e.id, e.exercise_type_id, e.competency_id, e.difficulty_level_id, 
                       e.title, e.description, e.instructions, e.learning_point_id, 
                       e.estimated_time, e.points_value, e.sequence_order, e.prompt_template_id,
                       e.created_at, e.updated_at
                FROM exercise e
                INNER JOIN exercise_attempt ea ON e.id = ea.exercise_template_id
                WHERE ea.student_profile_id = :student_profile_id AND ea.is_correct = 1
                ORDER BY ea.completed_at DESC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Exercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapExerciseFromResultMap(row));
            }
            
            return exercises;
        } catch (Exception e) {
            System.err.println("Error al obtener ejercicios completados: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Boolean isExerciseCompletedByStudent(Integer studentProfileId, Integer exerciseId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);
            parameters.addValue("exercise_id", exerciseId, Types.INTEGER);

            String sql = """
                SELECT COUNT(*)
                FROM exercise_attempt 
                WHERE student_profile_id = :student_profile_id 
                  AND exercise_id = :exercise_id 
                  AND is_correct = 1
                """;

            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return false;
            }
            
            Object count = results.get(0).values().iterator().next();
            return count != null && ((Number) count).intValue() > 0;
        } catch (Exception e) {
            System.err.println("Error al verificar si ejercicio está completado: " + e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<Double> getBestScoreByStudentAndExercise(Integer studentProfileId, Integer exerciseId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);
            parameters.addValue("exercise_id", exerciseId, Types.INTEGER);

            String sql = """
                SELECT MAX(score)
                FROM exercise_attempt 
                WHERE student_profile_id = :student_profile_id AND exercise_id = :exercise_id
                """;

            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            Object score = results.get(0).values().iterator().next();
            return score != null ? Optional.of(((Number) score).doubleValue()) : Optional.empty();
        } catch (Exception e) {
            System.err.println("Error al obtener mejor puntuación: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Integer countAttemptsByStudentAndExercise(Integer studentProfileId, Integer exerciseId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);
            parameters.addValue("exercise_id", exerciseId, Types.INTEGER);

            String sql = """
                SELECT COUNT(*)
                FROM exercise_attempt 
                WHERE student_profile_id = :student_profile_id AND exercise_id = :exercise_id
                """;

            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return 0;
            }
            
            Object count = results.get(0).values().iterator().next();
            return count != null ? ((Number) count).intValue() : 0;
        } catch (Exception e) {
            System.err.println("Error al contar intentos: " + e.getMessage());
            return 0;
        }
    }

    // ===================================================================
    // ANALYTICS & STATISTICS
    // ===================================================================

    @Override
    public Optional<StudentExerciseStats> getStudentExerciseStats(Integer studentProfileId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);

            String sql = """
                SELECT 
                    COUNT(DISTINCT ea.exercise_template_id) as total_exercises_attempted,
                    COUNT(DISTINCT CASE WHEN ea.is_correct = 1 THEN ea.exercise_template_id END) as total_exercises_completed,
                    AVG(CAST(ea.points_earned as FLOAT)) as average_score,
                    SUM(ea.time_spent) / 60 as total_time_spent_minutes,
                    (SELECT TOP 1 dl.value 
                     FROM exercise e 
                     INNER JOIN difficulty_level dl ON e.difficulty_level_id = dl.id
                     INNER JOIN exercise_attempt ea2 ON e.id = ea2.exercise_template_id 
                     WHERE ea2.student_profile_id = :student_profile_id AND ea2.is_correct = 1
                     GROUP BY dl.value 
                     ORDER BY COUNT(*) DESC) as preferred_difficulty
                FROM exercise_attempt ea
                WHERE ea.student_profile_id = :student_profile_id
                """;

            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }

            Map<String, Object> result = results.get(0);
            StudentExerciseStats stats = new StudentExerciseStats();
            stats.totalExercisesAttempted = result.get("total_exercises_attempted") != null ? 
                    ((Number) result.get("total_exercises_attempted")).intValue() : 0;
            stats.totalExercisesCompleted = result.get("total_exercises_completed") != null ? 
                    ((Number) result.get("total_exercises_completed")).intValue() : 0;
            stats.averageScore = result.get("average_score") != null ? 
                    ((Number) result.get("average_score")).doubleValue() : 0.0;
            stats.totalTimeSpentMinutes = result.get("total_time_spent_minutes") != null ? 
                    ((Number) result.get("total_time_spent_minutes")).intValue() : 0;
            stats.preferredDifficulty = (String) result.get("preferred_difficulty");
            
            return Optional.of(stats);
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas del estudiante: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Integer countRecentAttemptsByStudentAndLearningPoint(Integer studentId, Integer learningPointId, int days) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentId, Types.INTEGER);
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);
            parameters.addValue("days", days, Types.INTEGER);

            String sql = """
                SELECT COUNT(*)
                FROM exercise_attempt ea
                INNER JOIN exercise e ON ea.exercise_template_id = e.id
                WHERE ea.student_profile_id = :student_profile_id 
                  AND e.learning_point_id = :learning_point_id
                  AND ea.started_at >= DATEADD(day, -:days, GETDATE())
                """;

            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return 0;
            }
            
            Object count = results.get(0).values().iterator().next();
            return count != null ? ((Number) count).intValue() : 0;
        } catch (Exception e) {
            System.err.println("Error al contar intentos recientes: " + e.getMessage());
            return 0;
        }
    }

    @Override
    public List<ExerciseTypeStats> getStudentExerciseTypeStats(Integer studentProfileId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);

            String sql = """
                SELECT 
                    et.id as exercise_type_id,
                    et.name as exercise_type_name,
                    COUNT(ea.id) as total_attempts,
                    COUNT(CASE WHEN ea.is_correct = 1 THEN 1 END) as total_completed,
                    AVG(CAST(ea.points_earned as FLOAT)) as avg_score
                FROM exercise_type et
                INNER JOIN exercise e ON et.id = e.exercise_type_id
                INNER JOIN exercise_attempt ea ON e.id = ea.exercise_template_id
                WHERE ea.student_profile_id = :student_profile_id
                GROUP BY et.id, et.name
                ORDER BY total_attempts DESC
                """;

            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<ExerciseTypeStats> stats = new ArrayList<>();
            for (Map<String, Object> row : results) {
                ExerciseTypeStats stat = new ExerciseTypeStats();
                stat.exerciseTypeId = ((Number) row.get("exercise_type_id")).intValue();
                stat.exerciseTypeName = (String) row.get("exercise_type_name");
                stat.totalAttempts = ((Number) row.get("total_attempts")).intValue();
                stat.totalCompleted = ((Number) row.get("total_completed")).intValue();
                stat.averageScore = row.get("avg_score") != null ? 
                    ((Number) row.get("avg_score")).doubleValue() : 0.0;
                stat.strongestDifficulty = "medium"; // Calcular si es necesario
                stats.add(stat);
            }
            
            return stats;
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas por tipo de ejercicio: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Integer countAttemptsByStudentAndTemplate(Integer studentProfileId, Integer exerciseTemplateId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("student_profile_id", studentProfileId, Types.INTEGER);
            parameters.addValue("exercise_template_id", exerciseTemplateId, Types.INTEGER);

            // Contar intentos del estudiante en ejercicios generados de una plantilla específica
            String sql = """
                SELECT COUNT(*) as attempt_count
                FROM exercise_attempt ea
                INNER JOIN generated_exercise ge ON ea.generated_exercise_id = ge.id
                WHERE ea.student_profile_id = :student_profile_id 
                  AND ge.exercise_template_id = :exercise_template_id
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return 0;
            }
            
            Object count = results.get(0).get("attempt_count");
            return count != null ? ((Number) count).intValue() : 0;
        } catch (Exception e) {
            System.err.println("Error al contar intentos por estudiante y plantilla: " + e.getMessage());
            return 0;
        }
    }

    // ===================================================================
    // MAPPERS
    // ===================================================================

    private ExerciseType mapExerciseTypeFromResultMap(Map<String, Object> data) {
        return ExerciseType.builder()
                .id(((Number) data.get("id")).intValue())
                .name((String) data.get("name"))
                .description((String) data.get("description"))
                .supportedSubtypes((String) data.get("supported_subtypes"))
                .defaultConfig((String) data.get("default_config"))
                .validationRules((String) data.get("validation_rules"))
                .generationStrategy((String) data.get("generation_strategy"))
                // Campos removidos: difficulty_level y status no existen en el esquema UML
                //.difficultyLevel(null) // Se puede derivar de ejercicios individuales si es necesario
                //.status(1) // Asumimos activo por defecto
                .build();
    }

    private Exercise mapExerciseFromResultMap(Map<String, Object> data) {
        return Exercise.builder()
                .id(((Number) data.get("id")).intValue())
                .exerciseTypeId(((Number) data.get("exercise_type_id")).intValue())
                .competencyId(data.get("competency_id") != null ? ((Number) data.get("competency_id")).intValue() : null)
                .difficultyLevelId(data.get("difficulty_level_id") != null ? ((Number) data.get("difficulty_level_id")).intValue() : null)
                .title((String) data.get("title"))
                .description((String) data.get("description"))
                .instructions((String) data.get("instructions"))
                .unitId(data.get("unit_id") != null ? ((Number) data.get("unit_id")).intValue() : null)
                .estimatedTime(data.get("estimated_time") != null ? 
                    ((Number) data.get("estimated_time")).intValue() : null)
                .pointsValue(data.get("points_value") != null ? 
                    ((Number) data.get("points_value")).intValue() : null)
                .promptTemplateId(data.get("prompt_template_id") != null ? 
                    ((Number) data.get("prompt_template_id")).intValue() : null)
                .exerciseConfig((String) data.get("exercise_config"))
                .exerciseSubtype((String) data.get("exercise_subtype"))
                .generationRules((String) data.get("generation_rules"))
                .difficultyParameters((String) data.get("difficulty_parameters"))
                .createdAt(data.get("created_at") != null ? 
                    ((java.sql.Timestamp) data.get("created_at")).toLocalDateTime() : null)
                .updatedAt(data.get("updated_at") != null ? 
                    ((java.sql.Timestamp) data.get("updated_at")).toLocalDateTime() : null)
                .build();
    }

    private ExerciseAttempt mapExerciseAttemptFromResultMap(Map<String, Object> data) {
        return ExerciseAttempt.builder()
                .id(((Number) data.get("id")).intValue())
                .exerciseTemplateId(((Number) data.get("exercise_template_id")).intValue())
                .studentProfileId(((Number) data.get("student_profile_id")).intValue())
                .attemptNumber(data.get("attempt_number") != null ? 
                    ((Number) data.get("attempt_number")).intValue() : null)
                .isCorrect(data.get("is_correct") != null ? 
                    ((Number) data.get("is_correct")).intValue() == 1 : null)
                .pointsEarned(data.get("points_earned") != null ? 
                    ((Number) data.get("points_earned")).intValue() : null)
                .timeSpent(data.get("time_spent") != null ? 
                    ((Number) data.get("time_spent")).intValue() : null)
                .startedAt(data.get("started_at") != null ? 
                    ((java.sql.Timestamp) data.get("started_at")).toLocalDateTime() : null)
                .completedAt(data.get("completed_at") != null ? 
                    ((java.sql.Timestamp) data.get("completed_at")).toLocalDateTime() : null)
                .generatedExerciseId(data.get("generated_exercise_id") != null ? 
                    ((Number) data.get("generated_exercise_id")).longValue() : null)
                .build();
    }

    @Override
    public List<Exercise> findExercisesByUnitId(Integer unitId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("unit_id", unitId, Types.INTEGER);

            String sql = """
                SELECT TOP 100 id, exercise_type_id, competency_id, difficulty_level_id, 
                       title, description, instructions, estimated_time, points_value,
                       created_at, updated_at, prompt_template_id, exercise_config,
                       exercise_subtype, generation_rules, difficulty_parameters, unit_id
                FROM exercise 
                WHERE unit_id = :unit_id
                ORDER BY id
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Exercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapExerciseFromResultMap(row));
            }
            
            return exercises;
        } catch (Exception e) {
            System.err.println("Error al buscar ejercicios por unit ID " + unitId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}