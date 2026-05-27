package org.insurance.ai.repository;

import org.insurance.ai.model.DocumentEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentEmbeddingRepository extends JpaRepository<DocumentEmbedding, Long> {

    List<DocumentEmbedding> findByChunkId(Long chunkId);

    void deleteByChunkId(Long chunkId);

    @Query(value = "SELECT * FROM document_embeddings ORDER BY embedding <=> CAST(:queryVector AS vector) LIMIT :limit", nativeQuery = true)
    List<DocumentEmbedding> findNearestNeighbors(@Param("queryVector") String queryVector, @Param("limit") int limit);
}
