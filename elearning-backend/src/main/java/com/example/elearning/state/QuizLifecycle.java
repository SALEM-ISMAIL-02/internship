package com.example.elearning.state;

import com.example.elearning.entity.Quiz;
import org.springframework.stereotype.Component;

/**
 * STATE: a quiz is either DRAFT or PUBLISHED. Learners may only take published quizzes,
 * and a quiz cannot be published twice or unpublished while it still has no replacement.
 */
@Component
public class QuizLifecycle {

    public void publish(Quiz quiz) {
        if (quiz.isPublished()) {
            throw new IllegalStateException("Quiz #" + quiz.getId() + " is already published");
        }
        quiz.setPublished(true);
    }

    public void unpublish(Quiz quiz) {
        if (!quiz.isPublished()) {
            throw new IllegalStateException("Quiz #" + quiz.getId() + " is already a draft");
        }
        quiz.setPublished(false);
    }

    /** Guards the learner-facing read path. */
    public void assertTakeable(Quiz quiz) {
        if (!quiz.isPublished()) {
            throw new IllegalStateException("Quiz #" + quiz.getId() + " is not published yet");
        }
    }
}
