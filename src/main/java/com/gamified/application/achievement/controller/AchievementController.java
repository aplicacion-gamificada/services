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
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getAllAchievementsByUser(
            @PathVariable int userId) {
        try {
            List<Map<String, Object>> achievements = achievementService.getAllAchievementsByUser(userId);
            return ResponseEntity.ok(achievements);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
