package com.example.elearning.validation;

import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/** Rule 3: correctAnswer must be a valid index into that question's options. */
@Component
@Order(3)
public class CorrectAnswerIndexValidator extends QuizValidator {

    @Override
    public void validate(Quiz quiz, List<String> errors) {
        if (quiz.getQuestions() == null) {
            validateNext(quiz, errors);
            return;
        }
        for (Question question : quiz.getQuestions()) {
            List<String> options = question.getOptions();
            int index = question.getCorrectAnswer();
            if (options == null || index < 0 || index >= options.size()) {
                errors.add("question '" + question.getText() + "' has an out-of-range correctAnswer (" + index + ")");
            }
        }
        validateNext(quiz, errors);
    }
}
