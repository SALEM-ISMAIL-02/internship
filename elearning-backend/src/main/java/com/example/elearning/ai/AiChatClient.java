package com.example.elearning.ai;

/**
 * Port (interface) for any text-completion AI backend.
 * Implementations are interchangeable: OpenAI-compatible HTTP, fakes for tests, etc.
 */
public interface AiChatClient {

    /**
     * Sends a single user prompt and returns the assistant's raw text answer.
     *
     * @throws IllegalStateException when the provider returns no usable content
     */
    String complete(String prompt);
}
