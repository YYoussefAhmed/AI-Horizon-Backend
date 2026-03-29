package com.blinders.blinders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    @Schema(example = "1")
    private Long id;
    
    @Schema(example = "What is the opposite of 'Big'?")
    private String text;
    
    @Schema(example = "[\"Small\", \"Fast\", \"Heavy\", \"New\"]")
    private List<String> options;
}
