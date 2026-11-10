package com.example.elearning.service;

import com.example.elearning.entity.Quiz;
import com.example.elearning.quiz.AiQuizGenerator;
import com.example.elearning.quiz.QuizGenerationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * FACADE kept for backward compatibility: the extraction/provider details now live behind
 * {@link com.example.elearning.quiz.QuizGenerator} (template method + strategies).
 * DIP: depends on the abstraction, not the concrete generator.
 */
@Service
@RequiredArgsConstructor
public class QuizGenerationService {

    private final com.example.elearning.quiz.QuizGenerator generator;

    /** Generates an unpublished draft quiz from a course material. */
    public Quiz generateQuiz(Long materialId, int numQuestions, Long creatorId) throws IOException {
        return generator.generate(new QuizGenerationRequest(materialId, numQuestions, creatorId));
    }
}
