package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
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
    private Boolean isHandwritten;
    private String originalFilePath;
    private String digitalizedPdfPath;
    private String status; // ARCHIVED
    private LocalDateTime analyzedAt;
    private LocalDateTime archivedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}