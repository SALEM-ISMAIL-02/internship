package com.example.elearning.validation;

import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/** Rule 2: every question needs at least two answer options (and no blank ones). */
@Component
@Order(2)
public class OptionCountValidator extends QuizValidator {

    private static final int MIN_OPTIONS = 2;

    @Override
    public void validate(Quiz quiz, List<String> errors) {
        if (quiz.getQuestions() == null) {
            validateNext(quiz, errors);
            return;
        }
        for (Question question : quiz.getQuestions()) {
            List<String> options = question.getOptions();
            if (options == null || options.size() < MIN_OPTIONS) {
                errors.add("question '" + abbreviate(question.getText()) + "' must have at least "
                        + MIN_OPTIONS + " options");
                continue;
            }
            if (options.stream().anyMatch(option -> option == null || option.isBlank())) {
                errors.add("question '" + abbreviate(question.getText()) + "' contains a blank option");
            }
        }
        validateNext(quiz, errors);
    }

    private String abbreviate(String text) {
        if (text == null) {
            return "<empty>";
        }
        return text.length() <= 40 ? text : text.substring(0, 40) + "...";
    }
}
