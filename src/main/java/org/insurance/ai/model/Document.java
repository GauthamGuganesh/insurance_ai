package org.insurance.ai.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


/**
 * Document is a model class that represents a document related to an insurance policy.
 * It contains information about the document such as the company name, policy name, document type and the time the document was created.
 *
 * @author Yudong Liu
 * @version 1.0
 */
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String companyName;

    private String policyName;

    private String documentType;
	
	private String filePath;

    private LocalDateTime uploadedAt;

    public Document() {}

    public Document(Long id, String companyName, String policyName, String documentType, LocalDateTime uploadedAt) {
        this.id = id;
        this.companyName = companyName;
        this.policyName = policyName;
        this.documentType = documentType;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
