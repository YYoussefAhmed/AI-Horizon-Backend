package com.blinders.blinders.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmission {
    @Schema(example = "{\"1\": 0, \"2\": 2, \"3\": 2, \"4\": 3}", description = "Map of question ID to selected option index")
    private Map<Long, Integer> answers;
}
