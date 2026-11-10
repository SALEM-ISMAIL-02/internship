package com.example.elearning.quiz;

import com.example.elearning.ai.AiChatClient;
import com.example.elearning.entity.Material;
import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import com.example.elearning.entity.User;
import com.example.elearning.extract.TextExtractorFactory;
import com.example.elearning.repository.MaterialRepository;
import com.example.elearning.repository.QuizRepository;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * TEMPLATE METHOD: fixes the quiz-generation algorithm; subclasses only supply the two
 * steps that actually differ per provider/format (prompt building and response parsing).
 *
 * <pre>
 * 1. load material                  (final)
 * 2. extract text via strategy      (final)
 * 3. normalise / truncate           (final)
 * 4. buildPrompt(...)               (abstract)
 * 5. call the AI client             (final, decorated with retry + logging)
 * 6. parseQuestions(...)            (abstract)
 * 7. assemble + persist the draft   (final)
 * </pre>
 */
public abstract class AbstractQuizGenerator implements QuizGenerator {

    protected static final int MAX_SOURCE_CHARS = 12_000;

    private final MaterialRepository materialRepository;
    private final QuizRepository quizRepository;
    private final TextExtractorFactory textExtractorFactory;
    private final AiChatClient aiChatClient;

    protected AbstractQuizGenerator(MaterialRepository materialRepository,
                                    QuizRepository quizRepository,
                                    TextExtractorFactory textExtractorFactory,
                                    AiChatClient aiChatClient) {
        this.materialRepository = materialRepository;
        this.quizRepository = quizRepository;
        this.textExtractorFactory = textExtractorFactory;
        this.aiChatClient = aiChatClient;
    }

    /** The template method — subclasses must not override it. */
    public final Quiz generate(QuizGenerationRequest request) throws IOException {
        Material material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new IllegalArgumentException("Material not found: " + request.materialId()));

        String sourceText = textExtractorFactory.forType(material.getType())
                .extract(Paths.get(material.getFilePath()));
        if (sourceText == null || sourceText.isBlank()) {
            throw new IllegalStateException(
                    "No extractable text in this material (" + material.getType() + "). "
                            + "Supported formats: " + textExtractorFactory.supportedTypes());
        }

        String prompt = buildPrompt(request, truncate(sourceText));
        List<Question> questions = parseQuestions(aiChatClient.complete(prompt));
        if (questions.isEmpty()) {
            throw new IllegalStateException("The AI returned no usable questions.");
        }
        return saveDraftQuiz(material, request, questions);
    }

    protected abstract String buildPrompt(QuizGenerationRequest request, String sourceText);

    protected abstract List<Question> parseQuestions(String rawAiResponse);

    private String truncate(String text) {
        String normalized = text.strip();
        return normalized.length() > MAX_SOURCE_CHARS ? normalized.substring(0, MAX_SOURCE_CHARS) : normalized;
    }

    private Quiz saveDraftQuiz(Material material, QuizGenerationRequest request, List<Question> questions) {
        Quiz quiz = Quiz.builder()
                .course(material.getCourse())
                .createdBy(User.builder().id(request.creatorId()).build())
                .aiGenerated(true)
                .published(false)
                .questions(new ArrayList<>())
                .build();
        for (Question question : questions) {
            question.setQuiz(quiz);
            quiz.getQuestions().add(question);
        }
        return quizRepository.save(quiz);
    }
}
