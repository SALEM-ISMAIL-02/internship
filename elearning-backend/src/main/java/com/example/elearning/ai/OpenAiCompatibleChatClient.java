package com.example.elearning.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * ADAPTER: translates any OpenAI-compatible "/chat/completions" provider
 * (OpenRouter, Google Gemini's OpenAI layer, Ollama, vLLM, ...) into {@link AiChatClient},
 * so the rest of the application never sees vendor-specific JSON.
 */
public class OpenAiCompatibleChatClient implements AiChatClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String baseUrl;
    private final String apiKey;
    private final String model;

    public OpenAiCompatibleChatClient(RestTemplate restTemplate, String baseUrl, String apiKey, String model) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public String complete(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "AI API key is not configured. Set the AI_API_KEY environment variable.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "http://localhost:4200");
        headers.set("X-Title", "SafyronLearningQuizGenerator");

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3);

        Map<?, ?> response = restTemplate.postForEntity(
                baseUrl + "/chat/completions", new HttpEntity<>(body, headers), Map.class).getBody();
        if (response == null) {
            throw new IllegalStateException("Empty AI response");
        }
        JsonNode content = objectMapper.valueToTree(response)
                .path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || content.asText().isBlank()) {
            throw new IllegalStateException("AI provider returned no message content");
        }
        return content.asText();
    }
}
