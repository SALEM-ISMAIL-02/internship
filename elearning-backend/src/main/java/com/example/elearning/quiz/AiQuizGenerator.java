package com.example.elearning.quiz;

import com.example.elearning.ai.AiClientFactory;
import com.example.elearning.entity.Question;
import com.example.elearning.extract.TextExtractorFactory;
import com.example.elearning.repository.MaterialRepository;
import com.example.elearning.repository.QuizRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Concrete TEMPLATE METHOD implementation: MCQ generation via an OpenAI-compatible model. */
@Service
public class AiQuizGenerator extends AbstractQuizGenerator {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * DIP: depends on the {@link com.example.elearning.ai.AiChatClient} abstraction,
     * which Spring provides via {@code AiConfig#aiChatClient}. No factory coupling.
     */
    public AiQuizGenerator(MaterialRepository materialRepository,
                           QuizRepository quizRepository,
                           TextExtractorFactory textExtractorFactory,
                           com.example.elearning.ai.AiChatClient aiChatClient) {
        super(materialRepository, quizRepository, textExtractorFactory, aiChatClient);
    }

    @Override
    protected String buildPrompt(QuizGenerationRequest request, String sourceText) {
        return """
                You are a quiz generator. Based ONLY on the following course material text, \
                generate %d multiple-choice questions. Respond with ONLY a JSON array, each item: \
                {"text": "...", "options": ["A","B","C","D"], "correctAnswer": 0}. \
                correctAnswer is the 0-based index of the correct option.

                MATERIAL:
                %s
                """.formatted(request.numQuestions(), sourceText);
    }

    @Override
    protected List<Question> parseQuestions(String rawAiResponse) {
        String json = stripCodeFences(rawAiResponse);
        try {
            JsonNode array = objectMapper.readTree(json);
            if (!array.isArray()) {
                throw new IllegalStateException("AI did not return a JSON array");
            }
            List<Question> questions = new ArrayList<>();
            for (JsonNode node : array) {
                List<String> options = new ArrayList<>();
                node.path("options").forEach(option -> options.add(option.asText()));
                questions.add(Question.builder()
                        .text(node.path("text").asText())
                        .options(options)
                        .correctAnswer(node.path("correctAnswer").asInt(0))
                        .build());
            }
            return questions;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse AI response as JSON", e);
        }
    }

    private String stripCodeFences(String content) {
        String json = content == null ? "" : content.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
        }
        return json;
    }
}
