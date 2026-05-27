package org.insurance.ai.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    public float[] generateEmbedding(String text) {
        Embedding embedding = embeddingModel.embed(text).content();
        return embedding.vector();
    }
	
}
