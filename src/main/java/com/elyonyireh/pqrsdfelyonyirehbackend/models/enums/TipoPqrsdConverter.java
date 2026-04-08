package com.elyonyireh.pqrsdfelyonyirehbackend.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoPqrsdConverter implements AttributeConverter<TipoPqrsd, String> {

    @Override
    public String convertToDatabaseColumn(TipoPqrsd tipo) {
        return tipo != null ? tipo.name() : null;
    }

    @Override
    public TipoPqrsd convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        
        try {
            return TipoPqrsd.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Manejamos los valores legados (viejos) que ya están guardados en la BD
            String lower = dbData.toLowerCase();
            if (lower.contains("petición") || lower.contains("peticion")) {
                return TipoPqrsd.PETICION;
            } else if (lower.contains("queja")) {
                return TipoPqrsd.QUEJA;
            } else if (lower.contains("reclamo")) {
                return TipoPqrsd.RECLAMO;
            } else if (lower.contains("sugerencia")) {
                return TipoPqrsd.SUGERENCIA;
            } else if (lower.contains("denuncia")) { // Por si hay denuncias viejas, mapearlas como QUEJA o RECLAMO
                return TipoPqrsd.QUEJA;
            } else if (lower.contains("felicitaci")) {
                return TipoPqrsd.FELICITACION;
            }
            
            // Valor por defecto temporal si hay algo muy raro
            return TipoPqrsd.PETICION;
        }
    }
}
