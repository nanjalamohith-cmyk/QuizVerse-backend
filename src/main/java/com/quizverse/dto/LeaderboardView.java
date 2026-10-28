package com.quizverse.dto;

import java.util.List;

public record LeaderboardView(
        String quizId,
        int totalAttempts,
        List<LeaderboardEntry> entries
) {
}
