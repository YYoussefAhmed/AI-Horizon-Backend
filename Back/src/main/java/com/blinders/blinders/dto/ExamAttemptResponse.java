package com.blinders.blinders.dto;

import com.blinders.blinders.enums.ExamType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamAttemptResponse {
    @Schema(example = "1")
    private Long id;
    
    @Schema(example = "85")
    private Integer score;
    
    @Schema(example = "true")
    private Boolean passed;
    
    @Schema(example = "2026-03-12T03:44:00")
    private LocalDateTime attemptedAt;
    
    @Schema(example = "PLACEMENT")
    private ExamType examType;
    
    @Schema(example = "EASY")
    private String levelName;
}
