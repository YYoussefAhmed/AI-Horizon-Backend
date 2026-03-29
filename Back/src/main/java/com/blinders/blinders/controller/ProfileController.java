package com.blinders.blinders.controller;

import com.blinders.blinders.dto.ApiResponse;
import com.blinders.blinders.dto.UserOverviewResponse;
import com.blinders.blinders.entity.Level;
import com.blinders.blinders.entity.User;
import com.blinders.blinders.repository.LevelRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile and overview endpoints")
public class ProfileController {

    private final LevelRepository levelRepository;

    @GetMapping("/overview")
    @Operation(summary = "Get a comprehensive overview of user progress and system roadmap (The Cheat Sheet)")
    public ResponseEntity<ApiResponse<UserOverviewResponse>> getOverview(@AuthenticationPrincipal User user) {
        
        // 1. Prepare User Profile
        UserOverviewResponse.UserProfile profile = UserOverviewResponse.UserProfile.builder()
                .name(user.getName())
                .email(user.getEmail())
                .currentLevel(user.getCurrentLevel())
                .placementTestCompleted(user.getPlacementTestCompleted())
                .build();

        // 2. Prepare System Roadmap (All levels)
        List<Level> allLevels = levelRepository.findAllByOrderByOrderAsc();
        List<UserOverviewResponse.LevelInfo> roadmap = allLevels.stream()
                .map(l -> new UserOverviewResponse.LevelInfo(l.getId(), l.getName(), l.getOrder()))
                .collect(Collectors.toList());

        // 3. Find current level details
        Level currentLevelDetails = null;
        if (user.getCurrentLevel() != null) {
            currentLevelDetails = levelRepository.findByName(user.getCurrentLevel()).orElse(null);
        }

        UserOverviewResponse response = UserOverviewResponse.builder()
                .profile(profile)
                .systemRoadmap(roadmap)
                .currentLevelDetails(currentLevelDetails)
                .build();

        return ResponseEntity.ok(new ApiResponse<>(true, "Overview retrieved successfully", response));
    }
}
