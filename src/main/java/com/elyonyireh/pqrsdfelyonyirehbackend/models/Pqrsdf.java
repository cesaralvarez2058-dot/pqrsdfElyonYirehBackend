package com.elyonyireh.pqrsdfelyonyirehbackend.models;

import com.elyonyireh.pqrsdfelyonyirehbackend.models.enums.TipoPqrsd;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pqrsdfs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pqrsdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_radicado", nullable = false, unique = true)
    private String numeroRadicado;

    @Column(name = "tipo_pqrsd", nullable = false)
    private TipoPqrsd tipoPqrsd;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @CreationTimestamp
    @Column(name = "fecha_radicacion", updatable = false)
    private LocalDateTime fechaRadicacion;

    @Column(nullable = false)
    private String estado; // e.g., RECIBIDO, EN_TRAMITE, RESUELTO

    @Column(name = "dependencia_destino")
    private String dependenciaDestino;

    @Column(name = "funcionario_destino")
    private String funcionarioDestino;

    @Column(name = "respuesta_admin", columnDefinition = "TEXT")
    private String respuestaAdmin;

    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;

    @Column(name = "calificacion_usuario")
    private Integer calificacionUsuario;

    @Column(name = "observacion_usuario", columnDefinition = "TEXT")
    private String observacionUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id")
    private Area area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_id")
    private Sede sede;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Solicitante solicitante;

    @OneToMany(mappedBy = "pqrsdf", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evidencia> evidencias;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "recordatorio_enviado", nullable = false, columnDefinition = "boolean default false")
    private Boolean recordatorioEnviado = false;

    @Transient
    public Integer getDiasRestantes() {
        if (fechaAsignacion == null || !"EN REPARTO".equals(estado)) {
            return null; // Solo cuenta si está asignado y sin respuesta
        }
        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS.between(fechaAsignacion, LocalDateTime.now());
        return Math.max(0, 15 - (int) diasTranscurridos);
    }
}
