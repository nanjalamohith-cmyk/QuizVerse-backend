package com.quizverse.exception;

public class QuizNotFoundException extends RuntimeException {

    public QuizNotFoundException(String quizId) {
        super("Quiz " + quizId + " does not exist");
    }
}
