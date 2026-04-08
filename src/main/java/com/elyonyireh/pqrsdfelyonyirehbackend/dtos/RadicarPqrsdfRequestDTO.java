package com.elyonyireh.pqrsdfelyonyirehbackend.dtos;

import lombok.Data;
import com.elyonyireh.pqrsdfelyonyirehbackend.models.enums.TipoPqrsd;

@Data
public class RadicarPqrsdfRequestDTO {
    // Datos del Solicitante
    private String tipoIdentificacion;
    private String numeroIdentificacion;
    private String nombres;
    private String apellidos;
    private String correo;
    private String celular;
    private String tipoUsuario;
    private String semestre;
    private String programaFormacion;

    // Datos de la PQRSD
    private TipoPqrsd tipoPqrsd;
    private String descripcion;
    private String dependenciaDestino;
    private String funcionarioDestino;
    private Long sedeId;
    
    // Validacion Captcha
    private String captchaId;
    private String captchaValue;
    // Evidencias will be handled differently (e.g., MultipartFile) from Controller
}
