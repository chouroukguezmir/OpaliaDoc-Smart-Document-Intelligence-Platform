package com.example.demo.service;

import com.example.demo.model.AccessRequest;
import com.example.demo.model.AdminDocument;
import com.example.demo.model.Classification;
import com.example.demo.model.Employee;
import com.example.demo.model.ExternalMaterial;
import com.example.demo.model.KindOfUpdate;
import com.example.demo.model.Material;
import com.example.demo.model.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AiClassifierService {

    // ── Classification par mots-clés (fallback si Python KO) ──
    public String detectDocumentType(String text) {
        if (text == null || text.isBlank()) return "UNKNOWN";
        String t = text.toLowerCase();

        int scoreEmployee = 0, scoreA = 0, scoreB = 0, scoreC = 0;

        // EMPLOYEE — E DSI 3812
        if (t.contains("3812"))                             scoreEmployee += 4;
        if (t.contains("compte utilisateur"))               scoreEmployee += 3;
        if (t.contains("user account"))                     scoreEmployee += 3;
        if (t.contains("activation") && t.contains("removal")) scoreEmployee += 3;
        if (t.contains("profil vp") || t.contains("vpn"))  scoreEmployee += 2;
        if (t.contains("chemin réseau") || t.contains("chemin reseau")) scoreEmployee += 2;
        if (t.contains("login") || t.contains("identifiant")) scoreEmployee += 1;

        // TYPE_A — E DSI 3813
        if (t.contains("3813"))                             scoreA += 4;
        if (t.contains("droits d'accès") || t.contains("droits acces")) scoreA += 3;
        if (t.contains("classification"))                   scoreA += 2;
        if (t.contains("rssi"))                             scoreA += 2;
        if (t.contains("partage réseau") || t.contains("partage reseau")) scoreA += 1;

        // TYPE_B — E DSI 3328
        if (t.contains("3328"))                             scoreB += 4;
        if (t.contains("matériels informatique") || t.contains("materiels informatique")) scoreB += 3;
        if (t.contains("laptop") || t.contains("desktop")) scoreB += 2;
        if (t.contains("smartphone"))                       scoreB += 2;
        if (t.contains("clé internet") || t.contains("cle internet")) scoreB += 2;

        // TYPE_C — E DSI 3797
        if (t.contains("3797"))                             scoreC += 4;
        if (t.contains("matériel externe") || t.contains("materiel externe")) scoreC += 3;
        if (t.contains("clé usb") || t.contains("cle usb")) scoreC += 2;
        if (t.contains("disque dur"))                       scoreC += 2;
        if (t.contains("stagiaire") || t.contains("prestataire")) scoreC += 1;

        int max = Math.max(Math.max(scoreEmployee, scoreA), Math.max(scoreB, scoreC));
        if (max == 0) return "UNKNOWN";
        if (max == scoreEmployee) return "EMPLOYEE";
        if (max == scoreA) return "TYPE_A";
        if (max == scoreB) return "TYPE_B";
        return "TYPE_C";
    }

    // ── Mapping vers AdminDocument depuis les champs extraits ──
    public AdminDocument mapToAdminDocument(Map<String, String> fields, String subType, String rawText) {
        AdminDocument doc = new AdminDocument();
        doc.setSubType(subType);
        doc.setRawText(rawText);

        if (fields == null) return doc;

        doc.setNom(fields.get("nom"));
        doc.setPrenom(fields.get("prenom"));
        doc.setMatricule(fields.get("matricule"));
        doc.setSociete(fields.get("societe"));
        doc.setDirection(fields.get("direction"));
        doc.setSite(fields.get("site"));
        doc.setFonction(fields.get("fonction"));

        // TYPE_A
        doc.setClassification(fields.get("classification"));
        doc.setDemandeur(fields.get("demandeur"));
        doc.setResponsableDossier(fields.get("responsableDossier"));
        doc.setRssi(fields.get("rssi"));
        doc.setDateVisaDemandeur(fields.get("dateVisaDemandeur"));

        // TYPE_B
        doc.setNumeroTicket(fields.get("numeroTicket"));
        doc.setRemarque(fields.get("remarque"));
        if (fields.containsKey("ordinateurDesktop"))
            doc.setOrdinateurDesktop(Boolean.parseBoolean(fields.get("ordinateurDesktop")));
        if (fields.containsKey("ordinateurLaptop"))
            doc.setOrdinateurLaptop(Boolean.parseBoolean(fields.get("ordinateurLaptop")));
        if (fields.containsKey("telephoneSmartphone"))
            doc.setTelephoneSmartphone(Boolean.parseBoolean(fields.get("telephoneSmartphone")));
        if (fields.containsKey("internetCleInternet"))
            doc.setInternetCleInternet(Boolean.parseBoolean(fields.get("internetCleInternet")));

        // TYPE_C
        doc.setTypePersonne(fields.get("typePersonne"));
        doc.setRaisonAutorisation(fields.get("raisonAutorisation"));
        doc.setDureeFrom(fields.get("dureeFrom"));
        doc.setDureeTo(fields.get("dureeTo"));
        doc.setEncadreurOpalia(fields.get("encadreurOpalia"));

        return doc;
    }

    // ── Mapping vers Employee depuis les champs extraits ──────
    // Champs du formulaire E DSI 3812 (User Account Activation/Modification/Removal Form)
    public Employee mapToEmployee(Map<String, String> fields, String rawText) {
        Employee emp = new Employee();
        if (fields == null) return emp;

        emp.setName(fields.get("name"));
        emp.setCompany(fields.get("company"));
        emp.setSite(fields.get("site"));
        emp.setDepartment(fields.get("department"));
        emp.setMobile(fields.get("mobile"));
        emp.setOfficePhone(fields.get("officePhone"));
        emp.setKindOfUpdate(KindOfUpdate.fromLabel(fields.get("kindOfUpdate")));
        emp.setRequester(fields.get("requester"));
        emp.setRequesterJobRole(fields.get("requesterJobRole"));
        return emp;
    }

    // ── Mapping vers Material (Demande de matériel — E DSI 3328) ──
    public Material mapToMaterial(Map<String, String> fields) {
        Material m = new Material();
        if (fields == null) return m;
        m.setSociete(fields.get("societe"));
        m.setSite(fields.get("site"));
        m.setDirection(fields.get("direction"));
        m.setFonction(fields.get("fonction"));
        m.setPrenom(fields.get("prenom"));
        m.setNom(fields.get("nom"));
        m.setMatricule(fields.get("matricule"));
        m.setNumeroTicket(fields.get("numeroTicket"));
        m.setOrdinateurDesktop(parseBool(fields.get("ordinateurDesktop")));
        m.setOrdinateurLaptop(parseBool(fields.get("ordinateurLaptop")));
        m.setOrdinateurIpad(parseBool(fields.get("ordinateurIpad")));
        m.setTelephonePosteInterne(parseBool(fields.get("telephonePosteInterne")));
        m.setTelephoneSmartphone(parseBool(fields.get("telephoneSmartphone")));
        m.setInternetCleInternet(parseBool(fields.get("internetCleInternet")));
        m.setInternetPuceInternet(parseBool(fields.get("internetPuceInternet")));
        return m;
    }

    private Boolean parseBool(String v) {
        if (v == null || v.isBlank()) return null;
        return Boolean.parseBoolean(v.trim());
    }

    // ── Mapping vers ExternalMaterial (Matériel externe — E DSI 3797) ──
    public ExternalMaterial mapToExternalMaterial(Map<String, String> fields) {
        ExternalMaterial em = new ExternalMaterial();
        if (fields == null) return em;
        em.setRole(Role.fromLabel(fields.get("role")));
        em.setSocieteUniversite(fields.get("societeUniversite"));
        em.setSite(fields.get("site"));
        em.setDirectionDepartement(fields.get("directionDepartement"));
        em.setFonction(fields.get("fonction"));
        em.setEncadreurOpalia(fields.get("encadreurOpalia"));
        em.setPrenom(fields.get("prenom"));
        em.setNom(fields.get("nom"));
        em.setMatricule(fields.get("matricule"));
        em.setTel(fields.get("tel"));
        em.setNumeroTicket(fields.get("numeroTicket"));
        em.setRaisonAutorisation(fields.get("raisonAutorisation"));
        em.setCleUsb(parseBool(fields.get("cleUsb")));
        em.setDisqueDurExterne(parseBool(fields.get("disqueDurExterne")));
        em.setCle4G(parseBool(fields.get("cle4G")));
        em.setLecteurDvd(parseBool(fields.get("lecteurDvd")));
        em.setOrdinateurPersonale(parseBool(fields.get("ordinateurPersonale")));
        return em;
    }

    // ── Mapping vers AccessRequest (Droits d'Accès — E DSI 3813) ──
    public AccessRequest mapToAccessRequest(Map<String, String> fields) {
        AccessRequest ar = new AccessRequest();
        if (fields == null) return ar;
        ar.setSociete(fields.get("societe"));
        ar.setSite(fields.get("site"));
        ar.setDirection(fields.get("direction"));
        ar.setFonction(fields.get("fonction"));
        ar.setPrenom(fields.get("prenom"));
        ar.setNom(fields.get("nom"));
        ar.setMatricule(fields.get("matricule"));
        ar.setTel(fields.get("tel"));
        ar.setClassification(Classification.fromLabel(fields.get("classification")));
        return ar;
    }

    // ── Utilitaires ───────────────────────────────────────────
    public String extractEmail(String text) {
        if (text == null) return null;
        Pattern p = Pattern.compile("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : null;
    }
}