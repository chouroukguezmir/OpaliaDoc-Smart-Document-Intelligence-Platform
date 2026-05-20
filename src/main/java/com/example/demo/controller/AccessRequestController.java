package com.example.demo.controller;

import com.example.demo.model.AccessRequest;
import com.example.demo.service.AccessRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/api/access-requests")
@RequiredArgsConstructor
public class AccessRequestController {

    private final AccessRequestService service;

    @GetMapping
    public ResponseEntity<List<AccessRequest>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccessRequest> getOne(@PathVariable String id) {
        try {
            return ResponseEntity.ok(service.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable String id) {
        try {
            AccessRequest ar = service.getById(id);
            if (ar.getAttachedFile() == null) return ResponseEntity.notFound().build();

            File file = new File(ar.getAttachedFile());
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