package com.blinders.blinders.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1")
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    @Schema(example = "Greetings & Introductions")
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    @Schema(example = "Hello, Hi, My name is...")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    @JsonBackReference
    private Skill skill;

    @Column(name = "lesson_order", nullable = false)
    @Schema(example = "1")
    private Integer order;
}
