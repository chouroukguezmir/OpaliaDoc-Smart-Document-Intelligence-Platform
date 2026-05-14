package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AdminDocument;
import com.example.demo.repository.AdminDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDocumentServiceImpl implements AdminDocumentService {

    private final AdminDocumentRepository adminDocumentRepository;

    @Override
    public AdminDocument create(AdminDocument document) {
        document.setStatus("PENDING");
        return adminDocumentRepository.save(document);
    }

    @Override
    public List<AdminDocument> getAll() {
        return adminDocumentRepository.findAll();
    }

    @Override
    public AdminDocument getById(String id) {
        return adminDocumentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document introuvable : " + id));
    }

    @Override
    public AdminDocument save(AdminDocument document) {
        return adminDocumentRepository.save(document);
    }

    @Override
    public AdminDocument update(String id, AdminDocument document) {
        AdminDocument existing = getById(id);
        document.setId(existing.getId());
        document.setCreatedAt(existing.getCreatedAt());
        return adminDocumentRepository.save(document);
    }

    @Override
    public void delete(String id) {
        adminDocumentRepository.deleteById(id);
    }

    @Override
    public List<AdminDocument> getByStatus(String status) {
        return adminDocumentRepository.findByStatus(status);
    }

    @Override
    public List<AdminDocument> getBySubType(String subType) {
        return adminDocumentRepository.findBySubType(subType);
    }
}