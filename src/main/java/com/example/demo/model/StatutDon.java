package com.example.demo.model;

public enum StatutDon {
    EN_COURS("En cours"),
    ACCEPTE("Accepté"),
    REFUSE("Refusé");

    private final String label;

    StatutDon(String label) { this.label = label; }

    public String getLabel() { return label; }

    @Override
    public String toString() { return label; }
}