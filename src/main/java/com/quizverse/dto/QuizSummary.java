package com.quizverse.dto;

import java.util.List;

public record QuizSummary(
        String id,
        String title,
        String tagline,
        int questionCount,
        int estimatedMinutes
) {
}
