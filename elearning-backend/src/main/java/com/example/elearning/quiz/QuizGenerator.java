package com.example.elearning.quiz;

import com.example.elearning.entity.Quiz;

import java.io.IOException;

/**
 * Abstraction for quiz generation (DIP).
 * High-level modules (services, controllers) depend on this,
 * not on {@link AbstractQuizGenerator} subclasses.
 */
public interface QuizGenerator {

    Quiz generate(QuizGenerationRequest request) throws IOException;
}
