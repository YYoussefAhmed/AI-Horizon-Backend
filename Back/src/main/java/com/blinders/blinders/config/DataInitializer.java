package com.blinders.blinders.config;

import com.blinders.blinders.entity.*;
import com.blinders.blinders.enums.*;
import com.blinders.blinders.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final LevelRepository levelRepository;
    private final SkillRepository skillRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (levelRepository.count() > 0) {
            System.out.println(">> Database already has data. Skipping initialization.");
            return;
        }

        // 1. Create Levels
        Level easy = createLevel(LevelType.EASY, 1, "Start your journey here with basics.", 70);
        Level medium = createLevel(LevelType.MEDIUM, 2, "Improve your skills with intermediate topics.", 70);
        Level advanced = createLevel(LevelType.ADVANCED, 3, "Master complex structures and deep vocabulary.", 75);
        Level nativeLvl = createLevel(LevelType.NATIVE, 4, "Think and speak like a native speaker.", 80);

        // 2. Create Skills for EASY level
        Skill vocab = createSkill(SkillType.VOCABULARY, easy);
        Skill grammar = createSkill(SkillType.GRAMMAR, easy);
        createSkill(SkillType.LISTENING, easy);

        // 3. Create Lessons
        createLesson("Greetings & Introductions", "Hello, Hi, My name is... Nice to meet you.", 1, vocab);
        createLesson("Numbers 1-10", "One, Two, Three, Four, Five, Six, Seven, Eight, Nine, Ten.", 2, vocab);
        createLesson("Basic Pronouns", "I, You, He, She, It, We, They.", 1, grammar);

        // 4. Placement Test Questions
        createQuestion("What is the opposite of 'Big'?", Arrays.asList("Small", "Fast", "Heavy", "New"), 0, QuestionType.PLACEMENT, null, null);
        createQuestion("Choose the correct sentence:", Arrays.asList("I am student", "I is a student", "I am a student", "I are a student"), 2, QuestionType.PLACEMENT, null, null);
        createQuestion("How do you say 'شكراً' in English?", Arrays.asList("Please", "Sorry", "Thank you", "Excuse me"), 2, QuestionType.PLACEMENT, null, null);
        createQuestion("What comes after Sunday?", Arrays.asList("Tuesday", "Saturday", "Friday", "Monday"), 3, QuestionType.PLACEMENT, null, null);

        // 5. Level Exam Questions for EASY
        createQuestion("Which of these is a number?", Arrays.asList("Apple", "Seven", "Red", "Cat"), 1, QuestionType.LEVEL_EXAM, null, easy);
        createQuestion("Hello, ____ name is John.", Arrays.asList("me", "I", "my", "mine"), 2, QuestionType.LEVEL_EXAM, null, easy);

        // 6. Level Exam Questions for MEDIUM
        createQuestion("Choose the correct past tense: 'Go'", Arrays.asList("Goed", "Went", "Gone", "Going"), 1, QuestionType.LEVEL_EXAM, null, medium);
        createQuestion("I have been ____ here for three years.", Arrays.asList("live", "lives", "living", "lived"), 2, QuestionType.LEVEL_EXAM, null, medium);

        // 7. Level Exam Questions for ADVANCED
        createQuestion("Identify the synonym for 'Meticulous'", Arrays.asList("Careless", "Quick", "Thorough", "Lazy"), 2, QuestionType.LEVEL_EXAM, null, advanced);
        createQuestion("Had I known, I ____ have come.", Arrays.asList("will", "would", "must", "should"), 1, QuestionType.LEVEL_EXAM, null, advanced);

        System.out.println(">> Database Initialized with more mock data!");
    }

    private Level createLevel(LevelType name, int order, String desc, int score) {
        Level level = new Level();
        level.setName(name);
        level.setOrder(order);
        level.setDescription(desc);
        level.setPassingScore(score);
        return levelRepository.save(level);
    }

    private Skill createSkill(SkillType type, Level level) {
        Skill skill = new Skill();
        skill.setName(type);
        skill.setLevel(level);
        return skillRepository.save(skill);
    }

    private void createLesson(String title, String content, int order, Skill skill) {
        Lesson lesson = new Lesson();
        lesson.setTitle(title);
        lesson.setContent(content);
        lesson.setOrder(order);
        lesson.setSkill(skill);
        lessonRepository.save(lesson);
    }

    private void createQuestion(String text, List<String> options, int correctIdx, QuestionType type, Skill skill, Level level) {
        Question q = new Question();
        q.setText(text);
        q.setOptions(options);
        q.setCorrectAnswerIndex(correctIdx);
        q.setQuestionType(type);
        q.setSkill(skill);
        q.setLevel(level);
        questionRepository.save(q);
    }
}
