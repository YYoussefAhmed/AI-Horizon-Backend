package com.blinders.blinders.dto;

import com.blinders.blinders.enums.LevelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResult {
    @Schema(example = "100", description = "Score percentage")
    private Integer score;
    
    @Schema(example = "4", description = "Total number of questions")
    private Integer totalQuestions;
    
    @Schema(example = "4", description = "Number of correct answers")
    private Integer correctAnswers;
    
    @Schema(example = "true", description = "Whether the user passed the exam")
    private Boolean passed;
    
    @Schema(example = "NATIVE", description = "The level assigned to the user based on result")
    private LevelType assignedLevel;
}
