package com.blinders.blinders.repository;

import com.blinders.blinders.entity.Question;
import com.blinders.blinders.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySkillId(Long skillId);

    List<Question> findByLevelIdAndQuestionType(Long levelId, QuestionType questionType);

    List<Question> findByQuestionType(QuestionType questionType);

    @Query(value = "SELECT * FROM questions WHERE question_type = 'PRACTICE' ORDER BY RAND() LIMIT 15", nativeQuery = true)
    List<Question> findRandomPracticeQuestions();
}
