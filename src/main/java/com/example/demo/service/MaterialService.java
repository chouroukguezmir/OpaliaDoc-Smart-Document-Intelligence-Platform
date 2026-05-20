package com.example.demo.service;

import com.example.demo.model.Material;

import java.util.List;

public interface MaterialService {

    List<Material> getAllMaterials();

    Material getMaterialById(String id);
}