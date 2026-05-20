package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmationService {

    private final PendingDocumentRepository    pendingRepo;
    private final AdminDocumentRepository      adminDocRepo;
    private final EmployeeDocumentRepository   empDocRepo;
    private final EmployeeRepository           employeeRepo;
    private final MaterialRepository           materialRepo;
    private final ExternalMaterialRepository   externalMaterialRepo;
    private final AccessRequestRepository      accessRequestRepo;
    private final AiClassifierService          aiClassifier;

    // ── Confirmer un document → l'archiver dans la bonne collection
    public String confirmDocument(String pendingId) {

        PendingDocument pending = pendingRepo.findById(pendingId)
                .orElseThrow(() -> new RuntimeException("Document pending introuvable : " + pendingId));

        String docType = pending.getDocumentType();
        log.info("Confirmation archivage : {} → type {}", pendingId, docType);

        if ("EMPLOYEE".equals(docType)) {
            archiveAsEmployee(pending);
        } else if ("TYPE_A".equals(docType)) {
            archiveAsAccessRequest(pending);
        } else if ("TYPE_B".equals(docType)) {
            archiveAsMaterial(pending);
        } else if ("TYPE_C".equals(docType)) {
            archiveAsExternalMaterial(pending);
        } else {
            // Type inconnu → archive générique
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

    // ── Récupérer un document en attente par id ───────────────
    public PendingDocument getPendingById(String id) {
        return pendingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Document pending introuvable : " + id));
    }

    // ── Mettre à jour les champs extraits (édition avant confirmation)
    public PendingDocument updateFields(String id, Map<String, String> fields) {
        PendingDocument pending = getPendingById(id);
        pending.setExtractedFields(fields);
        return pendingRepo.save(pending);
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

        // Champs de l'employé
        Employee emp = aiClassifier.mapToEmployee(
                pending.getExtractedFields(), pending.getRawText()
        );

        // Renommer le fichier joint : <nom-employe>-<date>.<ext>
        String attachedFile = renameAttachedFile(
                pending.getOriginalFilePath(), emp.getName());

        // Créer EmployeeDocument
        EmployeeDocument empDoc = new EmployeeDocument();
        empDoc.setRawText(pending.getRawText());
        empDoc.setIsHandwritten(pending.isHandwritten());
        empDoc.setOriginalFilePath(attachedFile);
        empDoc.setDigitalizedPdfPath(pending.getDigitalizedPdfPath());
        empDoc.setStatus("ARCHIVED");
        empDoc.setAnalyzedAt(pending.getScannedAt());
        empDoc.setArchivedAt(LocalDateTime.now());
        EmployeeDocument saved = empDocRepo.save(empDoc);

        // Créer Employee — avec le document d'origine joint
        emp.setSourceDocumentId(saved.getId());
        emp.setAttachedFile(attachedFile);
        employeeRepo.save(emp);
        log.info("Employee archivé : {}", emp.getName());
    }

    // ── Archivage vers materials ──────────────────────────────
    private void archiveAsMaterial(PendingDocument pending) {
        Material mat = aiClassifier.mapToMaterial(pending.getExtractedFields());

        // Nom de fichier : prenom-nom-<date>.<ext>
        String fileBase = ((mat.getPrenom() != null ? mat.getPrenom() : "")
                + " " + (mat.getNom() != null ? mat.getNom() : "")).trim();
        if (fileBase.isBlank()) fileBase = "materiel";

        mat.setAttachedFile(renameAttachedFile(pending.getOriginalFilePath(), fileBase));
        materialRepo.save(mat);
        log.info("Matériel archivé : {} {}", mat.getPrenom(), mat.getNom());
    }

    // ── Archivage vers access_requests ────────────────────────
    private void archiveAsAccessRequest(PendingDocument pending) {
        AccessRequest ar = aiClassifier.mapToAccessRequest(pending.getExtractedFields());

        String fileBase = ((ar.getPrenom() != null ? ar.getPrenom() : "")
                + " " + (ar.getNom() != null ? ar.getNom() : "")).trim();
        if (fileBase.isBlank()) fileBase = "acces";

        ar.setAttachedFile(renameAttachedFile(pending.getOriginalFilePath(), fileBase));
        accessRequestRepo.save(ar);
        log.info("Demande d'accès archivée : {} {} ({})",
                ar.getPrenom(), ar.getNom(), ar.getClassification());
    }

    // ── Archivage vers external_materials ─────────────────────
    private void archiveAsExternalMaterial(PendingDocument pending) {
        ExternalMaterial em = aiClassifier.mapToExternalMaterial(pending.getExtractedFields());

        String fileBase = ((em.getPrenom() != null ? em.getPrenom() : "")
                + " " + (em.getNom() != null ? em.getNom() : "")).trim();
        if (fileBase.isBlank()) fileBase = "materiel-externe";

        em.setAttachedFile(renameAttachedFile(pending.getOriginalFilePath(), fileBase));
        externalMaterialRepo.save(em);
        log.info("Matériel externe archivé : {} {} ({})",
                em.getPrenom(), em.getNom(), em.getRole());
    }

    // ── Renomme le fichier joint en <nom-employe>-<date>.<ext> ──
    private String renameAttachedFile(String originalPath, String employeeName) {
        if (originalPath == null) return null;
        try {
            Path source = Paths.get(originalPath);
            if (!Files.exists(source)) return originalPath;

            String ext = "";
            int dot = originalPath.lastIndexOf('.');
            if (dot != -1) ext = originalPath.substring(dot);

            String safeName = (employeeName == null || employeeName.isBlank())
                    ? "employe"
                    : employeeName.trim().replaceAll("[^a-zA-Z0-9]+", "_");
            String baseName = safeName + "-" + LocalDate.now();   // ex: Navel-2026-05-19

            Path dir = source.getParent();
            Path target = dir.resolve(baseName + ext);
            int counter = 1;
            while (Files.exists(target)) {       // évite d'écraser un fichier existant
                target = dir.resolve(baseName + "-" + counter + ext);
                counter++;
            }
            Files.move(source, target);
            log.info("Fichier joint renommé : {} -> {}", source, target);
            return target.toString();

        } catch (IOException e) {
            log.error("Erreur renommage fichier joint : {}", e.getMessage());
            return originalPath;
        }
    }
}