package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Demande de matériel informatique — formulaire E DSI 3328.
 * Tous les champs sont optionnels.
 */
@Data
@Document(collection = "materials")
public class Material {

    @Id
    private String id;

    // ── Information Générale (Bénéficiaire) ──
    private String societe;
    private String site;
    private String direction;
    private String fonction;
    private String prenom;
    private String nom;
    private String matricule;

    // ── Détails de la demande ──
    private String numeroTicket;

    // Ordinateur (cases à cocher)
    private Boolean ordinateurDesktop;
    private Boolean ordinateurLaptop;
    private Boolean ordinateurIpad;

    // Téléphone (cases à cocher)
    private Boolean telephonePosteInterne;
    private Boolean telephoneSmartphone;

    // Internet (cases à cocher)
    private Boolean internetCleInternet;
    private Boolean internetPuceInternet;

    private String attachedFile;          // document d'origine joint
    private LocalDateTime createdAt = LocalDateTime.now();
}