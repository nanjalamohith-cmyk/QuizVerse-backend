package com.quizverse.dto;

import java.util.List;

public record QuestionReview(
        String questionId,
        String category,
        String prompt,
        List<OptionView> options,
        String selectedOptionId,
        String correctOptionId,
        boolean correct,
        String explanation
) {
}
