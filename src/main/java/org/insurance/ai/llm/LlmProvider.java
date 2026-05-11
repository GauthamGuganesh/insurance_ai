package org.insurance.ai.llm;

public interface LlmProvider {
    
    /**
     * Generate a response based on the provided prompt
     * @param prompt The input prompt
     * @return Generated response
     */
    String generateResponse(String prompt);
    
    /**
     * Generate a response with system and user messages
     * @param systemPrompt System-level instructions
     * @param userPrompt User's question/request
     * @return Generated response
     */
    String generateResponse(String systemPrompt, String userPrompt);
    
    /**
     * Get the provider name
     * @return Provider identifier
     */
    String getProviderName();
    
    /**
     * Check if the provider is available/configured
     * @return true if provider is ready to use
     */
    boolean isAvailable();
    
    /**
     * Get model information
     * @return Model details
     */
    String getModelInfo();
}
