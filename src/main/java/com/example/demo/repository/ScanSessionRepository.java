package com.example.demo.repository;

import com.example.demo.model.ScanSession;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScanSessionRepository extends MongoRepository<ScanSession, String> {
}