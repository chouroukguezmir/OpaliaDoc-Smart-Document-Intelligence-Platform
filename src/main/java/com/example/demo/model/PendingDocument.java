package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "pending_documents")
public class PendingDocument {

    @Id
    private String id;

    private String documentType;       // EMPLOYEE, TYPE_A, TYPE_B, TYPE_C, UNKNOWN
    private boolean isHandwritten;
    private String handwritingQuality; // GOOD, MEDIUM, POOR

    private String originalFilePath;
    private String digitalizedPdfPath; // chemin du PDF généré (original ou digitalisé)

    private Map<String, String> extractedFields;
    private String rawText;

    private String scannedBy;
    private LocalDateTime scannedAt = LocalDateTime.now();

    private String status; // PENDING_CONFIRMATION
}