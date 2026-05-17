package com.example.demo.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class PdfGeneratorService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final DeviceRgb PRIMARY    = new DeviceRgb(13, 148, 136);
    private static final DeviceRgb PRIMARY_DK = new DeviceRgb(15, 118, 110);
    private static final DeviceRgb LIGHT_BG   = new DeviceRgb(240, 253, 250);
    private static final DeviceRgb GRAY_TEXT  = new DeviceRgb(100, 116, 139);
    private static final DeviceRgb DARK_TEXT  = new DeviceRgb(30, 41, 59);

    // ── CAS 1 : document NON manuscrit — PDF du contenu scan ──
    public String generateScanPdf(String rawText, String docType,
                                  String originalFilename,
                                  Map<String, String> fields) {
        try {
            String pdfPath = buildPdfPath();
            Document document = openDocument(pdfPath);

            addHeader(document, docType, originalFilename, false);

            if (fields != null && !fields.isEmpty()) {
                addSection(document, "Données extraites automatiquement");
                addFieldsTable(document, fields);
                spacer(document);
            }

            addSection(document, "Contenu numérisé");
            document.add(new Paragraph(rawText)
                    .setFontSize(9)
                    .setFontColor(DARK_TEXT)
                    .setMultipliedLeading(1.4f));

            addFooter(document, docType, false);
            document.close();
            log.info("PDF scan généré : {}", pdfPath);
            return pdfPath;

        } catch (IOException e) {
            log.error("Erreur génération PDF scan : {}", e.getMessage());
            return null;
        }
    }

    // ── CAS 2 : document MANUSCRIT — PDF digitalisé propre ────
    public String generateDigitisedPdf(String digitisedText, String docType,
                                       String originalFilename,
                                       Map<String, String> fields,
                                       String handwritingQuality) {
        try {
            String pdfPath = buildPdfPath();
            Document document = openDocument(pdfPath);

            addHeader(document, docType, originalFilename, true);

            // Bandeaux qualité
            String qualityLabel = switch (handwritingQuality) {
                case "GOOD"   -> "Ecriture bien lisible — confiance élevée";
                case "MEDIUM" -> "Ecriture partiellement lisible — vérification recommandée";
                case "POOR"   -> "Ecriture difficile à lire — vérification obligatoire";
                default       -> "Qualité non déterminée";
            };
            DeviceRgb qualityColor = switch (handwritingQuality) {
                case "GOOD"   -> new DeviceRgb(34, 197, 94);
                case "MEDIUM" -> new DeviceRgb(245, 158, 11);
                default       -> new DeviceRgb(239, 68, 68);
            };

            document.add(new Paragraph("Qualité de l'écriture : " + qualityLabel)
                    .setFontSize(9)
                    .setFontColor(qualityColor)
                    .setBold()
                    .setBackgroundColor(new DeviceRgb(248, 250, 252))
                    .setPadding(8)
                    .setBorderRadius(new BorderRadius(6)));
            spacer(document);

            if (fields != null && !fields.isEmpty()) {
                addSection(document, "Données extraites (écriture manuscrite digitalisée)");
                addFieldsTable(document, fields);
                spacer(document);
            }

            addSection(document, "Texte digitalisé");
            document.add(new Paragraph(digitisedText)
                    .setFontSize(10)
                    .setFontColor(DARK_TEXT)
                    .setMultipliedLeading(1.5f));

            addFooter(document, docType, true);
            document.close();
            log.info("PDF digitalisé généré : {}", pdfPath);
            return pdfPath;

        } catch (IOException e) {
            log.error("Erreur génération PDF digitalisé : {}", e.getMessage());
            return null;
        }
    }

    // ── Méthode de compatibilité ──────────────────────────────
    public String generateDigitalizedPdf(String rawText, String docType,
                                         String originalFilename) {
        return generateScanPdf(rawText, docType, originalFilename, null);
    }

    // ── Helpers ───────────────────────────────────────────────
    private String buildPdfPath() throws IOException {
        Path dir = Paths.get(uploadDir, "pdfs");
        if (!Files.exists(dir)) Files.createDirectories(dir);
        return dir.resolve("DOC_" + UUID.randomUUID() + ".pdf").toString();
    }

    private Document openDocument(String path) throws IOException {
        PdfDocument pdf = new PdfDocument(new PdfWriter(path));
        Document doc = new Document(pdf);
        doc.setMargins(40, 50, 40, 50);
        return doc;
    }

    private void addHeader(Document doc, String docType,
                           String filename, boolean handwritten) {
        Table t = new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .setWidth(UnitValue.createPercentValue(100))
                .setBackgroundColor(PRIMARY)
                .setPadding(14)
                .setBorderRadius(new BorderRadius(8));

        Cell left = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBackgroundColor(PRIMARY);
        left.add(new Paragraph("OPALIA").setBold().setFontSize(20)
                .setFontColor(ColorConstants.WHITE));
        left.add(new Paragraph("Système de Gestion Documentaire").setFontSize(8)
                .setFontColor(new DeviceRgb(204, 251, 241)));

        Cell right = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBackgroundColor(PRIMARY)
                .setTextAlignment(TextAlignment.RIGHT);
        right.add(new Paragraph(getDocTypeLabel(docType)).setBold().setFontSize(10)
                .setFontColor(ColorConstants.WHITE));
        right.add(new Paragraph(LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFontSize(8).setFontColor(new DeviceRgb(204, 251, 241)));
        if (handwritten)
            right.add(new Paragraph("Document Manuscrit Digitalisé").setFontSize(8)
                    .setFontColor(new DeviceRgb(253, 224, 71)));

        t.addCell(left);
        t.addCell(right);
        doc.add(t);
        spacer(doc);
        doc.add(new Paragraph("Fichier source : " + filename)
                .setFontSize(8).setFontColor(GRAY_TEXT).setItalic());
        spacer(doc);
    }

    private void addSection(Document doc, String title) {
        doc.add(new Paragraph(title).setBold().setFontSize(10)
                .setFontColor(PRIMARY_DK)
                .setBackgroundColor(LIGHT_BG)
                .setPadding(7)
                .setMarginBottom(6));
    }

    private void addFieldsTable(Document doc, Map<String, String> fields) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{38, 62}))
                .setWidth(UnitValue.createPercentValue(100));
        boolean alt = false;
        for (Map.Entry<String, String> e : fields.entrySet()) {
            DeviceRgb bg = alt ? new DeviceRgb(248, 250, 252) : (DeviceRgb) ColorConstants.WHITE;
            SolidBorder border = new SolidBorder(new DeviceRgb(226, 232, 240), 0.5f);

            Cell k = new Cell().add(new Paragraph(formatFieldName(e.getKey()))
                            .setFontSize(8).setBold().setFontColor(GRAY_TEXT))
                    .setBackgroundColor(bg).setPadding(6).setBorder(border);
            Cell v = new Cell().add(new Paragraph(e.getValue() != null ? e.getValue() : "—")
                            .setFontSize(9).setFontColor(DARK_TEXT))
                    .setBackgroundColor(bg).setPadding(6).setBorder(border);

            table.addCell(k);
            table.addCell(v);
            alt = !alt;
        }
        doc.add(table);
    }

    private void addFooter(Document doc, String docType, boolean digitised) {
        spacer(doc);
        String label = digitised ? "Document manuscrit digitalisé" : "Document numérisé";
        doc.add(new Paragraph(label + " par OPALIA IA · " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) +
                " · " + docType)
                .setFontSize(7).setFontColor(GRAY_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setBorderTop(new SolidBorder(new DeviceRgb(226, 232, 240), 0.5f))
                .setPaddingTop(8));
    }

    private void spacer(Document doc) {
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private String formatFieldName(String key) {
        if (key == null) return "";
        String spaced = key.replaceAll("([A-Z])", " $1").trim();
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }

    private String getDocTypeLabel(String type) {
        if (type == null) return "Document";
        return switch (type) {
            case "EMPLOYEE" -> "E DSI 3812 — Compte Utilisateur";
            case "TYPE_A"   -> "E DSI 3813 — Droits Accès";
            case "TYPE_B"   -> "E DSI 3328 — Matériels";
            case "TYPE_C"   -> "E DSI 3797 — Matériel Externe";
            default         -> "Document Inconnu";
        };
    }
}