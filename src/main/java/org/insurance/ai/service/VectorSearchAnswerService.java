package org.insurance.ai.service;

import org.insurance.ai.config.PromptProperties;
import org.insurance.ai.dto.AnswerGenerationRequestDTO;
import org.insurance.ai.dto.RetrievalResultDTO;
import org.insurance.ai.dto.VectorSearchAnswerResponseDTO;
import org.insurance.ai.llm.LlmFactory;
import org.insurance.ai.model.AIRequestTrace;
import org.insurance.ai.model.DocumentChunk;
import org.insurance.ai.model.DocumentEmbedding;
import org.insurance.ai.repository.DocumentChunkRepository;
import org.insurance.ai.repository.DocumentEmbeddingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VectorSearchAnswerService {

    private static final Logger logger = LoggerFactory.getLogger(VectorSearchAnswerService.class);

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private DocumentEmbeddingRepository documentEmbeddingRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private LlmFactory llmFactory;

    @Autowired
    private PromptProperties promptProperties;

    @Value("${vector.search.confidence.threshold:0.5}")
    private double confidenceThreshold;

    @Autowired
    private LangChainTraceService langChainTraceService;

    public VectorSearchAnswerResponseDTO generateVectorSearchAnswer(AnswerGenerationRequestDTO request) {
        AIRequestTrace trace = new AIRequestTrace();
        trace.setQuestion(request.getQuestion());
        trace.setTimestamp(LocalDateTime.now());

        try {
            // Validate documentId
            if (request.getDocumentId() == null) {
                throw new RuntimeException("Document ID is required for vector search");
            }

            // Check if LLM provider is available
            if (!llmFactory.hasAvailableProviders()) {
                throw new RuntimeException("No LLM providers are available for answer generation");
            }

            // Step 1: Generate query embedding
            long retrievalStart = System.currentTimeMillis();
            float[] queryEmbedding = embeddingService.generateEmbedding(request.getQuestion());

            // Step 2: Vector similarity search to retrieve top chunks with distances
            String queryVector = arrayToPostgresVector(queryEmbedding);
            List<Object[]> nearestNeighborsWithDistance = documentEmbeddingRepository.findNearestNeighborsWithDistance(queryVector, request.getDocumentId(), 5);

            // Parse results and extract chunk IDs and distances
            List<RetrievalResultDTO> retrievalResults = new ArrayList<>();
            List<Long> chunkIds = new ArrayList<>();

            for (int i = 0; i < nearestNeighborsWithDistance.size(); i++) {
                Object[] result = nearestNeighborsWithDistance.get(i);
                Long chunkId = (Long) result[0];
                Double distance = (Double) result[1];
                retrievalResults.add(new RetrievalResultDTO(chunkId, distance, i + 1));
                chunkIds.add(chunkId);
            }

            // Populate retrieval results with chunk details
            List<DocumentChunk> chunks = populateRetrievalResultsWithChunkDetails(retrievalResults, chunkIds);
            long retrievalEnd = System.currentTimeMillis();
            trace.setRetrievalLatencyMs(retrievalEnd - retrievalStart);
            trace.setRetrievedChunks(retrievalResults);

            // Check confidence threshold - if top chunk distance is greater than threshold, return fallback
            if (!retrievalResults.isEmpty() && retrievalResults.get(0).getDistance() > confidenceThreshold) {
                trace.setLlmResponse("I could not confidently determine this from the policy.");
                trace.setLlmLatencyMs(0L);
                langChainTraceService.sendTrace(trace);
                return new VectorSearchAnswerResponseDTO("I could not confidently determine this from the policy.", retrievalResults);
            }

            List<String> chunkTexts = chunks.stream()
                    .map(DocumentChunk::getChunkText)
                    .collect(Collectors.toList());

            // Step 4: Build context from retrieved chunks
            String context = buildContextFromChunks(chunkTexts);

            // Step 5: Build prompt
            String systemPrompt = promptProperties.getSystemPrompt();
            String userPrompt = promptProperties.formatUserPrompt(request.getQuestion(), context);
            String fullPrompt = "System: " + systemPrompt + "\nUser: " + userPrompt;
            trace.setPrompt(fullPrompt);

            // Step 6: Generate LLM answer
            long llmStart = System.currentTimeMillis();
            String answer = llmFactory.getActiveProvider().generateResponse(systemPrompt, userPrompt);
            long llmEnd = System.currentTimeMillis();
            trace.setLlmLatencyMs(llmEnd - llmStart);
            trace.setLlmResponse(answer);

            langChainTraceService.sendTrace(trace);

            return new VectorSearchAnswerResponseDTO(answer, retrievalResults);

        } catch (Exception e) {
            trace.setLlmResponse("Error: " + e.getMessage());
            langChainTraceService.sendTrace(trace);
            throw new RuntimeException("Vector search answer generation failed: " + e.getMessage(), e);
        }
    }

    private List<DocumentChunk> populateRetrievalResultsWithChunkDetails(List<RetrievalResultDTO> retrievalResults, List<Long> chunkIds) {
        List<DocumentChunk> chunks = documentChunkRepository.findAllById(chunkIds);

        // Map chunk IDs to DocumentChunk for easy lookup
        Map<Long, DocumentChunk> chunkMap = chunks.stream()
                .collect(Collectors.toMap(DocumentChunk::getId, chunk -> chunk));

        // Update retrieval results with document chunk order and text
        for (RetrievalResultDTO result : retrievalResults) {
            DocumentChunk chunk = chunkMap.get(result.getChunkId());
            if (chunk != null) {
                result.setDocumentChunkOrder(chunk.getChunkOrder());
                result.setChunkText(chunk.getChunkText());
            }
        }

        return chunks;
    }

    private String arrayToPostgresVector(float[] array) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
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
}
