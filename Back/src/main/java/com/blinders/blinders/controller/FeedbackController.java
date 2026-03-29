package com.blinders.blinders.controller;

import com.blinders.blinders.dto.ApiResponse;
import com.blinders.blinders.dto.FeedbackResponse;
import com.blinders.blinders.entity.Level;
import com.blinders.blinders.entity.LevelFeedback;
import com.blinders.blinders.entity.User;
import com.blinders.blinders.repository.LevelFeedbackRepository;
import com.blinders.blinders.repository.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final LevelFeedbackRepository feedbackRepository;
    private final LevelRepository levelRepository;

    @PostMapping("/level/{levelId}")
    public ResponseEntity<ApiResponse<FeedbackResponse>> submitFeedback(
            @PathVariable Long levelId,
            @RequestBody Map<String, Integer> request,
            @AuthenticationPrincipal User user) {

        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found"));

        LevelFeedback feedback = feedbackRepository.findByUser_UserIdAndLevel_Id(user.getUserId(), levelId)
                .orElse(new LevelFeedback());

        feedback.setUser(user);
        feedback.setLevel(level);
        feedback.setRating(request.get("rating"));

        LevelFeedback saved = feedbackRepository.save(feedback);
        
        FeedbackResponse response = mapToResponse(saved);
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback submitted", response));
    }

    @GetMapping("/level/{levelId}")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getLevelFeedback(@PathVariable Long levelId) {
        List<LevelFeedback> feedbacks = feedbackRepository.findByLevelId(levelId);
        List<FeedbackResponse> responses = feedbacks.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Feedback retrieved", responses));
    }

    private FeedbackResponse mapToResponse(LevelFeedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .userName(feedback.getUser().getName())
                .levelName(feedback.getLevel().getName().name())
                .rating(feedback.getRating())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
