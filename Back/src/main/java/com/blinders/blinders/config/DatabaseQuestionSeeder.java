package com.blinders.blinders.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.blinders.blinders.entity.*;
import com.blinders.blinders.enums.*;
import com.blinders.blinders.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseQuestionSeeder implements CommandLineRunner {

    private final LevelRepository levelRepository;
    private final SkillRepository skillRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        List<Question> practiceQuestions = questionRepository.findByQuestionType(QuestionType.PRACTICE);
        
        // Auto-purge if we detect the old unclassified "all-speaking" import
        boolean needsReclassification = !practiceQuestions.isEmpty() && 
             practiceQuestions.stream().allMatch(q -> q.getSkill().getName() == SkillType.SPEAKING);
             
        if (needsReclassification) {
             long count = practiceQuestions.size();
             log.info("Detected {} old uni-skill questions. Purging before re-seed.", count);
             questionRepository.deleteAll(practiceQuestions);
             practiceQuestions.clear();
             // We also clear out auto-generated lessons because there were very few, 
             // but to be safe we just delete any lessons that were linked to SPEAKING and had no user progress
             // For simplicity, we'll let them be and just create the new accurate ones.
        } else if (practiceQuestions.size() > 50) {
            log.info("Database properly seeded and fully classified with AI questions.");
            return;
        }

        File jsonFile = new File("../database/questions.json");
        if (!jsonFile.exists()) {
             jsonFile = new File("database/questions.json");
        }
        if (!jsonFile.exists()) {
             jsonFile = new File("../../database/questions.json");
        }

        if (jsonFile.exists()) {
            log.info("Seeding structured questions from {}", jsonFile.getAbsolutePath());
            JsonNode root = objectMapper.readTree(jsonFile);
            
            seedLevel(root, "beginner", LevelType.EASY);
            seedLevel(root, "intermediate", LevelType.MEDIUM);
            seedLevel(root, "advanced", LevelType.ADVANCED);
            seedLevel(root, "native", LevelType.NATIVE);
            
            log.info("Seeding complete. Total questions in DB: " + questionRepository.count());
        } else {
            log.warn("Could not find database/questions.json!");
        }
    }

    private SkillType determineSkillType(String title) {
        if (title == null) return SkillType.GRAMMAR;
        String lower = title.toLowerCase();
        
        if (lower.contains("vocab") || lower.contains("word") || lower.contains("routine") 
            || lower.contains("food") || lower.contains("feeling")) {
            return SkillType.VOCABULARY;
        } else if (lower.contains("read") || lower.contains("listen") || lower.contains("hear") || lower.contains("comprehension")) {
            return SkillType.LISTENING;
        } else if (lower.contains("speak") || lower.contains("pronounce") || lower.contains("conversation")) {
            return SkillType.SPEAKING;
        } else {
            // "grammar", "verb", "present", "preposition", "pronoun", "comparatives", etc
            return SkillType.GRAMMAR;
        }
    }

    private void seedLevel(JsonNode root, String key, LevelType levelType) {
        if (!root.has(key)) return;

        Level level = levelRepository.findByName(levelType)
                .orElseGet(() -> {
                    Level l = new Level();
                    l.setName(levelType);
                    l.setPassingScore(70);
                    return levelRepository.save(l);
                });

        JsonNode items = root.get(key);
        Lesson currentLesson = null;
        Skill currentSkill = null;
        int lessonCount = 1;

        for (JsonNode item : items) {
            if (item.has("is_lesson_intro") || item.has("is_section_intro")) {
                String lessonName = item.has("lesson_name") ? item.get("lesson_name").asText() : 
                                   (item.has("section_name") ? item.get("section_name").asText() : "Lesson " + lessonCount);
                
                SkillType targetSkillType = determineSkillType(lessonName);
                
                // Fetch or create the specific Skill (Grammar, Vocab, etc) for this Level
                currentSkill = skillRepository.findByLevelId(level.getId()).stream()
                        .filter(s -> s.getName() == targetSkillType)
                        .findFirst()
                        .orElseGet(() -> {
                            Skill s = new Skill();
                            s.setName(targetSkillType);
                            s.setLevel(level);
                            return skillRepository.save(s);
                        });

                currentLesson = new Lesson();
                currentLesson.setTitle(lessonName);
                currentLesson.setContent(item.has("question") ? item.get("question").asText() : "Introduction");
                currentLesson.setSkill(currentSkill);
                currentLesson.setOrder(lessonCount++);
                currentLesson = lessonRepository.save(currentLesson);
                
            } else if (item.has("question") && !item.has("is_reading_text") && currentSkill != null) {
                Question q = new Question();
                q.setText(item.get("question").asText());
                q.setLevel(level);
                q.setSkill(currentSkill);
                q.setQuestionType(QuestionType.PRACTICE); 

                List<String> optionsList = new ArrayList<>();
                int correctIdx = 0;
                
                JsonNode optionsNode = item.get("options");
                if (optionsNode != null && optionsNode.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = optionsNode.fields();
                    int i = 0;
                    String correctChar = item.has("answer") ? item.get("answer").asText().toLowerCase().trim() : "";
                    
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> f = fields.next();
                        optionsList.add(f.getValue().asText());
                        if (f.getKey().toLowerCase().trim().equals(correctChar)) {
                            correctIdx = i;
                        }
                        i++;
                    }
                }

                if (optionsList.isEmpty()) {
                    optionsList.add("True");
                    optionsList.add("False");
                }
                
                q.setOptions(optionsList);
                q.setCorrectAnswerIndex(correctIdx);
                questionRepository.save(q);
            }
        }
    }
}
