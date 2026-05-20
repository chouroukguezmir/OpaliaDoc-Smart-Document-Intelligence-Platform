package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ScanSessionRepository       scanSessionRepository;
    private final PendingDocumentRepository   pendingDocumentRepository;
    private final NlpService                  nlpService;
    private final GeminiService               geminiService;
    private final AiClassifierService         aiClassifier;
    private final PdfGeneratorService         pdfGenerator;
    private final FileTextExtractorService    fileTextExtractor;

    @Override
    public ScanSession scanDocument(MultipartFile file, String adminUsername) {

        // 1. Sauvegarder le fichier physique
        String filePath = saveFile(file);
        File physicalFile = new File(filePath);

        // 2. Créer la session de scan
        ScanSession session = new ScanSession();
        session.setScannedBy(adminUsername);
        session.setFileOriginalName(file.getOriginalFilename());
        session.setFileType(file.getContentType());
        session.setFilePath(filePath);
        session.setStatus("PROCESSING");
        scanSessionRepository.save(session);

        try {
            // 3. Analyse : Gemini AI en priorité, puis Python NLP, sinon OCR Java
            Map<String, Object> nlpResult = new HashMap<>();

            if (geminiService.isConfigured()) {
                log.info("Analyse via Gemini AI");
                nlpResult = geminiService.analyzeDocument(physicalFile, file.getContentType());
            }
            if (nlpResult.isEmpty() && nlpService.isAvailable()) {
                log.info("Gemini indisponible — analyse via Python NLP");
                nlpResult = nlpService.analyzeDocument(physicalFile);
            }
            if (nlpResult.isEmpty()) {
                log.warn("Aucun service AI disponible — fallback OCR Java + mots-clés");
            }

            // 4. Extraire les données du résultat NLP
            String rawText         = extractString(nlpResult, "raw_text");
            boolean isHandwritten  = extractBoolean(nlpResult, "is_handwritten");
            String handwritingQuality = extractString(nlpResult, "handwriting_quality");
            String docType         = extractString(nlpResult, "document_type");
            String digitisedText   = extractString(nlpResult, "digitised_text");

            @SuppressWarnings("unchecked")
            Map<String, String> extractedFields =
                    (Map<String, String>) nlpResult.getOrDefault("extracted_fields", new HashMap<>());

            // Fallback si NLP indisponible ou résultat vide
            if (rawText == null || rawText.isBlank()) {
                rawText = fileTextExtractor.extractText(physicalFile, file.getContentType());
                isHandwritten = fileTextExtractor.isHandwritten(physicalFile, file.getContentType());
            }
            if (docType == null || docType.isBlank()) {
                docType = aiClassifier.detectDocumentType(rawText);
            }
            if (digitisedText == null) digitisedText = rawText;

            // 5. Générer le PDF selon le type
            String pdfPath;
            if (isHandwritten) {
                // PDF digitalisé avec le texte reconnu
                pdfPath = pdfGenerator.generateDigitisedPdf(
                        digitisedText, docType,
                        file.getOriginalFilename(),
                        extractedFields,
                        handwritingQuality != null ? handwritingQuality : "MEDIUM"
                );
            } else {
                // PDF standard du contenu scanné
                pdfPath = pdfGenerator.generateScanPdf(
                        rawText, docType,
                        file.getOriginalFilename(),
                        extractedFields
                );
            }

            // 6. Créer le PendingDocument — en attente de confirmation admin
            PendingDocument pending = new PendingDocument();
            pending.setDocumentType(docType);
            pending.setHandwritten(isHandwritten);
            pending.setHandwritingQuality(handwritingQuality);
            pending.setOriginalFilePath(filePath);
            pending.setDigitalizedPdfPath(pdfPath);
            pending.setExtractedFields(extractedFields);
            pending.setRawText(isHandwritten ? digitisedText : rawText);
            pending.setScannedBy(adminUsername);
            pending.setStatus("PENDING_CONFIRMATION");
            pendingDocumentRepository.save(pending);

            // 7. Mettre à jour la session
            session.setDocumentType(docType);
            session.setIsHandwritten(isHandwritten);
            session.setPendingDocumentId(pending.getId());
            session.setStatus("PENDING_CONFIRMATION");

        } catch (Exception e) {
            log.error("Erreur traitement scan : {}", e.getMessage(), e);
            session.setStatus("FAILED");
        }

        return scanSessionRepository.save(session);
    }

    @Override
    public ScanSession getSessionById(String id) {
        return scanSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Session introuvable : " + id));
    }

    // ── Utilitaires extraction résultat NLP ───────────────────
    private String extractString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private boolean extractBoolean(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) return Boolean.parseBoolean((String) val);
        return false;
    }

    // ── Sauvegarde du fichier uploadé ─────────────────────────
    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String ext      = getExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + ext;
            Path filePath   = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Erreur sauvegarde fichier : " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".bin";
        int dot = filename.lastIndexOf('.');
        return dot != -1 ? filename.substring(dot) : ".bin";
    }
}