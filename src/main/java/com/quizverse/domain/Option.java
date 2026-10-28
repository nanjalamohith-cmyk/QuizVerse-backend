package com.quizverse.domain;

public class Option {

    private String id;
    private String text;

    protected Option() {
    }

    public Option(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }
}
