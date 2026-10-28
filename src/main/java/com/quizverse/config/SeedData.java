package com.quizverse.config;

import com.quizverse.domain.Attempt;
import com.quizverse.domain.Option;
import com.quizverse.domain.Question;
import com.quizverse.domain.Quiz;
import com.quizverse.repository.AttemptRepository;
import com.quizverse.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SeedData implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);

    private final QuizRepository quizRepository;
    private final AttemptRepository attemptRepository;

    public SeedData(QuizRepository quizRepository, AttemptRepository attemptRepository) {
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
    }

    @Override
    public void run(String... args) {
        Quiz quiz = quizRepository.findBySlug("cosmic-origins")
                .orElseGet(() -> quizRepository.save(cosmicOriginsQuiz()));

        if (attemptRepository.count() == 0) {
            seedDemoAttempts(quiz);
        }

        log.info("QuizVerse ready: quiz '{}' with {} questions, {} attempts",
                quiz.getTitle(), quiz.getQuestions().size(), attemptRepository.countByQuizId(quiz.getId()));
    }

    private Quiz cosmicOriginsQuiz() {
        List<Question> questions = List.of(
                new Question("q1", "Planetary Science",
                        "Mars hosts Olympus Mons, the largest volcano in the solar system. Roughly how tall is it?",
                        options("About 8 km", "About 22 km", "About 45 km", "About 90 km"),
                        "b",
                        "Olympus Mons rises roughly 22 km above the Martian surface — about 2.5 times the height of Mount Everest."),
                new Question("q2", "Space Telescopes",
                        "The James Webb Space Telescope observes the universe primarily in which part of the spectrum?",
                        options("Ultraviolet", "Visible light", "Infrared", "X-ray"),
                        "c",
                        "Webb's infrared instruments see through cosmic dust and capture the redshifted light of the earliest galaxies."),
                new Question("q3", "Physics",
                        "Roughly how long does sunlight take to travel from the Sun to Earth?",
                        options("8 seconds", "8 minutes", "8 hours", "8 days"),
                        "b",
                        "Light crosses the ~150 million km between the Sun and Earth in about 8 minutes and 20 seconds."),
                new Question("q4", "Earth Science",
                        "Which gas makes up the largest share of Earth's atmosphere?",
                        options("Oxygen", "Carbon dioxide", "Nitrogen", "Argon"),
                        "c",
                        "Nitrogen accounts for roughly 78% of the atmosphere, while oxygen makes up about 21%."),
                new Question("q5", "Particle Physics",
                        "Which subatomic particle carries a negative electric charge?",
                        options("Proton", "Neutron", "Electron", "Positron"),
                        "c",
                        "Electrons carry one negative elementary charge; protons are positive and neutrons have no charge."),
                new Question("q6", "Astrophysics",
                        "What is the boundary around a black hole beyond which nothing can escape called?",
                        options("Photon sphere", "Event horizon", "Accretion disk", "Singularity"),
                        "b",
                        "At the event horizon, escape velocity exceeds the speed of light, so nothing — not even light — can return."));

        return new Quiz(
                "cosmic-origins",
                "Cosmic Origins",
                "Six questions. One orbit.",
                "An expedition through planetary science, quantum physics, and the deep universe. "
                        + "Answers are scored the instant you transmit them — no hints, no second chances until the next run.",
                4,
                questions);
    }

    private void seedDemoAttempts(Quiz quiz) {
        Instant now = Instant.now();
        attemptRepository.saveAll(List.of(
                new Attempt(quiz.getId(), "Nova", 6, 6, 104, correctAnswers(quiz, 6),
                        now.minus(12, ChronoUnit.MINUTES)),
                new Attempt(quiz.getId(), "Kepler", 5, 6, 132, correctAnswers(quiz, 5),
                        now.minus(47, ChronoUnit.MINUTES)),
                new Attempt(quiz.getId(), "Vega", 4, 6, 158, correctAnswers(quiz, 4),
                        now.minus(3, ChronoUnit.HOURS))));
        log.info("Seeded {} demo attempts for the live leaderboard", 3);
    }

    private Map<String, String> correctAnswers(Quiz quiz, int count) {
        Map<String, String> answers = new LinkedHashMap<>();
        quiz.getQuestions().stream()
                .limit(count)
                .forEach(question -> answers.put(question.getId(), question.getCorrectOptionId()));
        return answers;
    }

    private List<Option> options(String a, String b, String c, String d) {
        return List.of(new Option("a", a), new Option("b", b), new Option("c", c), new Option("d", d));
    }
}
