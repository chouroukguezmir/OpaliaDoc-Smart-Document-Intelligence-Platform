package com.example.demo.service;

import com.example.demo.model.AdminDocument;

import java.util.List;

public interface AdminDocumentService {

    AdminDocument create(AdminDocument document);

    List<AdminDocument> getAll();

    AdminDocument getById(String id);

    AdminDocument save(AdminDocument document);

    AdminDocument update(String id, AdminDocument document);

    void delete(String id);

    List<AdminDocument> getByStatus(String status);

    List<AdminDocument> getBySubType(String subType);
}