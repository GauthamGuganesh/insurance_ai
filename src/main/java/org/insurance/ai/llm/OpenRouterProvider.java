package org.insurance.ai.llm;

import org.insurance.ai.dto.openrouter.OpenRouterChatRequest;
import org.insurance.ai.dto.openrouter.OpenRouterChatResponse;
import org.insurance.ai.dto.openrouter.OpenRouterMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component("openrouter")
public class OpenRouterProvider implements LlmProvider {

    private static final Logger logger = LoggerFactory.getLogger(OpenRouterProvider.class);
    private static final String OPENROUTER_API_BASE = "https://openrouter.ai/api/v1";

    @Autowired
    private WebClient.Builder webClientBuilder;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    @Value("${openrouter.max-tokens}")
    private Integer maxTokens;

    @Value("${openrouter.temperature}")
    private Double temperature;

    @Value("${openrouter.site-url}")
    private String siteUrl;

    @Value("${openrouter.app-name}")
    private String appName;

    @Override
    public String generateResponse(String prompt) {
        return generateResponse("You are a helpful assistant.", prompt);
    }

    @Override
    public String generateResponse(String systemPrompt, String userPrompt) {
        logger.debug("Generating response with OpenRouter model: {}", model);

        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(OPENROUTER_API_BASE)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            // Prepare messages
            List<OpenRouterMessage> messages = new ArrayList<>();
            messages.add(new OpenRouterMessage("system", systemPrompt));
            messages.add(new OpenRouterMessage("user", userPrompt));

            // Create request
            OpenRouterChatRequest request = new OpenRouterChatRequest(
                    model, messages, maxTokens, temperature, siteUrl, appName
            );

            // Make API call
            OpenRouterChatResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenRouterChatResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new RuntimeException("Invalid response from OpenRouter API");
            }

            return response.getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            logger.error("OpenRouter API call failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate response with OpenRouter: " + e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "OpenRouter";
    }

    @Override
    public boolean isAvailable() {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(OPENROUTER_API_BASE)
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .build();

            // Simple health check - try to get models
            Mono<String> response = webClient.get()
                    .uri("/models")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60));

            response.block();
            return true;

        } catch (Exception e) {
            logger.warn("OpenRouter provider not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getModelInfo() {
        return String.format("OpenRouter - Model: %s, Max Tokens: %d, Temperature: %.1f, Site: %s", 
                            model, maxTokens, temperature, siteUrl);
    }
}
