package com.gamified.application.achievement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class StudentAchievement {
    @Setter
    @Getter
    @JsonProperty("id")
    private int id;

    @Setter
    @Getter
    @JsonProperty("student_profile_id")
    private int studentProfileId;

    @Setter
    @Getter
    @JsonProperty("achievement_id")
    private int achievementId;

    @Setter
    @Getter
    @JsonProperty("earned_at")
    private LocalDateTime earnedAt;

    @Setter
    @Getter
    @JsonProperty("points_awarded")
    private int pointsAwarded;

    public StudentAchievement(){}

    public StudentAchievement(int id, int studentProfileId, int achievementId, int pointsAwarded, LocalDateTime earnedAt) {
        this.id = id;
        this.studentProfileId = studentProfileId;
        this.achievementId = achievementId;
        this.pointsAwarded = pointsAwarded;
        this.earnedAt = earnedAt;
    }

    @Override
    public String toString() {
        return "StudentAchievement{"+
                "id=" + this.id +
                ", studentProfileId=" + this.studentProfileId + '\'' +
                ", earnedAt=" + this.earnedAt + '\'' +
                ", pointsAwarded=" + this.pointsAwarded +
                '}';
    }
}
