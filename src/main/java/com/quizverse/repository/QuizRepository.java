package com.quizverse.repository;

import com.quizverse.domain.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface QuizRepository extends MongoRepository<Quiz, String> {

    Optional<Quiz> findBySlug(String slug);
}
