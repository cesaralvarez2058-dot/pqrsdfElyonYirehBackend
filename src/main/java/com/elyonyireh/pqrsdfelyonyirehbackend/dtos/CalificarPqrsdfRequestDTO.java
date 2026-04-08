package com.elyonyireh.pqrsdfelyonyirehbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalificarPqrsdfRequestDTO {
    private String numeroRadicado;
    private String numeroIdentificacion; // Security check
    private Integer calificacionUsuario; // 1-5 or similar
    private String observacionUsuario;
}
