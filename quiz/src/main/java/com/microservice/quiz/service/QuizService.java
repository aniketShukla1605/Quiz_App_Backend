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
    public ResponseEntity<String> createQuiz(String category, int numOfQ, String title, Integer duration) {

        List<Integer> questions = quizInterface.generateQuestions(category, numOfQ).getBody();
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setCategory(category);
        quiz.setQuestionId(questions);

        if(duration != null) {quiz.setDurationMinutes(duration);}

        quizRepo.save(quiz);

        return new ResponseEntity<>("Question added successfully", HttpStatus.CREATED);
    }

    //Method to get all the questions of a quiz
    public ResponseEntity<List<QuestionDto>> getQuizQuestions(int id) {
        Quiz quiz = quizRepo.findById(id).orElseThrow(()-> new RuntimeException("Quiz id " + id + " not found"));
        List<Integer> questionId = quiz.getQuestionId();
        ResponseEntity<List<QuestionDto>> questions = quizInterface.getQuestionsFromId(questionId);
        return questions;
    }

    public ResponseEntity<String> createCustomQuiz(CustomQuizDto dto) {
        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle());
        quiz.setCategory(dto.getCategory());
        quiz.setQuestionId(dto.getQuestionIds());
        if(dto.getDuration() != null) {quiz.setDurationMinutes(dto.getDuration());}
        quizRepo.save(quiz);
        return new ResponseEntity<>("Custom quiz created successfully", HttpStatus.CREATED);
    }

    //calculate the result
    //no longer in use
    public ResponseEntity<Integer> calculateResult(int id, List<Response> responses, String userId) {
        ResponseEntity<Integer> correct = quizInterface.getScore(responses);

        try {
            Quiz quiz = quizRepo.findById(id).get();
            profileInterface.recordQuizResult(new QuizResultEvent(
                    userId,
                    id,
                    correct.getBody(),
                    quiz.getQuestionId().size()
            ));
        } catch (Exception ignored) {}

        return correct;
    }
}
