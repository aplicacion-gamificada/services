package com.gamified.application.achievement.repository;

import java.util.List;
import java.util.Map;

public interface IAchievementRepository {
    List<Map<String, Object>> getAllAchievementsByUser(int userId);
    List<Map<String, Object>> getUnlockedAchievementsByUser(int userId);
    List<Map<String, Object>> getAchievements();
    List<Map<String, Object>> getAchievementsTypes();
    Map<String, Object> getAchievementDetails(int achievementId);
    Map<String, Object> unlockAchievement(int userId, int achievementId, int pointsAwarded);
    Map<String, Object> getCountAchievementByUser(int userId);
    Integer getUserIdFromStudentProfile(Integer studentProfileId);
    Map<String, Object> getAchievementStats(int userId);
    Map<String, Object> getTotalPoints(int userId);
}
