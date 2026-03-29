package com.blinders.blinders.controller;

import com.blinders.blinders.dto.ApiResponse;
import com.blinders.blinders.dto.ExamAttemptResponse;
import com.blinders.blinders.entity.ExamAttempt;
import com.blinders.blinders.entity.User;
import com.blinders.blinders.entity.UserProgress;
import com.blinders.blinders.repository.ExamAttemptRepository;
import com.blinders.blinders.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final UserProgressRepository userProgressRepository;
    private final ExamAttemptRepository examAttemptRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProgress(@AuthenticationPrincipal User user) {
        List<UserProgress> completedLessons = userProgressRepository
                .findByUser_UserIdAndCompletedTrue(user.getUserId());
        List<ExamAttempt> examAttempts = examAttemptRepository.findByUser_UserId(user.getUserId());

        List<ExamAttemptResponse> examAttemptResponses = examAttempts.stream()
                .map(attempt -> ExamAttemptResponse.builder()
                        .id(attempt.getId())
                        .score(attempt.getScore())
                        .passed(attempt.getPassed())
                        .attemptedAt(attempt.getAttemptedAt())
                        .examType(attempt.getExamType())
                        .levelName(attempt.getLevel() != null ? attempt.getLevel().getName().name() : "N/A")
                        .build())
                .collect(Collectors.toList());

        Map<String, Object> progress = new HashMap<>();
        progress.put("currentLevel", user.getCurrentLevel());
        progress.put("placementTestCompleted", user.getPlacementTestCompleted());
        progress.put("completedLessonsCount", completedLessons.size());
        progress.put("examAttempts", examAttemptResponses);

        return ResponseEntity.ok(new ApiResponse<>(true, "Progress retrieved", progress));
    }

    @GetMapping("/level/{levelId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLevelProgress(
            @PathVariable Long levelId,
            @AuthenticationPrincipal User user) {

        List<UserProgress> allProgress = userProgressRepository.findByUser_UserId(user.getUserId());
        List<ExamAttempt> levelAttempts = examAttemptRepository.findByUser_UserIdAndLevel_Id(user.getUserId(), levelId);

        List<ExamAttemptResponse> examAttemptResponses = levelAttempts.stream()
                .map(attempt -> ExamAttemptResponse.builder()
                        .id(attempt.getId())
                        .score(attempt.getScore())
                        .passed(attempt.getPassed())
                        .attemptedAt(attempt.getAttemptedAt())
                        .examType(attempt.getExamType())
                        .levelName(attempt.getLevel() != null ? attempt.getLevel().getName().name() : "N/A")
                        .build())
                .collect(Collectors.toList());

        Map<String, Object> progress = new HashMap<>();
        progress.put("totalProgress", allProgress.size());
        progress.put("examAttempts", examAttemptResponses);

        return ResponseEntity.ok(new ApiResponse<>(true, "Level progress retrieved", progress));
    }
}
