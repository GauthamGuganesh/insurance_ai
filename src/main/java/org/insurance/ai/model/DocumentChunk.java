package org.insurance.ai.model;

import jakarta.persistence.*;

@Entity
public class DocumentChunk {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "document_id")
	private Document document;

	@Column(name = "chunk_text")
	private String chunkText;

	@Column(name = "chunk_order")
	private Integer chunkOrder;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getChunkText() {
		return chunkText;
	}
	public void setChunkText(String chunkText) {
		this.chunkText = chunkText;
	}
	public Integer getChunkOrder() {
		return chunkOrder;
	}
	public void setChunkOrder(Integer chunkOrder) {
		this.chunkOrder = chunkOrder;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
}
