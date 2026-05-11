package org.insurance.ai.service;

import org.insurance.ai.config.PromptProperties;
import org.insurance.ai.dto.AnswerGenerationRequestDTO;
import org.insurance.ai.llm.LlmFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(AnswerGenerationService.class);

    @Autowired
    private LlmFactory llmFactory;

    @Autowired
    private PromptProperties promptProperties;

    @Autowired
    private ChunkRetrievalService chunkRetrievalService;

    public String generateGroundedAnswer(AnswerGenerationRequestDTO request) {
        logger.info("Generating grounded answer for question: {} (documentId: {})", 
                   request.getQuestion(), request.getDocumentId());

        try {
            // Check if LLM provider is available
            if (!llmFactory.hasAvailableProviders()) {
                throw new RuntimeException("No LLM providers are available for answer generation");
            }

            // Retrieve chunks from database
            List<String> retrievedChunks = chunkRetrievalService.getChunksByDocumentId(request.getDocumentId());
            logger.debug("Retrieved {} chunks from database", retrievedChunks.size());

            // Build context from retrieved chunks
            String context = buildContextFromChunks(retrievedChunks);
            
            // Get prompts from properties
            String systemPrompt = promptProperties.getSystemPrompt();
            String userPrompt = promptProperties.formatUserPrompt(request.getQuestion(), context);

            // Generate answer using the active LLM provider
            String answer = llmFactory.getActiveProvider().generateResponse(systemPrompt, userPrompt);

            logger.info("Successfully generated grounded answer using provider: {} (documentId: {})", 
                       llmFactory.getActiveProvider().getProviderName(), request.getDocumentId());
            logger.debug("Generated answer: {}", answer);

            return answer;

        } catch (Exception e) {
            logger.error("Failed to generate grounded answer: {}", e.getMessage(), e);
            throw new RuntimeException("Answer generation failed: " + e.getMessage(), e);
        }
    }

    private String buildContextFromChunks(List<String> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "No relevant document chunks were found.";
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Relevant document excerpts:\n\n");
        
        for (int i = 0; i < chunks.size(); i++) {
            contextBuilder.append("Excerpt ").append(i + 1).append(":\n");
            contextBuilder.append(chunks.get(i)).append("\n\n");
        }
        
        return contextBuilder.toString();
    }

    public String getActiveProviderInfo() {
        if (!llmFactory.hasAvailableProviders()) {
            return "No LLM providers available";
        }
        return llmFactory.getActiveProvider().getModelInfo() + " | Prompts: " + promptProperties.getPrompts().size() + " configured";
    }
}
