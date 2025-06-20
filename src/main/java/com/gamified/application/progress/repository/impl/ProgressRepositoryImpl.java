package com.gamified.application.progress.repository.impl;

import com.gamified.application.progress.model.entity.LearningPath;
import com.gamified.application.progress.model.entity.LessonProgress;
import com.gamified.application.progress.repository.ProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementación del repository para operaciones del módulo Progress
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProgressRepositoryImpl implements ProgressRepository {

    private final JdbcTemplate jdbcTemplate;
    
    // Cache en memoria para el progreso de lecciones (temporal)
    private final Map<String, LessonProgress> lessonProgressCache = new ConcurrentHashMap<>();

    @Override
    public Optional<LearningPath> findActiveLearningPathByStudent(Integer studentProfileId) {
        log.debug("Buscando learning path activo para estudiante ID: {}", studentProfileId);
        
        String sql = """
            SELECT TOP 1 
                lp.id, lp.student_profile_id, lp.adaptive_intervention_id,
                lp.current_learning_point_id, lp.units_id, lp.completion_percentage,
                lp.difficulty_adjustment, lp.is_active, lp.created_at, lp.updated_at
            FROM learning_path lp
            WHERE lp.student_profile_id = ? AND lp.is_active = 1
            ORDER BY lp.created_at DESC
            """;
        
        try {
            LearningPath learningPath = jdbcTemplate.queryForObject(sql, learningPathRowMapper(), studentProfileId);
            return Optional.of(learningPath);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No se encontró learning path activo para estudiante ID: {}", studentProfileId);
            return Optional.empty();
        }
    }

    @Override
    public LearningPath createLearningPath(LearningPath learningPath) {
        log.debug("Creando learning path para estudiante ID: {}", learningPath.getStudentProfileId());
        
        String sql = """
            INSERT INTO learning_path 
            (student_profile_id, adaptive_intervention_id, current_learning_point_id, 
             units_id, completion_percentage, difficulty_adjustment, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), GETDATE())
            """;
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        int rowsAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, learningPath.getStudentProfileId());
            ps.setObject(2, learningPath.getAdaptiveInterventionId());
            ps.setInt(3, learningPath.getCurrentLearningPointId());
            ps.setInt(4, learningPath.getUnitsId());
            ps.setBigDecimal(5, learningPath.getCompletionPercentage());
            ps.setBigDecimal(6, learningPath.getDifficultyAdjustment());
            ps.setInt(7, learningPath.getIsActive());
            return ps;
        }, keyHolder);
        
        if (rowsAffected > 0 && keyHolder.getKey() != null) {
            learningPath.setId(keyHolder.getKey().intValue());
            learningPath.setCreatedAt(LocalDateTime.now());
            learningPath.setUpdatedAt(LocalDateTime.now());
            log.info("Learning path creado exitosamente con ID: {}", learningPath.getId());
            return learningPath;
        }
        
        throw new RuntimeException("Error al crear learning path");
    }

    @Override
    public LearningPath updateLearningPath(LearningPath learningPath) {
        log.debug("Actualizando learning path ID: {}", learningPath.getId());
        
        String sql = """
            UPDATE learning_path 
            SET current_learning_point_id = ?, completion_percentage = ?, 
                difficulty_adjustment = ?, updated_at = GETDATE()
            WHERE id = ?
            """;
        
        int rowsAffected = jdbcTemplate.update(sql,
                learningPath.getCurrentLearningPointId(),
                learningPath.getCompletionPercentage(),
                learningPath.getDifficultyAdjustment(),
                learningPath.getId());
        
        if (rowsAffected > 0) {
            learningPath.setUpdatedAt(LocalDateTime.now());
            log.info("Learning path actualizado exitosamente ID: {}", learningPath.getId());
            return learningPath;
        }
        
        throw new RuntimeException("Error al actualizar learning path con ID: " + learningPath.getId());
    }

    @Override
    public Optional<LearningPointInfo> findLearningPointById(Integer learningPointId) {
        log.debug("Buscando learning point ID: {}", learningPointId);
        
        String sql = """
            SELECT lp.id, lp.title, lp.description, lp.sequence_order, 
                   lp.estimated_duration, lp.difficulty_weight, lp.unlock_criteria, lp.learning_path_id
            FROM learning_point lp
            WHERE lp.id = ? AND lp.status = 1
            """;
        
        try {
            LearningPointInfo info = jdbcTemplate.queryForObject(sql, learningPointInfoRowMapper(), learningPointId);
            return Optional.of(info);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No se encontró learning point ID: {}", learningPointId);
            return Optional.empty();
        }
    }

    @Override
    public Optional<UnitInfo> findUnitById(Integer unitId) {
        log.debug("Buscando unidad ID: {}", unitId);
        
        String sql = """
            SELECT u.id, u.title
            FROM units u
            WHERE u.id = ? AND u.status = 1
            """;
        
        try {
            UnitInfo info = jdbcTemplate.queryForObject(sql, unitInfoRowMapper(), unitId);
            return Optional.of(info);
        } catch (EmptyResultDataAccessException e) {
            log.debug("No se encontró unidad ID: {}", unitId);
            return Optional.empty();
        }
    }

    // Placeholder implementations for remaining methods
    @Override
    public List<LessonProgress> findLessonProgressByStudentAndLearningPoint(Integer studentProfileId, Integer learningPointId) {
        return new ArrayList<>();
    }

    @Override
    public LessonProgress markLessonAsCompleted(LessonProgress lessonProgress) {
        return lessonProgress;
    }

    @Override
    public boolean isLessonCompleted(Integer studentProfileId, Integer lessonId) {
        return false;
    }

    @Override
    public Optional<LessonInfo> findLessonById(Integer lessonId) {
        return Optional.empty();
    }

    @Override
    public Optional<LearningPointInfo> findNextLearningPoint(Integer currentLearningPointId, Integer unitId) {
        return Optional.empty();
    }

    @Override
    public Integer countLessonsByLearningPoint(Integer learningPointId) {
        return 0;
    }

    @Override
    public Integer countCompletedLessonsByStudentAndLearningPoint(Integer studentProfileId, Integer learningPointId) {
        return 0;
    }

    @Override
    public List<LessonInfo> findLessonsByLearningPoint(Integer learningPointId) {
        return new ArrayList<>();
    }

    @Override
    public Integer countLearningPointsByUnit(Integer unitId) {
        return 0;
    }

    @Override
    public Integer countCompletedLearningPointsByStudentAndUnit(Integer studentProfileId, Integer unitId) {
        return 0;
    }

    // ROW MAPPERS
    private RowMapper<LearningPath> learningPathRowMapper() {
        return (rs, rowNum) -> LearningPath.builder()
                .id(rs.getInt("id"))
                .studentProfileId(rs.getInt("student_profile_id"))
                .adaptiveInterventionId(rs.getObject("adaptive_intervention_id", Integer.class))
                .currentLearningPointId(rs.getInt("current_learning_point_id"))
                .unitsId(rs.getInt("units_id"))
                .completionPercentage(rs.getBigDecimal("completion_percentage"))
                .difficultyAdjustment(rs.getBigDecimal("difficulty_adjustment"))
                .isActive(rs.getInt("is_active"))
                .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null)
                .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null)
                .build();
    }

    private RowMapper<LearningPointInfo> learningPointInfoRowMapper() {
        return (rs, rowNum) -> new LearningPointInfo(
                rs.getInt("id"), rs.getString("title"), rs.getString("description"),
                rs.getInt("sequence_order"), rs.getInt("estimated_duration"),
                rs.getBigDecimal("difficulty_weight"), rs.getString("unlock_criteria"), rs.getInt("learning_path_id")
        );
    }

    private RowMapper<UnitInfo> unitInfoRowMapper() {
        return (rs, rowNum) -> new UnitInfo(rs.getInt("id"), rs.getString("title"));
    }
} 