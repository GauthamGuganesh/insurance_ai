package org.insurance.ai.dto;

public class AnswerGenerationRequestDTO {
    
    private String question;
    private Long documentId;
    
    public AnswerGenerationRequestDTO() {}
    
    public AnswerGenerationRequestDTO(String question, Long documentId) {
        this.question = question;
        this.documentId = documentId;
    }
    
    public String getQuestion() {
        return question;
    }
    
    public void setQuestion(String question) {
        this.question = question;
    }
    
    public Long getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
}
