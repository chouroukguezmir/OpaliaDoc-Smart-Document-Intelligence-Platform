package com.example.demo.controller;

import com.example.demo.model.PendingDocument;
import com.example.demo.service.ConfirmationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pending")
@RequiredArgsConstructor
public class ConfirmationController {

    private final ConfirmationService confirmationService;

    // Récupérer tous les documents en attente
    @GetMapping
    public ResponseEntity<List<PendingDocument>> getAllPending() {
        return ResponseEntity.ok(confirmationService.getAllPending());
    }

    // Confirmer un document → archivage
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Map<String, String>> confirm(@PathVariable String id) {
        try {
            String docType = confirmationService.confirmDocument(id);
            return ResponseEntity.ok(Map.of(
                    "status", "ARCHIVED",
                    "documentType", docType,
                    "message", "Document archivé avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Rejeter un document → suppression
    @PostMapping("/{id}/reject")
    public ResponseEntity<Map<String, String>> reject(@PathVariable String id) {
        try {
            confirmationService.rejectDocument(id);
            return ResponseEntity.ok(Map.of(
                    "status", "REJECTED",
                    "message", "Document rejeté et supprimé"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Mettre à jour les champs extraits (édition avant confirmation)
    @PutMapping("/{id}")
    public ResponseEntity<PendingDocument> update(
            @PathVariable String id,
            @RequestBody Map<String, String> fields) {
        try {
            return ResponseEntity.ok(confirmationService.updateFields(id, fields));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Servir le fichier scanné d'origine
    @GetMapping("/{id}/file")
    public ResponseEntity<FileSystemResource> getFile(@PathVariable String id) {
        try {
            PendingDocument pending = confirmationService.getPendingById(id);
            if (pending.getOriginalFilePath() == null)
                return ResponseEntity.notFound().build();

            File file = new File(pending.getOriginalFilePath());
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

    // Servir le PDF généré pour prévisualisation
    @GetMapping("/{id}/pdf")
    public ResponseEntity<FileSystemResource> getPdf(@PathVariable String id) {
        PendingDocument pending = confirmationService.getAllPending().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (pending == null || pending.getDigitalizedPdfPath() == null)
            return ResponseEntity.notFound().build();

        File pdfFile = new File(pending.getDigitalizedPdfPath());
        if (!pdfFile.exists()) return ResponseEntity.notFound().build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline().filename("document_preview.pdf").build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(new FileSystemResource(pdfFile));
    }
}