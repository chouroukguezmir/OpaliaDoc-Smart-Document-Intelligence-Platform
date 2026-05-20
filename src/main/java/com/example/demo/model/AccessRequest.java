package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Demande de droits d'accès informatique — formulaire E DSI 3813.
 * Tous les champs sont optionnels.
 */
@Data
@Document(collection = "access_requests")
public class AccessRequest {

    @Id
    private String id;

    // ── Information Générale ──
    private String societe;
    private String site;
    private String direction;
    private String fonction;
    private String prenom;
    private String nom;
    private String matricule;
    private String tel;

    // ── Détail de la demande ──
    private Classification classification;   // Confidentiel | Non confidentiel

    private String attachedFile;             // document d'origine joint
    private LocalDateTime createdAt = LocalDateTime.now();
}