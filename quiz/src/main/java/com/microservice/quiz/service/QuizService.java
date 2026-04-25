package com.microservice.quiz.service;

import com.microservice.quiz.dto.QuestionDto;
import com.microservice.quiz.dto.Response;
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

    //To create a random Quiz
    public ResponseEntity<String> createQuiz(String category, int numOfQ, String title) {

        List<Integer> questions = quizInterface.generateQuestions(category,numOfQ).getBody();
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setCategory(category);
        quiz.setQuestionId(questions);
        quizRepo.save(quiz);

        return new ResponseEntity<>("Question added successfully", HttpStatus.CREATED);
    }

    //Method to get all the questions of a quiz
    public ResponseEntity<List<QuestionDto>> getQuizQuestions(int id) {
        Quiz quiz = quizRepo.findById(id).get();
        List<Integer> questionId = quiz.getQuestionId();
        ResponseEntity<List<QuestionDto>> questions = quizInterface.getQuestionsFromId(questionId);
        return questions;
    }

     //to calculate the result
    public ResponseEntity<Integer> calculateResult(int id, List<Response> responses) {
        ResponseEntity<Integer> correct = quizInterface.getScore(responses);
        return correct;
    }
}
