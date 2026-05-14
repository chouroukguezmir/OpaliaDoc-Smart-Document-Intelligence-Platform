package com.example.demo.repository;

import com.example.demo.model.AdminDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AdminDocumentRepository
        extends MongoRepository<AdminDocument, String> {

    List<AdminDocument> findByStatus(String status);
    List<AdminDocument> findBySubType(String subType);
    long countByStatus(String status);
    long countBySubType(String subType);
    long countByIsHandwritten(Boolean isHandwritten);  // ← NOUVEAU
}