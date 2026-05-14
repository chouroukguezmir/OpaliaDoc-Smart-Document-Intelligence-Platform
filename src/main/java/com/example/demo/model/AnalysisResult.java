package com.example.demo.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    // Classification
    private String documentType;      // EMPLOYEE, TYPE_A, TYPE_B, TYPE_C
    private String documentSubType;
    private double confidence;

    // Extraction des champs
    private Map<String, String> extractedFields;

    // Manuscrit
    private boolean isHandwritten;
    private String handwritingQuality; // GOOD, MEDIUM, POOR

    // Résumé IA
    private String aiSummary;

    // Statut
    private String status;            // SUCCESS, PARTIAL, FAILED
    private String errorMessage;

    // PDF digitalisé
    private String digitalizedPdfPath;
}