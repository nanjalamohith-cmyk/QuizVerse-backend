package com.quizverse.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("quizzes")
public class Quiz {

    @Id
    private String id;

    @Indexed(unique = true)
    private String slug;

    private String title;
    private String tagline;
    private String description;
    private int estimatedMinutes;
    private List<Question> questions;
    private Instant createdAt;

    protected Quiz() {
    }

    public Quiz(String slug, String title, String tagline, String description, int estimatedMinutes,
                List<Question> questions) {
        this.slug = slug;
        this.title = title;
        this.tagline = tagline;
        this.description = description;
        this.estimatedMinutes = estimatedMinutes;
        this.questions = List.copyOf(questions);
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public String getTagline() {
        return tagline;
    }

    public String getDescription() {
        return description;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
