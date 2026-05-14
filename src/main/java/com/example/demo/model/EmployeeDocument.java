package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = "employee_documents")
public class EmployeeDocument {

    @Id
    private String id;

    private String scanSessionId;
    private String employeeId;

    private String rawText;
    private Map<String, Object> extractedFields;

    private Double confidence;

    @Indexed
    private Boolean isHandwritten;

    private String originalFilePath;
    private String digitalizedPdfPath;
    private String aiSummary;        // ← NOUVEAU

    @Indexed
    private String status;

    private LocalDateTime analyzedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}