package com.example.elearning.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * FACTORY + REGISTRY (OCP): new providers are added via {@link #register},
 * never by editing a switch. Swapping vendors stays a config change.
 */
@Component
public class AiClientFactory {

    private final RestTemplate restTemplate;
    private final Map<String, Supplier<AiChatClient>> registry = new ConcurrentHashMap<>();

    @Value("${app.ai.provider:openai-compatible}")
    private String provider;
    @Value("${app.ai.base-url}")
    private String baseUrl;
    @Value("${app.ai.api-key}")
    private String apiKey;
    @Value("${app.ai.model}")
    private String model;
    @Value("${app.ai.max-attempts:3}")
    private int maxAttempts;
    @Value("${app.ai.retry-delay-ms:2000}")
    private long retryDelayMs;

    public AiClientFactory(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        // Default OpenAI-compatible providers share one adapter (OCP: aliases, not branches).
        Supplier<AiChatClient> openAiCompatible =
                () -> new OpenAiCompatibleChatClient(restTemplate, baseUrl, apiKey, model);
        registry.put("openai-compatible", openAiCompatible);
        registry.put("openrouter", openAiCompatible);
        registry.put("gemini", openAiCompatible);
    }

    /** Extension point: plug a new vendor without touching this class's logic. */
    public void register(String name, Supplier<AiChatClient> supplier) {
        registry.put(name, supplier);
    }

    /** Returns: Logging( Retry( provider-specific client ) ). */
    public AiChatClient create() {
        Supplier<AiChatClient> supplier = registry.get(provider);
        if (supplier == null) {
            throw new IllegalArgumentException(
                    "Unsupported app.ai.provider '" + provider + "'. Registered: " + registry.keySet());
        }
        AiChatClient base = supplier.get();
        return new LoggingChatClient(new RetryChatClient(base, maxAttempts, retryDelayMs));
    }
}
