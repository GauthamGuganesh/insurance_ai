package org.insurance.ai.service;

import org.insurance.ai.model.DocumentChunk;
import org.insurance.ai.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChunkRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(ChunkRetrievalService.class);

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    public List<String> getChunksByDocumentId(Long documentId) {
        logger.info("Retrieving chunks for document ID: {}", documentId);

        try {
            List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkOrder(documentId);
            
            List<String> chunkTexts = chunks.stream()
                    .map(DocumentChunk::getChunkText)
                    .collect(Collectors.toList());

            logger.info("Retrieved {} chunks for document ID: {}", chunkTexts.size(), documentId);
            return chunkTexts;

        } catch (Exception e) {
            logger.error("Failed to retrieve chunks for document ID {}: {}", documentId, e.getMessage(), e);
            throw new RuntimeException("Chunk retrieval failed: " + e.getMessage(), e);
        }
    }

    public List<DocumentChunk> getDocumentChunks(Long documentId) {
        logger.info("Retrieving document chunks for document ID: {}", documentId);

        try {
            List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkOrder(documentId);
            logger.info("Retrieved {} document chunks for document ID: {}", chunks.size(), documentId);
            return chunks;

        } catch (Exception e) {
            logger.error("Failed to retrieve document chunks for document ID {}: {}", documentId, e.getMessage(), e);
            throw new RuntimeException("Document chunk retrieval failed: " + e.getMessage(), e);
        }
    }
}
