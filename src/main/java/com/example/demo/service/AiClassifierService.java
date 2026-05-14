package com.example.demo.service;

import com.example.demo.model.AdminDocument;
import com.example.demo.model.Employee;
import com.example.demo.model.embedded.CheminPartage;
import com.example.demo.model.embedded.ProfileEntry;
import com.example.demo.model.embedded.VpnEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AiClassifierService {

    /**
     * Détecte le type du document à partir du texte extrait
     * Retourne : "EMPLOYEE" | "TYPE_A" | "TYPE_B" | "TYPE_C" | "UNKNOWN"
     */
    public String detectDocumentType(String text) {
        if (text == null || text.isBlank()) return "UNKNOWN";
        String upper = text.toUpperCase();

        if (upper.contains("USER ACCOUNT") ||
                upper.contains("ACTIVATION") && upper.contains("JOB ROLE") ||
                upper.contains("E DSI 3812")) {
            return "EMPLOYEE";
        }

        if (upper.contains("DROITS D") && upper.contains("ACCES") ||
                upper.contains("ACCES INFORMATIQUE") ||
                upper.contains("E DSI 3813") ||
                upper.contains("CHEMIN DE PARTAGE")) {
            return "TYPE_A";
        }

        if (upper.contains("MATERIELS INFORMATIQUE") && !upper.contains("EXTERNE") ||
                upper.contains("E DSI 3328") ||
                upper.contains("LA RECEPTION") && upper.contains("LE RETOUR")) {
            return "TYPE_B";
        }

        if (upper.contains("MATERIEL INFORMATIQUE EXTERNE") ||
                upper.contains("E DSI 3797") ||
                upper.contains("STAGIAIRE") && upper.contains("CONSULTANT") ||
                upper.contains("CLE USB") || upper.contains("CLÉ USB")) {
            return "TYPE_C";
        }

        return "UNKNOWN";
    }

    /**
     * Extrait et mappe les champs dans AdminDocument selon le subType
     */
    public AdminDocument mapToAdminDocument(String text, String subType) {
        AdminDocument doc = new AdminDocument();
        doc.setSubType(subType);
        doc.setRawText(text);

        // Extraction des champs communs
        doc.setSociete(extractField(text, "Société", "Societe", "Society"));
        doc.setDirection(extractField(text, "Direction"));
        doc.setPrenom(extractField(text, "Prénom", "Prenom"));
        doc.setNom(extractField(text, "Nom"));
        doc.setMatricule(extractField(text, "Matricule"));
        doc.setSite(extractField(text, "Site"));
        doc.setFonction(extractField(text, "Fonction", "Job Role", "Function"));

        switch (subType) {
            case "TYPE_A" -> mapTypeA(text, doc);
            case "TYPE_B" -> mapTypeB(text, doc);
            case "TYPE_C" -> mapTypeC(text, doc);
        }

        return doc;
    }

    /**
     * Extrait et mappe les champs dans Employee
     */
    public Employee mapToEmployee(String text) {
        Employee emp = new Employee();

        emp.setFullName(extractField(text,
                "User to be activated", "Nom complet", "Full Name"));
        emp.setPosition(extractField(text, "Job Role", "Poste", "Position"));
        emp.setDepartment(extractField(text, "Department", "Département"));
        emp.setCompany(extractField(text, "Company", "Société"));
        emp.setSite(extractField(text, "Site"));
        emp.setMobile(extractField(text, "Mobile"));
        emp.setOfficePhone(extractField(text, "Office Phone", "Téléphone"));
        emp.setRequester(extractField(text, "Requester", "Demandeur"));

        // Détection du type de mise à jour
        String upper = text.toUpperCase();
        if (upper.contains("ACTIVATION")) emp.setKindOfUpdate("Activation");
        else if (upper.contains("SUSPENSION")) emp.setKindOfUpdate("Suspension");
        else if (upper.contains("MODIFICATION")) emp.setKindOfUpdate("Modification");
        else if (upper.contains("REACTIVATION")) emp.setKindOfUpdate("Reactivation");
        else if (upper.contains("REMOVAL")) emp.setKindOfUpdate("Removal");

        return emp;
    }

    // ── Mapping TYPE_A ────────────────────────────────────────────
    private void mapTypeA(String text, AdminDocument doc) {
        String upper = text.toUpperCase();

        if (upper.contains("CONFIDENTIEL") && !upper.contains("NON CONFIDENTIEL")) {
            doc.setClassification("Confidentiel");
        } else if (upper.contains("NON CONFIDENTIEL")) {
            doc.setClassification("Non confidentiel");
        }

        doc.setDemandeur(extractField(text, "Demandeur"));
        doc.setVerificateurChefProjetIT(
                extractField(text, "Chef Projet IT", "Vérificateur"));
        doc.setResponsableDossier(
                extractField(text, "Responsable de Dossier", "Responsable Dossier"));
        doc.setRssi(extractField(text, "RSSI"));

        // Extraction basique des chemins de partage
        List<CheminPartage> chemins = new ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.contains("\\\\") || line.contains("//")) {
                CheminPartage cp = new CheminPartage();
                cp.setChemin(line.trim());
                cp.setLecture(line.toUpperCase().contains(" L ") ||
                        line.toUpperCase().endsWith("L"));
                cp.setModification(line.toUpperCase().contains(" M ") ||
                        line.toUpperCase().endsWith("M"));
                chemins.add(cp);
            }
        }
        doc.setCheminsDePartage(chemins);
    }

    // ── Mapping TYPE_B ────────────────────────────────────────────
    private void mapTypeB(String text, AdminDocument doc) {
        String upper = text.toUpperCase();

        doc.setNumeroTicket(extractField(text, "N° Ticket", "Ticket"));

        doc.setOrdinateurDesktop(upper.contains("DESKTOP"));
        doc.setOrdinateurLaptop(upper.contains("LAPTOP"));
        doc.setOrdinateurIpad(upper.contains("IPAD"));
        doc.setTelephonePosteInterne(upper.contains("POSTE INTERNE"));
        doc.setTelephoneSmartphone(upper.contains("SMARTPHONE"));
        doc.setInternetCleInternet(upper.contains("CLÉ INTERNET") ||
                upper.contains("CLE INTERNET"));
        doc.setInternetPuceInternet(upper.contains("PUCE INTERNET"));
        doc.setRemarque(extractField(text, "Remarque"));
    }

    // ── Mapping TYPE_C ────────────────────────────────────────────
    private void mapTypeC(String text, AdminDocument doc) {
        String upper = text.toUpperCase();

        if (upper.contains("STAGIAIRE")) doc.setTypePersonne("Stagiaire");
        else if (upper.contains("CONSULTANT")) doc.setTypePersonne("Consultant");
        else doc.setTypePersonne("Employeur Opalia");

        doc.setEncadreurOpalia(extractField(text, "Encadreur Opalia"));
        doc.setTel(extractField(text, "Tél", "Tel", "Téléphone"));
        doc.setNumeroTicketExterne(extractField(text, "N° Ticket", "Ticket"));
        doc.setRaisonAutorisation(
                extractField(text, "Raison d'autorisation", "Raison"));

        doc.setCleUsb(upper.contains("CLÉ USB") || upper.contains("CLE USB"));
        doc.setDisqueDurExterne(upper.contains("DISQUE DUR EXTERNE"));
        doc.setCle4G(upper.contains("CLÉ 4G") || upper.contains("CLE 4G"));
        doc.setLecteurDvd(upper.contains("LECTEUR DVD"));
        doc.setOrdinateurPersonale(upper.contains("ORDINATEUR PERSONALE") ||
                upper.contains("ORDINATEUR PERSONNEL"));

        if (upper.contains("ILLIMITÉE") || upper.contains("ILLIMITEE")) {
            doc.setDureeAutorisation("Illimitée");
        } else if (upper.contains("LIMITÉE") || upper.contains("LIMITEE")) {
            doc.setDureeAutorisation("Limitée");
        }

        doc.setAutres(extractField(text, "Autres", "Other"));
    }

    // ── Utilitaire : extraction de champ par label ────────────────
    private String extractField(String text, String... labels) {
        String[] lines = text.split("\n");
        for (String label : labels) {
            for (String line : lines) {
                if (line.toLowerCase().contains(label.toLowerCase())) {
                    // Prend la valeur après ":" ou après le label
                    int colonIndex = line.indexOf(":");
                    if (colonIndex != -1 && colonIndex < line.length() - 1) {
                        String value = line.substring(colonIndex + 1).trim();
                        // Nettoie les points de remplissage "......."
                        value = value.replaceAll("\\.{2,}", "").trim();
                        if (!value.isBlank()) return value;
                    }
                }
            }
        }
        return null;
    }
}