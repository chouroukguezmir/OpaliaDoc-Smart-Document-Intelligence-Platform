package com.example.demo.service;

import com.example.demo.model.Material;
import com.example.demo.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    @Override
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Override
    public Material getMaterialById(String id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matériel introuvable : " + id));
    }
}