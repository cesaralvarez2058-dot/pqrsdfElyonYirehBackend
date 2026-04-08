package com.elyonyireh.pqrsdfelyonyirehbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String nombre;
    private String apellidos;
    private String email;
    private String rol;
}
