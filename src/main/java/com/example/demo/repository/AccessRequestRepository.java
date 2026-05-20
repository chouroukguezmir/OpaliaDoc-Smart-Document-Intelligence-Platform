package com.example.demo.repository;

import com.example.demo.model.AccessRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccessRequestRepository extends MongoRepository<AccessRequest, String> {
}