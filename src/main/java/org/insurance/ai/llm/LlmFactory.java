package org.insurance.ai.llm;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LlmFactory {

    private static final Logger logger = LoggerFactory.getLogger(LlmFactory.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${llm.provider}")
    private String defaultProvider;

    private Map<String, LlmProvider> providers = new HashMap<>();
    private LlmProvider activeProvider;

    @PostConstruct
    public void initialize() {
        // Auto-discover all LLM providers
        Map<String, LlmProvider> discoveredProviders = applicationContext.getBeansOfType(LlmProvider.class);
        
        for (LlmProvider provider : discoveredProviders.values()) {
            String providerName = provider.getProviderName().toLowerCase();
            providers.put(providerName, provider);
            logger.info("Discovered LLM provider: {} ({})", provider.getProviderName(), provider.getModelInfo());
        }

        // Set active provider
        setActiveProvider(defaultProvider);
        
        if (providers.isEmpty()) {
            logger.warn("No LLM providers found. Answer generation will not be available.");
        }
    }

    public LlmProvider getActiveProvider() {
        if (activeProvider == null) {
            throw new IllegalStateException("No active LLM provider configured");
        }
        return activeProvider;
    }

    public void setActiveProvider(String providerName) {
        String normalizedName = providerName.toLowerCase();
        LlmProvider provider = providers.get(normalizedName);
        
        if (provider == null) {
            throw new IllegalArgumentException("LLM provider not found: " + providerName + 
                                             ". Available providers: " + providers.keySet());
        }

        if (!provider.isAvailable()) {
            throw new IllegalStateException("LLM provider not available: " + providerName);
        }

        this.activeProvider = provider;
        logger.info("Active LLM provider set to: {} ({})", 
                   provider.getProviderName(), provider.getModelInfo());
    }

    public Map<String, String> getAvailableProviders() {
        Map<String, String> providerInfo = new HashMap<>();
        for (Map.Entry<String, LlmProvider> entry : providers.entrySet()) {
            providerInfo.put(entry.getKey(), entry.getValue().getModelInfo());
        }
        return providerInfo;
    }

    public boolean hasAvailableProviders() {
        return !providers.isEmpty() && providers.values().stream().anyMatch(LlmProvider::isAvailable);
    }

    public String getDefaultProviderName() {
        return defaultProvider;
    }
}
