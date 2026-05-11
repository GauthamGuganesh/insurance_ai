package org.insurance.ai.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@PropertySource("classpath:prompts.properties")
@Component
public class PromptProperties {

    @Autowired
    private Environment environment;

    private Map<String, String> prompts = new HashMap<>();

    // Specific prompt keys for easy access
    public static final String SYSTEM_PROMPT_KEY = "answer.generation.system";
    public static final String USER_PROMPT_TEMPLATE_KEY = "answer.generation.user.template";

    public Map<String, String> getPrompts() {
        return prompts;
    }

    public void setPrompts(Map<String, String> prompts) {
        this.prompts = prompts;
    }

    // Initialize prompts from environment properties
    @PostConstruct 
    public void initializePrompts() {
        prompts.put("answer.generation.system", environment.getProperty("answer.generation.system"));
			prompts.put("answer.generation.user.template", environment.getProperty("answer.generation.user.template"));
    }

    public String getPrompt(String key) {
        String prompt = prompts.get(key);
        if (prompt == null) {
            throw new IllegalArgumentException("Prompt not found: " + key);
        }
        return prompt;
    }

    public String formatPrompt(String key, Object... args) {
        String template = getPrompt(key);
        
        // Replace placeholders with provided arguments
        String result = template;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(args[i]));
        }
        
        return result;
    }

    public boolean hasPrompt(String key) {
        return prompts.containsKey(key);
    }

    // Convenience methods for commonly used prompts
    public String getSystemPrompt() {
        return getPrompt(SYSTEM_PROMPT_KEY);
    }

    public String getUserPromptTemplate() {
        return getPrompt(USER_PROMPT_TEMPLATE_KEY);
    }

    public String formatUserPrompt(String question, String context) {
        return formatPrompt(USER_PROMPT_TEMPLATE_KEY, question, context);
    }

    @Override
    public String toString() {
        return "PromptProperties{" +
                "promptCount=" + prompts.size() +
                ", keys=" + prompts.keySet() +
                '}';
    }
}
