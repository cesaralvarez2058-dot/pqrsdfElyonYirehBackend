package com.elyonyireh.pqrsdfelyonyirehbackend.services;

import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.CaptchaResponseDTO;

public interface CaptchaService {
    CaptchaResponseDTO generarCaptcha();
    boolean validarCaptcha(String captchaId, String captchaValue);
}
