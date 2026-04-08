package com.elyonyireh.pqrsdfelyonyirehbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "evidencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivoOriginal;

    @Column(name = "ruta_almacenamiento", nullable = false)
    private String rutaAlmacenamiento;

    @Column(name = "tipo_mime", nullable = false)
    private String tipoMime;

    @Column(name = "tamano", nullable = false)
    private long tamano;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pqrsdf_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Pqrsdf pqrsdf;
}
