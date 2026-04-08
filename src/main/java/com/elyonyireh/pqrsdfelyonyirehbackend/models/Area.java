package com.elyonyireh.pqrsdfelyonyirehbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "areas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(name = "responsable_nombre", nullable = false)
    private String responsableNombre;

    @Column(name = "responsable_email", nullable = false)
    private String responsableEmail;

    @Column(name = "responsable_celular")
    private String responsableCelular;
}
