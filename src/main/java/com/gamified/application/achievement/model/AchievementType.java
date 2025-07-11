package com.gamified.application.achievement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class AchievementType {
    @Setter
    @Getter
    @JsonProperty("id")
    private int id;

    @Setter
    @Getter
    @JsonProperty("category_name")
    private String categoryName;

    @Setter
    @Getter
    @JsonProperty("achievement_type_description")
    private String achievementTypeDescription;

    @Setter
    @Getter
    @JsonProperty("image_url")
    private String imageUrl;

    @Setter
    @Getter
    @JsonProperty("title_color")
    private String titleColor;

    @Setter
    @Getter
    @JsonProperty("points_color")
    private String pointsColor;

    @Setter
    @Getter
    @JsonProperty("is_active")
    private int isActive;

    @Setter
    @Getter
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Setter
    @Getter
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public AchievementType() {}

    public AchievementType(int id, String categoryName, String achievementTypeDescription, String imageUrl, String titleColor,
                           String pointsColor, int isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.categoryName = categoryName;
        this.achievementTypeDescription = achievementTypeDescription;
        this.imageUrl = imageUrl;
        this.titleColor = titleColor;
        this.pointsColor = pointsColor;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "AchievementType{" +
                "id=" + this.id +
                ", categoryName='" + this.categoryName + '\'' +
                ", achievementTypeDescription='" + this.achievementTypeDescription + '\'' +
                ", imageUrl='" + this.imageUrl + '\'' +
                ", titleColor='" + this.titleColor + '\'' +
                ", pointsColor='" + this.pointsColor + '\'' +
                ", isActive=" + this.isActive + '\'' +
                ", createdAt=" + this.createdAt + '\'' +
                ", updatedAt=" + this.updatedAt +
                '}';
    }
}
