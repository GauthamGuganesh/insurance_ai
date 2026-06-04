package org.insurance.ai.controller;

import org.insurance.ai.dto.AnswerGenerationRequestDTO;
import org.insurance.ai.dto.VectorSearchAnswerResponseDTO;
import org.insurance.ai.llm.LlmFactory;
import org.insurance.ai.service.AnswerGenerationService;
import org.insurance.ai.service.VectorSearchAnswerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/answer")
public class AnswerController {

    private static final Logger logger = LoggerFactory.getLogger(AnswerController.class);

    @Autowired
    private AnswerGenerationService answerGenerationService;

    @Autowired
    private VectorSearchAnswerService vectorSearchAnswerService;

    @Autowired
    private LlmFactory llmFactory;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateAnswer(@RequestBody AnswerGenerationRequestDTO request) {
        logger.info("Received answer generation request for document ID: {}", request.getDocumentId());

        try {
            // Validate request
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Question cannot be empty");
                return ResponseEntity.badRequest().body(error);
            }

            if (request.getDocumentId() == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Document ID is required");
                return ResponseEntity.badRequest().body(error);
            }

            // Generate grounded answer
            String answer = answerGenerationService.generateGroundedAnswer(request);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Answer generated successfully");
            response.put("answer", answer);
            response.put("question", request.getQuestion());
            response.put("documentId", request.getDocumentId());
            response.put("provider", answerGenerationService.getActiveProviderInfo());

            logger.info("Answer generation completed successfully for document ID: {}", request.getDocumentId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to generate answer for document ID {}: {}", 
                        request.getDocumentId(), e.getMessage(), e);
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to generate answer: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("service", "Answer Generation Service");
        response.put("status", "healthy");
        response.put("hasProviders", llmFactory.hasAvailableProviders());
        response.put("activeProvider", answerGenerationService.getActiveProviderInfo());
        response.put("availableProviders", llmFactory.getAvailableProviders());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getProviders() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("defaultProvider", llmFactory.getDefaultProviderName());
        response.put("availableProviders", llmFactory.getAvailableProviders());
        response.put("hasAvailableProviders", llmFactory.hasAvailableProviders());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/vector-search")
    public ResponseEntity<Map<String, Object>> generateVectorSearchAnswer(@RequestBody AnswerGenerationRequestDTO request) {
        logger.info("Received vector search answer generation request for question: {}", request.getQuestion());

        try {
            // Validate request
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Question cannot be empty");
                return ResponseEntity.badRequest().body(error);
            }

            // Generate vector search answer with sources
            VectorSearchAnswerResponseDTO responseDTO = vectorSearchAnswerService.generateVectorSearchAnswer(request);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vector search answer generated successfully");
            response.put("answer", responseDTO.getAnswer());
            response.put("sources", responseDTO.getSources());
            response.put("question", request.getQuestion());
            response.put("method", "vector_search");

            logger.info("Vector search answer generation completed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Failed to generate vector search answer: {}", e.getMessage(), e);

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to generate vector search answer: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
