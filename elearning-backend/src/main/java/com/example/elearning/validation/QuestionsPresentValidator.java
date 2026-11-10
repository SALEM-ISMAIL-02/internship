package com.example.elearning.validation;

import com.example.elearning.entity.Quiz;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/** Rule 1: a quiz cannot be published without questions. */
@Component
@Order(1)
public class QuestionsPresentValidator extends QuizValidator {

    @Override
    public void validate(Quiz quiz, List<String> errors) {
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            errors.add("quiz has no questions");
        }
        validateNext(quiz, errors);
    }
}
