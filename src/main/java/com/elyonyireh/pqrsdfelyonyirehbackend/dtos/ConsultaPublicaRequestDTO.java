package com.elyonyireh.pqrsdfelyonyirehbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaPublicaRequestDTO {
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String numeroRadicado;
    private String captchaId;
    private String captchaValue;
}
