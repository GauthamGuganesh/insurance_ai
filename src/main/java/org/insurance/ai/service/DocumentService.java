package org.insurance.ai.service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.insurance.ai.dto.DocumentUploadRequestDTO;
import org.insurance.ai.model.Document;
import org.insurance.ai.model.DocumentChunk;
import org.insurance.ai.model.DocumentEmbedding;
import org.insurance.ai.repository.DocumentEmbeddingRepository;
import org.insurance.ai.repository.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private PdfExtractionService pdfExtractionService;
	

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private DocumentEmbeddingRepository documentEmbeddingRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

	@Transactional
    public Document uploadDocument(DocumentUploadRequestDTO request) throws IOException {
        logger.info("Starting document upload for company: {}, policy: {}, type: {}", 
                   request.getCompanyName(), request.getPolicyName(), request.getDocumentType());
        
        // Create folder structure: uploads/companyName/policyName/
        Path companyPath = Paths.get(uploadDir, sanitizeFolderName(request.getCompanyName()));
        Path policyPath = companyPath.resolve(sanitizeFolderName(request.getPolicyName()));
        
        logger.debug("Creating directory structure: {}", policyPath);
        // Create directories if they don't exist
        Files.createDirectories(policyPath);
        
        // Generate unique filename
        String originalFilename = request.getFile().getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        logger.debug("Generated unique filename: {} for original file: {}", uniqueFilename, originalFilename);
        
        // Save file to disk
        Path filePath = policyPath.resolve(uniqueFilename);
        logger.info("Saving file to: {}", filePath);
        Files.copy(request.getFile().getInputStream(), filePath);
        logger.info("File saved successfully. Size: {} bytes", request.getFile().getSize());
        
        // Save document metadata to database
        Document document = new Document();
        document.setCompanyName(request.getCompanyName());
        document.setPolicyName(request.getPolicyName());
        document.setDocumentType(request.getDocumentType());
        document.setUploadedAt(LocalDateTime.now());
		document.setFilePath(filePath.toAbsolutePath().toString());
        
        logger.info("Saving document metadata to database");
        Document savedDocument = documentRepository.save(document);
        logger.info("Document saved successfully with ID: {}", savedDocument.getId());
        
        // Extract and chunk PDF content if it's a PDF file
        if (pdfExtractionService.isPdfFile(request.getFile())) {
            try {
                List<DocumentChunk> chunks = pdfExtractionService.extractAndChunkPdfContent(request.getFile(), savedDocument);
                logger.info("PDF content extracted and chunked successfully for file: {} ({} chunks created)", originalFilename, chunks.size());

                // Generate and store embeddings for each chunk
                for (DocumentChunk chunk : chunks) {
                    try {
                        float[] embedding = embeddingService.generateEmbedding(chunk.getChunkText());
                        DocumentEmbedding documentEmbedding = new DocumentEmbedding();
                        documentEmbedding.setChunkId(chunk.getId());
                        documentEmbedding.setEmbedding(embedding);
                        documentEmbedding.setEmbeddingModel("all-minilm-l6-v2");
                        documentEmbeddingRepository.save(documentEmbedding);
                        logger.debug("Generated and stored embedding for chunk ID: {}", chunk.getId());
                    } catch (Exception e) {
                        logger.error("Failed to generate embedding for chunk ID {}: {}", chunk.getId(), e.getMessage());
                    }
                }
                logger.info("Embeddings generated and stored for {} chunks", chunks.size());
            } catch (Exception e) {
                logger.warn("Failed to extract and chunk PDF content from {}: {}", originalFilename, e.getMessage());
            }
        }
        
        return savedDocument;
    }

    private String sanitizeFolderName(String folderName) {
        // Remove special characters and replace spaces with underscores
        return folderName.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", "_");
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElse(null);
    }
}
