package com.blinders.blinders.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.blinders.blinders.config.StringListConverter;
import com.blinders.blinders.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false, columnDefinition = "TEXT")
    private String text;

    @Convert(converter = StringListConverter.class)
    @Column(name = "options", nullable = false, columnDefinition = "TEXT")
    private List<String> options;

    @Column(name = "correct_answer_index", nullable = false)
    private Integer correctAnswerIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    @JsonIgnore
    private Skill skill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    @JsonIgnore
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;
}
