package com.example.demo.repository;

import com.example.demo.model.EmployeeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EmployeeDocumentRepository
        extends MongoRepository<EmployeeDocument, String> {

    List<EmployeeDocument> findByStatus(String status);
    long countByStatus(String status);
    long countByIsHandwritten(Boolean isHandwritten);
}