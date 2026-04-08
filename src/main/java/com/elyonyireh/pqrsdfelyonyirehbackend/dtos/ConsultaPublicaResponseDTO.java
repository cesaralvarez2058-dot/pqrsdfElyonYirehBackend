package com.elyonyireh.pqrsdfelyonyirehbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.enums.TipoPqrsd;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaPublicaResponseDTO {
    private boolean exito;
    private String mensaje;
    private String numeroRadicado;
    private String estado;
    private LocalDateTime fechaRadicacion;
    private TipoPqrsd tipoPqrsd;
    private String nombresSolicitante;
    private String apellidosSolicitante;
    private String descripcion;
    private String respuestaAdmin;
    private LocalDateTime fechaRespuesta;
    private Integer calificacionUsuario;
    private String observacionUsuario;
}
