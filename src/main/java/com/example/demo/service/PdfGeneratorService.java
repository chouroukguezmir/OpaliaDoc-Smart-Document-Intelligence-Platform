package com.example.demo.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class PdfGeneratorService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Génère un PDF digitalisé propre à partir du texte extrait
     */
    public String generateDigitalizedPdf(String rawText,
                                         String docType,
                                         String originalFileName) {
        String outputDir = uploadDir + "digitalized/";
        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (IOException e) {
            log.error("Erreur création dossier PDF : {}", e.getMessage());
        }

        String pdfFileName = UUID.randomUUID() + "_digitalized.pdf";
        String pdfPath = outputDir + pdfFileName;

        try (PdfWriter writer = new PdfWriter(pdfPath);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc)) {

            // En-tête
            document.add(new Paragraph("OPALIA PHARMA — RECORDATI GROUP")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(14));

            document.add(new Paragraph(getDocumentTitle(docType))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(12));

            document.add(new Paragraph(" "));

            // Métadonnées
            String dateStr = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            document.add(new Paragraph("Document digitalisé le : " + dateStr)
                    .setFontSize(9)
                    .setItalic());
            document.add(new Paragraph("Fichier original : " + originalFileName)
                    .setFontSize(9)
                    .setItalic());
            document.add(new Paragraph(" "));

            // Ligne de séparation
            document.add(new Paragraph("─".repeat(80))
                    .setFontSize(8));
            document.add(new Paragraph(" "));

            // Contenu extrait
            document.add(new Paragraph("CONTENU EXTRAIT :")
                    .setBold()
                    .setFontSize(11));
            document.add(new Paragraph(" "));

            // Affiche le texte ligne par ligne
            if (rawText != null && !rawText.isBlank()) {
                for (String line : rawText.split("\n")) {
                    if (!line.trim().isEmpty()) {
                        document.add(new Paragraph(line.trim())
                                .setFontSize(10));
                    }
                }
            }

            document.add(new Paragraph(" "));
            document.add(new Paragraph("─".repeat(80))
                    .setFontSize(8));
            document.add(new Paragraph(
                    "Document généré automatiquement par le système de gestion documentaire")
                    .setFontSize(8)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER));

        } catch (Exception e) {
            log.error("Erreur génération PDF : {}", e.getMessage());
            return null;
        }

        log.info("PDF digitalisé généré : {}", pdfPath);
        return pdfPath;
    }

    private String getDocumentTitle(String docType) {
        return switch (docType) {
            case "EMPLOYEE" ->
                    "USER ACCOUNT ACTIVATION / MODIFICATION / REMOVAL FORM";
            case "TYPE_A"   ->
                    "DEMANDE DES DROITS D'ACCES INFORMATIQUE";
            case "TYPE_B"   ->
                    "DEMANDE DE MATERIELS INFORMATIQUE";
            case "TYPE_C"   ->
                    "DEMANDE POUR UTILISATION MATERIEL INFORMATIQUE EXTERNE";
            default         ->
                    "DOCUMENT ADMINISTRATIF";
        };
    }
}