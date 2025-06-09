package com.gamified.application.achievement.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class Achievement {
    @Setter
    @Getter
    @JsonProperty("id")
    private int id;

    @Setter
    @Getter
    @JsonProperty("achievement_type_id")
    private int achievementTypeId;

    @Setter
    @Getter
    @JsonProperty("rarity_tier_id")
    private int rarityTierId;

    @Setter
    @Getter
    @JsonProperty("achievement_name")
    private String achievementName;

    @Setter
    @Getter
    @JsonProperty("achievement_description")
    private String achievementDescription;

    @Setter
    @Getter
    @JsonProperty("large_description")
    private String largeDescription;

    @Setter
    @Getter
    @JsonProperty("points_value")
    private int pointsValue;

    @Setter
    @Getter
    @JsonProperty("requirements")
    private String requirements;

    @Setter
    @Getter
    @JsonProperty("trigger_rule")
    private String triggerRule;

    @Getter
    @Setter
    @JsonProperty("is_active")
    private int isActive;

    @Setter
    @Getter
    @JsonProperty("is_hidden")
    private int isHidden;

    @Setter
    @Getter
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Setter
    @Getter
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public Achievement() {}

    public Achievement(int id, int achievementTypeId, int rarityTierId,
                       String achievementName, String achievementDescription, int pointsValue,
                       String requirements, String triggerRule, int isActive, int isHidden, LocalDateTime createdAt, LocalDateTime updatedAt, String largeDescription) {
        this.id = id;
        this.achievementTypeId = achievementTypeId;
        this.rarityTierId = rarityTierId;
        this.achievementName = achievementName;
        this.achievementDescription = achievementDescription;
        this.pointsValue = pointsValue;
        this.requirements = requirements;
        this.triggerRule = triggerRule;
        this.isActive = isActive;
        this.isHidden = isHidden;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.largeDescription = largeDescription;
    }

    @Override
    public String toString() {
        return "Achievement{" +
                "id=" + this.id +
                ", achievementTypeId='" + this.achievementTypeId + '\'' +
                ", rarityTierId='" + this.rarityTierId + '\'' +
                ", achievementName='" + this.achievementName + '\'' +
                ", achievementDescription=" + this.achievementDescription +
                ", pointsValue='" + this.pointsValue + '\'' +
                ", requirements=" + this.requirements + '\'' +
                ", triggerRule='" + this.triggerRule + '\'' +
                ", isActive=" + this.isActive +
                ", isHidden=" + this.isHidden +
                ", createdAt=" + this.createdAt +
                ", updatedAt=" + this.updatedAt +
                ", largeDescription='" + this.largeDescription +
                '}';
    }
}
