package com.example.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Employé créé à partir du formulaire
 * "User Account Activation / Modification / Removal Form" (E DSI 3812).
 * Tous les champs sont optionnels.
 */
@Data
@Document(collection = "employees")
public class Employee {

    @Id
    private String id;

    private String name;            // User to be activated
    private String company;         // Company
    private String site;            // Site
    private String department;      // Department
    private String mobile;          // Mobile
    private String officePhone;     // Office Phone
    private KindOfUpdate kindOfUpdate;

    // Demandeur du formulaire — le "Job Role" est celui du demandeur, pas de l'employé
    private String requester;          // Requester (System Owner / HR responsible)
    private String requesterJobRole;   // Job Role du demandeur

    private String attachedFile;    // chemin du document d'origine joint

    private String sourceDocumentId;
    private LocalDateTime createdAt = LocalDateTime.now();
}