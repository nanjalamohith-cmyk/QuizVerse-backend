package com.quizverse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizverse.domain.Question;
import com.quizverse.domain.Quiz;
import com.quizverse.repository.AttemptRepository;
import com.quizverse.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "spring.data.mongodb.uri=mongodb://localhost:27017/quizverse_test")
class QuizApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private AttemptRepository attemptRepository;

    private Quiz quiz;

    @BeforeEach
    void setUp() {
        attemptRepository.deleteAll();
        quiz = quizRepository.findBySlug("cosmic-origins").orElseThrow();
    }

    @Test
    void quizPayloadNeverExposesCorrectAnswers() throws Exception {
        mockMvc.perform(get("/api/quizzes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isString())
                .andExpect(jsonPath("$[0].questionCount").value(6));

        String payload = mockMvc.perform(get("/api/quizzes/{id}", quiz.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions.length()").value(6))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(payload).doesNotContain("correctOptionId", "explanation", "\"correct\"");
        assertThat(quiz.getQuestions().size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void scoresAPerfectSubmissionOnTheServer() throws Exception {
        Map<String, String> answers = new LinkedHashMap<>();
        quiz.getQuestions().forEach(q -> answers.put(q.getId(), q.getCorrectOptionId()));

        mockMvc.perform(post("/api/quizzes/{id}/submissions", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionJson("Nova Prime", answers, 83)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(6))
                .andExpect(jsonPath("$.total").value(6))
                .andExpect(jsonPath("$.percentage").value(100))
                .andExpect(jsonPath("$.rank").value(1))
                .andExpect(jsonPath("$.studentName").value("Nova Prime"))
                .andExpect(jsonPath("$.reviews[0].correct").value(true))
                .andExpect(jsonPath("$.reviews[0].correctOptionId").isString())
                .andExpect(jsonPath("$.leaderboard[0].studentName").value("Nova Prime"))
                .andExpect(jsonPath("$.leaderboard[0].current").value(true));
    }

    @Test
    void marksWrongAndUnansweredQuestionsAsIncorrect() throws Exception {
        Map<String, String> answers = new LinkedHashMap<>();
        Question first = quiz.getQuestions().get(0);
        String wrongOption = first.getOptions().stream()
                .map(option -> option.getId())
                .filter(id -> !id.equals(first.getCorrectOptionId()))
                .findFirst()
                .orElseThrow();
        answers.put(first.getId(), wrongOption);

        quiz.getQuestions().subList(1, quiz.getQuestions().size() - 1)
                .forEach(q -> answers.put(q.getId(), q.getCorrectOptionId()));

        mockMvc.perform(post("/api/quizzes/{id}/submissions", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionJson("   ", answers, 42)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.score").value(4))
                .andExpect(jsonPath("$.percentage").value(67))
                .andExpect(jsonPath("$.studentName").value("Anonymous"))
                .andExpect(jsonPath("$.reviews[0].correct").value(false))
                .andExpect(jsonPath("$.reviews[0].selectedOptionId").value(wrongOption))
                .andExpect(jsonPath("$.reviews[5].correct").value(false))
                .andExpect(jsonPath("$.reviews[5].selectedOptionId").value(nullValue()));
    }

    @Test
    void rejectsUnknownOptionReferences() throws Exception {
        Map<String, String> answers = Map.of(quiz.getQuestions().get(0).getId(), "zzz");

        mockMvc.perform(post("/api/quizzes/{id}/submissions", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionJson("Rogue", answers, 10)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Unknown option reference for question " + quiz.getQuestions().get(0).getId()));
    }

    @Test
    void returnsNotFoundForUnknownQuiz() throws Exception {
        mockMvc.perform(get("/api/quizzes/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Quiz does-not-exist does not exist"));

        mockMvc.perform(get("/api/quizzes/does-not-exist/leaderboard"))
                .andExpect(status().isNotFound());
    }

    @Test
    void exposesLeaderboardOfSubmittedAttempts() throws Exception {
        Map<String, String> answers = Map.of(quiz.getQuestions().get(0).getId(),
                quiz.getQuestions().get(0).getCorrectOptionId());

        mockMvc.perform(post("/api/quizzes/{id}/submissions", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submissionJson("Orion", answers, 61)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/quizzes/{id}/leaderboard", quiz.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAttempts").value(1))
                .andExpect(jsonPath("$.entries[0].studentName").value("Orion"))
                .andExpect(jsonPath("$.entries[0].score").value(1))
                .andExpect(jsonPath("$.entries[0].current").value(false));
    }

    private String submissionJson(String name, Map<String, String> answers, int durationSeconds) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "studentName", name,
                "answers", answers,
                "durationSeconds", durationSeconds));
    }
}
