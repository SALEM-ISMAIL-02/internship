package com.example.elearning.service;

import com.example.elearning.entity.Material;
import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import com.example.elearning.repository.MaterialRepository;
import com.example.elearning.repository.QuizRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizGenerationService {

    private final MaterialRepository materialRepository;
    private final QuizRepository quizRepository;
    private final MaterialService materialService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.base-url}")
    private String baseUrl;
    @Value("${app.ai.api-key}")
    private String apiKey;
    @Value("${app.ai.model}")
    private String model;

    /** Generates a quiz from a material's text via an OpenAI-compatible AI API (e.g. OpenRouter free tier). */
    public Quiz generateQuiz(Long materialId, int numQuestions, Long creatorId) throws IOException {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));
        String text = materialService.extractText(material);
        if (text.isBlank()) {
            throw new IllegalStateException("No extractable text in this material (videos/unsupported formats).");
        }
        if (text.length() > 12000) {
            text = text.substring(0, 12000); // keep prompt within context limits
        }

        String prompt = """
                You are a quiz generator. Based ONLY on the following course material text, \
                generate %d multiple-choice questions. Respond with ONLY a JSON array, each item: \
                {"text": "...", "options": ["A","B","C","D"], "correctAnswer": 0}. \
                correctAnswer is the 0-based index of the correct option.

                MATERIAL:
                %s
                """.formatted(numQuestions, text);

        String content = callAi(prompt);
        List<Question> questions = parseQuestions(content);

        Quiz quiz = Quiz.builder()
                .course(material.getCourse())
                .createdBy(com.example.elearning.entity.User.builder().id(creatorId).build())
                .aiGenerated(true)
                .published(false)
                .questions(new ArrayList<>())
                .build();
        for (Question q : questions) {
            q.setQuiz(quiz);
            quiz.getQuestions().add(q);
        }
        return quizRepository.save(quiz);
    }

    private String callAi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "http://localhost:4200");
        headers.set("X-Title", "ELearningQuizGenerator");

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3);

        Map<String, Object> response = restTemplate.postForEntity(
                baseUrl + "/chat/completions", new HttpEntity<>(body, headers), Map.class).getBody();
        if (response == null) throw new IllegalStateException("Empty AI response");
        return ((JsonNode) objectMapper.valueToTree(response))
                .path("choices").path(0).path("message").path("content").asText();
    }

    private List<Question> parseQuestions(String content) {
        // Strip possible markdown code fences
        String json = content.trim();
        if (json.startsWith("```")) {
            json = json.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
        }
        try {
            JsonNode arr = objectMapper.readTree(json);
            if (!arr.isArray()) throw new IllegalStateException("AI did not return a JSON array");
            List<Question> questions = new ArrayList<>();
            for (JsonNode node : arr) {
                List<String> options = new ArrayList<>();
                node.path("options").forEach(o -> options.add(o.asText()));
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
}
