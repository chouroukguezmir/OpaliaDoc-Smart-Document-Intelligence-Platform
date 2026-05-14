package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "scan_sessions")
public class ScanSession {

    @Id
    private String id;

    private String scannedBy;

    @Indexed
    private LocalDateTime scannedAt = LocalDateTime.now();

    private String fileOriginalName;
    private String fileType;
    private String filePath;
    private Boolean isHandwritten;

    @Indexed
    private String status;

    @Indexed
    private String documentType;

    private String documentId;
}