package com.example.elearning.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/**
 * DECORATOR: adds bounded retry with linear back-off around another {@link AiChatClient}.
 * Needed in practice because free AI tiers answer with 503 "high demand" or drop connections.
 */
@Slf4j
public class RetryChatClient implements AiChatClient {

    private final AiChatClient delegate;
    private final int maxAttempts;
    private final long delayMs;

    public RetryChatClient(AiChatClient delegate, int maxAttempts, long delayMs) {
        this.delegate = delegate;
        this.maxAttempts = Math.max(1, maxAttempts);
        this.delayMs = Math.max(0, delayMs);
    }

    @Override
    public String complete(String prompt) {
        RuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return delegate.complete(prompt);
            } catch (RestClientResponseException | ResourceAccessException e) {
                lastFailure = e;
                if (attempt == maxAttempts) {
                    break;
                }
                log.warn("AI call attempt {}/{} failed ({}). Retrying in {} ms.",
                        attempt, maxAttempts, e.getMessage(), delayMs * attempt);
                sleep(delayMs * attempt);
            }
        }
        throw lastFailure;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting to retry the AI call", ie);
        }
    }
}
