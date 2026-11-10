package com.example.elearning.quiz;

/** Input data for one AI quiz-generation run. */
public record QuizGenerationRequest(Long materialId, int numQuestions, Long creatorId) {

    public QuizGenerationRequest {
        if (materialId == null) throw new IllegalArgumentException("materialId is required");
        if (numQuestions < 1 || numQuestions > 25) {
            throw new IllegalArgumentException("numQuestions must be between 1 and 25");
        }
    }
}
