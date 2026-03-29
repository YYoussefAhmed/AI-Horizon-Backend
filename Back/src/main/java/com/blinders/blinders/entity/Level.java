package com.blinders.blinders.entity;

import com.blinders.blinders.enums.LevelType;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "levels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    @Schema(example = "EASY")
    private LevelType name;

    @Column(name = "level_order", nullable = false)
    @Schema(example = "1")
    private Integer order;

    @Column(name = "description", length = 500)
    @Schema(example = "Start your journey here with basics.")
    private String description;

    @Column(name = "passing_score", nullable = false)
    @Schema(example = "70")
    private Integer passingScore = 70;

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Skill> skills;
}
