package com.microservice.quiz.service;

import com.microservice.quiz.dto.CustomQuizDto;
import com.microservice.quiz.dto.QuestionDto;
import com.microservice.quiz.dto.QuizResultEvent;
import com.microservice.quiz.dto.Response;
import com.microservice.quiz.exception.ResourceNotFoundException;
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

import java.util.ArrayList;
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
        Quiz quiz = quizRepo.findById(id).orElseThrow(()-> new ResourceNotFoundException("Quiz id " + id + " not found"));
        List<Integer> questionId = quiz.getQuestionId();
        if (questionId == null || questionId.isEmpty()) {
            return new ArrayList<>();
        }
        ResponseEntity<List<QuestionDto>> response = quizInterface.getQuestionsFromId(questionId);
        return (response != null && response.getBody() != null) ? response.getBody() : new ArrayList<>();
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
        Integer correct = 0;
        if (responses != null && !responses.isEmpty()) {
            ResponseEntity<Integer> scoreResp = quizInterface.getScore(responses);
            if (scoreResp != null && scoreResp.getBody() != null) {
                correct = scoreResp.getBody();
            }
        }

        try {
            Quiz quiz = quizRepo.findById(id).orElse(null);
            if (quiz != null && quiz.getQuestionId() != null) {
                profileInterface.recordQuizResult(new QuizResultEvent(
                        userId,
                        id,
                        correct,
                        quiz.getQuestionId().size()
                ));
            }
        } catch (Exception ignored) {}

        return correct;
    }
}
