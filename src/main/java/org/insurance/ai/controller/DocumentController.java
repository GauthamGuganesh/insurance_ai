package org.insurance.ai.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.insurance.ai.dto.DocumentUploadRequestDTO;
import org.insurance.ai.model.Document;
import org.insurance.ai.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadDocument(HttpServletRequest servletRequest, @ModelAttribute DocumentUploadRequestDTO request) {
        logger.info("Received document upload request: {}", request.getFile().getOriginalFilename());
        
        try {
            Document savedDocument = documentService.uploadDocument(request);
            
            logger.info("Document upload completed successfully for document ID: {}", savedDocument.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Document uploaded successfully");
            response.put("documentId", savedDocument.getId());
            response.put("fileName", request.getFile().getOriginalFilename());
            response.put("uploadedAt", savedDocument.getUploadedAt());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to upload document: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to upload document: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
	
	
}
