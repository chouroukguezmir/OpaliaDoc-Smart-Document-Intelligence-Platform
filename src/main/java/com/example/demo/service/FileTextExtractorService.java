package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class FileTextExtractorService {

    @Value("${tesseract.data-path}")
    private String tesseractDataPath;

    @Value("${tesseract.language}")
    private String tesseractLanguage;

    public String extractText(File file, String contentType) {
        log.info("Extraction texte : {} ({})", file.getName(), contentType);

        if (contentType == null) contentType = detectContentType(file);

        return switch (contentType) {
            case "application/pdf"
                    -> extractFromPdf(file);
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                 "application/msword"
                    -> extractFromWord(file);
            case "image/png",
                 "image/jpeg",
                 "image/jpg",
                 "image/tiff",
                 "image/bmp"
                    -> extractFromImage(file);
            default -> {
                log.warn("Type non reconnu : {}, tentative OCR", contentType);
                yield extractFromImage(file);
            }
        };
    }

    public boolean isHandwritten(File file, String contentType) {
        if (contentType != null && (
                contentType.contains("image") ||
                        contentType.contains("word"))) {
            return false;
        }
        String nativeText = extractWithPdfBox(file);
        return nativeText == null || nativeText.trim().length() < 50;
    }

    // ── PDF ──────────────────────────────────────────────────────
    private String extractFromPdf(File file) {
        String text = extractWithPdfBox(file);
        if (text != null && text.trim().length() >= 50) {
            log.info("Texte natif PDF : {} chars", text.length());
            return text;
        }
        log.info("Texte natif insuffisant → OCR Tesseract");
        return extractPdfWithOcr(file);
    }

    private String extractWithPdfBox(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Erreur PDFBox : {}", e.getMessage());
            return "";
        }
    }

    private String extractPdfWithOcr(File file) {
        StringBuilder result = new StringBuilder();
        try (PDDocument document = PDDocument.load(file)) {
            PDFRenderer renderer = new PDFRenderer(document);
            Tesseract tesseract = buildTesseract();

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 300);
                File tempImg = File.createTempFile("ocr_page_" + i + "_", ".png");
                ImageIO.write(image, "PNG", tempImg);
                try {
                    String pageText = tesseract.doOCR(tempImg);
                    result.append(pageText).append("\n");
                } finally {
                    tempImg.delete();
                }
            }
        } catch (IOException | TesseractException e) {
            log.error("Erreur OCR PDF : {}", e.getMessage());
        }
        return result.toString();
    }

    // ── WORD ─────────────────────────────────────────────────────
    private String extractFromWord(File file) {
        StringBuilder result = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    result.append(text).append("\n");
                }
            }

            document.getTables().forEach(table ->
                    table.getRows().forEach(row ->
                            row.getTableCells().forEach(cell -> {
                                String cellText = cell.getText();
                                if (cellText != null && !cellText.isBlank()) {
                                    result.append(cellText).append(" | ");
                                }
                            })
                    )
            );
            log.info("Texte Word extrait : {} chars", result.length());
        } catch (IOException e) {
            log.error("Erreur lecture Word : {}", e.getMessage());
        }
        return result.toString();
    }

    // ── IMAGE ────────────────────────────────────────────────────
    private String extractFromImage(File file) {
        try {
            Tesseract tesseract = buildTesseract();
            String text = tesseract.doOCR(file);
            log.info("Texte image extrait : {} chars", text.length());
            return text;
        } catch (TesseractException e) {
            log.error("Erreur OCR image : {}", e.getMessage());
            return "";
        }
    }

    // ── Utilitaires ──────────────────────────────────────────────
    private Tesseract buildTesseract() {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tesseractDataPath);
        tesseract.setLanguage(tesseractLanguage);
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);
        return tesseract;
    }

    private String detectContentType(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".pdf"))  return "application/pdf";
        if (name.endsWith(".docx")) return
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (name.endsWith(".doc"))  return "application/msword";
        if (name.endsWith(".png"))  return "image/png";
        if (name.endsWith(".jpg") ||
                name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".tiff") ||
                name.endsWith(".tif"))  return "image/tiff";
        if (name.endsWith(".bmp"))  return "image/bmp";
        return "application/octet-stream";
    }
}