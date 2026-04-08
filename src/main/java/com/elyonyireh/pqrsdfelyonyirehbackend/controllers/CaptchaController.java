package com.elyonyireh.pqrsdfelyonyirehbackend.controllers;

import com.elyonyireh.pqrsdfelyonyirehbackend.dtos.CaptchaResponseDTO;
import com.elyonyireh.pqrsdfelyonyirehbackend.services.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/captcha")
@Slf4j
public class CaptchaController {

    private final CaptchaService captchaService;

    @Autowired
    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping("/generar")
    public ResponseEntity<CaptchaResponseDTO> generarCaptcha() {
        log.info("Generando nuevo CAPTCHA para el cliente...");
        return ResponseEntity.ok(captchaService.generarCaptcha());
    }
}
