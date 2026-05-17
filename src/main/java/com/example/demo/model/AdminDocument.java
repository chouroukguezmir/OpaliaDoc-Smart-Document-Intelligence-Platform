package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "admin_documents")
public class AdminDocument {

    @Id
    private String id;

    private String subType; // TYPE_A, TYPE_B, TYPE_C

    // Informations communes
    private String nom;
    private String prenom;
    private String matricule;
    private String societe;
    private String direction;
    private String site;
    private String fonction;

    // TYPE_A — Droits Accès Informatique (E DSI 3813)
    private String classification;
    private String demandeur;
    private String responsableDossier;
    private String rssi;
    private String dateVisaDemandeur;

    // TYPE_B — Matériels Informatiques (E DSI 3328)
    private Boolean ordinateurDesktop;
    private Boolean ordinateurLaptop;
    private Boolean telephoneSmartphone;
    private Boolean internetCleInternet;
    private String numeroTicket;
    private String remarque;

    // TYPE_C — Matériel Externe (E DSI 3797)
    private String typePersonne;
    private String raisonAutorisation;
    private String dureeFrom;
    private String dureeTo;
    private String encadreurOpalia;
    private Boolean cleUsb;
    private Boolean disqueDurExterne;
    private Boolean cle4G;
    private Boolean ordinateurPersonale;

    // Métadonnées
    private String rawText;
    private Double confidence;
    private Boolean isHandwritten;
    private String originalFilePath;
    private String digitalizedPdfPath;
    private String status; // ARCHIVED
    private LocalDateTime analyzedAt;
    private LocalDateTime archivedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}