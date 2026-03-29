package com.blinders.blinders.controller;

import com.blinders.blinders.dto.*;
import com.blinders.blinders.entity.*;
import com.blinders.blinders.enums.ExamType;
import com.blinders.blinders.enums.LevelType;
import com.blinders.blinders.enums.QuestionType;
import com.blinders.blinders.repository.*;
import com.blinders.blinders.service.AiEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuestionRepository questionRepository;
    private final LevelRepository levelRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final UserRepository userRepository;
    private final AiEvaluationService aiEvaluationService;
    private final LessonRepository lessonRepository;
    private final UserProgressRepository userProgressRepository;

    @GetMapping("/placement-test")
    public ResponseEntity<ApiResponse<List<QuestionDto>>> getPlacementTest() {
        // Fetch 15 random practice questions from the AI JSON import
        List<Question> questions = questionRepository.findRandomPracticeQuestions();
        List<QuestionDto> dtos = questions.stream()
                .map(q -> new QuestionDto(q.getId(), q.getText(), q.getOptions()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Placement test questions", dtos));
    }

    @PostMapping("/placement-test/submit")
    public ResponseEntity<ApiResponse<QuizResult>> submitPlacementTest(
            @RequestBody QuizSubmission submission,
            @AuthenticationPrincipal User user) {

        // Dynamically grade the specific randomized questions the user received
        List<Question> questions = questionRepository.findAllById(submission.getAnswers().keySet());

        int correct = 0;
        for (Question q : questions) {
            Integer answer = submission.getAnswers().get(q.getId());
            if (answer != null && answer.equals(q.getCorrectAnswerIndex())) {
                correct++;
            }
        }

        int score = questions.isEmpty() ? 0 : (correct * 100) / questions.size();

        LevelType assignedLevel;
        if (score >= 90) {
            assignedLevel = LevelType.NATIVE;
        } else if (score >= 70) {
            assignedLevel = LevelType.ADVANCED;
        } else if (score >= 50) {
            assignedLevel = LevelType.MEDIUM;
        } else {
            assignedLevel = LevelType.EASY;
        }

        user.setCurrentLevel(assignedLevel);
        user.setPlacementTestCompleted(true);
        userRepository.save(user);

        ExamAttempt attempt = new ExamAttempt();
        attempt.setUser(user);
        attempt.setScore(score);
        attempt.setPassed(true);
        attempt.setExamType(ExamType.PLACEMENT);
        examAttemptRepository.save(attempt);

        QuizResult result = new QuizResult(score, questions.size(), correct, true, assignedLevel);
        return ResponseEntity.ok(new ApiResponse<>(true, "Placement test completed", result));
    }

    @GetMapping("/level-exam/{levelId}")
    public ResponseEntity<ApiResponse<List<QuestionDto>>> getLevelExam(@PathVariable Long levelId) {
        List<Question> questions = questionRepository.findByLevelIdAndQuestionType(levelId, QuestionType.LEVEL_EXAM);
        List<QuestionDto> dtos = questions.stream()
                .map(q -> new QuestionDto(q.getId(), q.getText(), q.getOptions()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, "Level exam questions", dtos));
    }

    @PostMapping("/level-exam/{levelId}/submit")
    public ResponseEntity<ApiResponse<QuizResult>> submitLevelExam(
            @PathVariable Long levelId,
            @RequestBody QuizSubmission submission,
            @AuthenticationPrincipal User user) {

        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found"));

        List<Question> questions = questionRepository.findByLevelIdAndQuestionType(levelId, QuestionType.LEVEL_EXAM);

        int correct = 0;
        for (Question q : questions) {
            Integer answer = submission.getAnswers().get(q.getId());
            if (answer != null && answer.equals(q.getCorrectAnswerIndex())) {
                correct++;
            }
        }

        int score = questions.isEmpty() ? 0 : (correct * 100) / questions.size();
        boolean passed = score >= level.getPassingScore();

        ExamAttempt attempt = new ExamAttempt();
        attempt.setUser(user);
        attempt.setLevel(level);
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setExamType(ExamType.LEVEL_EXAM);
        examAttemptRepository.save(attempt);

        LevelType nextLevel = null;
        if (passed) {
            LevelType currentLevel = level.getName();
            if (currentLevel == LevelType.EASY) {
                nextLevel = LevelType.MEDIUM;
            } else if (currentLevel == LevelType.MEDIUM) {
                nextLevel = LevelType.ADVANCED;
            } else if (currentLevel == LevelType.ADVANCED) {
                nextLevel = LevelType.NATIVE;
            }

            if (nextLevel != null) {
                user.setCurrentLevel(nextLevel);
                userRepository.save(user);
            }
        }

        QuizResult result = new QuizResult(score, questions.size(), correct, passed, nextLevel);
        return ResponseEntity
                .ok(new ApiResponse<>(true, passed ? "Exam passed!" : "Exam failed. Please retry.", result));
    }

    @GetMapping(value = "/question/{questionId}/audio", produces = "audio/mpeg")
    public ResponseEntity<byte[]> getQuestionAudio(@PathVariable Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
        
        String fullText = question.getText() + ". ";
        if (question.getOptions() != null) {
            for (String option : question.getOptions()) {
               fullText += option + ". ";
            }
        }
        
        byte[] audioBytes = aiEvaluationService.synthesizeSpeech(fullText);
        
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("audio/mpeg"))
                .body(audioBytes);
    }

    @PostMapping(value = "/question/{questionId}/submit-voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AiEvaluationService.AiEvaluationResult>> submitVoiceAnswer(
            @PathVariable Long questionId,
            @RequestParam("audio") MultipartFile audio,
            @AuthenticationPrincipal User user) throws IOException {
            
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
                
        String correctAnswerText = question.getOptions().get(question.getCorrectAnswerIndex());
        
        AiEvaluationService.AiEvaluationResult result = aiEvaluationService.evaluateVoice(audio, correctAnswerText);
        
        if (result.isWasCorrect() && user != null && question.getSkill() != null) {
            List<Lesson> lessons = lessonRepository.findBySkillIdOrderByOrderAsc(question.getSkill().getId());
            if (!lessons.isEmpty()) {
                Lesson targetLesson = lessons.get(0);
                boolean exists = userProgressRepository.findByUser_UserIdAndLesson_Id(user.getUserId(), targetLesson.getId()).isPresent();
                if (!exists) {
                    UserProgress progress = new UserProgress();
                    progress.setUser(user);
                    progress.setLesson(targetLesson);
                    progress.setCompleted(true);
                    progress.setCompletedAt(java.time.LocalDateTime.now());
                    userProgressRepository.save(progress);
                }
            }
        }
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Voice evaluated successfully", result));
    }
}
