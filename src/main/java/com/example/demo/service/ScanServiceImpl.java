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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScanServiceImpl implements ScanService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private final ScanSessionRepository      scanSessionRepository;
    private final AdminDocumentRepository    adminDocumentRepository;
    private final EmployeeDocumentRepository employeeDocumentRepository;
    private final EmployeeRepository         employeeRepository;
    private final DailyStatRepository        dailyStatRepository;
    private final FileTextExtractorService   fileTextExtractor;
    private final AiClassifierService        aiClassifier;
    private final PdfGeneratorService        pdfGenerator;
    private final NlpService                 nlpService;

    @Override
    public ScanSession scanDocument(MultipartFile file, String adminUsername) {

        // 1. Sauvegarde du fichier
        String filePath    = saveFile(file);
        String contentType = file.getContentType();

        // 2. Création ScanSession
        ScanSession session = new ScanSession();
        session.setScannedBy(adminUsername);
        session.setFileOriginalName(file.getOriginalFilename());
        session.setFileType(contentType);
        session.setFilePath(filePath);
        session.setStatus("PROCESSING");
        scanSessionRepository.save(session);

        try {
            File physicalFile = new File(filePath);

            // 3. Extraction du texte
            String rawText        = fileTextExtractor.extractText(physicalFile, contentType);
            boolean isHandwritten = fileTextExtractor.isHandwritten(physicalFile, contentType);

            session.setIsHandwritten(isHandwritten);
            log.info("Texte extrait : {} chars | manuscrit : {}",
                    rawText.length(), isHandwritten);

            // 4. Classification
            String docType = aiClassifier.detectDocumentType(rawText);
            session.setDocumentType(docType);
            log.info("Type détecté : {}", docType);

            // 5. Traitement selon le type
            if ("EMPLOYEE".equals(docType)) {
                handleEmployee(session, rawText, isHandwritten, filePath, file);
            } else if (docType.startsWith("TYPE_")) {
                handleAdminDoc(session, rawText, docType, isHandwritten, filePath, file);
            } else {
                session.setStatus("FAILED");
                log.warn("Document non reconnu");
            }

            // 6. Mise à jour stats journalières
            updateDailyStats(docType, isHandwritten);

        } catch (Exception e) {
            session.setStatus("FAILED");
            log.error("Erreur scan : {}", e.getMessage());
        }

        return scanSessionRepository.save(session);
    }

    @Override
    public ScanSession getSessionById(String id) {
        return scanSessionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Session introuvable : " + id));
    }

    // ── Document Employé ──────────────────────────────────────────
    private void handleEmployee(ScanSession session, String rawText,
                                boolean isHandwritten, String filePath,
                                MultipartFile file) {

        EmployeeDocument empDoc = new EmployeeDocument();
        empDoc.setScanSessionId(session.getId());
        empDoc.setRawText(rawText);
        empDoc.setIsHandwritten(isHandwritten);
        empDoc.setOriginalFilePath(filePath);
        empDoc.setStatus("PENDING");
        empDoc.setConfidence(0.75);
        employeeDocumentRepository.save(empDoc);

        Employee employee = aiClassifier.mapToEmployee(rawText);
        employee.setSourceDocumentId(empDoc.getId());
        employeeRepository.save(employee);

        String pdfPath = pdfGenerator.generateDigitalizedPdf(
                rawText, "EMPLOYEE", file.getOriginalFilename());
        empDoc.setDigitalizedPdfPath(pdfPath);
        empDoc.setStatus("ANALYZED");
        empDoc.setAnalyzedAt(LocalDateTime.now());
        employeeDocumentRepository.save(empDoc);

        session.setDocumentId(empDoc.getId());
        session.setStatus("ANALYZED");
    }

    // ── Document Administratif ────────────────────────────────────
    private void handleAdminDoc(ScanSession session, String rawText,
                                String subType, boolean isHandwritten,
                                String filePath, MultipartFile file) {

        // Essai NLP Python en priorité
        String finalSubType = subType;

        if (nlpService.isAvailable()) {
            Map<String, Object> nlpResult =
                    nlpService.analyzeDocument(new File(filePath));

            if (!nlpResult.isEmpty()) {
                String nlpType = (String) nlpResult.get("document_type");
                if (nlpType != null && nlpType.startsWith("TYPE_")) {
                    finalSubType = nlpType;
                    log.info("NLP override subType: {}", finalSubType);
                }
            }
        }

        AdminDocument adminDoc = aiClassifier
                .mapToAdminDocument(rawText, finalSubType);
        adminDoc.setScanSessionId(session.getId());
        adminDoc.setIsHandwritten(isHandwritten);
        adminDoc.setOriginalFilePath(filePath);
        adminDoc.setStatus("PENDING");
        adminDoc.setConfidence(0.80);

        adminDocumentRepository.save(adminDoc);

        String pdfPath = pdfGenerator.generateDigitalizedPdf(
                rawText, finalSubType, file.getOriginalFilename());
        adminDoc.setDigitalizedPdfPath(pdfPath);
        adminDoc.setStatus("ANALYZED");
        adminDoc.setAnalyzedAt(LocalDateTime.now());
        adminDocumentRepository.save(adminDoc);

        session.setDocumentId(adminDoc.getId());
        session.setStatus("ANALYZED");
    }

    // ── Sauvegarde fichier ────────────────────────────────────────
    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath))
                Files.createDirectories(uploadPath);

            String ext      = getExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + ext;
            Path filePath   = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath,
                    StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Erreur sauvegarde : " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".bin";
        int dot = filename.lastIndexOf(".");
        return dot != -1 ? filename.substring(dot) : ".bin";
    }

    // ── Statistiques journalières ─────────────────────────────────
    private void updateDailyStats(String docType, boolean isHandwritten) {
        LocalDate today = LocalDate.now();
        DailyStat stat  = dailyStatRepository
                .findByStatDate(today)
                .orElseGet(() -> {
                    DailyStat s = new DailyStat();
                    s.setStatDate(today);
                    return s;
                });

        stat.setScannedTotal(stat.getScannedTotal() + 1);
        stat.setAnalyzedTotal(stat.getAnalyzedTotal() + 1);

        switch (docType) {
            case "EMPLOYEE" -> {
                stat.setScannedEmployee(stat.getScannedEmployee() + 1);
                stat.setAnalyzedEmployee(stat.getAnalyzedEmployee() + 1);
            }
            case "TYPE_A", "TYPE_B", "TYPE_C" -> {
                stat.setScannedAdministrative(stat.getScannedAdministrative() + 1);
                stat.setAnalyzedAdministrative(stat.getAnalyzedAdministrative() + 1);
                stat.getBySubType().merge(docType, 1L, Long::sum);
            }
            default -> stat.setScannedUnknown(stat.getScannedUnknown() + 1);
        }

        if (isHandwritten) stat.setHandwritten(stat.getHandwritten() + 1);
        stat.setPdfsGenerated(stat.getPdfsGenerated() + 1);

        dailyStatRepository.save(stat);
    }
}