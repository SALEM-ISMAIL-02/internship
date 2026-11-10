package com.example.elearning.config;

import com.example.elearning.ai.AiChatClient;
import com.example.elearning.ai.AiClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * DIP wiring: exposes the decorated {@link AiChatClient} as a singleton bean,
 * so generators depend on the abstraction, never on the factory.
 */
@Configuration
public class AppConfig {

    /** Single shared HTTP client (SINGLETON) reused by every outbound integration. */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    @Bean
    public AiChatClient aiChatClient(AiClientFactory factory) {
        return factory.create();
    }
}
