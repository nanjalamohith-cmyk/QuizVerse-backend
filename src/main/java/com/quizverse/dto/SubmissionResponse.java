package com.quizverse.dto;

import java.time.Instant;
import java.util.List;

public record SubmissionResponse(
        String attemptId,
        String studentName,
        int score,
        int total,
        int percentage,
        int rank,
        int totalAttempts,
        int durationSeconds,
        Instant submittedAt,
        List<QuestionReview> reviews,
        List<LeaderboardEntry> leaderboard
) {
}
