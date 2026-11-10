package com.example.elearning.validation;

import com.example.elearning.entity.Quiz;

import java.util.List;

/**
 * CHAIN OF RESPONSIBILITY: each handler inspects one aspect of a quiz and appends
 * human-readable problems to the shared {@code errors} list before passing control on.
 */
public abstract class QuizValidator {

    private QuizValidator next;

    /** Links the next handler and returns it, allowing fluent chain building. */
    public QuizValidator linkWith(QuizValidator nextValidator) {
        this.next = nextValidator;
        return nextValidator;
    }

    public abstract void validate(Quiz quiz, List<String> errors);

    protected void validateNext(Quiz quiz, List<String> errors) {
        if (next != null) {
            next.validate(quiz, errors);
        }
    }
}
