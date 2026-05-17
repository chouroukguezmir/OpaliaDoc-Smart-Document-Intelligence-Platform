package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "scan_sessions")
public class ScanSession {

    @Id
    private String id;

    private String scannedBy;
    private String fileOriginalName;
    private String fileType;
    private String filePath;
    private String status;         // PROCESSING, PENDING_CONFIRMATION, FAILED
    private String documentType;   // EMPLOYEE, TYPE_A, TYPE_B, TYPE_C
    private Boolean isHandwritten;
    private String pendingDocumentId; // référence au PendingDocument créé
    private LocalDateTime createdAt = LocalDateTime.now();
}