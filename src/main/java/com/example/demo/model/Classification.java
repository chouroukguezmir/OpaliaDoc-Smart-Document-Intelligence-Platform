package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Classification d'une demande de droits d'accès. */
public enum Classification {

    CONFIDENTIEL("Confidentiel"),
    NON_CONFIDENTIEL("Non confidentiel");

    private final String label;
    Classification(String label) { this.label = label; }

    @JsonValue
    public String getLabel() { return label; }

    /** Parsing tolérant : accepte le label ("Confidentiel") ou le nom ("CONFIDENTIEL"). */
    @JsonCreator
    public static Classification fromLabel(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        for (Classification c : values()) {
            if (c.label.equalsIgnoreCase(v) || c.name().equalsIgnoreCase(v)) return c;
        }
        return null;
    }
}