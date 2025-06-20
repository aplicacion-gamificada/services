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
            String sql = "SELECT TOP 100 id, name, description, difficulty_level, status " +
                        "FROM exercise_type WHERE status = 1 ORDER BY name";
            
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

            String sql = "SELECT id, name, description, difficulty_level, status " +
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

            // Buscar ejercicio que no haya sido completado exitosamente por el estudiante
            String sql = """
                SELECT TOP 1 e.id, e.learning_point_id, e.exercise_type_id, e.title, 
                       e.question_text, e.correct_answer, e.possible_answers, e.difficulty, 
                       e.metadata, e.hints, e.estimated_time_minutes, e.status, 
                       e.created_at, e.updated_at
                FROM exercise e
                WHERE e.learning_point_id = :learning_point_id 
                  AND e.status = 1
                  AND (e.difficulty = :difficulty OR :difficulty = 'any')
                  AND NOT EXISTS (
                      SELECT 1 FROM exercise_attempt ea 
                      WHERE ea.exercise_id = e.id 
                        AND ea.student_profile_id = :student_profile_id 
                        AND ea.is_correct = 1
                  )
                ORDER BY NEWID()
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
                SELECT id, learning_point_id, exercise_type_id, title, question_text, 
                       correct_answer, possible_answers, difficulty, metadata, hints, 
                       estimated_time_minutes, status, created_at, updated_at
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
            parameters.addValue("learning_point_id", exercise.getLearningPointId(), Types.INTEGER);
            parameters.addValue("exercise_type_id", exercise.getExerciseTypeId(), Types.INTEGER);
            parameters.addValue("title", exercise.getTitle(), Types.VARCHAR);
            parameters.addValue("question_text", exercise.getQuestionText(), Types.VARCHAR);
            parameters.addValue("correct_answer", exercise.getCorrectAnswer(), Types.VARCHAR);
            parameters.addValue("possible_answers", exercise.getPossibleAnswers(), Types.VARCHAR);
            parameters.addValue("difficulty", exercise.getDifficulty(), Types.VARCHAR);
            parameters.addValue("metadata", exercise.getMetadata(), Types.VARCHAR);
            parameters.addValue("hints", exercise.getHints(), Types.VARCHAR);
            parameters.addValue("estimated_time_minutes", exercise.getEstimatedTimeMinutes(), Types.INTEGER);
            parameters.addValue("status", exercise.getStatus() != null ? exercise.getStatus() : 1, Types.INTEGER);

            String sql = """
                INSERT INTO exercise (learning_point_id, exercise_type_id, title, question_text, 
                                    correct_answer, possible_answers, difficulty, metadata, hints, 
                                    estimated_time_minutes, status, created_at, updated_at)
                VALUES (:learning_point_id, :exercise_type_id, :title, :question_text, 
                        :correct_answer, :possible_answers, :difficulty, :metadata, :hints, 
                        :estimated_time_minutes, :status, GETDATE(), GETDATE())
                """;

            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
            
            return keyHolder.getKey().intValue();
        } catch (Exception e) {
            System.err.println("Error al crear ejercicio: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Exercise> findExercisesByLearningPoint(Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = """
                SELECT TOP 100 id, learning_point_id, exercise_type_id, title, question_text, 
                       correct_answer, possible_answers, difficulty, metadata, hints, 
                       estimated_time_minutes, status, created_at, updated_at
                FROM exercise 
                WHERE learning_point_id = :learning_point_id AND status = 1
                ORDER BY created_at DESC
                """;
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Exercise> exercises = new ArrayList<>();
            for (Map<String, Object> row : results) {
                exercises.add(mapExerciseFromResultMap(row));
            }
            
            return exercises;
        } catch (Exception e) {
            System.err.println("Error al obtener ejercicios por learning point " + learningPointId + ": " + e.getMessage());
            return new ArrayList<>();
        }
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
            
            return keyHolder.getKey().intValue();
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
                SELECT DISTINCT e.id, e.learning_point_id, e.exercise_type_id, e.title, 
                       e.question_text, e.correct_answer, e.possible_answers, e.difficulty, 
                       e.metadata, e.hints, e.estimated_time_minutes, e.status, 
                       e.created_at, e.updated_at
                FROM exercise e
                INNER JOIN exercise_attempt ea ON e.id = ea.exercise_id
                WHERE ea.student_profile_id = :student_profile_id AND ea.is_correct = 1
                ORDER BY ea.submitted_at DESC
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
                    COUNT(DISTINCT ea.exercise_id) as total_attempted,
                    COUNT(DISTINCT CASE WHEN ea.is_correct = 1 THEN ea.exercise_id END) as total_completed,
                    AVG(CAST(ea.score as FLOAT)) as avg_score,
                    SUM(ea.time_spent_seconds) / 60 as total_time_minutes
                FROM exercise_attempt ea
                WHERE ea.student_profile_id = :student_profile_id
                """;

            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            Map<String, Object> row = results.get(0);
            StudentExerciseStats stats = new StudentExerciseStats();
            stats.totalExercisesAttempted = row.get("total_attempted") != null ? 
                ((Number) row.get("total_attempted")).intValue() : 0;
            stats.totalExercisesCompleted = row.get("total_completed") != null ? 
                ((Number) row.get("total_completed")).intValue() : 0;
            stats.averageScore = row.get("avg_score") != null ? 
                ((Number) row.get("avg_score")).doubleValue() : 0.0;
            stats.totalTimeSpentMinutes = row.get("total_time_minutes") != null ? 
                ((Number) row.get("total_time_minutes")).intValue() : 0;
            stats.preferredDifficulty = "medium"; // Calcular en otra consulta si es necesario
            
            return Optional.of(stats);
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas del estudiante: " + e.getMessage());
            return Optional.empty();
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
                    AVG(CAST(ea.score as FLOAT)) as avg_score
                FROM exercise_type et
                INNER JOIN exercise e ON et.id = e.exercise_type_id
                INNER JOIN exercise_attempt ea ON e.id = ea.exercise_id
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

    // ===================================================================
    // MAPPERS
    // ===================================================================

    private ExerciseType mapExerciseTypeFromResultMap(Map<String, Object> data) {
        return ExerciseType.builder()
                .id(((Number) data.get("id")).intValue())
                .name((String) data.get("name"))
                .description((String) data.get("description"))
                .difficultyLevel((String) data.get("difficulty_level"))
                .status(((Number) data.get("status")).intValue())
                .build();
    }

    private Exercise mapExerciseFromResultMap(Map<String, Object> data) {
        return Exercise.builder()
                .id(((Number) data.get("id")).intValue())
                .learningPointId(((Number) data.get("learning_point_id")).intValue())
                .exerciseTypeId(((Number) data.get("exercise_type_id")).intValue())
                .title((String) data.get("title"))
                .questionText((String) data.get("question_text"))
                .correctAnswer((String) data.get("correct_answer"))
                .possibleAnswers((String) data.get("possible_answers"))
                .difficulty((String) data.get("difficulty"))
                .metadata((String) data.get("metadata"))
                .hints((String) data.get("hints"))
                .estimatedTimeMinutes(data.get("estimated_time_minutes") != null ? 
                    ((Number) data.get("estimated_time_minutes")).intValue() : null)
                .status(((Number) data.get("status")).intValue())
                .createdAt(data.get("created_at") != null ? 
                    ((java.sql.Timestamp) data.get("created_at")).toLocalDateTime() : null)
                .updatedAt(data.get("updated_at") != null ? 
                    ((java.sql.Timestamp) data.get("updated_at")).toLocalDateTime() : null)
                .build();
    }

    private ExerciseAttempt mapExerciseAttemptFromResultMap(Map<String, Object> data) {
        return ExerciseAttempt.builder()
                .id(((Number) data.get("id")).intValue())
                .exerciseId(((Number) data.get("exercise_id")).intValue())
                .studentProfileId(((Number) data.get("student_profile_id")).intValue())
                .submittedAnswer((String) data.get("submitted_answer"))
                .isCorrect((Boolean) data.get("is_correct"))
                .timeSpentSeconds(data.get("time_spent_seconds") != null ? 
                    ((Number) data.get("time_spent_seconds")).intValue() : null)
                .hintsUsed(data.get("hints_used") != null ? 
                    ((Number) data.get("hints_used")).intValue() : null)
                .score(data.get("score") != null ? 
                    ((Number) data.get("score")).doubleValue() : null)
                .feedback((String) data.get("feedback"))
                .attemptNumber(data.get("attempt_number") != null ? 
                    ((Number) data.get("attempt_number")).intValue() : null)
                .submittedAt(data.get("submitted_at") != null ? 
                    ((java.sql.Timestamp) data.get("submitted_at")).toLocalDateTime() : null)
                .createdAt(data.get("created_at") != null ? 
                    ((java.sql.Timestamp) data.get("created_at")).toLocalDateTime() : null)
                .build();
    }
}