package com.example.elearning.validation;

import com.example.elearning.entity.Quiz;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the ordered validator chain (Spring injects every {@link QuizValidator}
 * bean sorted by {@code @Order}) and runs it as one call.
 */
@Slf4j
@Component
public class QuizValidationChain {

    private final QuizValidator head;

    public QuizValidationChain(List<QuizValidator> orderedValidators) {
        QuizValidator first = null;
        QuizValidator current = null;
        for (QuizValidator validator : orderedValidators) {
            if (first == null) {
                first = validator;
            } else {
                current.linkWith(validator);
            }
            current = validator;
        }
        this.head = first;
    }

    /**
     * @throws IllegalArgumentException with every collected problem when the quiz is not publishable
     */
    public void validateOrThrow(Quiz quiz) {
        List<String> errors = new ArrayList<>();
        if (head != null) {
            head.validate(quiz, errors);
        }
        if (!errors.isEmpty()) {
            log.warn("Quiz #{} rejected by validation chain: {}", quiz.getId(), errors);
            throw new IllegalArgumentException("Quiz validation failed: " + String.join("; ", errors));
        }
    }
}
