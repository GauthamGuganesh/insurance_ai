package org.insurance.ai.service;

import org.insurance.ai.dto.RetrievalResultDTO;
import org.insurance.ai.model.AIRequestTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class LangChainTraceService {

    private static final Logger logger = LoggerFactory.getLogger(LangChainTraceService.class);

    @Value("${langsmith_tracing_v2}")
    private boolean langsmithTracingEnabled;

    @Value("${langsmith_api_key}")
    private String langsmithApiKey;

    @Value("${langsmith_endpoint}")
    private String langsmithEndpoint;

    @Value("${langsmith_project}")
    private String langsmithProject;

    private final RestTemplate restTemplate;

    public LangChainTraceService() {
        this.restTemplate = new RestTemplate();
    }

    public void sendTrace(AIRequestTrace trace) {
        if (!langsmithTracingEnabled || langsmithApiKey == null || langsmithApiKey.isEmpty()) {
            logger.debug("LangSmith tracing is disabled or API key not configured");
            return;
        }

        try {
            // Build inputs for the run
            Map<String, Object> inputs = new HashMap<>();
            inputs.put("question", trace.getQuestion());
            inputs.put("retrieval_latency_ms", trace.getRetrievalLatencyMs());
            inputs.put("llm_latency_ms", trace.getLlmLatencyMs());
            inputs.put("num_chunks", trace.getRetrievedChunks() != null ? trace.getRetrievedChunks().size() : 0);

            // Add retrieved chunks as inputs
            if (trace.getRetrievedChunks() != null) {
                for (int i = 0; i < trace.getRetrievedChunks().size(); i++) {
                    RetrievalResultDTO chunk = trace.getRetrievedChunks().get(i);
                    inputs.put("chunk_" + i + "_id", chunk.getChunkId());
                    inputs.put("chunk_" + i + "_distance", chunk.getDistance());
                    inputs.put("chunk_" + i + "_order", chunk.getChunkOrder());
                    if (chunk.getChunkText() != null) {
                        inputs.put("chunk_" + i + "_text", chunk.getChunkText());
                    }
                }
            }

            // Build outputs
            Map<String, Object> outputs = new HashMap<>();
            outputs.put("llm_response", trace.getLlmResponse());

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", "vector_search");
            requestBody.put("run_type", "chain");
            requestBody.put("inputs", inputs);
            requestBody.put("outputs", outputs);
            requestBody.put("session_name", langsmithProject);
            requestBody.put("start_time", Instant.now().toString());
            requestBody.put("end_time", Instant.now().toString());

            // Add prompt as extra metadata
            if (trace.getPrompt() != null) {
                Map<String, Object> extra = new HashMap<>();
                extra.put("prompt", trace.getPrompt());
                requestBody.put("extra", extra);
            }

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", langsmithApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Send POST request to LangSmith
            String url = langsmithEndpoint + "/runs";
            restTemplate.postForObject(url, request, String.class);

            logger.debug("Successfully sent trace to LangSmith");
        } catch (Exception e) {
            logger.error("Failed to send trace to LangSmith: {}", e.getMessage(), e);
        }
    }
}
