package com.example.elearning.validation;

import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Rule 4: no duplicated question stems inside one quiz. */
@Component
@Order(4)
public class DuplicateStemValidator extends QuizValidator {

    @Override
    public void validate(Quiz quiz, List<String> errors) {
        if (quiz.getQuestions() == null) {
            validateNext(quiz, errors);
            return;
        }
        Set<String> seen = new HashSet<>();
        for (Question question : quiz.getQuestions()) {
            String stem = normalize(question.getText());
            if (!seen.add(stem)) {
                errors.add("duplicated question stem: '" + question.getText() + "'");
            }
        }
        validateNext(quiz, errors);
    }

    private String normalize(String text) {
        return text == null ? "" : text.toLowerCase().replaceAll("\\s+", " ").trim();
    }
}
