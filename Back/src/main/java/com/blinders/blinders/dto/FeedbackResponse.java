package com.blinders.blinders.dto;

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
public class FeedbackResponse {
    @Schema(example = "1")
    private Long id;
    
    @Schema(example = "youssef")
    private String userName;
    
    @Schema(example = "EASY")
    private String levelName;
    
    @Schema(example = "5")
    private Integer rating;
    
    @Schema(example = "2026-03-12T05:10:00")
    private LocalDateTime createdAt;
}
