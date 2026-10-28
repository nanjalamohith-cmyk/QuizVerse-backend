package com.quizverse.domain;

import java.util.List;

public class Question {

    private String id;
    private String category;
    private String prompt;
    private List<Option> options;
    private String correctOptionId;
    private String explanation;

    protected Question() {
    }

    public Question(String id, String category, String prompt, List<Option> options, String correctOptionId,
                    String explanation) {
        this.id = id;
        this.category = category;
        this.prompt = prompt;
        this.options = List.copyOf(options);
        this.correctOptionId = correctOptionId;
        this.explanation = explanation;
    }

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getPrompt() {
        return prompt;
    }

    public List<Option> getOptions() {
        return options;
    }

    public String getCorrectOptionId() {
        return correctOptionId;
    }

    public String getExplanation() {
        return explanation;
    }
}
