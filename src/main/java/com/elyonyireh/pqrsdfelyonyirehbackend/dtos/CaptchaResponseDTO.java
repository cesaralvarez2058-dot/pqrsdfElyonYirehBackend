package com.elyonyireh.pqrsdfelyonyirehbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaResponseDTO {
    private String captchaId;
    private String imagenBase64;
}
