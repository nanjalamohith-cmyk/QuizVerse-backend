package com.quizverse.web;

import com.quizverse.dto.LeaderboardView;
import com.quizverse.dto.QuizSummary;
import com.quizverse.dto.QuizView;
import com.quizverse.dto.SubmissionRequest;
import com.quizverse.dto.SubmissionResponse;
import com.quizverse.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/quizzes")
    public List<QuizSummary> listQuizzes() {
        return quizService.listQuizzes();
    }

    @GetMapping("/quizzes/{quizId}")
    public QuizView getQuiz(@PathVariable String quizId) {
        return quizService.getQuiz(quizId);
    }

    @PostMapping("/quizzes/{quizId}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse submitQuiz(@PathVariable String quizId,
                                         @Valid @RequestBody SubmissionRequest request) {
        return quizService.submit(quizId, request);
    }

    @GetMapping("/quizzes/{quizId}/leaderboard")
    public LeaderboardView getLeaderboard(@PathVariable String quizId) {
        return quizService.getLeaderboard(quizId);
    }
}
