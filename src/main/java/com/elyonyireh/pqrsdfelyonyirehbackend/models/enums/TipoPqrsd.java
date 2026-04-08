package com.elyonyireh.pqrsdfelyonyirehbackend.models.enums;

public enum TipoPqrsd {
    PETICION("Petición"),
    QUEJA("Queja"),
    RECLAMO("Reclamo"),
    SUGERENCIA("Sugerencia"),
    FELICITACION("Felicitación");

    private final String label;

    TipoPqrsd(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TipoPqrsd fromString(String text) {
        for (TipoPqrsd b : TipoPqrsd.values()) {
            if (b.name().equalsIgnoreCase(text) || b.label.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}
