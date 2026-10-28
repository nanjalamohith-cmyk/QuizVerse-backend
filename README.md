# QuizVerse — Backend

Spring Boot REST API for QuizVerse. It serves quiz questions without answers, validates submissions, calculates the score, and persists attempts in MongoDB.

## Stack

Java 17, Spring Boot 3.2, Spring Data MongoDB, Bean Validation.

## Run

Prerequisites: Java 17+, Maven 3.9+, and MongoDB on `mongodb://localhost:27017`.

```bash
mvn spring-boot:run
```

The API starts on **http://localhost:8081** (8080 is often occupied by local servers). The frontend lives at [QuizVerse-frontend](https://github.com/nanjalamohith-cmyk/QuizVerse-frontend).

| Variable | Default | Purpose |
| --- | --- | --- |
| `MONGODB_URI` | `mongodb://localhost:27017/quizverse` | MongoDB connection |
| `SERVER_PORT` | `8081` | HTTP port |

## API

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/quizzes` | List available quizzes |
| `GET` | `/api/quizzes/{id}` | Questions and options — no correct answers |
| `POST` | `/api/quizzes/{id}/submissions` | Validate answers, compute score, persist attempt |
| `GET` | `/api/quizzes/{id}/leaderboard` | Top 8 attempts and total attempts |

Example submission:

```bash
curl -X POST http://localhost:8081/api/quizzes/<quizId>/submissions \
  -H "Content-Type: application/json" \
  -d '{"studentName":"Nova","answers":{"q1":"b","q2":"c","q3":"b","q4":"c","q5":"c","q6":"b"},"durationSeconds":83}'
```

The response contains `score`, `total`, `percentage`, `rank`, per-question `reviews` with the correct option and explanation, and the refreshed leaderboard.

## Data & scoring

- `quizzes` stores the quiz with embedded questions, options, correct option ids, and explanations.
- `attempts` stores the student name, answers, score, duration, and timestamp.
- Every submission is validated (unknown quiz, question, or option ids are rejected) and scored on the server. Correct answers are only returned in the submission response.
- The app seeds one quiz (**Cosmic Origins**, six questions) and three demo attempts when the `attempts` collection is empty.

## Tests

```bash
mvn test
```

Integration tests run against a separate `quizverse_test` database and cover answer hiding, scoring (perfect, partial, skipped), invalid references, unknown quizzes, and leaderboard persistence.
