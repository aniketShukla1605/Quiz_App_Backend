package com.microservice.quiz.repository;

import com.microservice.quiz.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepo extends JpaRepository<Quiz,Integer> {
    List<Quiz> findByTitleContainingIgnoreCase(String title);
}
