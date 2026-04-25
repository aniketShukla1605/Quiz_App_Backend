package com.microservice.question.repository;

import com.microservice.question.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface QuestionRepo extends JpaRepository<Question,Integer> {
    //Specific category of questions
    List<Question> findByCategory(String category);

    //Find Random Questions for Quiz
    @Query(value = "SELECT q.id FROM question q WHERE q.category = :category ORDER BY RAND() LIMIT :numOfQ", nativeQuery = true)
    List<Integer> findRandomQuestionsByCategory(String category, int numOfQ);
}
