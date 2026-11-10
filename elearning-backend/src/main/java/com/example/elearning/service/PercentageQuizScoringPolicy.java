package com.example.elearning.service;

import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Default scoring: percentage of correctly answered questions.
 */
@Component
public class PercentageQuizScoringPolicy implements QuizScoringPolicy {

    @Override
    public int score(Quiz quiz, Map<Long, Integer> answers) {
        int total = quiz.getQuestions() == null ? 0 : quiz.getQuestions().size();
        if (total == 0) {
            throw new IllegalStateException("Quiz has no questions");
        }
        long correct = quiz.getQuestions().stream()
                .filter(q -> answers != null && answers.get(q.getId()) != null
                        && answers.get(q.getId()) == q.getCorrectAnswer())
                .count();
        return (int) Math.round(correct * 100.0 / total);
    }
}
