package com.blinders.blinders.controller;

import com.blinders.blinders.dto.ApiResponse;
import com.blinders.blinders.entity.Lesson;
import com.blinders.blinders.entity.User;
import com.blinders.blinders.entity.UserProgress;
import com.blinders.blinders.repository.LessonRepository;
import com.blinders.blinders.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonRepository lessonRepository;
    private final UserProgressRepository userProgressRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Lesson>>> getLessons(@RequestParam(required = false) Long skillId) {
        List<Lesson> lessons;
        if (skillId != null) {
            lessons = lessonRepository.findBySkillIdOrderByOrderAsc(skillId);
        } else {
            lessons = lessonRepository.findAll();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "Lessons retrieved", lessons));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Lesson>> getLessonById(@PathVariable Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        return ResponseEntity.ok(new ApiResponse<>(true, "Lesson retrieved", lesson));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<UserProgress>> completeLesson(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        UserProgress progress = userProgressRepository
                .findByUser_UserIdAndLesson_Id(user.getUserId(), id)
                .orElse(new UserProgress());

        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());

        UserProgress saved = userProgressRepository.save(progress);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lesson marked as complete", saved));
    }
}
