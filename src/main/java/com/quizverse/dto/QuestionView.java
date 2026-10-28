package com.quizverse.dto;

import java.util.List;

public record QuestionView(
        String id,
        String category,
        String prompt,
        List<OptionView> options
) {
}
