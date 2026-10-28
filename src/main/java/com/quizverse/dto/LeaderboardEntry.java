package com.quizverse.dto;

import java.time.Instant;

public record LeaderboardEntry(
        String attemptId,
        String studentName,
        int score,
        int total,
        int percentage,
        int durationSeconds,
        Instant submittedAt,
        boolean current
) {
}
