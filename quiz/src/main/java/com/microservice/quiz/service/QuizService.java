package com.microservice.quiz.service;

import com.microservice.quiz.dto.CustomQuizDto;
import com.microservice.quiz.dto.QuestionDto;
import com.microservice.quiz.dto.QuizResultEvent;
import com.microservice.quiz.dto.Response;
import com.microservice.quiz.feign.ProfileInterface;
import com.microservice.quiz.feign.QuizInterface;
import com.microservice.quiz.model.Quiz;
import com.microservice.quiz.repository.QuizRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {
    @Autowired
    private QuizRepo quizRepo;
    @Autowired
    private QuizInterface quizInterface;
    @Autowired
    private ProfileInterface profileInterface;

    //To create a random Quiz
    @Caching(evict = {
            @CacheEvict(value = "quizQuestions", allEntries = true),
            @CacheEvict(value = "generatedQuestions", allEntries = true)
    })
    public String createQuiz(String category, int numOfQ, String title, Integer duration) {

        List<Integer> questions = quizInterface.generateQuestions(category, numOfQ).getBody();
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setCategory(category);
        quiz.setQuestionId(questions);

        if(duration != null) {quiz.setDurationMinutes(duration);}

        quizRepo.save(quiz);

        return "Question added successfully";
    }

    //Method to get all the questions of a quiz
    @Cacheable(value = "quizQuestions", key = "#id")
    public List<QuestionDto> getQuizQuestions(int id) {
        Quiz quiz = quizRepo.findById(id).orElseThrow(()-> new RuntimeException("Quiz id " + id + " not found"));
        List<Integer> questionId = quiz.getQuestionId();
        List<QuestionDto> questions = quizInterface.getQuestionsFromId(questionId).getBody();
        return questions;
    }

    @Caching(evict = {
            @CacheEvict(value = "quizQuestions", allEntries = true),
            @CacheEvict(value = "questionsFromId", allEntries = true)
    })
    public String createCustomQuiz(CustomQuizDto dto) {
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setCategory(dto.getCategory());
        quiz.setQuestionId(dto.getQuestionIds());
        if(dto.getDuration() != null) {quiz.setDurationMinutes(dto.getDuration());}
        quizRepo.save(quiz);
        return "Custom quiz created successfully";
    }

    //calculate the result
    //no longer in use
    public Integer calculateResult(int id, List<Response> responses, String userId) {
        Integer correct = quizInterface.getScore(responses).getBody();

        try {
            Quiz quiz = quizRepo.findById(id).get();
            profileInterface.recordQuizResult(new QuizResultEvent(
                    userId,
                    id,
                    correct,
                    quiz.getQuestionId().size()
            ));
        } catch (Exception ignored) {}

        return correct;
    }
}
