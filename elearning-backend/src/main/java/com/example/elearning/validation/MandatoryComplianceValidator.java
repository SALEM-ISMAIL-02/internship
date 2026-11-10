package com.example.elearning.validation;

import com.example.elearning.entity.Course;
import com.example.elearning.entity.Quiz;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/** Rule 5: compliance (MANDATORY) modules must ship a meaningful assessment. */
@Component
@Order(5)
public class MandatoryComplianceValidator extends QuizValidator {

    private static final int MIN_QUESTIONS_FOR_MANDATORY = 3;

    @Override
    public void validate(Quiz quiz, List<String> errors) {
        Course course = quiz.getCourse();
        int questionCount = quiz.getQuestions() == null ? 0 : quiz.getQuestions().size();
        if (course != null
                && course.getPriority() == Course.Priority.MANDATORY
                && questionCount < MIN_QUESTIONS_FOR_MANDATORY) {
            errors.add("MANDATORY modules require at least " + MIN_QUESTIONS_FOR_MANDATORY
                    + " questions (found " + questionCount + ")");
        }
        validateNext(quiz, errors);
    }
}
