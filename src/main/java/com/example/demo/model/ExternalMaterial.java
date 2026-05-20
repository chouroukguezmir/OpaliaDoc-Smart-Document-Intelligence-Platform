package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Demande pour utilisation de matériel informatique externe — formulaire E DSI 3797.
 * Tous les champs sont optionnels.
 */
@Data
@Document(collection = "external_materials")
public class ExternalMaterial {

    @Id
    private String id;

    // ── Information Générale ──
    private Role role;
    private String societeUniversite;
    private String site;
    private String directionDepartement;
    private String fonction;
    private String encadreurOpalia;
    private String prenom;
    private String nom;
    private String matricule;
    private String tel;

    // ── Détail de la demande ──
    private String numeroTicket;
    private String raisonAutorisation;

    // Liste de matériels à autoriser (cases à cocher)
    private Boolean cleUsb;
    private Boolean disqueDurExterne;
    private Boolean cle4G;
    private Boolean lecteurDvd;
    private Boolean ordinateurPersonale;

    private String attachedFile;          // document d'origine joint
    private LocalDateTime createdAt = LocalDateTime.now();
}