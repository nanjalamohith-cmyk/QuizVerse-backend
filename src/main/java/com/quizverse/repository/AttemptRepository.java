package com.quizverse.repository;

import com.quizverse.domain.Attempt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AttemptRepository extends MongoRepository<Attempt, String> {

    long countByQuizId(String quizId);

    long countByQuizIdAndScoreGreaterThan(String quizId, int score);

    List<Attempt> findTop8ByQuizIdOrderByScoreDescDurationSecondsAscSubmittedAtAsc(String quizId);
}
