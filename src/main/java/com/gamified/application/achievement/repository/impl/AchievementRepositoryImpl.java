package com.gamified.application.achievement.repository.impl;

import com.gamified.application.achievement.repository.IAchievementRepository;
import com.gamified.application.config.DatabaseConfig;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.sql.Types;
import java.util.Map;

@Repository
public class AchievementRepositoryImpl implements IAchievementRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AchievementRepositoryImpl(JdbcTemplate jdbcTemplate,
                                     NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<Map<String, Object>> getAllAchievementsByUser(int userId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("userId", userId, Types.INTEGER);

            // Opción 1: Sintaxis SQL Server con parámetro nombrado
            String sql = "EXEC sp_get_achievements_by_user @user_id = :userId";

            return namedParameterJdbcTemplate.queryForList(sql, parameters);

        } catch (DataAccessException ex) {
            throw new RuntimeException("Error al obtener logros del usuario " + userId + ": " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Error inesperado al obtener logros del usuario " + userId + ": " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Map<String, Object>> getUnlockedAchievementsByUser(int userId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("@user_id", userId, Types.INTEGER);

            String sql = "EXEC sp_get_unlocked_achievements_by_user : userId";
            return namedParameterJdbcTemplate.queryForList(sql, parameters);

        } catch (Exception ex) {
            throw new RuntimeException("Error al obtener logros desbloqueados: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Map<String, Object>> getAchievements() {
        try {
            String sql = "EXEC sp_get_achievements";
            return namedParameterJdbcTemplate.queryForList(sql, new HashMap<>());

        } catch (Exception ex) {
            throw new RuntimeException("Error al obtener logros: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Map<String, Object>> getAchievementsTypes() {
        try {
            String sql = "EXEC sp_get_achievement_types";
            return namedParameterJdbcTemplate.queryForList(sql, new HashMap<>());

        } catch (Exception ex) {
            throw new RuntimeException("Error al obtener los tipos de logros: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Map<String, Object> getAchievementDetails(int achievementId) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("@achievement_id", achievementId, Types.INTEGER);

            String sql = "EXEC sp_get_achievement_details :achievement_id";
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);

            return results.isEmpty() ? null : results.getFirst();

        } catch (Exception ex) {
            throw new RuntimeException("Error al obtener detalles del logro: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Map<String, Object> unlockAchievement(int userId, int achievementId, int pointsAwarded) {
        try {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("@user_id", userId, Types.INTEGER);
            parameters.addValue("@achievement_id", achievementId, Types.INTEGER);
            parameters.addValue("@points_awarded", pointsAwarded, Types.INTEGER);

            String sql = "EXEC sp_unlock_achievement :userId, :achievement_id, :points_awarded";
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);

            return results.isEmpty() ? null : results.getFirst();

        } catch (Exception ex) {
            throw new RuntimeException("Error al desbloquear logro: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Map<String, Object> getCountAchievementByUser(int userId){
        try{
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("@user_id", userId, Types.INTEGER);
            String sql = "EXEC sp_get_count_achievement_by_user : userId";
            List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(sql, parameters);
            return results.isEmpty() ? null : results.getFirst();
        } catch (Exception ex) {
            throw new RuntimeException("Error al traer los datos: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Integer getUserIdFromStudentProfile(Integer studentProfileId) {
        try {
            String sql = "SELECT user_id FROM student_profile WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, Integer.class, studentProfileId);
        } catch (Exception e) {
            // Si no se encuentra, retornar null
            return null;
        }
    }
}
