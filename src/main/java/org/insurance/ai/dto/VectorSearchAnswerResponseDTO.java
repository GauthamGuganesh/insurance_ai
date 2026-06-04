package org.insurance.ai.dto;

import java.util.List;

public class VectorSearchAnswerResponseDTO {
    private String answer;
    private List<RetrievalResultDTO> sources;

    public VectorSearchAnswerResponseDTO() {
    }

    public VectorSearchAnswerResponseDTO(String answer, List<RetrievalResultDTO> sources) {
        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<RetrievalResultDTO> getSources() {
        return sources;
    }

    public void setSources(List<RetrievalResultDTO> sources) {
        this.sources = sources;
    }
}
