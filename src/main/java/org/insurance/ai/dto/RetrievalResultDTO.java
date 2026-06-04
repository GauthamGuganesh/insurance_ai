package org.insurance.ai.dto;

public class RetrievalResultDTO {
    private Long chunkId;
    private double distance;
    private int chunkOrder;
    private Integer documentChunkOrder;
    private String chunkText;

    public RetrievalResultDTO() {
    }

    public RetrievalResultDTO(Long chunkId, double distance) {
        this.chunkId = chunkId;
        this.distance = distance;
    }

    public RetrievalResultDTO(Long chunkId, double distance, int chunkOrder) {
        this.chunkId = chunkId;
        this.distance = distance;
        this.chunkOrder = chunkOrder;
    }

    public RetrievalResultDTO(Long chunkId, double distance, int chunkOrder, Integer documentChunkOrder, String chunkText) {
        this.chunkId = chunkId;
        this.distance = distance;
        this.chunkOrder = chunkOrder;
        this.documentChunkOrder = documentChunkOrder;
        this.chunkText = chunkText;
    }

    public Long getChunkId() {
        return chunkId;
    }

    public void setChunkId(Long chunkId) {
        this.chunkId = chunkId;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getChunkOrder() {
        return chunkOrder;
    }

    public void setChunkOrder(int chunkOrder) {
        this.chunkOrder = chunkOrder;
    }

    public Integer getDocumentChunkOrder() {
        return documentChunkOrder;
    }

    public void setDocumentChunkOrder(Integer documentChunkOrder) {
        this.documentChunkOrder = documentChunkOrder;
    }

    public String getChunkText() {
        return chunkText;
    }

    public void setChunkText(String chunkText) {
        this.chunkText = chunkText;
    }

    @Override
    public String toString() {
        return "RetrievalResultDTO{" +
                "chunkId=" + chunkId +
                ", distance=" + distance +
                ", chunkOrder=" + chunkOrder +
                ", documentChunkOrder=" + documentChunkOrder +
                ", chunkText='" + chunkText + '\'' +
                '}';
    }
	
}
