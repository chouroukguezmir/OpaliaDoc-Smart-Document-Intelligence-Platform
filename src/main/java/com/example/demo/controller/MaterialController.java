package com.example.demo.controller;

import com.example.demo.model.Material;
import com.example.demo.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    public ResponseEntity<List<Material>> getAll() {
        return ResponseEntity.ok(materialService.getAllMaterials());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Material> getOne(@PathVariable String id) {
        try {
            return ResponseEntity.ok(materialService.getMaterialById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Télécharger le document d'origine joint au matériel
    @GetMapping("/{id}/file")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable String id) {
        try {
            Material mat = materialService.getMaterialById(id);
            if (mat.getAttachedFile() == null) return ResponseEntity.notFound().build();

            File file = new File(mat.getAttachedFile());
            if (!file.exists()) return ResponseEntity.notFound().build();

            String contentType = Files.probeContentType(file.toPath());
            return ResponseEntity.ok()
                    .contentType(contentType != null
                            ? MediaType.parseMediaType(contentType)
                            : MediaType.APPLICATION_OCTET_STREAM)
                    .body(new FileSystemResource(file));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}