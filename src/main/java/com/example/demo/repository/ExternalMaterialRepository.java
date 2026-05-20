package com.example.demo.repository;

import com.example.demo.model.ExternalMaterial;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExternalMaterialRepository extends MongoRepository<ExternalMaterial, String> {
}