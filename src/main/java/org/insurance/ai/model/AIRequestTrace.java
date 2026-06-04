package org.insurance.ai.model;

import org.insurance.ai.dto.RetrievalResultDTO;

import java.time.LocalDateTime;
import java.util.List;

public class AIRequestTrace {
    private String question;
    private List<RetrievalResultDTO> retrievedChunks;
    private String prompt;
    private String llmResponse;
    private Long retrievalLatencyMs;
    private Long llmLatencyMs;
    private LocalDateTime timestamp;

    public AIRequestTrace() {
    }

    public AIRequestTrace(String question, List<RetrievalResultDTO> retrievedChunks, String prompt, String llmResponse,
                          Long retrievalLatencyMs, Long llmLatencyMs, LocalDateTime timestamp) {
        this.question = question;
        this.retrievedChunks = retrievedChunks;
        this.prompt = prompt;
        this.llmResponse = llmResponse;
        this.retrievalLatencyMs = retrievalLatencyMs;
        this.llmLatencyMs = llmLatencyMs;
        this.timestamp = timestamp;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<RetrievalResultDTO> getRetrievedChunks() {
        return retrievedChunks;
    }

    public void setRetrievedChunks(List<RetrievalResultDTO> retrievedChunks) {
        this.retrievedChunks = retrievedChunks;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getLlmResponse() {
        return llmResponse;
    }

    public void setLlmResponse(String llmResponse) {
        this.llmResponse = llmResponse;
    }

    public Long getRetrievalLatencyMs() {
        return retrievalLatencyMs;
    }

    public void setRetrievalLatencyMs(Long retrievalLatencyMs) {
        this.retrievalLatencyMs = retrievalLatencyMs;
    }

    public Long getLlmLatencyMs() {
        return llmLatencyMs;
    }

    public void setLlmLatencyMs(Long llmLatencyMs) {
        this.llmLatencyMs = llmLatencyMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
