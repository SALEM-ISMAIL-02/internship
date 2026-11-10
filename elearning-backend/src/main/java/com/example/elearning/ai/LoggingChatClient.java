package com.example.elearning.ai;

import lombok.extern.slf4j.Slf4j;

/**
 * DECORATOR: adds observability (prompt/response size and latency) around another
 * {@link AiChatClient} without the concrete client knowing about logging.
 */
@Slf4j
public class LoggingChatClient implements AiChatClient {

    private final AiChatClient delegate;

    public LoggingChatClient(AiChatClient delegate) {
        this.delegate = delegate;
    }

    @Override
    public String complete(String prompt) {
        long start = System.currentTimeMillis();
        log.info("AI request dispatched (prompt chars={})", prompt.length());
        try {
            String answer = delegate.complete(prompt);
            log.info("AI response received (chars={}, took={} ms)",
                    answer == null ? 0 : answer.length(), System.currentTimeMillis() - start);
            return answer;
        } catch (RuntimeException e) {
            log.error("AI request failed after {} ms: {}", System.currentTimeMillis() - start, e.getMessage());
            throw e;
        }
    }
}
