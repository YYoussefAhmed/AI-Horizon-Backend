package com.blinders.blinders.dto;

import com.blinders.blinders.entity.Level;
import com.blinders.blinders.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOverviewResponse {
    
    @Schema(description = "User basic information")
    private UserProfile profile;

    @Schema(description = "List of all available levels in the system for reference")
    private List<LevelInfo> systemRoadmap;

    @Schema(description = "Details of the user's current level, including skills and lessons")
    private Level currentLevelDetails;

    @Data
    @AllArgsConstructor
    @Builder
    public static class UserProfile {
        private String name;
        private String email;
        private LevelType currentLevel;
        private boolean placementTestCompleted;
    }

    @Data
    @AllArgsConstructor
    public static class LevelInfo {
        private Long id;
        private LevelType name;
        private Integer order;
    }
}
