package com.example.demo.controller;

import com.example.demo.model.ScanSession;
import com.example.demo.service.ScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class ScanController {

    private final ScanService scanService;

    @PostMapping("/upload")
    public ResponseEntity<ScanSession> upload(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {

        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        String username = auth != null ? auth.getName() : "admin";
        log.info("Upload : {} | par : {}", file.getOriginalFilename(), username);

        ScanSession session = scanService.scanDocument(file, username);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScanSession> getSession(@PathVariable String id) {
        try {
            return ResponseEntity.ok(scanService.getSessionById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}