package com.example.demo.controller;

import com.example.demo.model.ScanSession;
import com.example.demo.service.ScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class ScanController {

    private  ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    /**
     * Upload et analyse d'un document PDF
     * POST /api/scan/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<ScanSession> uploadDocument(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String adminUsername = authentication.getName();
        ScanSession session = scanService.scanDocument(file, adminUsername);
        return ResponseEntity.ok(session);
    }

    /**
     * Récupère une session de scan par ID
     * GET /api/scan/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScanSession> getSession(@PathVariable String id) {
        return ResponseEntity.ok(scanService.getSessionById(id));
    }
}