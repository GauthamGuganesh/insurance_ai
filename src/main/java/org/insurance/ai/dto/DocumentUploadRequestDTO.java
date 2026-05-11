package org.insurance.ai.dto;

import org.springframework.web.multipart.MultipartFile;

public class DocumentUploadRequestDTO {
    
    private String companyName;
    
    private String policyName;
    
    private String documentType;
    
    private MultipartFile file;

    public DocumentUploadRequestDTO() {}

    public DocumentUploadRequestDTO(String companyName, String policyName, String documentType, MultipartFile file) {
        this.companyName = companyName;
        this.policyName = policyName;
        this.documentType = documentType;
        this.file = file;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
