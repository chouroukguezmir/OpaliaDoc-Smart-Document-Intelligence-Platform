package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Rôle de la personne demandant l'utilisation de matériel externe. */
public enum Role {

    EMPLOYEUR_OPALIA("Employeur Opalia"),
    STAGIAIRE("Stagiaire"),
    CONSULTANT("Consultant");

    private final String label;
    Role(String label) { this.label = label; }

    @JsonValue
    public String getLabel() { return label; }

    @JsonCreator
    public static Role fromLabel(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        for (Role r : values()) {
            if (r.label.equalsIgnoreCase(v) || r.name().equalsIgnoreCase(v)) return r;
        }
        return null;
    }
}