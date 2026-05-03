package com.microservice.question.service;

import com.microservice.question.dto.CreateQuizWithQuestionsDto;
import com.microservice.question.dto.CustomQuizDto;
import com.microservice.question.dto.QuestionDto;
import com.microservice.question.dto.Response;
import com.microservice.question.feign.QuizServiceClient;
import com.microservice.question.model.Question;
import com.microservice.question.repository.QuestionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    @Autowired
    private QuestionRepo questionRepo;

    //All Questions
    public ResponseEntity<List<Question>> getAllQuestions() {
        try {
            return new ResponseEntity<>(questionRepo.findAll(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //Specific Category of Questions
    public ResponseEntity<List<Question>> getQuestionsByCategory(String category) {
        try {
            return new ResponseEntity<>(questionRepo.findByCategory(category), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //Add a question
    public String addQuestion(Question question) {
        questionRepo.save(question);
        return "Question added successfully";
    }

    public ResponseEntity<List<Integer>> generateQuestions(String category, int numberOfQuestions) {
        List<Integer> questions = questionRepo.findRandomQuestionsByCategory(category,numberOfQuestions);
        return new ResponseEntity<>(questions, HttpStatus.OK);
    }

    public ResponseEntity<List<QuestionDto>> getQuestionsFromId(List<Integer> ids) {
        List<QuestionDto> questionDTOS = new ArrayList<>();
        List<Question> questions = new ArrayList<>();

        for (Integer id : ids) {
            questions.add(questionRepo.findById(id).get());
        }
        for (Question question : questions) {
            QuestionDto questionDTO = new QuestionDto();
            questionDTO.setId(question.getId());
            questionDTO.setQuestionTitle(question.getQuestionTitle());
            questionDTO.setOptionA(question.getOptionA());
            questionDTO.setOptionB(question.getOptionB());
            questionDTO.setOptionC(question.getOptionC());
            questionDTO.setOptionD(question.getOptionD());

            questionDTOS.add(questionDTO);
        }

        return new ResponseEntity<>(questionDTOS, HttpStatus.OK);
    }

    public ResponseEntity<Integer> getScore(List<Response> responses) {
        int correct = 0;

        for(Response response : responses){
            Question question = questionRepo.findById(response.getId()).orElseThrow(()-> new RuntimeException("Question not found"));
            if(response.getResponse().equals(question.getCorrectAnswer())){
                correct ++;
            }
        }
        return new ResponseEntity<>(correct, HttpStatus.OK);
    }

    @Autowired
    private QuizServiceClient quizServiceClient;

    public ResponseEntity<String> createQuizWithQuestions(CreateQuizWithQuestionsDto request) {
        try {
            //Validate that all questions belong to the stated category
            boolean categoryMismatch = request.getQuestions().stream()
                    .anyMatch(q -> q.getCategory() != null
                            && !q.getCategory().equals(request.getCategory()));

            if (categoryMismatch) {
                return new ResponseEntity<>(
                        "All questions must match the quiz category: " + request.getCategory(),
                        HttpStatus.BAD_REQUEST
                );
            }

            //Force category on each question to match quiz category
            request.getQuestions().forEach(q -> q.setCategory(request.getCategory()));

            //Save all questions and collect their generated IDs
            List<Question> savedQuestions = questionRepo.saveAll(request.getQuestions());
            List<Integer> questionIds = savedQuestions.stream()
                    .map(Question::getId)
                    .collect(Collectors.toList());

            //Call quiz service to create the quiz with these IDs
            CustomQuizDto customQuizDto = new CustomQuizDto(
                    request.getQuizTitle(),
                    request.getCategory(),
                    questionIds
            );

            ResponseEntity<String> quizResponse = quizServiceClient.createCustomQuiz(customQuizDto);

            if (quizResponse.getStatusCode().is2xxSuccessful()) {
                return new ResponseEntity<>(
                        "Quiz created successfully with " + questionIds.size() + " questions",
                        HttpStatus.CREATED
                );
            } else {
                //Questions saved but quiz creation failed — log this or handle rollback
                return new ResponseEntity<>(
                        "Questions saved but quiz creation failed: " + quizResponse.getBody(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

        } catch (Exception e) {
            return new ResponseEntity<>(
                    "Failed to create quiz: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
