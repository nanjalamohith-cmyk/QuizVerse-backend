package com.quizverse.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record SubmissionRequest(
        @Size(max = 40, message = "Call sign must be 40 characters or fewer")
        String studentName,

        @NotNull(message = "Answers payload is required")
        Map<String, String> answers,

        @Min(value = 0, message = "Duration cannot be negative")
        @Max(value = 86400, message = "Duration is too large")
        Integer durationSeconds
) {
}
