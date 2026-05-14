package com.example.demo.model;

import com.example.demo.model.embedded.CheminPartage;
import com.example.demo.model.embedded.ReceptionRetour;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "admin_documents")
public class AdminDocument {

    @Id
    private String id;

    private String scanSessionId;

    @Indexed
    private String subType;         // TYPE_A | TYPE_B | TYPE_C

    // Champs communs
    private String societe;
    private String direction;
    private String prenom;
    private String nom;
    private String matricule;
    private String site;
    private String fonction;

    // TYPE_A
    private String classification;
    private List<CheminPartage> cheminsDePartage;
    private String demandeur;
    private String dateVisaDemandeur;
    private String verificateurChefProjetIT;
    private String dateVisaChefProjetIT;
    private String responsableDossier;
    private String dateVisaResponsable;
    private String rssi;
    private String dateVisaRssi;

    // TYPE_B
    private String numeroTicket;
    private Boolean ordinateurDesktop;
    private Boolean ordinateurLaptop;
    private Boolean ordinateurIpad;
    private Boolean telephonePosteInterne;
    private Boolean telephoneSmartphone;
    private Boolean internetCleInternet;
    private Boolean internetPuceInternet;
    private String remarque;
    private ReceptionRetour receptionRetour;

    // TYPE_C
    private String typePersonne;
    private String societeUniversite;
    private String encadreurOpalia;
    private String tel;
    private String numeroTicketExterne;
    private String raisonAutorisation;
    private Boolean cleUsb;
    private Boolean disqueDurExterne;
    private Boolean cle4G;
    private Boolean lecteurDvd;
    private Boolean ordinateurPersonale;
    private String dureeAutorisation;
    private String dureeFrom;
    private String dureeTo;
    private String autres;
    private String dateVisaDemandeurC;
    private String directeurSupHi;
    private String dateVisaDirecteurSupHi;
    private String directeurSI;
    private String dateVisaDirecteurSI;

    // Métadonnées
    private String rawText;
    private Double confidence;
    private Boolean isHandwritten;
    private String originalFilePath;
    private String digitalizedPdfPath;
    private String status;
    private String aiSummary;        // ← NOUVEAU
    private LocalDateTime analyzedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Indexed
    private String status;          // PENDING | ANALYZED | FAILED

    private LocalDateTime analyzedAt;

    @Indexed
    private LocalDateTime createdAt = LocalDateTime.now();
}