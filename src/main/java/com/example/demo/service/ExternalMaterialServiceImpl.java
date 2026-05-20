package com.example.demo.service;

import com.example.demo.model.ExternalMaterial;
import com.example.demo.repository.ExternalMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExternalMaterialServiceImpl implements ExternalMaterialService {

    private final ExternalMaterialRepository repository;

    @Override
    public List<ExternalMaterial> getAll() {
        return repository.findAll();
    }

    @Override
    public ExternalMaterial getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matériel externe introuvable : " + id));
    }
}