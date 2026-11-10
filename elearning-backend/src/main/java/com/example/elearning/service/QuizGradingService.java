package com.example.elearning.service;

import com.example.elearning.entity.Quiz;
import com.example.elearning.entity.QuizResult;
import com.example.elearning.entity.User;
import com.example.elearning.repository.QuizRepository;
import com.example.elearning.repository.QuizResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * SRP: ONLY grades a quiz attempt and persists the {@link QuizResult}.
 * Progress bonuses live in {@link ProgressService}.
 * DIP: depends on the {@link QuizScoringPolicy} abstraction.
 */
@Service
@RequiredArgsConstructor
public class QuizGradingService {

    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizScoringPolicy scoringPolicy;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record GradingOutcome(Quiz quiz, QuizResult result) {
    }

    public GradingOutcome grade(Long learnerId, Long quizId, Map<Long, Integer> answers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        int score = scoringPolicy.score(quiz, answers);
        String answersJson;
        try {
            answersJson = objectMapper.writeValueAsString(answers);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not store quiz answers", e);
        }
        QuizResult result = QuizResult.builder()
                .learner(User.builder().id(learnerId).build())
                .quiz(quiz)
                .score(score)
                .date(LocalDateTime.now())
                .answersJson(answersJson)
                .approved(false)
                .build();
        return new GradingOutcome(quiz, quizResultRepository.save(result));
    }
}
