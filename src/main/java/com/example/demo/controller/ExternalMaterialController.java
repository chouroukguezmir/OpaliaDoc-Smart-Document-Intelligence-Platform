package com.example.demo.controller;

import com.example.demo.model.ExternalMaterial;
import com.example.demo.service.ExternalMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/external-materials")
@RequiredArgsConstructor
public class ExternalMaterialController {

    private final ExternalMaterialService service;

    @GetMapping
    public ResponseEntity<List<ExternalMaterial>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExternalMaterial> getOne(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable String id) {
        try {
            ExternalMaterial em = service.getById(id);
            if (em.getAttachedFile() == null) return ResponseEntity.notFound().build();

            File file = new File(em.getAttachedFile());
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