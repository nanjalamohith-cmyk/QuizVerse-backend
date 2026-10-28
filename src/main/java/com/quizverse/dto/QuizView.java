package com.quizverse.dto;

import java.util.List;

public record QuizView(
        String id,
        String title,
        String tagline,
        String description,
        int estimatedMinutes,
        int questionCount,
        List<QuestionView> questions
) {
}
