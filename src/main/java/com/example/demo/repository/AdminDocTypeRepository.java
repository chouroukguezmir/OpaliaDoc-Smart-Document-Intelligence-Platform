package com.example.demo.repository;

import com.example.demo.model.AdminDocType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdminDocTypeRepository
        extends MongoRepository<AdminDocType, String> {

    Optional<AdminDocType> findByCode(String code);
}