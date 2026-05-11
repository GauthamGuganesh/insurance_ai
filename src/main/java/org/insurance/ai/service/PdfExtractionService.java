package org.insurance.ai.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.insurance.ai.model.Document;
import org.insurance.ai.model.DocumentChunk;
import org.insurance.ai.repository.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PdfExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExtractionService.class);
    private static final int CHUNK_SIZE_WORDS = 500;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    public List<DocumentChunk> extractAndChunkPdfContent(MultipartFile file, Document pdfDocument) throws IOException {
        logger.info("Starting PDF extraction and chunking for file: {}", file.getOriginalFilename());
        
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            
            // Extract text content
            PDFTextStripper textStripper = new PDFTextStripper();
            String text = textStripper.getText(document);
            
            logger.info("Extracted {} pages of text from: {}", document.getNumberOfPages(), file.getOriginalFilename());
            logger.debug("Total extracted text length: {} characters", text.length());
            
            // Split text into chunks of 500 words
            List<DocumentChunk> chunks = splitTextIntoChunks(text, pdfDocument);
            
            logger.info("Created {} chunks from document: {}", chunks.size(), file.getOriginalFilename());
            
            return chunks;
            
        } catch (IOException e) {
            logger.error("Failed to extract PDF content from {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new IOException("PDF extraction failed: " + e.getMessage(), e);
        }
    }
    
    private List<DocumentChunk> splitTextIntoChunks(String text, Document pdfDocument) {
        List<DocumentChunk> chunks = new ArrayList<>();
        
        // Clean and normalize text
        String cleanedText = text.replaceAll("\\s+", " ").trim();
        
        // Split into words
        String[] words = cleanedText.split("\\s+");
        logger.debug("Total words extracted: {}", words.length);
        
        // Create chunks of 500 words each
        int chunkOrder = 0;
        for (int i = 0; i < words.length; i += CHUNK_SIZE_WORDS) {
            int endIndex = Math.min(i + CHUNK_SIZE_WORDS, words.length);
            String[] chunkWords = Arrays.copyOfRange(words, i, endIndex);
            String chunkText = String.join(" ", chunkWords);
            
            DocumentChunk chunk = new DocumentChunk();
            chunk.setDocument(pdfDocument);
            chunk.setChunkText(chunkText);
            chunk.setChunkOrder(chunkOrder);
            
            chunks.add(chunk);
            chunkOrder++;
            
            logger.debug("Created chunk {} with {} words", chunkOrder, chunkWords.length);
        }
        
		documentChunkRepository.saveAll(chunks);
        return chunks;
    }
    
    public boolean isPdfFile(MultipartFile file) {
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();
        
        return (contentType != null && contentType.equals("application/pdf")) ||
               (fileName != null && fileName.toLowerCase().endsWith(".pdf"));
    }
}
