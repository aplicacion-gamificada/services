package com.gamified.application.achievement.controller;

import com.gamified.application.achievement.service.AchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/achievements")
public class AchievementController {
    @Autowired
    private AchievementService achievementService;

    /**
     ** GET /api/achievements/user/{userId} - Todos los logros del usuario
     **/
    @GetMapping("/all-achievements-by-user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getAllAchievementsByUser(
            @PathVariable int userId) {
        try {
            List<Map<String, Object>> achievements = achievementService.getAllAchievementsByUser(userId);
            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/unlocked-achievements/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUnlockedAchievementsByUser(@PathVariable int userId){
        try{
            List<Map<String, Object>> achievements = achievementService.getUnlockedAchievementsByUser(userId);
            return ResponseEntity.ok(achievements);
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/total-points/{userId}")
    public ResponseEntity<Map<String, Object>> getTotalPoints(@PathVariable int userId){
        try{
            Map<String, Object> points = achievementService.getTotalPoints(userId);
            return ResponseEntity.ok(points);
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/achievement-stats/{userId}")
    public ResponseEntity<Map<String, Object>> getAchievementStatsByUser(@PathVariable int userId){
        try {
            Map<String, Object> stats = achievementService.getAchievementStats(userId);
            return ResponseEntity.ok(stats);
        }
        catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }
}
