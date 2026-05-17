package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmationService {

    private final PendingDocumentRepository   pendingRepo;
    private final AdminDocumentRepository     adminDocRepo;
    private final EmployeeDocumentRepository  empDocRepo;
    private final EmployeeRepository          employeeRepo;
    private final AiClassifierService         aiClassifier;

    // ── Confirmer un document → l'archiver dans la bonne collection
    public String confirmDocument(String pendingId) {

        PendingDocument pending = pendingRepo.findById(pendingId)
                .orElseThrow(() -> new RuntimeException("Document pending introuvable : " + pendingId));

        String docType = pending.getDocumentType();
        log.info("Confirmation archivage : {} → type {}", pendingId, docType);

        if ("EMPLOYEE".equals(docType)) {
            archiveAsEmployee(pending);
        } else if (docType != null && docType.startsWith("TYPE_")) {
            archiveAsAdminDocument(pending);
        } else {
            // Type inconnu → archiver comme admin document générique
            pending.setDocumentType("TYPE_A");
            archiveAsAdminDocument(pending);
        }

        // Supprimer le pending après archivage
        pendingRepo.deleteById(pendingId);
        log.info("Document archivé et pending supprimé : {}", pendingId);

        return docType;
    }

    // ── Rejeter un document → supprimer le pending ────────────
    public void rejectDocument(String pendingId) {
        PendingDocument pending = pendingRepo.findById(pendingId)
                .orElseThrow(() -> new RuntimeException("Document pending introuvable : " + pendingId));

        // On supprime juste le pending, le fichier physique reste
        pendingRepo.deleteById(pendingId);
        log.info("Document rejeté et pending supprimé : {}", pendingId);
    }

    // ── Récupérer tous les documents en attente ───────────────
    public List<PendingDocument> getAllPending() {
        return pendingRepo.findByStatus("PENDING_CONFIRMATION");
    }

    // ── Archivage vers admin_documents ────────────────────────
    private void archiveAsAdminDocument(PendingDocument pending) {
        AdminDocument doc = aiClassifier.mapToAdminDocument(
                pending.getExtractedFields(),
                pending.getDocumentType(),
                pending.getRawText()
        );
        doc.setIsHandwritten(pending.isHandwritten());
        doc.setOriginalFilePath(pending.getOriginalFilePath());
        doc.setDigitalizedPdfPath(pending.getDigitalizedPdfPath());
        doc.setStatus("ARCHIVED");
        doc.setAnalyzedAt(pending.getScannedAt());
        doc.setArchivedAt(LocalDateTime.now());
        adminDocRepo.save(doc);
        log.info("AdminDocument archivé : type={}", pending.getDocumentType());
    }

    // ── Archivage vers employees + employee_documents ─────────
    private void archiveAsEmployee(PendingDocument pending) {
        // Créer EmployeeDocument
        EmployeeDocument empDoc = new EmployeeDocument();
        empDoc.setRawText(pending.getRawText());
        empDoc.setIsHandwritten(pending.isHandwritten());
        empDoc.setOriginalFilePath(pending.getOriginalFilePath());
        empDoc.setDigitalizedPdfPath(pending.getDigitalizedPdfPath());
        empDoc.setStatus("ARCHIVED");
        empDoc.setAnalyzedAt(pending.getScannedAt());
        empDoc.setArchivedAt(LocalDateTime.now());
        EmployeeDocument saved = empDocRepo.save(empDoc);

        // Créer Employee
        Employee emp = aiClassifier.mapToEmployee(
                pending.getExtractedFields(), pending.getRawText()
        );
        emp.setSourceDocumentId(saved.getId());
        employeeRepo.save(emp);
        log.info("Employee archivé depuis document manuscrit : {}", pending.isHandwritten());
    }
}