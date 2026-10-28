package com.quizverse.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document("attempts")
@CompoundIndex(name = "quiz_leaderboard", def = "{'quizId': 1, 'score': -1, 'durationSeconds': 1}")
public class Attempt {

    @Id
    private String id;

    private String quizId;
    private String studentName;
    private int score;
    private int total;
    private int durationSeconds;
    private Map<String, String> answers;
    private Instant submittedAt;

    protected Attempt() {
    }

    public Attempt(String quizId, String studentName, int score, int total, int durationSeconds,
                   Map<String, String> answers, Instant submittedAt) {
        this.quizId = quizId;
        this.studentName = studentName;
        this.score = score;
        this.total = total;
        this.durationSeconds = durationSeconds;
        this.answers = Map.copyOf(answers);
        this.submittedAt = submittedAt;
    }

    public String getId() {
        return id;
    }

    public String getQuizId() {
        return quizId;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getScore() {
        return score;
    }

    public int getTotal() {
        return total;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public Map<String, String> getAnswers() {
        return answers;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }
}
