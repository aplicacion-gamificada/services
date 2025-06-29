package com.gamified.application.learning.repository;

import com.gamified.application.learning.model.entity.*;
import com.gamified.application.shared.util.DatabaseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación del repositorio Learning con JDBC y SQL Server
 */
@Repository
@RequiredArgsConstructor
public class LearningRepositoryImpl implements LearningRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    // ===================================================================
    // STEM AREAS
    // ===================================================================

    @Override
    public List<StemArea> findAllActiveStemAreas() {
        try {
            String sql = "SELECT TOP 100 id, title, description, status " +
                        "FROM stem_area WHERE status = 1 ORDER BY title";
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            List<StemArea> stemAreas = new ArrayList<>();
            for (Map<String, Object> row : results) {
                stemAreas.add(mapStemAreaFromResultMap(row));
            }
            
            return stemAreas;
        } catch (Exception e) {
            System.err.println("Error al obtener áreas STEM activas: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<StemArea> findStemAreaById(Integer stemAreaId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("stem_area_id", stemAreaId, Types.INTEGER);

            String sql = "SELECT id, title, description, status " +
                        "FROM stem_area WHERE id = :stem_area_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapStemAreaFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar área STEM por ID " + stemAreaId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    // ===================================================================
    // SPECIALIZATIONS
    // ===================================================================

    @Override
    public List<Specialization> findSpecializationsByStemArea(Integer stemAreaId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("stem_area_id", stemAreaId, Types.INTEGER);

            String sql = "SELECT TOP 100 id, stem_area_id, title, description, status " +
                        "FROM specialization WHERE stem_area_id = :stem_area_id AND status = 1 ORDER BY title";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Specialization> specializations = new ArrayList<>();
            for (Map<String, Object> row : results) {
                specializations.add(mapSpecializationFromResultMap(row));
            }
            
            return specializations;
        } catch (Exception e) {
            System.err.println("Error al obtener especializaciones del área STEM " + stemAreaId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Specialization> findSpecializationById(Integer specializationId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("specialization_id", specializationId, Types.INTEGER);

            String sql = "SELECT id, stem_area_id, title, description, status " +
                        "FROM specialization WHERE id = :specialization_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapSpecializationFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar especialización por ID " + specializationId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    // ===================================================================
    // MODULES
    // ===================================================================

    @Override
    public List<com.gamified.application.learning.model.entity.Module> findModulesBySpecialization(Integer specializationId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("specialization_id", specializationId, Types.INTEGER);

            String sql = "SELECT TOP 100 id, specialization_id, title, description, sequence, status " +
                        "FROM module WHERE specialization_id = :specialization_id AND status = 1 ORDER BY sequence";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<com.gamified.application.learning.model.entity.Module> modules = new ArrayList<>();
            for (Map<String, Object> row : results) {
                modules.add(mapModuleFromResultMap(row));
            }
            
            return modules;
        } catch (Exception e) {
            System.err.println("Error al obtener módulos de la especialización " + specializationId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<com.gamified.application.learning.model.entity.Module> findModuleById(Integer moduleId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("module_id", moduleId, Types.INTEGER);

            String sql = "SELECT id, specialization_id, title, description, sequence, status " +
                        "FROM module WHERE id = :module_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapModuleFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar módulo por ID " + moduleId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    // ===================================================================
    // UNITS
    // ===================================================================

    @Override
    public List<Unit> findUnitsByModule(Integer moduleId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("module_id", moduleId, Types.INTEGER);

            String sql = "SELECT TOP 100 id, module_id, title, description, sequence, status " +
                        "FROM units WHERE module_id = :module_id AND status = 1 ORDER BY sequence";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Unit> units = new ArrayList<>();
            for (Map<String, Object> row : results) {
                units.add(mapUnitFromResultMap(row));
            }
            
            return units;
        } catch (Exception e) {
            System.err.println("Error al obtener unidades del módulo " + moduleId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Unit> findUnitById(Integer unitId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("unit_id", unitId, Types.INTEGER);

            String sql = "SELECT id, module_id, title, description, sequence, status, created_at, updated_at " +
                        "FROM units WHERE id = :unit_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapUnitFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar unidad por ID " + unitId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    // ===================================================================
    // LEARNING POINTS
    // ===================================================================

    @Override
    public List<LearningPoint> findLearningPointsByUnit(Integer unitId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("unit_id", unitId, Types.INTEGER);

            // Según el modelo UML: learning_point -> learning_path -> units
            // Necesitamos hacer JOIN para obtener learning_points a través de learning_path
            String sql = "SELECT TOP 100 lp.id, lp.learning_path_id, lp.title, lp.description, lp.sequence_order, " +
                        "lp.estimated_duration, lp.difficulty_weight, lp.mastery_threshold, " +
                        "lp.is_prerequisite, lp.unlock_criteria, lp.status, lp.created_at, lp.updated_at " +
                        "FROM learning_point lp " +
                        "INNER JOIN learning_path path ON lp.learning_path_id = path.id " +
                        "WHERE path.units_id = :unit_id AND lp.status = 1 " +
                        "ORDER BY lp.sequence_order";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<LearningPoint> learningPoints = new ArrayList<>();
            for (Map<String, Object> row : results) {
                learningPoints.add(mapLearningPointFromResultMap(row));
            }
            
            return learningPoints;
        } catch (Exception e) {
            System.err.println("Error al obtener learning points de la unidad " + unitId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<LearningPoint> findLearningPointById(Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = "SELECT id, learning_path_id, title, description, sequence_order, " +
                        "estimated_duration, difficulty_weight, mastery_threshold, " +
                        "is_prerequisite, unlock_criteria, status, created_at, updated_at " +
                        "FROM learning_point WHERE id = :learning_point_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapLearningPointFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar learning point por ID " + learningPointId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    // ===================================================================
    // LESSONS
    // ===================================================================

    @Override
    public List<Lesson> findLessonsByLearningPoint(Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = "SELECT TOP 100 id, learning_point_id, title, content_data, sequence_order, " +
                        "estimated_reading_time, is_mandatory, created_at, updated_at " +
                        "FROM lesson WHERE learning_point_id = :learning_point_id ORDER BY sequence_order";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            List<Lesson> lessons = new ArrayList<>();
            for (Map<String, Object> row : results) {
                lessons.add(mapLessonFromResultMap(row));
            }
            
            return lessons;
        } catch (Exception e) {
            System.err.println("Error al obtener lecciones del learning point " + learningPointId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Lesson> findLessonById(Integer lessonId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("lesson_id", lessonId, Types.INTEGER);

            String sql = "SELECT id, learning_point_id, title, content_data, sequence_order, " +
                        "estimated_reading_time, is_mandatory, created_at, updated_at " +
                        "FROM lesson WHERE id = :lesson_id";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapLessonFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar lección por ID " + lessonId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Lesson> findPreviousLesson(Integer learningPointId, Integer currentSequence) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);
            parameters.addValue("current_sequence", currentSequence, Types.INTEGER);

            String sql = "SELECT TOP 1 id, learning_point_id, title, content_data, sequence_order, " +
                        "estimated_reading_time, is_mandatory, created_at, updated_at " +
                        "FROM lesson WHERE learning_point_id = :learning_point_id " +
                        "AND sequence_order < :current_sequence ORDER BY sequence_order DESC";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapLessonFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar lección anterior: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<Lesson> findNextLesson(Integer learningPointId, Integer currentSequence) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);
            parameters.addValue("current_sequence", currentSequence, Types.INTEGER);

            String sql = "SELECT TOP 1 id, learning_point_id, title, content_data, sequence_order, " +
                        "estimated_reading_time, is_mandatory, created_at, updated_at " +
                        "FROM lesson WHERE learning_point_id = :learning_point_id " +
                        "AND sequence_order > :current_sequence ORDER BY sequence_order";
            
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            return Optional.of(mapLessonFromResultMap(results.get(0)));
        } catch (Exception e) {
            System.err.println("Error al buscar lección siguiente: " + e.getMessage());
            return Optional.empty();
        }
    }

    // ===================================================================
    // CONTEOS (para DTOs)
    // ===================================================================

    @Override
    public Integer countSpecializationsByStemArea(Integer stemAreaId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("stem_area_id", stemAreaId, Types.INTEGER);

            String sql = "SELECT COUNT(*) FROM specialization WHERE stem_area_id = :stem_area_id AND status = 1";
            
            Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Integer countModulesBySpecialization(Integer specializationId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("specialization_id", specializationId, Types.INTEGER);

            String sql = "SELECT COUNT(*) FROM module WHERE specialization_id = :specialization_id AND status = 1";
            
            Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Integer countUnitsByModule(Integer moduleId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("module_id", moduleId, Types.INTEGER);

            String sql = "SELECT COUNT(*) FROM units WHERE module_id = :module_id AND status = 1";
            
            Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Integer countLearningPointsByUnit(Integer unitId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("unit_id", unitId, Types.INTEGER);

            // Según el modelo UML: learning_point -> learning_path -> units
            String sql = "SELECT COUNT(*) FROM learning_point lp " +
                        "INNER JOIN learning_path path ON lp.learning_path_id = path.id " +
                        "WHERE path.units_id = :unit_id AND lp.status = 1";
            
            Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Integer countLessonsByLearningPoint(Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = "SELECT COUNT(*) FROM lesson WHERE learning_point_id = :learning_point_id";
            
            Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public Integer countExercisesByLearningPoint(Integer learningPointId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("learning_point_id", learningPointId, Types.INTEGER);

            String sql = "SELECT COUNT(*) FROM exercise WHERE learning_point_id = :learning_point_id";
            
            Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameters, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ===================================================================
    // MAPPERS
    // ===================================================================

    private StemArea mapStemAreaFromResultMap(Map<String, Object> data) {
        return StemArea.builder()
                .id((Integer) data.get("id"))
                .title((String) data.get("title"))
                .description((String) data.get("description"))
                .status((Integer) data.get("status"))
                .build();
    }

    private Specialization mapSpecializationFromResultMap(Map<String, Object> data) {
        return Specialization.builder()
                .id((Integer) data.get("id"))
                .stemAreaId((Integer) data.get("stem_area_id"))
                .title((String) data.get("title"))
                .description((String) data.get("description"))
                .status((Integer) data.get("status"))
                .build();
    }

    private com.gamified.application.learning.model.entity.Module mapModuleFromResultMap(Map<String, Object> data) {
        return com.gamified.application.learning.model.entity.Module.builder()
                .id((Integer) data.get("id"))
                .specializationId((Integer) data.get("specialization_id"))
                .title((String) data.get("title"))
                .description((String) data.get("description"))
                .sequence((Integer) data.get("sequence"))
                .status((Integer) data.get("status"))
                .build();
    }

    private Unit mapUnitFromResultMap(Map<String, Object> data) {
        return Unit.builder()
                .id((Integer) data.get("id"))
                .moduleId((Integer) data.get("module_id"))
                .title((String) data.get("title"))
                .description((String) data.get("description"))
                .sequence((Integer) data.get("sequence"))
                .status((Integer) data.get("status"))
                .build();
    }

    private LearningPoint mapLearningPointFromResultMap(Map<String, Object> data) {
        return LearningPoint.builder()
                .id((Integer) data.get("id"))
                .learningPathId((Integer) data.get("learning_path_id"))
                .title((String) data.get("title"))
                .description((String) data.get("description"))
                .sequenceOrder((Integer) data.get("sequence_order"))
                .estimatedDuration((Integer) data.get("estimated_duration"))
                .difficultyWeight(DatabaseUtils.safeToBigDecimal(data.get("difficulty_weight")))
                .masteryThreshold(DatabaseUtils.safeToBigDecimal(data.get("mastery_threshold")))
                .isPrerequisite((Integer) data.get("is_prerequisite"))
                .unlockCriteria((String) data.get("unlock_criteria"))
                .status((Integer) data.get("status"))
                .createdAt(DatabaseUtils.safeToLocalDateTime(data.get("created_at")))
                .updatedAt(DatabaseUtils.safeToLocalDateTime(data.get("updated_at")))
                .build();
    }

    private Lesson mapLessonFromResultMap(Map<String, Object> data) {
        return Lesson.builder()
                .id((Integer) data.get("id"))
                .learningPointId((Integer) data.get("learning_point_id"))
                .title((String) data.get("title"))
                .contentData((String) data.get("content_data"))
                .sequenceOrder((Integer) data.get("sequence_order"))
                .estimatedReadingTime((Integer) data.get("estimated_reading_time"))
                .isMandatory((Integer) data.get("is_mandatory"))
                .createdAt(DatabaseUtils.safeToLocalDateTime(data.get("created_at")))
                .updatedAt(DatabaseUtils.safeToLocalDateTime(data.get("updated_at")))
                .build();
    }
} 