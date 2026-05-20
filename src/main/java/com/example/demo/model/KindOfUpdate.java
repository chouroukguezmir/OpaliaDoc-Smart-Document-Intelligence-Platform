package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Type de demande sur le formulaire User Account (E DSI 3812). */
public enum KindOfUpdate {

    ACTIVATION("Activation"),
    MODIFICATION("Modification"),
    REMOVAL("Removal"),
    SUSPENSION("Suspension"),
    REACTIVATION("Reactivation");

    private final String label;
    KindOfUpdate(String label) { this.label = label; }

    @JsonValue
    public String getLabel() { return label; }

    @JsonCreator
    public static KindOfUpdate fromLabel(String value) {
        if (value == null || value.isBlank()) return null;
        String v = value.trim();
        for (KindOfUpdate k : values()) {
            if (k.label.equalsIgnoreCase(v) || k.name().equalsIgnoreCase(v)) return k;
        }
        return null;
    }
}