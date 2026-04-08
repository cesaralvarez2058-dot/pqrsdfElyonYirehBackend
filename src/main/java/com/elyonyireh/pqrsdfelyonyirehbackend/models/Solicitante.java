package com.elyonyireh.pqrsdfelyonyirehbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "solicitantes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Solicitante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_identificacion", nullable = false)
    private String tipoIdentificacion;

    @Column(name = "numero_identificacion", nullable = false, unique = true)
    private String numeroIdentificacion;

    @Column(nullable = false)
    private String nombres;

    @Column(nullable = false)
    private String apellidos;

    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private String celular;

    @Column(name = "tipo_usuario")
    private String tipoUsuario;

    @Column
    private String semestre;

    @Column(name = "programa_formacion")
    private String programaFormacion;

    // Optional: Only configure OneToMany if strictly necessary, to prevent N+1 issues.
    // However, as specified in the plan, leaving here mappedBy for a bi-directional.
    @OneToMany(mappedBy = "solicitante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Pqrsdf> radicados;
}
