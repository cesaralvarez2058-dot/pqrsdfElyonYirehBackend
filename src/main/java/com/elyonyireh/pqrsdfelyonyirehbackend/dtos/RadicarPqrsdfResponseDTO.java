package com.elyonyireh.pqrsdfelyonyirehbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RadicarPqrsdfResponseDTO {
    private boolean exito;
    private String mensaje;
    private String numeroRadicado;
}
