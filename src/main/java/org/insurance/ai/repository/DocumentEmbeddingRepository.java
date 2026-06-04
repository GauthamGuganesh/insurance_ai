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

    /**
     * This method finds the nearest neighbors to a given query vector and returns them with their distances.
     * The distance is calculated using the cosine function, which gives a value between -1 and 1. It represents the cosine of the angle between the two vectors.
     * A distance of 1 indicates that the vectors are opposite, a distance of -1 indicates that the vectors are the same, and a distance of 0 indicates that the vectors are orthogonal.
     *
	 * So the smaller the distance, the more similar the vectors are and more relevant the document chunk is to the query.
	 *
     * @param queryVector The vector for which we want to find the nearest neighbors.
     * @param documentId The document ID to filter embeddings by.
     * @param limit The maximum number of neighbors to retrieve.
     * @return A list of objects, where each object contains a DocumentEmbedding object and its distance to the query vector.
	 */
	@Query(value = "SELECT de.chunk_id as chunkId, de.embedding <=> CAST(:queryVector AS vector) as distance " +
			"FROM document_embeddings de " +
			"JOIN document_chunk dc ON de.chunk_id = dc.id " +
			"WHERE dc.document_id = :documentId " +
			"ORDER BY de.embedding <=> CAST(:queryVector AS vector) LIMIT :limit", nativeQuery = true)
	List<Object[]> findNearestNeighborsWithDistance(@Param("queryVector") String queryVector, @Param("documentId") Long documentId, @Param("limit") int limit);
}
