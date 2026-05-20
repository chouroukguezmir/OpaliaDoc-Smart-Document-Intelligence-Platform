package com.example.demo.service;

import com.example.demo.model.ExternalMaterial;

import java.util.List;

public interface ExternalMaterialService {

    List<ExternalMaterial> getAll();

    ExternalMaterial getById(String id);
}