package com.blinders.blinders.controller;

import com.blinders.blinders.dto.ApiResponse;
import com.blinders.blinders.entity.*;
import com.blinders.blinders.enums.LevelType;
import com.blinders.blinders.enums.QuestionType;
import com.blinders.blinders.enums.Role;
import com.blinders.blinders.enums.SkillType;
import com.blinders.blinders.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final LevelRepository levelRepository;
    private final SkillRepository skillRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    @PostMapping("/levels")
    public ResponseEntity<ApiResponse<Level>> createLevel(@RequestBody Map<String, Object> request) {
        Level level = new Level();
        level.setName(LevelType.valueOf((String) request.get("name")));
        level.setOrder((Integer) request.get("order"));
        level.setDescription((String) request.get("description"));
        level.setPassingScore((Integer) request.getOrDefault("passingScore", 70));

        Level saved = levelRepository.save(level);
        return ResponseEntity.ok(new ApiResponse<>(true, "Level created", saved));
    }

    @GetMapping("/levels")
    public ResponseEntity<ApiResponse<List<Level>>> getAllLevels() {
        List<Level> levels = levelRepository.findAllByOrderByOrderAsc();
        return ResponseEntity.ok(new ApiResponse<>(true, "Levels retrieved", levels));
    }

    @PostMapping("/skills")
    public ResponseEntity<ApiResponse<Skill>> createSkill(@RequestBody Map<String, Object> request) {
        Skill skill = new Skill();
        skill.setName(SkillType.valueOf((String) request.get("name")));

        Long levelId = Long.valueOf(request.get("levelId").toString());
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found"));
        skill.setLevel(level);

        Skill saved = skillRepository.save(skill);
        return ResponseEntity.ok(new ApiResponse<>(true, "Skill created", saved));
    }

    @GetMapping("/skills")
    public ResponseEntity<ApiResponse<List<Skill>>> getAllSkills() {
        List<Skill> skills = skillRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Skills retrieved", skills));
    }

    @PostMapping("/lessons")
    public ResponseEntity<ApiResponse<Lesson>> createLesson(@RequestBody Map<String, Object> request) {
        Lesson lesson = new Lesson();
        lesson.setTitle((String) request.get("title"));
        lesson.setContent((String) request.get("content"));
        lesson.setOrder((Integer) request.get("order"));

        Long skillId = Long.valueOf(request.get("skillId").toString());
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
        lesson.setSkill(skill);

        Lesson saved = lessonRepository.save(lesson);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lesson created", saved));
    }

    @PutMapping("/lessons/{id}")
    public ResponseEntity<ApiResponse<Lesson>> updateLesson(@PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        if (request.containsKey("title"))
            lesson.setTitle((String) request.get("title"));
        if (request.containsKey("content"))
            lesson.setContent((String) request.get("content"));
        if (request.containsKey("order"))
            lesson.setOrder((Integer) request.get("order"));

        Lesson saved = lessonRepository.save(lesson);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lesson updated", saved));
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Long id) {
        lessonRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Lesson deleted", null));
    }

    @GetMapping("/lessons")
    public ResponseEntity<ApiResponse<List<Lesson>>> getAllLessons() {
        List<Lesson> lessons = lessonRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Lessons retrieved", lessons));
    }

    @PostMapping("/questions")
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResponse<Question>> createQuestion(@RequestBody Map<String, Object> request) {
        Question question = new Question();
        question.setText((String) request.get("text"));
        question.setOptions((List<String>) request.get("options"));
        question.setCorrectAnswerIndex((Integer) request.get("correctAnswerIndex"));
        question.setQuestionType(QuestionType.valueOf((String) request.get("questionType")));

        if (request.containsKey("skillId")) {
            Long skillId = Long.valueOf(request.get("skillId").toString());
            Skill skill = skillRepository.findById(skillId).orElse(null);
            question.setSkill(skill);
        }

        if (request.containsKey("levelId")) {
            Long levelId = Long.valueOf(request.get("levelId").toString());
            Level level = levelRepository.findById(levelId).orElse(null);
            question.setLevel(level);
        }

        Question saved = questionRepository.save(question);
        return ResponseEntity.ok(new ApiResponse<>(true, "Question created", saved));
    }

    @PutMapping("/questions/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<ApiResponse<Question>> updateQuestion(@PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (request.containsKey("text"))
            question.setText((String) request.get("text"));
        if (request.containsKey("options"))
            question.setOptions((List<String>) request.get("options"));
        if (request.containsKey("correctAnswerIndex"))
            question.setCorrectAnswerIndex((Integer) request.get("correctAnswerIndex"));

        Question saved = questionRepository.save(question);
        return ResponseEntity.ok(new ApiResponse<>(true, "Question updated", saved));
    }

    @DeleteMapping("/questions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long id) {
        questionRepository.deleteById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Question deleted", null));
    }

    @GetMapping("/questions")
    public ResponseEntity<ApiResponse<List<Question>>> getAllQuestions() {
        List<Question> questions = questionRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Questions retrieved", questions));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved", users));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<User>> updateUserRole(@PathVariable Long id,
            @RequestBody Map<String, String> request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.valueOf(request.get("role")));
        User saved = userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "User role updated", saved));
    }
}
