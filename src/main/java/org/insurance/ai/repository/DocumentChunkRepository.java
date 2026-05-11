package org.insurance.ai.repository;

import org.insurance.ai.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    
    List<DocumentChunk> findByDocumentIdOrderByChunkOrder(Long documentId);
    
    void deleteByDocumentId(Long documentId);
}
