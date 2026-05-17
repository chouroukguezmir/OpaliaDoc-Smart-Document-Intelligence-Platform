package com.example.demo.repository;

import com.example.demo.model.PendingDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PendingDocumentRepository
        extends MongoRepository<PendingDocument, String> {

    List<PendingDocument> findByStatus(String status);
    List<PendingDocument> findByScannedBy(String scannedBy);
    long countByStatus(String status);
}