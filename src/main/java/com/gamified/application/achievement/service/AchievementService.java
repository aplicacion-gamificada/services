package com.gamified.application.achievement.service;

import com.gamified.application.achievement.repository.IAchievementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AchievementService {
    @Autowired
    private IAchievementRepository achievementRepository;

    public List<Map<String, Object>> getAllAchievementsByUser(int userId) {
        return achievementRepository.getAllAchievementsByUser(userId);
    }

    public List<Map<String, Object>> getUnlockedAchievementsByUser(int userId){
        return achievementRepository.getUnlockedAchievementsByUser(userId);
    }
    public List<Map<String, Object>> getAchievements(){
        return achievementRepository.getAchievements();
    }
    public List<Map<String, Object>> getAchievementsTypes(){
        return achievementRepository.getAchievementsTypes();
    }
    public Map<String, Object> getAchievementDetails(int achievementId){
        return achievementRepository.getAchievementDetails(achievementId);
    }
    public Map<String, Object> unlockAchievement(int userId, int achievementId, int pointsAwarded){
        return achievementRepository.unlockAchievement(userId, achievementId, pointsAwarded);
    }
    public Map<String, Object> getCountAchievementByUser(int userId){
        return achievementRepository.getCountAchievementByUser(userId);
    }
}
