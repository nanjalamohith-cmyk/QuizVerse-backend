package com.quizverse.service;

import com.quizverse.domain.Attempt;
import com.quizverse.domain.Option;
import com.quizverse.domain.Question;
import com.quizverse.domain.Quiz;
import com.quizverse.dto.LeaderboardEntry;
import com.quizverse.dto.LeaderboardView;
import com.quizverse.dto.OptionView;
import com.quizverse.dto.QuestionReview;
import com.quizverse.dto.QuestionView;
import com.quizverse.dto.QuizSummary;
import com.quizverse.dto.QuizView;
import com.quizverse.dto.SubmissionRequest;
import com.quizverse.dto.SubmissionResponse;
import com.quizverse.exception.InvalidSubmissionException;
import com.quizverse.exception.QuizNotFoundException;
import com.quizverse.repository.AttemptRepository;
import com.quizverse.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);
    private static final int MAX_CALL_SIGN_LENGTH = 24;

    private final QuizRepository quizRepository;
    private final AttemptRepository attemptRepository;

    public QuizService(QuizRepository quizRepository, AttemptRepository attemptRepository) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
    }

    public List<QuizSummary> listQuizzes() {
        return quizRepository.findAll().stream()
                .map(quiz -> new QuizSummary(
                        quiz.getId(),
                        quiz.getTitle(),
                        quiz.getTagline(),
                        quiz.getQuestions().size(),
                        quiz.getEstimatedMinutes()))
                .toList();
    }

    public QuizView getQuiz(String quizId) {
        Quiz quiz = findQuiz(quizId);
        List<QuestionView> questions = quiz.getQuestions().stream()
                .map(question -> new QuestionView(
                        question.getId(),
                        question.getCategory(),
                        question.getPrompt(),
                        toOptionViews(question)))
                .toList();
        return new QuizView(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getTagline(),
                quiz.getDescription(),
                quiz.getEstimatedMinutes(),
                questions.size(),
                questions);
    }

    public SubmissionResponse submit(String quizId, SubmissionRequest request) {
        Quiz quiz = findQuiz(quizId);
        Map<String, String> answers = request.answers() == null ? Map.of() : request.answers();
        Map<String, Question> questionsById = quiz.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        validateAnswers(answers, questionsById);

        int total = quiz.getQuestions().size();
        int score = 0;
        List<QuestionReview> reviews = new ArrayList<>(total);

        for (Question question : quiz.getQuestions()) {
            String selectedOptionId = answers.get(question.getId());
            boolean correct = question.getCorrectOptionId().equals(selectedOptionId);
            if (correct) {
                score++;
            }
            reviews.add(new QuestionReview(
                    question.getId(),
                    question.getCategory(),
                    question.getPrompt(),
                    toOptionViews(question),
                    selectedOptionId,
                    question.getCorrectOptionId(),
                    correct,
                    question.getExplanation()));
        }

        int durationSeconds = request.durationSeconds() == null ? 0 : request.durationSeconds();
        String studentName = normalizeCallSign(request.studentName());

        Attempt attempt = attemptRepository.save(new Attempt(
                quiz.getId(),
                studentName,
                score,
                total,
                durationSeconds,
                answers,
                Instant.now()));

        int totalAttempts = (int) attemptRepository.countByQuizId(quiz.getId());
        int rank = (int) attemptRepository.countByQuizIdAndScoreGreaterThan(quiz.getId(), score) + 1;

        log.info("Attempt {} by '{}' scored {}/{} in {}s", attempt.getId(), studentName, score, total, durationSeconds);

        return new SubmissionResponse(
                attempt.getId(),
                studentName,
                score,
                total,
                percentage(score, total),
                rank,
                totalAttempts,
                durationSeconds,
                attempt.getSubmittedAt(),
                reviews,
                toLeaderboard(quiz, attempt.getId()));
    }

    public LeaderboardView getLeaderboard(String quizId) {
        Quiz quiz = findQuiz(quizId);
        return new LeaderboardView(
                quiz.getId(),
                (int) attemptRepository.countByQuizId(quiz.getId()),
                toLeaderboard(quiz, null));
    }

    private void validateAnswers(Map<String, String> answers, Map<String, Question> questionsById) {
        for (Map.Entry<String, String> entry : answers.entrySet()) {
            Question question = questionsById.get(entry.getKey());
            if (question == null) {
                throw new InvalidSubmissionException("Unknown question reference: " + entry.getKey());
            }
            boolean optionExists = question.getOptions().stream()
                    .anyMatch(option -> option.getId().equals(entry.getValue()));
            if (!optionExists) {
                throw new InvalidSubmissionException(
                        "Unknown option reference for question " + question.getId());
            }
        }
    }

    private List<LeaderboardEntry> toLeaderboard(Quiz quiz, String currentAttemptId) {
        int total = quiz.getQuestions().size();
        return attemptRepository
                .findTop8ByQuizIdOrderByScoreDescDurationSecondsAscSubmittedAtAsc(quiz.getId())
                .stream()
                .map(attempt -> new LeaderboardEntry(
                        attempt.getId(),
                        attempt.getStudentName(),
                        attempt.getScore(),
                        total,
                        percentage(attempt.getScore(), total),
                        attempt.getDurationSeconds(),
                        attempt.getSubmittedAt(),
                        attempt.getId().equals(currentAttemptId)))
                .toList();
    }

    private Quiz findQuiz(String quizId) {
        try {
            return quizRepository.findById(quizId)
                    .orElseThrow(() -> new QuizNotFoundException(quizId));
        } catch (IllegalArgumentException ex) {
            throw new QuizNotFoundException(quizId);
        }
    }

    private List<OptionView> toOptionViews(Question question) {
        return question.getOptions().stream()
                .map(option -> new OptionView(option.getId(), option.getText()))
                .toList();
    }

    private String normalizeCallSign(String rawName) {
        if (rawName == null) {
            return "Anonymous";
        }
        String cleaned = rawName
                .replaceAll("\\p{Cntrl}", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) {
            return "Anonymous";
        }
        return cleaned.length() > MAX_CALL_SIGN_LENGTH
                ? cleaned.substring(0, MAX_CALL_SIGN_LENGTH).trim()
                : cleaned;
    }

    private int percentage(int score, int total) {
        return total == 0 ? 0 : (int) Math.round(score * 100.0 / total);
    }
}
